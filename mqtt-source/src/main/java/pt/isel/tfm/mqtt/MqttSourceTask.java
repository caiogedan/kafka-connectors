package pt.isel.tfm.mqtt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.isel.tfm.utils.VersionUtil;

public class MqttSourceTask extends SourceTask {
	static final Logger log = LoggerFactory.getLogger(MqttSourceTask.class);

	private MqttConsumerClient mqttClient;
	BlockingQueue<SourceRecord> mqttRecordQueue;
	// private RestService registry;
	// private Schema schema;

	@Override
	public String version() {
		return VersionUtil.getVersion();
	}

	@Override
	public void start(Map<String, String> map) {
		mqttRecordQueue = new LinkedBlockingQueue<SourceRecord>();
		MqttSourceConnectorConfig sourceConfig = new MqttSourceConnectorConfig(map);

		SourceCallback sourceCallback = new SourceCallback(
				sourceConfig.getString(MqttSourceConnectorConfig.DESTINATION_TOPIC_CONFIG), mqttRecordQueue);

		mqttClient = new MqttConsumerClient(sourceConfig, sourceCallback);

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

	/*
	 * Method which gets events from the input system and returns a
	 * List<SourceRecord>
	 */
	@Override
	public List<SourceRecord> poll() throws InterruptedException {
		List<SourceRecord> records = new ArrayList<>();
		records.add(mqttRecordQueue.take());
		return records;
	}

	@Override
	public synchronized void stop() {
		mqttClient.disconnect();
	}

}