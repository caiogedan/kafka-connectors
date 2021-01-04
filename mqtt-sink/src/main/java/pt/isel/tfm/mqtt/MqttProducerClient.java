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

	private SSLSocketFactory sslSocketFactory;
	private MqttClient mqttClient;
	private MqttSinkConnectorConfig sinkConfig;

	private String mqttTopic;
	private String sourceTopic;
	private String mqttClientId;
	private String mqttBrokerUri;
	private int qos;

	public MqttProducerClient(MqttSinkConnectorConfig config) {
		log.debug("STARTING MQTT-SINK-CONNECTOR.");
		sinkConfig = config;

		mqttBrokerUri = sinkConfig.getString(MqttSinkConnectorConfig.SINK_BROKER_SERVERS_CONFIG);
		mqttClientId = sinkConfig.getString(MqttSinkConnectorConfig.SINK_CLIENT_ID_CONFIG);
		mqttTopic = sinkConfig.getString(MqttSinkConnectorConfig.SINK_TOPIC_CONFIG);
		qos = sinkConfig.getInt(MqttSinkConnectorConfig.SINK_QOS_CONFIG);
		sourceTopic = sinkConfig.getString(MqttSinkConnectorConfig.SOURCE_TOPIC_WHITELIST_CONFIG);

		connect();
	}

	public void connect() {
		/*
		 * MQTT CONFIGURATIONS
		 */
		MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();

		mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
		mqttConnectOptions.setServerURIs(new String[] { mqttBrokerUri });

		mqttConnectOptions.setConnectionTimeout(sinkConfig.getInt(MqttSinkConnectorConfig.SINK_CON_TIMEOUT_CONFIG));
		mqttConnectOptions.setKeepAliveInterval(sinkConfig.getInt(MqttSinkConnectorConfig.SINK_KEEP_ALIVE_CONFIG));
		mqttConnectOptions.setCleanSession(sinkConfig.getBoolean(MqttSinkConnectorConfig.SINK_CLEAN_CONFIG));

		/*
		 * SSL CONFIGURATION
		 */
		if (sinkConfig.getBoolean(MqttSinkConnectorConfig.SINK_SSL_CONFIG)) {
			log.debug("SSL TRUE for MqttSinkConnector: MQTT Client: '{}'.", mqttClientId);

			String caCrtFilePath = sinkConfig.getString(MqttSinkConnectorConfig.SINK_SSL_CA_CONFIG);
			String crtFilePath = sinkConfig.getString(MqttSinkConnectorConfig.SINK_SSL_CRT_CONFIG);
			String keyFilePath = sinkConfig.getString(MqttSinkConnectorConfig.SINK_SSL_KEY_CONFIG);

			try {
				SSLUtils sslUtils = new SSLUtils(caCrtFilePath, crtFilePath, keyFilePath);
				sslSocketFactory = sslUtils.getMqttSocketFactory();
			} catch (IOException e) {
				log.error(e.getCause().toString());
			}

			mqttConnectOptions.setSocketFactory(sslSocketFactory);
		} else
			log.debug("SSL FALSE for MqttSinkConnector: MQTT Client: '{}'.", mqttClientId);

		/*
		 * MQTT CONNECT
		 */

		try {
			mqttClient = new MqttClient(mqttBrokerUri, mqttClientId, new MemoryPersistence());
			mqttClient.connect(mqttConnectOptions);
			log.debug("SUCCESSFULL MQTT CONNECTION for MqttSinkConnector, MQTT Client: '{}'.", mqttClientId);
		} catch (MqttException e) {
			log.error(e.getCause().toString());
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

	public String getSourceTopic() {
		return sourceTopic;
	}

	public String getMqttTopic() {
		return mqttTopic;
	}

	public int getQos() {
		return qos;
	}

}
