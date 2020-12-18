package pt.isel.tfm.mqtt;

import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_CLEAN;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_CLIENTID;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_COMM_TIMEOUT;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_CONNECTOR_TOPIC_KEY;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_KEEP_ALIVE;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_QOS;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_SERVER;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.TOPIC_REGEX;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.security.SSLSocketFactoryFactory;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttProducerClient {

	private static final Logger log = LoggerFactory.getLogger(MqttProducerClient.class);

	private int qos;
	private String mqttClientId;
	private String mqttTopicKey;
	private String connectorName;
	private MqttClient mqttClient;
	private String topic_regex;
	private MqttSinkConnectorConfig connectorConfiguration;
	private SSLSocketFactoryFactory sslSocketFactory;

	public MqttProducerClient(MqttSinkConnectorConfig config) {
		this.connectorConfiguration = config;

		connectorName = connectorConfiguration.getString(MQTT_SERVER);
		mqttClientId = connectorConfiguration.getString(MQTT_CLIENTID);
		mqttTopicKey = connectorConfiguration.getString(MQTT_CONNECTOR_TOPIC_KEY);
		qos = connectorConfiguration.getInt(MQTT_QOS);
		topic_regex = connectorConfiguration.getString(TOPIC_REGEX);

		log.debug("Starting MqttProducerClient with connector name: '{}'", connectorName);
		connect();
	}

	public void connect() {
		log.debug("Starting mqtt client: '{}', for connector: '{}'", mqttClientId, connectorName);
		MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
		mqttConnectOptions.setServerURIs(new String[] { connectorConfiguration.getString(MQTT_SERVER) });
		mqttConnectOptions.setConnectionTimeout(connectorConfiguration.getInt(MQTT_COMM_TIMEOUT));
		mqttConnectOptions.setKeepAliveInterval(connectorConfiguration.getInt(MQTT_KEEP_ALIVE));
		mqttConnectOptions.setCleanSession(connectorConfiguration.getBoolean(MQTT_CLEAN));

		if (connectorConfiguration.getBoolean("mqtt.connector.ssl") == true) {
			log.debug("SSL TRUE for MqttSinkConnectorTask: '{}, and mqtt client: '{}'.", connectorName, mqttClientId);
			try {
				String caCrtFilePath = connectorConfiguration.getString("mqtt.connector.ssl.ca");
				String crtFilePath = connectorConfiguration.getString("mqtt.connector.ssl.crt");
				String keyFilePath = connectorConfiguration.getString("mqtt.connector.ssl.key");
				// SSLUtils sslUtils = new SSLUtils(caCrtFilePath, crtFilePath, keyFilePath);
				// sslSocketFactory = sslUtils.getMqttSocketFactory();
				// mqttConnectOptions.setSocketFactory(sslSocketFactory);
			} catch (Exception e) {
				log.error("Not able to create socket factory for mqtt client: '{}', and connector: '{}'", mqttClientId,
						connectorName);
				log.error(e.getMessage());
			}
		} else {
			log.debug("SSL FALSE for MqttSinkConnectorTask: '{}, and mqtt client: '{}'.", connectorName, mqttClientId);
		}

		try {
			mqttClient = new MqttClient(connectorConfiguration.getString(MQTT_SERVER), mqttClientId,
					new MemoryPersistence());
			mqttClient.connect(mqttConnectOptions);
			log.debug("SUCCESSFULL MQTT CONNECTION for MqttSinkConnectorTask: '{}, and mqtt client: '{}'.",
					connectorName, mqttClientId);
		} catch (MqttException e) {
			log.error("FAILED MQTT CONNECTION for MqttSinkConnectorTask: '{}, and mqtt client: '{}'.", connectorName,
					mqttClientId);
			log.error(e.getMessage());
		}
	}

	public boolean isConnected() {
		return mqttClient.isConnected();
	}

	public void publish(String topic, MqttMessage message) {
		try {
			mqttClient.publish(topic, message);
		} catch (MqttException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
	}

	public void disconnect() {
		try {
			mqttClient.disconnect();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public MqttClient getMqttClient() {
		return mqttClient;
	}

	public String getMqttClientId() {
		return mqttClientId;
	}

	public String getConnectorName() {
		return connectorName;
	}

	public String getMqttTopicKey() {
		return mqttTopicKey;
	}

	public int getQos() {
		return qos;
	}

	public String getTopic_regex() {
		return topic_regex;
	}

}
