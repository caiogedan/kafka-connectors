package pt.isel.tfm.mqtt;

import java.util.concurrent.BlockingQueue;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceCallback implements MqttCallback {
	static final Logger log = LoggerFactory.getLogger(SourceCallback.class);

	private BlockingQueue<SourceRecord> cbRecordQueue;
	private String dstTopic;

	public SourceCallback(String topic, BlockingQueue<SourceRecord> mqttRecordQueue) {
		cbRecordQueue = mqttRecordQueue;
		dstTopic = topic;
	}

	@Override
	public void connectionLost(Throwable cause) {
		log.error("Connection for connector lost :: {}.", cause);
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		log.debug("Mqtt message arrived to connector: on topic: '{}'.", topic);
		try {
			cbRecordQueue.put(generateSourceRecord(message));
		} catch (Exception e) {
			log.error("ERROR: Not able to create source record from mqtt message '{}' arrived on topic '{}'.",
					message.toString(), topic);
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		throw new UnsupportedOperationException("Delivery mode not supported.");
	}

	private SourceRecord generateSourceRecord(MqttMessage message) throws Exception {
		return new SourceRecord(null, null, dstTopic, null, Schema.OPTIONAL_STRING_SCHEMA,
				buildRecordValue(message.getPayload()));
	}

	private Object buildRecordValue(byte[] payload) throws Exception {
		String msg = new String(payload);
		return msg;
		// return Location.jsonFormat(new Gson().fromJson(msg, Location.class));
	}
}
