package pt.isel.tfm.mqtt;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Validator;
import org.apache.kafka.common.config.ConfigException;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class MqttSinkConnectorConfig extends AbstractConfig {

	private static final Validator TOPIC_REGEX_VALIDATOR = new ConfigDef.Validator() {
		@Override
		public void ensureValid(String name, Object value) {
			getTopicWhitelistPattern((String) value);
		}
	};

	/*
	 * Used to determinate different kinds of configurations.
	 */
	public static final String SOURCE_PREFIX = "source.";
	public static final String SINK_PREFIX = "mqtt.sink.";

	public static final String SOURCE_TOPIC_WHITELIST_CONFIG = "topics";
	private static final String SOURCE_TOPIC_WHITELIST_DOC = "Regular expressions indicating the topic to consume from the source broker. "
			+ "The regex is compiled to a <code>java.util.regex.Pattern</code>. "
			+ "For convenience, comma (',') is interpreted as interpreted as the regex-choice symbol ('|').";
	private static final Object SOURCE_TOPIC_WHITELIST_DEFAULT = ConfigDef.NO_DEFAULT_VALUE;

	// Partition Monitor
	public static final String TOPIC_LIST_TIMEOUT_MS_CONFIG = "topic.list.timeout.ms";
	public static final String TOPIC_LIST_TIMEOUT_MS_DOC = "Amount of time the partition monitor thread should wait for kafka to return topic information before logging a timeout error.";
	public static final int TOPIC_LIST_TIMEOUT_MS_DEFAULT = 60000;
	public static final String TOPIC_LIST_POLL_INTERVAL_MS_CONFIG = "topic.list.poll.interval.ms";
	public static final String TOPIC_LIST_POLL_INTERVAL_MS_DOC = "How long to wait before re-querying the source cluster for a change in the partitions to be consumed";
	public static final int TOPIC_LIST_POLL_INTERVAL_MS_DEFAULT = 300000;
	public static final String RECONFIGURE_TASKS_ON_LEADER_CHANGE_CONFIG = "reconfigure.tasks.on.partition.leader.change";
	public static final String RECONFIGURE_TASKS_ON_LEADER_CHANGE_DOC = "Indicates whether the partition monitor should request a task reconfiguration when partition leaders have changed";
	public static final boolean RECONFIGURE_TASKS_ON_LEADER_CHANGE_DEFAULT = false;

	// Internal Connector Timing
	public static final String POLL_LOOP_TIMEOUT_MS_CONFIG = "poll.loop.timeout.ms";
	public static final String POLL_LOOP_TIMEOUT_MS_DOC = "Maximum amount of time to wait in each poll loop without data before cancelling the poll and returning control to the worker task";
	public static final int POLL_LOOP_TIMEOUT_MS_DEFAULT = 1000;
	public static final String MAX_SHUTDOWN_WAIT_MS_CONFIG = "max.shutdown.wait.ms";
	public static final String MAX_SHUTDOWN_WAIT_MS_DOC = "Maximum amount of time to wait before forcing the consumer to close";
	public static final int MAX_SHUTDOWN_WAIT_MS_DEFAULT = 2000;

	/*
	 * General Sink MQTT Config
	 * 
	 */
	public static final String SINK_BROKER_SERVERS_CONFIG = SINK_PREFIX.concat("broker.uri");
	private static final String SINK_BROKER_SERVERS_DOC = "Full uri to mqtt broker.";
	private static final Object SINK_BROKER_SERVERS_DEFAULT = ConfigDef.NO_DEFAULT_VALUE;

	public static final String SINK_TOPIC_CONFIG = SINK_PREFIX.concat("topic");
	private static final String SINK_TOPIC_DOC = "Regular expressions indicating the topic to consume from the source broker. "
			+ "The regex is compiled to a <code>java.util.regex.Pattern</code>. "
			+ "For convenience, comma (',') is interpreted as interpreted as the regex-choice symbol ('|').";
	private static final Object SINK_TOPIC_DEFAULT = ConfigDef.NO_DEFAULT_VALUE;

	public static final String SINK_QOS_CONFIG = SINK_PREFIX.concat("qos");
	private static final String SINK_QOS_DOC = "which QoS to use for mqtt client connection";
	private static final int SINK_QOS_DEFAULT = 0;

	public static final String SINK_CLEAN_CONFIG = SINK_PREFIX.concat("clean.session");
	private static final String SINK_CLEAN_DOC = "If connection should begin with clean session";
	private static final boolean SINK_CLEAN_DEFAULT = MqttConnectOptions.CLEAN_SESSION_DEFAULT;

	public static final String SINK_CLIENT_ID_CONFIG = SINK_PREFIX.concat("client.id");
	private static final String SINK_CLIENT_ID_DOC = "MQTT client id to use don't set to use random";
	private static final String SINK_CLIENT_ID_DEFAULT = "MQTT-source-connector";

	public static final String SINK_CON_TIMEOUT_CONFIG = SINK_PREFIX.concat("connection.timeout");
	private static final String SINK_CON_TIMEOUT_DOC = "Connection timeout limit";
	private static final int SINK_CON_TIMEOUT_DEFAULT = MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT;

	public static final String SINK_KEEP_ALIVE_CONFIG = SINK_PREFIX.concat("keep.alive");
	private static final String SINK_KEEP_ALIVE_DOC = "The interval to keep alive";
	private static final int SINK_KEEP_ALIVE_DEFAULT = MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT;

	/*
	 * SSL DEFS
	 */
	public static final String SINK_SSL_CONFIG = SINK_PREFIX.concat("ssl");
	private static final String SINK_SSL_DOC = "If it will use SSL protocol.";
	private static final Boolean SINK_SSL_DEFAULT = false;

	public static final String SINK_SSL_CA_CONFIG = SINK_PREFIX.concat("ssl.ca");
	private static final String SINK_SSL_CA_DOC = "If secure (SSL) then path to CA is needed.";
	private static final Object SINK_SSL_CA_DEFAULT = ConfigDef.NO_DEFAULT_VALUE;

	public static final String SINK_SSL_CRT_CONFIG = SINK_PREFIX.concat("ssl.crt");
	private static final String SINK_SSL_CRT_DOC = "If secure (SSL) then path to client crt is needed.";
	private static final Object SINK_SSL_CRT_DEFAULT = ConfigDef.NO_DEFAULT_VALUE;

	public static final String SINK_SSL_KEY_CONFIG = SINK_PREFIX.concat("ssl.key");
	private static final String SINK_SSL_KEY_DOC = "If secure (SSL) then path to client key is needed.";
	private static final Object SINK_SSL_KEY_DEFAULT = ConfigDef.NO_DEFAULT_VALUE;

	/* DEFAULTS (KAFKA) */
	public static final String VALUE_SCHEMA_REGISTRY_URL = "value.converter.schema.registry.url";

	public MqttSinkConnectorConfig(Map<String, String> parsedConfig) {
		super(CONFIG, parsedConfig);
	}

	public static final ConfigDef CONFIG = new ConfigDef()
			.define(SOURCE_TOPIC_WHITELIST_CONFIG, Type.STRING, SOURCE_TOPIC_WHITELIST_DEFAULT, TOPIC_REGEX_VALIDATOR,
					Importance.HIGH, SOURCE_TOPIC_WHITELIST_DOC)
			.define(TOPIC_LIST_TIMEOUT_MS_CONFIG, Type.INT, TOPIC_LIST_TIMEOUT_MS_DEFAULT, Importance.LOW,
					TOPIC_LIST_TIMEOUT_MS_DOC)
			.define(TOPIC_LIST_POLL_INTERVAL_MS_CONFIG, Type.INT, TOPIC_LIST_POLL_INTERVAL_MS_DEFAULT,
					Importance.MEDIUM, TOPIC_LIST_POLL_INTERVAL_MS_DOC)
			.define(RECONFIGURE_TASKS_ON_LEADER_CHANGE_CONFIG, Type.BOOLEAN, RECONFIGURE_TASKS_ON_LEADER_CHANGE_DEFAULT,
					Importance.MEDIUM, RECONFIGURE_TASKS_ON_LEADER_CHANGE_DOC)
			.define(POLL_LOOP_TIMEOUT_MS_CONFIG, Type.INT, POLL_LOOP_TIMEOUT_MS_DEFAULT, Importance.LOW,
					POLL_LOOP_TIMEOUT_MS_DOC)
			.define(MAX_SHUTDOWN_WAIT_MS_CONFIG, Type.INT, MAX_SHUTDOWN_WAIT_MS_DEFAULT, Importance.LOW,
					MAX_SHUTDOWN_WAIT_MS_DOC)
			.define(SINK_TOPIC_CONFIG, Type.STRING, SINK_TOPIC_DEFAULT, Importance.HIGH, SINK_TOPIC_DOC)
			.define(SINK_BROKER_SERVERS_CONFIG, Type.STRING, SINK_BROKER_SERVERS_DEFAULT, Importance.HIGH,
					SINK_BROKER_SERVERS_DOC)
			.define(SINK_QOS_CONFIG, Type.INT, SINK_QOS_DEFAULT, Importance.MEDIUM, SINK_QOS_DOC)
			.define(SINK_CLEAN_CONFIG, Type.BOOLEAN, SINK_CLEAN_DEFAULT, Importance.MEDIUM, SINK_CLEAN_DOC)
			.define(SINK_CLIENT_ID_CONFIG, Type.STRING, SINK_CLIENT_ID_DEFAULT, Importance.HIGH, SINK_CLIENT_ID_DOC)
			.define(SINK_CON_TIMEOUT_CONFIG, Type.INT, SINK_CON_TIMEOUT_DEFAULT, Importance.LOW, SINK_CON_TIMEOUT_DOC)
			.define(SINK_KEEP_ALIVE_CONFIG, Type.INT, SINK_KEEP_ALIVE_DEFAULT, Importance.LOW, SINK_KEEP_ALIVE_DOC)
			.define(SINK_SSL_CONFIG, Type.BOOLEAN, SINK_SSL_DEFAULT, Importance.HIGH, SINK_SSL_DOC)
			.define(SINK_SSL_CA_CONFIG, Type.STRING, SINK_SSL_CA_DEFAULT, Importance.MEDIUM, SINK_SSL_CA_DOC)
			.define(SINK_SSL_CRT_CONFIG, Type.STRING, SINK_SSL_CRT_DEFAULT, Importance.MEDIUM, SINK_SSL_CRT_DOC)
			.define(SINK_SSL_KEY_CONFIG, Type.STRING, SINK_SSL_KEY_DEFAULT, Importance.MEDIUM, SINK_SSL_KEY_DOC);

	/*
	 * Returns a java regex pattern that can be used to match kafka topics
	 */
	private static Pattern getTopicWhitelistPattern(String rawRegex) {
		String regex = new String();
		if (Objects.nonNull(rawRegex)) {
			regex = rawRegex.trim().replace(',', '|').replace(" ", "").replaceAll("^[\"']+", "").replaceAll("[\"']+$",
					""); // property files may bring quotes
		}
		try {
			return Pattern.compile(regex);
		} catch (PatternSyntaxException e) {
			throw new ConfigException(regex + " is an invalid regex for CONFIG " + SOURCE_TOPIC_WHITELIST_CONFIG);
		}
	}
}
