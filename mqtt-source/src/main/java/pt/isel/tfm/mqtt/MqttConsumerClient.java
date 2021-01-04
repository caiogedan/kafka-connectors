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

	private SSLSocketFactory sslSocketFactory;
	private MqttClient mqttClient;
	private MqttSourceConnectorConfig mqttProps;
	private MqttCallback callback;

	private String destinationTopic;
	private String mqttTopic;
	private String mqttClientId;
	private String mqttBrokerUri;
	private int qos;

	public MqttConsumerClient(MqttSourceConnectorConfig mqttConsumerProps, MqttCallback callback) {
		log.debug("STARTING MQTT-SOURCE-CONNECTOR.");

		mqttProps = mqttConsumerProps;
		this.callback = callback;

		mqttBrokerUri = mqttProps.getString(MqttSourceConnectorConfig.SOURCE_BROKER_SERVERS_CONFIG);
		mqttClientId = mqttProps.getString(MqttSourceConnectorConfig.SOURCE_CLIENT_ID_CONFIG);
		mqttTopic = mqttProps.getString(MqttSourceConnectorConfig.SOURCE_TOPIC_CONFIG);
		qos = mqttProps.getInt(MqttSourceConnectorConfig.SOURCE_QOS_CONFIG);
		destinationTopic = mqttProps.getString(MqttSourceConnectorConfig.DESTINATION_TOPIC_CONFIG);

		connect();
	}

	public void connect() {
		/*
		 * MQTT CONFIGURATIONS
		 */
		MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();

		mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
		mqttConnectOptions.setServerURIs(new String[] { mqttBrokerUri });

		mqttConnectOptions.setConnectionTimeout(mqttProps.getInt(MqttSourceConnectorConfig.SOURCE_CON_TIMEOUT_CONFIG));
		mqttConnectOptions.setKeepAliveInterval(mqttProps.getInt(MqttSourceConnectorConfig.SOURCE_KEEP_ALIVE_CONFIG));
		mqttConnectOptions.setCleanSession(mqttProps.getBoolean(MqttSourceConnectorConfig.SOURCE_CLEAN_CONFIG));

		/*
		 * SSL CONFIGURATION
		 */
		if (mqttProps.getBoolean(MqttSourceConnectorConfig.SOURCE_SSL_CONFIG)) {
			log.debug("SSL TRUE for MqttSourceConnector: MQTT Client: '{}'.", mqttClientId);

			String caCrtFilePath = mqttProps.getString(MqttSourceConnectorConfig.SOURCE_SSL_CA_CONFIG);
			String crtFilePath = mqttProps.getString(MqttSourceConnectorConfig.SOURCE_SSL_CRT_CONFIG);
			String keyFilePath = mqttProps.getString(MqttSourceConnectorConfig.SOURCE_SSL_KEY_CONFIG);

			try {
				SSLUtils sslUtils = new SSLUtils(caCrtFilePath, crtFilePath, keyFilePath);
				sslSocketFactory = sslUtils.getMqttSocketFactory();
			} catch (IOException e) {
				log.error(e.getCause().toString());
			}

			mqttConnectOptions.setSocketFactory(sslSocketFactory);
		} else
			log.debug("SSL FALSE for MqttSourceConnector: MQTT Client: '{}'.", mqttClientId);

		/*
		 * MQTT CONNECT
		 */
		try {
			mqttClient = new MqttClient(mqttBrokerUri, mqttClientId, new MemoryPersistence());
			mqttClient.setCallback(callback);
			mqttClient.connect(mqttConnectOptions);

			log.debug("SUCCESSFULL MQTT CONNECTION for MqttSourceConnector: MQTT Client: '{}'.", mqttClientId);

			if (mqttClient.isConnected())
				mqttClient.subscribe(mqttTopic, qos);
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

	@Override
	public void onSuccess(IMqttToken asyncActionToken) {
		log.info("MQTT client Connected!");
	}

	@Override
	public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
		try {
			mqttClient.reconnect();
		} catch (MqttException e) {
			log.error(e.getCause().toString());
		}
	}

	public String getDestinationTopic() {
		return destinationTopic;
	}

	public String getMqttTopic() {
		return mqttTopic;
	}

	public String getMqttClientId() {
		return mqttClientId;
	}

}
