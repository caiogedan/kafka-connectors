package pt.isel.tfm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.apache.kafka.connect.util.ConnectorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicatorSourceConnector extends SourceConnector {
	private static Logger log = LoggerFactory.getLogger(ReplicatorSourceConnector.class);
	private ReplicatorConnectorConfig config;

	private MonitoringThread monitoringThread;

	@Override
	public String version() {
		return VersionUtil.getVersion();
	}

	@Override
	public void start(Map<String, String> map) {
		log.info("Starting up Replicator Source Connector");
		config = new ReplicatorConnectorConfig(map);
		monitoringThread = new MonitoringThread(context, config);
		monitoringThread.start();
	}

	@Override
	public Class<? extends Task> taskClass() {
		// TODO: Return your task implementation.
		return ReplicatorSourceTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		List<String> leaderTopicPartitions = monitoringThread.getCurrentLeaderTopicPartitions().stream()
				.map(LeaderTopicPartition::toString).sorted().collect(Collectors.toList());

		int taskCount = Math.min(maxTasks, leaderTopicPartitions.size());

		if (taskCount < 1) {
			log.warn("No tasks to start.");
			return new ArrayList<>();
		}

		return ConnectorUtils.groupPartitions(leaderTopicPartitions, taskCount).stream()
				.map(leaderTopicPartitionsGroup -> {
					Map<String, String> taskConfig = new HashMap<>();

					taskConfig.putAll(config.allAsStrings());
					taskConfig.put(ReplicatorConnectorConfig.TASK_LEADER_TOPIC_PARTITION_CONFIG,
							String.join(",", leaderTopicPartitionsGroup));

					return taskConfig;
				}).collect(Collectors.toList());

	}

	@Override
	public void stop() {
		log.info("STOPPING Replicator source connector");
	}

	@Override
	public ConfigDef config() {
		return ReplicatorConnectorConfig.CONFIG;
	}
}
