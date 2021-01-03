package pt.isel.tfm.mqtt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.kafka.connect.data.Schema;
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
	// private RestService registry;
	private Schema schema;

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
		mqttClient = new MqttConsumerClient(config, this);
		
		/*
		 * SCHEMA REGISTRY (TESTE)
		 */
		// registry = new
		// RestService(config.getString(MqttSourceConnectorConfig.VALUE_SCHEMA_REGISTRY_URL));
//		try {
//			// schema =
//			// registry.getLatestVersion(config.getString(mqttClient.getKafkaTopic()).concat("-value")).getSchema();
//
//			//ProtobufSchema s = new ProtobufSchema(registry.getLatestVersion("locations-value").getSchema());
//
//		} catch (IOException | RestClientException e) {
//			log.error("Schema not found!");
//		}
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
		log.error("Connection for connector lost :: {}.", cause);
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
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		throw new UnsupportedOperationException("Not supported, delivery mode.");
	}

	private SourceRecord generateSourceRecord(MqttMessage message) throws Exception {
		return new SourceRecord(null, null, mqttClient.getKafkaTopic(), null, Location.VALUE_SCHEMA,
				buildRecordValue(message.getPayload()));
	}

	private Object buildRecordValue(byte[] payload) throws Exception {
		String msg = new String(payload);

		if (config.getString(MqttSourceConnectorConfig.GENERAL_VALUE_CONVERTER).equalsIgnoreCase("json")) {
			return Location.jsonFormat(new Gson().fromJson(msg, Location.class));
		} else if (config.getString(MqttSourceConnectorConfig.GENERAL_VALUE_CONVERTER).equalsIgnoreCase("avro")) {
			throw new UnsupportedOperationException("AVRO Not supported yet.");
		} else if (config.getString(MqttSourceConnectorConfig.GENERAL_VALUE_CONVERTER).equalsIgnoreCase("proto")) {
			throw new UnsupportedOperationException("PROTOBUF Not supported yet.");
			// log.info("PAYLOAD::: " + LocationMessage.parseFrom(payload).toString());
			// return LocationMessage.parseFrom(payload);
		} else {
			throw new UnsupportedOperationException(
					config.getString(MqttSourceConnectorConfig.GENERAL_VALUE_CONVERTER).concat(" not supported."));
		}
	}
}