package pt.isel.tfm.mqtt;

import java.io.IOException;

import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.isel.tfm.utils.SSLUtils;

public class MqttConsumerClient implements IMqttActionListener {

	private static final Logger log = LoggerFactory.getLogger(MqttConsumerClient.class);

	private MqttClient mqttClient;
	private String kafkaTopic;
	private String mqttTopic;
	private String mqttClientId;
	private String connectorName;
	private MqttSourceConnectorConfig connectorConfiguration;
	private SSLSocketFactory sslSocketFactory;
	private MqttCallback callback;

	public MqttConsumerClient(MqttSourceConnectorConfig config, MqttCallback callback) {
		this.connectorConfiguration = config;

		log.debug("Starting MqttConsumerClient with connector name: '{}'", connectorName);

		connectorName = connectorConfiguration.getString(MqttSourceConnectorConfig.MQTT_CONNECTOR_KAFKA_NAME);
		kafkaTopic = connectorConfiguration.getString(MqttSourceConnectorConfig.MQTT_KAFKA_TOPIC);
		mqttClientId = connectorConfiguration.getString(MqttSourceConnectorConfig.MQTT_CLIENTID);
		mqttTopic = connectorConfiguration.getString(MqttSourceConnectorConfig.MQTT_TOPIC);
		this.callback = callback;

		connect();
	}

	public void connect() {
		/*
		 * MQTT CONFIGURATIONS
		 */
		MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
		mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
		mqttConnectOptions.setServerURIs(
				new String[] { connectorConfiguration.getString(MqttSourceConnectorConfig.MQTT_SERVER) });
		mqttConnectOptions
				.setConnectionTimeout(connectorConfiguration.getInt(MqttSourceConnectorConfig.MQTT_COMM_TIMEOUT));
		mqttConnectOptions
				.setKeepAliveInterval(connectorConfiguration.getInt(MqttSourceConnectorConfig.MQTT_KEEP_ALIVE));
		mqttConnectOptions.setCleanSession(connectorConfiguration.getBoolean(MqttSourceConnectorConfig.MQTT_CLEAN));

		/*
		 * SSL CONFIGURATION
		 */
		if (connectorConfiguration.getBoolean(MqttSourceConnectorConfig.MQTT_SSL)) {
			log.debug("SSL TRUE for MqttSourceConnectorTask: '{}, and mqtt client: '{}'.", connectorName, mqttClientId);
			String caCrtFilePath = connectorConfiguration.getString(MqttSourceConnectorConfig.MQTT_SSL_CA);
			String crtFilePath = connectorConfiguration.getString(MqttSourceConnectorConfig.MQTT_SSL_CRT);
			String keyFilePath = connectorConfiguration.getString(MqttSourceConnectorConfig.MQTT_SSL_KEY);

			try {
				SSLUtils sslUtils = new SSLUtils(caCrtFilePath, crtFilePath, keyFilePath);
				sslSocketFactory = sslUtils.getMqttSocketFactory();
			} catch (IOException e) {
				log.error(e.getCause().toString());
			}

			mqttConnectOptions.setSocketFactory(sslSocketFactory);
		} else
			log.debug("SSL FALSE for MqttSourceConnectorTask: '{}, and mqtt client: '{}'.", connectorName,
					mqttClientId);

		/*
		 * MQTT CONNECT
		 */
		try {
			mqttClient = new MqttClient(connectorConfiguration.getString(MqttSourceConnectorConfig.MQTT_SERVER),
					mqttClientId, new MemoryPersistence());
			mqttClient.setCallback(callback);
			mqttClient.connect(mqttConnectOptions);

			log.debug("SUCCESSFULL MQTT CONNECTION for AsamMqttSourceConnectorTask: '{}, and mqtt client: '{}'.",
					connectorName, mqttClientId);

			if (mqttClient.isConnected())
				mqttClient.subscribe(mqttTopic, connectorConfiguration.getInt(MqttSourceConnectorConfig.MQTT_QOS));
		} catch (MqttException e) {
			log.error("FAILED MQTT CONNECTION for AsamMqttSourceConnectorTask: '{}, and mqtt client: '{}'.",
					connectorName, mqttClientId);
			log.error(e.getMessage());
		}
	}

	public void disconnect() {
		try {
			mqttClient.disconnect();
		} catch (MqttException e) {
			log.error(e.getCause().toString());
		}
	}

	@Override
	public void onSuccess(IMqttToken asyncActionToken) {
		log.info("MQTT client Connected!");
	}

	@Override
	public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
		connect();
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

	public String getMqttTopic() {
		return mqttTopic;
	}

	public String getKafkaTopic() {
		return kafkaTopic;
	}

}
