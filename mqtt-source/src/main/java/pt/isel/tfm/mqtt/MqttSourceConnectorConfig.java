package pt.isel.tfm.mqtt;

import java.util.HashMap;
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

public class MqttSourceConnectorConfig extends AbstractConfig {

	private static final Validator TOPIC_REGEX_VALIDATOR = new ConfigDef.Validator() {
		@Override
		public void ensureValid(String name, Object value) {
			getTopicPattern((String) value);
		}
	};

	/*
	 * Used to determinate different kinds of configurations.
	 */
	public static final String SOURCE_PREFIX = "mqtt.source.";
	public static final String DESTINATION_PREFIX = "destination.";

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
	 * General Source MQTT Config - Applies to Consumer and Admin Client if not
	 * overridden by ADMIN_CLIENT_PREFIX
	 */
	public static final String SOURCE_BROKER_SERVERS_CONFIG = SOURCE_PREFIX.concat("broker.uri");
	private static final String SOURCE_BROKER_SERVERS_DOC = "Full uri to mqtt broker.";
	private static final Object SOURCE_BROKER_SERVERS_DEFAULT = ConfigDef.NO_DEFAULT_VALUE;

	public static final String SOURCE_TOPIC_CONFIG = SOURCE_PREFIX.concat("topic");
	private static final String SOURCE_TOPIC_DOC = "Regular expressions indicating the topic to consume from the source broker. "
			+ "The regex is compiled to a <code>java.util.regex.Pattern</code>. "
			+ "For convenience, comma (',') is interpreted as interpreted as the regex-choice symbol ('|').";
	private static final Object SOURCE_TOPIC_DEFAULT = ConfigDef.NO_DEFAULT_VALUE;

	public static final String SOURCE_QOS_CONFIG = SOURCE_PREFIX.concat("qos");
	private static final String SOURCE_QOS_DOC = "which QoS to use for mqtt client connection";
	private static final int SOURCE_QOS_DEFAULT = 0;

	public static final String SOURCE_CLEAN_CONFIG = SOURCE_PREFIX.concat("clean.session");
	private static final String SOURCE_CLEAN_DOC = "If connection should begin with clean session";
	private static final boolean SOURCE_CLEAN_DEFAULT = MqttConnectOptions.CLEAN_SESSION_DEFAULT;

	public static final String SOURCE_CLIENT_ID_CONFIG = SOURCE_PREFIX.concat("client.id");
	private static final String SOURCE_CLIENT_ID_DOC = "MQTT client id to use don't set to use random";
	private static final String SOURCE_CLIENT_ID_DEFAULT = "MQTT-source-connector";

	public static final String SOURCE_CON_TIMEOUT_CONFIG = SOURCE_PREFIX.concat("connection.timeout");
	private static final String SOURCE_CON_TIMEOUT_DOC = "Connection timeout limit";
	private static final int SOURCE_CON_TIMEOUT_DEFAULT = MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT;

	public static final String SOURCE_KEEP_ALIVE_CONFIG = SOURCE_PREFIX.concat("keep.alive");
	private static final String SOURCE_KEEP_ALIVE_DOC = "The interval to keep alive";
	private static final int SOURCE_KEEP_ALIVE_DEFAULT = MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT;

	public static final String SOURCE_SSL_CONFIG = SOURCE_PREFIX.concat("ssl");
	private static final String SOURCE_SSL_DOC = "If it will use SSL protocol.";
	private static final Boolean SOURCE_SSL_DEFAULT = false;

	public static final String SOURCE_SSL_CA_CONFIG = SOURCE_PREFIX.concat("ssl.ca");
	private static final String SOURCE_SSL_CA_DOC = "If secure (SSL) then path to CA is needed.";
	private static final Object SOURCE_SSL_CA_DEFAULT = ConfigDef.NO_DEFAULT_VALUE;

	public static final String SOURCE_SSL_CRT_CONFIG = SOURCE_PREFIX.concat("ssl.crt");
	private static final String SOURCE_SSL_CRT_DOC = "If secure (SSL) then path to client crt is needed.";
	private static final Object SOURCE_SSL_CRT_DEFAULT = ConfigDef.NO_DEFAULT_VALUE;

	public static final String SOURCE_SSL_KEY_CONFIG = SOURCE_PREFIX.concat("ssl.key");
	private static final String SOURCE_SSL_KEY_DOC = "If secure (SSL) then path to client key is needed.";
	private static final Object SOURCE_SSL_KEY_DEFAULT = ConfigDef.NO_DEFAULT_VALUE;

	/*
	 * DESTINATION (KAFKA)
	 */
	public static final String DESTINATION_TOPIC_CONFIG = DESTINATION_PREFIX.concat("topic");
	private static final String DESTINATION_TOPIC_DOC = "topic to publish on.";
	private static final Object DESTINATION_TOPIC_DEFAULT = ConfigDef.NO_DEFAULT_VALUE;

	public static final String VALUE_SCHEMA_REGISTRY_URL = "value.converter.schema.registry.url";
	public static final String ENCODING = "UTF-8";

	public MqttSourceConnectorConfig(ConfigDef config, Map<String, String> parsedConfig) {
		super(config, parsedConfig);
	}

	public MqttSourceConnectorConfig(Map<String, String> parsedConfig) {
		super(CONFIG, parsedConfig);
	}

	public static final ConfigDef CONFIG = new ConfigDef()
			.define(SOURCE_TOPIC_CONFIG, Type.STRING, SOURCE_TOPIC_DEFAULT, TOPIC_REGEX_VALIDATOR, Importance.HIGH,
					SOURCE_TOPIC_DOC)
			.define(SOURCE_BROKER_SERVERS_CONFIG, Type.STRING, SOURCE_BROKER_SERVERS_DEFAULT, Importance.HIGH,
					SOURCE_BROKER_SERVERS_DOC)
			.define(SOURCE_QOS_CONFIG, Type.INT, SOURCE_QOS_DEFAULT, Importance.MEDIUM, SOURCE_QOS_DOC)
			.define(SOURCE_CLEAN_CONFIG, Type.BOOLEAN, SOURCE_CLEAN_DEFAULT, Importance.MEDIUM, SOURCE_CLEAN_DOC)
			.define(SOURCE_CLIENT_ID_CONFIG, Type.STRING, SOURCE_CLIENT_ID_DEFAULT, Importance.HIGH,
					SOURCE_CLIENT_ID_DOC)
			.define(SOURCE_CON_TIMEOUT_CONFIG, Type.INT, SOURCE_CON_TIMEOUT_DEFAULT, Importance.LOW,
					SOURCE_CON_TIMEOUT_DOC)
			.define(SOURCE_KEEP_ALIVE_CONFIG, Type.INT, SOURCE_KEEP_ALIVE_DEFAULT, Importance.LOW,
					SOURCE_KEEP_ALIVE_DOC)
			.define(SOURCE_SSL_CONFIG, Type.BOOLEAN, SOURCE_SSL_DEFAULT, Importance.HIGH, SOURCE_SSL_DOC)
			.define(SOURCE_SSL_CA_CONFIG, Type.STRING, SOURCE_SSL_CA_DEFAULT, Importance.MEDIUM, SOURCE_SSL_CA_DOC)
			.define(SOURCE_SSL_CRT_CONFIG, Type.STRING, SOURCE_SSL_CRT_DEFAULT, Importance.MEDIUM, SOURCE_SSL_CRT_DOC)
			.define(SOURCE_SSL_KEY_CONFIG, Type.STRING, SOURCE_SSL_KEY_DEFAULT, Importance.MEDIUM, SOURCE_SSL_KEY_DOC)
			.define(DESTINATION_TOPIC_CONFIG, Type.STRING, DESTINATION_TOPIC_DEFAULT, Importance.HIGH,
					DESTINATION_TOPIC_DOC);

	/*
	 * Returns all values with a specified prefix with the prefix stripped from the
	 * key
	 */
	public Map<String, Object> allWithPrefix(String prefix) {
		return allWithPrefix(prefix, true);
	}

	/*
	 * Returns all values with a specified prefix with the prefix stripped from the
	 * key if desired. Original input is set first, then overwritten (if applicable)
	 * with the parsed values
	 */
	public Map<String, Object> allWithPrefix(String prefix, boolean stripPrefix) {
		Map<String, Object> result = originalsWithPrefix(prefix, stripPrefix);
		for (Map.Entry<String, ?> entry : values().entrySet()) {
			if (entry.getKey().startsWith(prefix) && entry.getKey().length() > prefix.length()) {
				if (stripPrefix)
					result.put(entry.getKey().substring(prefix.length()), entry.getValue());
				else
					result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}

	public Map<String, String> allAsStrings() {
		Map<String, String> result = new HashMap<>();
		result.put(TOPIC_LIST_TIMEOUT_MS_CONFIG, String.valueOf(getInt(TOPIC_LIST_TIMEOUT_MS_CONFIG)));
		result.put(TOPIC_LIST_POLL_INTERVAL_MS_CONFIG, String.valueOf(getInt(TOPIC_LIST_POLL_INTERVAL_MS_CONFIG)));
		result.put(RECONFIGURE_TASKS_ON_LEADER_CHANGE_CONFIG,
				String.valueOf(getBoolean(RECONFIGURE_TASKS_ON_LEADER_CHANGE_CONFIG)));
		result.put(POLL_LOOP_TIMEOUT_MS_CONFIG, String.valueOf(getInt(POLL_LOOP_TIMEOUT_MS_CONFIG)));
		result.put(MAX_SHUTDOWN_WAIT_MS_CONFIG, String.valueOf(getInt(MAX_SHUTDOWN_WAIT_MS_CONFIG)));
		result.putAll(originalsStrings());

		return result;
	}

	/*
	 * Returns a java regex pattern that can be used to match MQTT topics
	 */
	private static Pattern getTopicPattern(String rawRegex) {
		String regex = new String();
		if (Objects.nonNull(rawRegex)) {
			regex = rawRegex.trim().replace("/", "\\").replace("+", "[a-zA-Z0-9 _.-]*").replace("#",
					"[a-zA-Z0-9 //_#+.-]*");
		}
		try {
			return Pattern.compile(regex);
		} catch (PatternSyntaxException e) {
			throw new ConfigException(regex + " is an invalid regex for CONFIG " + SOURCE_TOPIC_CONFIG);
		}
	}
}
