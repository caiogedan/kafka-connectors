package pt.isel.tfm.mqtt;

import static pt.isel.tfm.mqtt.MqttSourceConnectorConfig.MQTT_CLEAN;
import static pt.isel.tfm.mqtt.MqttSourceConnectorConfig.MQTT_CLIENTID;
import static pt.isel.tfm.mqtt.MqttSourceConnectorConfig.MQTT_COMM_TIMEOUT;
import static pt.isel.tfm.mqtt.MqttSourceConnectorConfig.MQTT_CONNECTOR_KAFKA_NAME;
import static pt.isel.tfm.mqtt.MqttSourceConnectorConfig.MQTT_KAFKA_TOPIC;
import static pt.isel.tfm.mqtt.MqttSourceConnectorConfig.MQTT_KEEP_ALIVE;
import static pt.isel.tfm.mqtt.MqttSourceConnectorConfig.MQTT_QOS;
import static pt.isel.tfm.mqtt.MqttSourceConnectorConfig.MQTT_SERVER;
import static pt.isel.tfm.mqtt.MqttSourceConnectorConfig.MQTT_SSL;
import static pt.isel.tfm.mqtt.MqttSourceConnectorConfig.MQTT_SSL_CA;
import static pt.isel.tfm.mqtt.MqttSourceConnectorConfig.MQTT_SSL_CRT;
import static pt.isel.tfm.mqtt.MqttSourceConnectorConfig.MQTT_SSL_KEY;
import static pt.isel.tfm.mqtt.MqttSourceConnectorConfig.MQTT_TOPIC;

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

	public MqttConsumerClient(MqttSourceConnectorConfig config, MqttCallback callback) {
		this.connectorConfiguration = config;

		connectorName = connectorConfiguration.getString(MQTT_CONNECTOR_KAFKA_NAME);
		kafkaTopic = connectorConfiguration.getString(MQTT_KAFKA_TOPIC);
		mqttClientId = connectorConfiguration.getString(MQTT_CLIENTID);
		mqttTopic = connectorConfiguration.getString(MQTT_TOPIC);
		log.info("Starting MqttConsumerClient with connector name: '{}'", connectorName);

		connect(callback);
	}

	public void connect(MqttCallback callback) {
		MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
		mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
		mqttConnectOptions.setServerURIs(new String[] { connectorConfiguration.getString(MQTT_SERVER) });
		mqttConnectOptions.setConnectionTimeout(connectorConfiguration.getInt(MQTT_COMM_TIMEOUT));
		mqttConnectOptions.setKeepAliveInterval(connectorConfiguration.getInt(MQTT_KEEP_ALIVE));
		mqttConnectOptions.setCleanSession(connectorConfiguration.getBoolean(MQTT_CLEAN));

		if (connectorConfiguration.getBoolean(MQTT_SSL)) {
			log.info("SSL TRUE for MqttSourceConnectorTask: '{}, and mqtt client: '{}'.", connectorName, mqttClientId);
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
			log.info("SSL FALSE for MqttSourceConnectorTask: '{}, and mqtt client: '{}'.", connectorName, mqttClientId);

		try {
			mqttClient = new MqttClient(connectorConfiguration.getString(MQTT_SERVER), mqttClientId,
					new MemoryPersistence());
			mqttClient.setCallback(callback);
			mqttClient.connect(mqttConnectOptions);
			log.info("SUCCESSFULL MQTT CONNECTION for AsamMqttSourceConnectorTask: '{}, and mqtt client: '{}'.",
					connectorName, mqttClientId);

			if (mqttClient.isConnected())
				mqttClient.subscribe(mqttTopic, connectorConfiguration.getInt(MQTT_QOS));
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onSuccess(IMqttToken asyncActionToken) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
		// TODO Auto-generated method stub

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
