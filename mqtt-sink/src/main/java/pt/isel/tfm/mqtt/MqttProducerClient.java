package pt.isel.tfm.mqtt;

import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_CLEAN;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_CLIENTID;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_COMM_TIMEOUT;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_CONNECTOR_TOPIC_KEY;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_KEEP_ALIVE;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_QOS;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_SERVER;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.internal.security.SSLSocketFactoryFactory;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttProducerClient {

	private static final Logger log = LoggerFactory.getLogger(MqttProducerClient.class);

	private MqttClient mqttClient;
	private String kafkaTopic;
	private String mqttTopicKey;
	private String mqttClientId;
	private String connectorName;
	private int qos;
	private MqttSinkConnectorConfig connectorConfiguration;
	private SSLSocketFactoryFactory sslSocketFactory;

	public MqttProducerClient(MqttSinkConnectorConfig config) {
		this.connectorConfiguration = config;

		log.info("Starting MqttProducerClient with connector name: '{}'", connectorName);

		connectorName = connectorConfiguration.getString(MQTT_SERVER);
		mqttClientId = connectorConfiguration.getString(MQTT_CLIENTID);
		mqttTopicKey = connectorConfiguration.getString(MQTT_CONNECTOR_TOPIC_KEY);
		qos = connectorConfiguration.getInt(MQTT_QOS);
		log.info("Starting MqttProducerClient with connector name: '{}'", connectorName);
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
			log.info("SSL TRUE for MqttSinkConnectorTask: '{}, and mqtt client: '{}'.", connectorName, mqttClientId);
			try {
				// String caCrtFilePath =
				// connectorConfiguration.getString("mqtt.connector.ssl.ca");
				// String crtFilePath =
				// connectorConfiguration.getString("mqtt.connector.ssl.crt");
				// String keyFilePath =
				// connectorConfiguration.getString("mqtt.connector.ssl.key");
				// SSLUtils sslUtils = new SSLUtils(caCrtFilePath, crtFilePath, keyFilePath);
				// sslSocketFactory = sslUtils.getMqttSocketFactory();
				// mqttConnectOptions.setSocketFactory(sslSocketFactory);
			} catch (Exception e) {
				log.error("Not able to create socket factory for mqtt client: '{}', and connector: '{}'", mqttClientId,
						connectorName);
				log.error(e.getMessage());
			}
		} else {
			log.info("SSL FALSE for MqttSinkConnectorTask: '{}, and mqtt client: '{}'.", connectorName, mqttClientId);
		}

		try {
			mqttClient = new MqttClient(connectorConfiguration.getString(MQTT_SERVER), mqttClientId,
					new MemoryPersistence());
			mqttClient.connect(mqttConnectOptions);
			log.info("SUCCESSFULL MQTT CONNECTION for MqttSinkConnectorTask: '{}, and mqtt client: '{}'.",
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

	public void publish(String topic, MqttMessage message) throws MqttPersistenceException, MqttException {
		mqttClient.publish(topic, message);
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

	public String getKafkaTopic() {
		return kafkaTopic;
	}

	public String getMqttTopicKey() {
		return mqttTopicKey;
	}

	public int getQos() {
		return qos;
	}

}
