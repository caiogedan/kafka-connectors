package pt.isel.tfm;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.connector.ConnectorContext;
import org.apache.kafka.connect.errors.ConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitoringThread {
	private static final Logger log = LoggerFactory.getLogger(MonitoringThread.class);

	private AtomicBoolean shutdown = new AtomicBoolean(false);
	private AdminClient partitionMonitorClient;
	private Pattern topicWhitelistPattern;
	private volatile Set<LeaderTopicPartition> currentLeaderTopicPartitions = new HashSet<>();
	private final CountDownLatch firstFetch = new CountDownLatch(1);

	private int maxShutdownWaitMs;
	private int topicRequestTimeoutMs;
	private boolean reconfigureTasksOnLeaderChange;
	private Runnable pollThread;
	private int topicPollIntervalMs;
	private ScheduledExecutorService pollExecutorService;
	private ScheduledFuture<?> pollHandle;

	MonitoringThread(ConnectorContext connectorContext, ReplicatorConnectorConfig sourceConnectorConfig) {
		topicWhitelistPattern = sourceConnectorConfig.getTopicWhitelistPattern();

		reconfigureTasksOnLeaderChange = sourceConnectorConfig
				.getBoolean(ReplicatorConnectorConfig.RECONFIGURE_TASKS_ON_LEADER_CHANGE_CONFIG);
		topicPollIntervalMs = sourceConnectorConfig
				.getInt(ReplicatorConnectorConfig.TOPIC_LIST_POLL_INTERVAL_MS_CONFIG);
		maxShutdownWaitMs = sourceConnectorConfig.getInt(ReplicatorConnectorConfig.MAX_SHUTDOWN_WAIT_MS_CONFIG);
		topicRequestTimeoutMs = sourceConnectorConfig.getInt(ReplicatorConnectorConfig.TOPIC_LIST_TIMEOUT_MS_CONFIG);

		partitionMonitorClient = AdminClient.create(sourceConnectorConfig.getAdminClientProperties());
		// Thread to periodically poll the kafka cluster for changes in topics or
		// partitions
		pollThread = new Runnable() {
			@Override
			public void run() {
				if (!shutdown.get()) {
					log.info("Fetching latest topic partitions.");
					try {
						Set<LeaderTopicPartition> retrievedLeaderTopicPartitions = retrieveLeaderTopicPartitions(
								topicRequestTimeoutMs);
						if (log.isDebugEnabled()) {
							log.debug("retrievedLeaderTopicPartitions: {}", retrievedLeaderTopicPartitions);
							log.debug("currentLeaderTopicPartitions: {}", currentLeaderTopicPartitions);
						}
						boolean requestTaskReconfiguration = false;
						if (currentLeaderTopicPartitions != null) {
							if (reconfigureTasksOnLeaderChange) {
								if (!retrievedLeaderTopicPartitions.equals(currentLeaderTopicPartitions)) {
									log.info(
											"Retrieved leaders and topic partitions do not match currently stored leaders and topic partitions, will request task reconfiguration");
									requestTaskReconfiguration = true;
								}
							} else {
								Set<TopicPartition> retrievedTopicPartitions = retrievedLeaderTopicPartitions.stream()
										.map(LeaderTopicPartition::toTopicPartition).collect(Collectors.toSet());
								if (log.isDebugEnabled())
									log.debug("retrievedTopicPartitions: {}", retrievedTopicPartitions);
								Set<TopicPartition> currentTopicPartitions = currentLeaderTopicPartitions.stream()
										.map(LeaderTopicPartition::toTopicPartition).collect(Collectors.toSet());
								if (log.isDebugEnabled())
									log.debug("currentTopicPartitions: {}", currentTopicPartitions);
								if (!retrievedTopicPartitions.equals(currentTopicPartitions)) {
									log.info(
											"Retrieved topic partitions do not match currently stored topic partitions, will request task reconfiguration");
									requestTaskReconfiguration = true;
								}
							}
							setCurrentLeaderTopicPartitions(retrievedLeaderTopicPartitions);
							if (requestTaskReconfiguration)
								connectorContext.requestTaskReconfiguration();
							else
								log.info("No partition changes which require reconfiguration have been detected.");
						} else {
							setCurrentLeaderTopicPartitions(retrievedLeaderTopicPartitions);
						}
					} catch (TimeoutException e) {
						log.error(
								"Timeout while waiting for AdminClient to return topic list. This indicates a (possibly transient) connection issue, or is an indicator that the timeout is set too low. {}",
								e);
					} catch (ExecutionException e) {
						log.error("Unexpected ExecutionException. {}", e);
					} catch (InterruptedException e) {
						log.error("InterruptedException. Probably shutting down. {}, e");
					}
				}
			}
		};
	}

	public void start() {
		// Schedule a task to periodically run to poll for new data
		pollExecutorService = Executors.newSingleThreadScheduledExecutor();
		pollHandle = pollExecutorService.scheduleWithFixedDelay(pollThread, 0, topicPollIntervalMs,
				TimeUnit.MILLISECONDS);
	}

	private boolean matchedTopicFilter(String topic) {
		return topicWhitelistPattern.matcher(topic).matches();
	}

	private void setCurrentLeaderTopicPartitions(Set<LeaderTopicPartition> leaderTopicPartitions) {
		currentLeaderTopicPartitions = leaderTopicPartitions;
		firstFetch.countDown();
	}

	public Set<LeaderTopicPartition> getCurrentLeaderTopicPartitions() {
		if (currentLeaderTopicPartitions == null) {
			try {
				firstFetch.await(topicRequestTimeoutMs, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				log.error("Interrupted while waiting for AdminClient to first return topic list");
				Thread.currentThread().interrupt();
			}
		}
		if (currentLeaderTopicPartitions == null) {
			throw new ConnectException("Timeout while waiting for AdminClient to first return topic list");
		}
		return currentLeaderTopicPartitions;
	}

	// Allow the main thread a chance to shut down gracefully
	@SuppressWarnings("deprecation")
	public void shutdown() {
		log.info("Shutdown called.");
		long startWait = System.currentTimeMillis();
		shutdown.set(true);
		partitionMonitorClient.close(maxShutdownWaitMs - (System.currentTimeMillis() - startWait),
				TimeUnit.MILLISECONDS);
		// Cancel our scheduled task, but wait for an existing task to complete if
		// running
		pollHandle.cancel(false);
		// Ask nicely to shut down the partition monitor executor service if it hasn't
		// already
		if (!pollExecutorService.isShutdown()) {
			try {
				pollExecutorService.awaitTermination(maxShutdownWaitMs - (System.currentTimeMillis() - startWait),
						TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				log.warn(
						"Got InterruptedException while waiting for pollExecutorService to shutdown, shutdown will be forced.");
			}
		}
		if (!pollExecutorService.isShutdown()) {
			pollExecutorService.shutdownNow();
		}
		log.info("Shutdown Complete.");
	}

	// Retrieve a list of LeaderTopicPartitions that match our topic filter
	private synchronized Set<LeaderTopicPartition> retrieveLeaderTopicPartitions(int requestTimeoutMs)
			throws InterruptedException, ExecutionException, TimeoutException {
		long startWait = System.currentTimeMillis();

		ListTopicsOptions listTopicsOptions = new ListTopicsOptions().listInternal(false)
				.timeoutMs((int) (requestTimeoutMs - (System.currentTimeMillis() - startWait)));
		Set<String> retrievedTopicSet = partitionMonitorClient.listTopics(listTopicsOptions).names()
				.get(requestTimeoutMs - (System.currentTimeMillis() - startWait), TimeUnit.MILLISECONDS);
		log.debug("Server topic list: {}", retrievedTopicSet);
		Set<String> matchedTopicSet = retrievedTopicSet.stream().filter(topic -> matchedTopicFilter(topic))
				.collect(Collectors.toSet());
		if (matchedTopicSet.size() > 0) {
			log.debug("Matched topic list: {}", matchedTopicSet);
		} else {
			log.warn(
					"Provided pattern {} does currently not match any topic."
							+ " Thus connector won't spawn any task until partition monitor recognizes matching topic.",
					this.topicWhitelistPattern.toString());

		}

		DescribeTopicsOptions describeTopicsOptions = new DescribeTopicsOptions()
				.timeoutMs((int) (requestTimeoutMs - (System.currentTimeMillis() - startWait)));
		Map<String, TopicDescription> retrievedTopicDescriptions = partitionMonitorClient
				.describeTopics(matchedTopicSet, describeTopicsOptions).all()
				.get(requestTimeoutMs - (System.currentTimeMillis() - startWait), TimeUnit.MILLISECONDS);
		return retrievedTopicDescriptions.values().stream()
				.map(topicDescription -> topicDescription.partitions().stream()
						.map(partitionInfo -> new LeaderTopicPartition(partitionInfo.leader().id(),
								topicDescription.name(), partitionInfo.partition())))
				.flatMap(Function.identity()).collect(Collectors.toSet());
	}

}
