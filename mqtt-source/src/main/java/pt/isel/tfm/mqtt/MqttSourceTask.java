package pt.isel.tfm.mqtt;

import static pt.isel.tfm.mqtt.MqttSourceConnectorConfig.GENERAL_VALUE_CONVERTER;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import pt.isel.tfm.model.Location;
import pt.isel.tfm.utils.VersionUtil;

public class MqttSourceTask extends SourceTask implements MqttCallback {
	static final Logger log = LoggerFactory.getLogger(MqttSourceTask.class);

	private MqttConsumerClient mqttClient;
	private MqttSourceConnectorConfig config;

	BlockingQueue<SourceRecord> mqttRecordQueue;

	@Override
	public String version() {
		return VersionUtil.getVersion();
	}

	@Override
	public void start(Map<String, String> map) {
		// TODO: Do things here that are required to start your task. This could be open
		// a connection to MQTT-Broker.
		config = new MqttSourceConnectorConfig(map);
		mqttRecordQueue = new LinkedBlockingQueue<SourceRecord>();
		this.mqttClient = new MqttConsumerClient(config, this);
	}

	// Method which gets events from the input system and returns a
	// List<SourceRecord>
	@Override
	public List<SourceRecord> poll() throws InterruptedException {
		List<SourceRecord> records = new ArrayList<>();
		records.add(mqttRecordQueue.take());
		return records;
	}

	@Override
	public void stop() {
		this.mqttClient.disconnect();
	}

	@Override
	public void connectionLost(Throwable cause) {
		log.error("Connection for connector lost to topic:");
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		log.debug("Mqtt message arrived to connector: on topic: '{}'.", topic);
		try {
			log.debug("Mqtt message payload in byte array: '{}'", message.getPayload());
			mqttRecordQueue.put(generateSourceRecord(message));

		} catch (Exception e) {
			log.error("ERROR: Not able to create source record from mqtt message '{}' arrived on topic '{}'.",
					message.toString(), topic);
			log.error(e.getMessage());
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
	}

	private SourceRecord generateSourceRecord(MqttMessage message) throws Exception {
		return new SourceRecord(null, null, mqttClient.getKafkaTopic(), null, Location.VALUE_SCHEMA,
				buildRecordValue(message.getPayload()));
	}

	private Object buildRecordValue(byte[] payload) throws Exception {
		String msg = new String(payload);

		if (config.getString(GENERAL_VALUE_CONVERTER).equalsIgnoreCase("json")) {
			return Location.jsonFormat(new Gson().fromJson(msg, Location.class));
		} else if (config.getString(GENERAL_VALUE_CONVERTER).equalsIgnoreCase("avro")) {
			throw new UnsupportedOperationException("AVRO Not supported yet.");
		} else {
			throw new UnsupportedOperationException(config.getString(GENERAL_VALUE_CONVERTER) + " not supported.");
		}
	}
}