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

	/* MQTT Defs */
	public static final String MQTT_SERVER = "mqtt.connector.broker.uri";
	public static final String MQTT_TOPIC = "mqtt.connector.broker.topic";
	public static final String MQTT_QOS = "mqtt.connector.qos";
	public static final String MQTT_CLEAN = "mqtt.connector.clean_session";
	public static final String MQTT_CLIENTID = "mqtt.connector.client.id";
	public static final String MQTT_COMM_TIMEOUT = "mqtt.connector.connection_timeout";
	public static final String MQTT_KEEP_ALIVE = "mqtt.connector.keep_alive";
	public static final String MQTT_SSL = "mqtt.connector.ssl";
	public static final String MQTT_SSL_CA = "mqtt.connector.ssl.ca";
	public static final String MQTT_SSL_CRT = "mqtt.connector.ssl.crt";
	public static final String MQTT_SSL_KEY = "mqtt.connector.ssl.key";
	
	public static final String TOPIC_REGEX = "topics.regex";

	/* KAFKA defs */
	public static final String MQTT_CONNECTOR_KAFKA_NAME = "mqtt.connector.kafka.name";
	public static final String MQTT_CONNECTOR_TOPIC_KEY = "mqtt.connector.mqtt_topic_key";

	public MqttSinkConnectorConfig(ConfigDef config, Map<String, String> parsedConfig) {
		super(config, parsedConfig);
	}

	public MqttSinkConnectorConfig(Map<String, String> parsedConfig) {
		super(configuration, parsedConfig);
	}

	public static ConfigDef baseConfigDef() {
		ConfigDef configDef = new ConfigDef();
		configDef.define(MQTT_SERVER, Type.STRING, "tcp://localhost:1883", Importance.HIGH, "Full uri to mqtt broker")
				.define(MQTT_TOPIC, Type.STRING, "upstream/#", Importance.HIGH, "mqtt server to connect to")
				.define(MQTT_CLIENTID, Type.STRING, "kafka_sink_connector", Importance.MEDIUM,
						"mqtt client id to use don't set to use random")
				.define(MQTT_CLEAN, Type.BOOLEAN, true, Importance.MEDIUM,
						"If connection should begin with clean session")
				.define(MQTT_COMM_TIMEOUT, Type.INT, 30, Importance.LOW, "Connection timeout limit")
				.define(MQTT_KEEP_ALIVE, Type.INT, 60, Importance.LOW, "The interval to keep alive")
				.define(MQTT_QOS, Type.INT, 1, Importance.LOW, "which qos to use for paho client connection")
				.define(MQTT_SSL, Type.BOOLEAN, false, Importance.LOW, "which qos to use for paho client connection")
				.define(MQTT_SSL_CA, Type.STRING, "./ca.crt", Importance.LOW,
						"If secure (SSL) then path to CA is needed.")
				.define(MQTT_SSL_CRT, Type.STRING, "./client.crt", Importance.LOW,
						"If secure (SSL) then path to client crt is needed.")
				.define(MQTT_SSL_KEY, Type.STRING, "./client.key", Importance.LOW,
						"If secure (SSL) then path to client key is needed.")
				.define(MQTT_CONNECTOR_KAFKA_NAME, Type.STRING, "mqtt_downstream", Importance.MEDIUM,
						"Name used by conenctor to Kafka connection api.")

				.define("topics.regex", Type.STRING, "test*", Importance.MEDIUM,
						"Kafka topic to publish on. This depends on processing unit.")

				.define(MQTT_CONNECTOR_TOPIC_KEY, Type.STRING, "topic", Importance.MEDIUM,
						"Mqtt topic key, used to fetch topic from json record. processing unit. Topic used to publish to mqtt broker");
			
		log.debug("ConfigDef loaded: '{}'", configDef.toRst());
		return configDef;
	}
}
