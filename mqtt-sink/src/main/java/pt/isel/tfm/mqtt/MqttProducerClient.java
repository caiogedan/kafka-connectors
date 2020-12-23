package pt.isel.tfm.mqtt;

import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_CLEAN;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_CLIENTID;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_COMM_TIMEOUT;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_CONNECTOR_TOPIC_KEY;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_KEEP_ALIVE;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_QOS;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_SERVER;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_SSL;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_SSL_CA;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_SSL_CRT;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.MQTT_SSL_KEY;
import static pt.isel.tfm.mqtt.MqttSinkConnectorConfig.TOPIC_REGEX;

import java.io.IOException;

import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.isel.tfm.utils.SSLUtils;

public class MqttProducerClient {

	private static final Logger log = LoggerFactory.getLogger(MqttProducerClient.class);

	private int qos;
	private String mqttClientId;
	private String mqttTopicKey;
	private String connectorName;
	private MqttClient mqttClient;
	private String topic_regex;
	private MqttSinkConnectorConfig connectorConfiguration;
	private SSLSocketFactory sslSocketFactory;

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

		if (connectorConfiguration.getBoolean(MQTT_SSL)) {
			log.info("SSL TRUE for AsamMqttSinkConnectorTask: '{}, and mqtt client: '{}'.", connectorName, mqttClientId);

			String caCrtFilePath = connectorConfiguration.getString(MQTT_SSL_CA);
			String crtFilePath = connectorConfiguration.getString(MQTT_SSL_CRT);
			String keyFilePath = connectorConfiguration.getString(MQTT_SSL_KEY);

			try {
				SSLUtils sslUtils = new SSLUtils(caCrtFilePath, crtFilePath, keyFilePath);
				sslSocketFactory = sslUtils.getMqttSocketFactory();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			mqttConnectOptions.setSocketFactory(sslSocketFactory);
		} else
			log.info("SSL FALSE for AsamMqttSinkConnectorTask: '{}, and mqtt client: '{}'.", connectorName, mqttClientId);

		try {
			mqttClient = new MqttClient(connectorConfiguration.getString(MQTT_SERVER), mqttClientId,
					new MemoryPersistence());
			mqttClient.connect(mqttConnectOptions);
			log.info("SUCCESSFULL MQTT CONNECTION for AsamMqttSinkConnectorTask: '{}, and mqtt client: '{}'.",
					connectorName, mqttClientId);
		} catch (MqttException e) {
			log.error("FAILED MQTT CONNECTION for AsamMqttSinkConnectorTask: '{}, and mqtt client: '{}'.",
					connectorName, mqttClientId);
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
