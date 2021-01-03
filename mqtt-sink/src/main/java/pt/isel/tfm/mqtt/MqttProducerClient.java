package pt.isel.tfm.mqtt;

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
	private String connectorName;
	private MqttClient mqttClient;
	private String topic_regex;
	private String mqtt_topic;
	private MqttSinkConnectorConfig connectorConfiguration;
	private SSLSocketFactory sslSocketFactory;

	public MqttProducerClient(MqttSinkConnectorConfig config) {
		this.connectorConfiguration = config;

		connectorName = connectorConfiguration.getString(MqttSinkConnectorConfig.MQTT_SERVER);
		mqttClientId = connectorConfiguration.getString(MqttSinkConnectorConfig.MQTT_CLIENTID);
		topic_regex = connectorConfiguration.getString(MqttSinkConnectorConfig.TOPIC_REGEX);
		mqtt_topic = connectorConfiguration.getString(MqttSinkConnectorConfig.MQTT_TOPIC);
		qos = connectorConfiguration.getInt(MqttSinkConnectorConfig.MQTT_QOS);

		log.debug("Starting MqttProducerClient with connector name: '{}'", connectorName);
		connect();
	}

	public void connect() {
		log.debug("Starting mqtt client: '{}', for connector: '{}'", mqttClientId, connectorName);
		MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
		mqttConnectOptions
				.setServerURIs(new String[] { connectorConfiguration.getString(MqttSinkConnectorConfig.MQTT_SERVER) });
		mqttConnectOptions
				.setConnectionTimeout(connectorConfiguration.getInt(MqttSinkConnectorConfig.MQTT_COMM_TIMEOUT));
		mqttConnectOptions.setKeepAliveInterval(connectorConfiguration.getInt(MqttSinkConnectorConfig.MQTT_KEEP_ALIVE));
		mqttConnectOptions.setCleanSession(connectorConfiguration.getBoolean(MqttSinkConnectorConfig.MQTT_CLEAN));

		if (connectorConfiguration.getBoolean(MqttSinkConnectorConfig.MQTT_SSL)) {
			log.debug("SSL TRUE for AsamMqttSinkConnectorTask: '{}, and mqtt client: '{}'.", connectorName,
					mqttClientId);

			String caCrtFilePath = connectorConfiguration.getString(MqttSinkConnectorConfig.MQTT_SSL_CA);
			String crtFilePath = connectorConfiguration.getString(MqttSinkConnectorConfig.MQTT_SSL_CRT);
			String keyFilePath = connectorConfiguration.getString(MqttSinkConnectorConfig.MQTT_SSL_KEY);

			try {
				SSLUtils sslUtils = new SSLUtils(caCrtFilePath, crtFilePath, keyFilePath);
				sslSocketFactory = sslUtils.getMqttSocketFactory();

			} catch (IOException e) {
				log.error(e.getCause().toString());
			}

			mqttConnectOptions.setSocketFactory(sslSocketFactory);
		} else
			log.debug("SSL FALSE for AsamMqttSinkConnectorTask: '{}, and mqtt client: '{}'.", connectorName,
					mqttClientId);

		try {
			mqttClient = new MqttClient(connectorConfiguration.getString(MqttSinkConnectorConfig.MQTT_SERVER),
					mqttClientId, new MemoryPersistence());
			mqttClient.connect(mqttConnectOptions);
			log.debug("SUCCESSFULL MQTT CONNECTION for AsamMqttSinkConnectorTask: '{}, and mqtt client: '{}'.",
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
			log.error(e.getCause().toString());
		}
	}

	public void disconnect() {
		try {
			mqttClient.disconnect();
		} catch (MqttException e) {
			log.error(e.getCause().toString());
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

	public String getMqtt_topic() {
		return mqtt_topic;
	}

	public int getQos() {
		return qos;
	}

	public String getTopic_regex() {
		return topic_regex;
	}

}
