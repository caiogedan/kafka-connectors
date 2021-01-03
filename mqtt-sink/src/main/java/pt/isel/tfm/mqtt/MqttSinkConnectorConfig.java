package pt.isel.tfm.mqtt;

import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttSinkConnectorConfig extends AbstractConfig {

	private static final Logger log = LoggerFactory.getLogger(MqttSinkConnectorConfig.class);
	public static ConfigDef configuration = baseConfigDef();

	/* MQTT BROKER DEFS */
	public static final String MQTT_SERVER = "mqtt.connector.broker.uri";
	private static final String MQTT_SERVER_DOC = "Full uri to mqtt broker";

	public static final String MQTT_TOPIC = "mqtt.connector.broker.topic";
	private static final String MQTT_TOPIC_DOC = "mqtt server to connect to";

	public static final String MQTT_QOS = "mqtt.connector.qos";
	private static final String MQTT_QOS_DOC = "which qos to use for paho client connection";

	public static final String MQTT_CLEAN = "mqtt.connector.clean_session";
	private static final String MQTT_CLEAN_DOC = "If connection should begin with clean session";

	public static final String MQTT_CLIENTID = "mqtt.connector.client.id";
	private static final String MQTT_CLIENTID_DOC = "mqtt client id to use don't set to use random";

	public static final String MQTT_COMM_TIMEOUT = "mqtt.connector.connection_timeout";
	private static final String MQTT_COMM_TIMEOUT_DOC = "Connection timeout limit";

	public static final String MQTT_KEEP_ALIVE = "mqtt.connector.keep_alive";
	private static final String MQTT_KEEP_ALIVE_DOC = "The interval to keep alive";

	public static final String MQTT_SSL = "mqtt.connector.ssl";
	private static final String MQTT_SSL_DOC = "which qos to use for paho client connection";

	public static final String MQTT_SSL_CA = "mqtt.connector.ssl.ca";
	private static final String MQTT_SSL_CA_DOC = "If secure (SSL) then path to CA is needed.";

	public static final String MQTT_SSL_CRT = "mqtt.connector.ssl.crt";
	private static final String MQTT_SSL_CRT_DOC = "If secure (SSL) then path to client crt is needed.";

	public static final String MQTT_SSL_KEY = "mqtt.connector.ssl.key";
	private static final String MQTT_SSL_KEY_DOC = "If secure (SSL) then path to client key is needed.";

	/* KAFKA DEFS (COPY) */

	public static final String MQTT_KAFKA_URI = "mqtt.connector.kafka.uri";
	private static final String MQTT_KAFKA_URI_DOC = "Full uri to Kafka Cluster";

	public static final String MQTT_KAFKA_REPLICATION_FACTOR = "mqtt.connector.replication.factor";
	private static final String MQTT_KAFKA_REPLICATION_FACTOR_DOC = "Kafka replication factor";

	public static final String MQTT_KAFKA_TOPIC = "mqtt.connector.kafka.topic";
	private static final String MQTT_KAFKA_TOPIC_DOC = "Kafka topic to publish on. This depends on processing unit.";

	public static final String MQTT_CONNECTOR_KAFKA_NAME = "mqtt.connector.kafka.name";
	private static final String MQTT_CONNECTOR_KAFKA_NAME_DOC = "Name used by connector to Kafka connection api";

	/* DEFAULTS (KAFKA) */
	public static final String TOPIC_REGEX = "topics.regex";

	public static final String CONNECTOR_KAFKA_DEFAULT_URI = "confluent.topic.bootstrap.servers";
	public static final String CONNECTOR_KAFKA_DEFAULT_FACTOR = "confluent.topic.replication.factor";

	public static final String VALUE_SCHEMA_REGISTRY_URL = "value.converter.schema.registry.url";

	public MqttSinkConnectorConfig(ConfigDef config, Map<String, String> parsedConfig) {
		super(config, parsedConfig);
	}

	public MqttSinkConnectorConfig(Map<String, String> parsedConfig) {
		super(configuration, parsedConfig);
	}

	public static ConfigDef baseConfigDef() {
		ConfigDef configDef = new ConfigDef();
		configDef.define(MQTT_SERVER, Type.STRING, "tcp://localhost:1883", Importance.HIGH, MQTT_SERVER_DOC)
				.define(MQTT_TOPIC, Type.STRING, "locations/#", Importance.HIGH, MQTT_TOPIC_DOC)
				.define(MQTT_CLIENTID, Type.STRING, "kafka_source_connector", Importance.MEDIUM, MQTT_CLIENTID_DOC)
				.define(MQTT_CLEAN, Type.BOOLEAN, true, Importance.MEDIUM, MQTT_CLEAN_DOC)
				.define(MQTT_COMM_TIMEOUT, Type.INT, 30, Importance.LOW, MQTT_COMM_TIMEOUT_DOC)
				.define(MQTT_KEEP_ALIVE, Type.INT, 60, Importance.LOW, MQTT_KEEP_ALIVE_DOC)
				.define(MQTT_QOS, Type.INT, 1, Importance.LOW, MQTT_QOS_DOC)
				.define(MQTT_SSL, Type.BOOLEAN, false, Importance.LOW, MQTT_SSL_DOC)
				.define(MQTT_SSL_CA, Type.STRING, "./ca.crt", Importance.LOW, MQTT_SSL_CA_DOC)
				.define(MQTT_SSL_CRT, Type.STRING, "./client.crt", Importance.LOW, MQTT_SSL_CRT_DOC)
				.define(MQTT_SSL_KEY, Type.STRING, "./client.key", Importance.LOW, MQTT_SSL_KEY_DOC)
				.define(MQTT_KAFKA_TOPIC, Type.STRING, "locations", Importance.MEDIUM, MQTT_KAFKA_TOPIC_DOC)
				.define(MQTT_CONNECTOR_KAFKA_NAME, Type.STRING, "mqtt-source_kafka", Importance.MEDIUM,
						MQTT_CONNECTOR_KAFKA_NAME_DOC)
				.define(MQTT_KAFKA_URI, Type.STRING, "localhost:9092", Importance.HIGH, MQTT_KAFKA_URI_DOC)
				.define(MQTT_KAFKA_REPLICATION_FACTOR, Type.INT, 1, Importance.MEDIUM,
						MQTT_KAFKA_REPLICATION_FACTOR_DOC);

		log.debug("ConfigDef loaded: '{}'", configDef.toRst());
		return configDef;
	}
}
