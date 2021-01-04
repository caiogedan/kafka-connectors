package pt.isel.tfm.mqtt;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import pt.isel.tfm.utils.VersionUtil;

public class MqttSinkTask extends SinkTask {
	private static Logger log = LoggerFactory.getLogger(MqttSinkTask.class);

	private MqttProducerClient mqttClient;

	@Override
	public String version() {
		return VersionUtil.getVersion();
	}

	@Override
	public void start(Map<String, String> map) {
		MqttSinkConnectorConfig sinkConfig = new MqttSinkConnectorConfig(map);
		this.mqttClient = new MqttProducerClient(sinkConfig);
	}

	@Override
	public void put(Collection<SinkRecord> collection) {

		for (Iterator<SinkRecord> sinkRecordIterator = collection.iterator(); sinkRecordIterator.hasNext();) {
			SinkRecord sinkRecord = sinkRecordIterator.next();

			JsonObject jsonSinkRecord;
			MqttMessage mqttMessage = null;

			try {
				jsonSinkRecord = new Gson().fromJson(sinkRecord.value().toString(), JsonObject.class);

				mqttMessage = new MqttMessage(jsonSinkRecord.toString().getBytes("UTF-8"));
				mqttMessage.setQos(mqttClient.getQos());

				mqttClient.publish(mqttClient.getMqttTopic(), mqttMessage);

			} catch (UnsupportedEncodingException e) {
				log.error(e.getCause().toString());
			}
		}
	}

	@Override
	public void flush(Map<TopicPartition, OffsetAndMetadata> map) {

	}

	@Override
	public synchronized void stop() {
		mqttClient.disconnect();
	}

}
