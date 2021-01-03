package pt.isel.tfm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicatorSourceTask extends SourceTask {
	static final Logger log = LoggerFactory.getLogger(ReplicatorSourceTask.class);

	private ReplicatorConnectorConfig config;
	Consumer<String, String> consumer;

	@Override
	public String version() {
		return VersionUtil.getVersion();
	}

	@Override
	public void start(Map<String, String> map) {
		config = new ReplicatorConnectorConfig(map);

		/*
		 * CONFIGURE KAFKA CONSUMER
		 */

		log.debug("Replicator INITIALIZED ::");
		Properties properties = new Properties();

		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
				config.getString(ReplicatorConnectorConfig.CONNECTOR_ORIGIN_URI));
		// properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.getClass("key.converter"));
		// properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.getClass("value.converter"));
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(ConsumerConfig.GROUP_ID_CONFIG,
				config.getString(ReplicatorConnectorConfig.CONNECTOR_GROUP_NAME));
		// properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

		consumer = new KafkaConsumer<>(properties);
		consumer.subscribe(Collections.singleton(config.getString(ReplicatorConnectorConfig.CONNECTOR_ORIGIN_TOPIC)));
	}

	@Override
	public List<SourceRecord> poll() throws InterruptedException {
		List<SourceRecord> records = new ArrayList<>();

		ConsumerRecords<String, String> messages = consumer.poll(10);
		for (ConsumerRecord<String, String> message : messages) {
			try {
				records.add(generateSourceRecord(message.value().toString()));
			} catch (Exception e) {
				log.error(e.getCause().toString());
			}
		}
		consumer.commitAsync();
		return records;
	}

	@Override
	public void stop() {
		consumer.close();
	}

	private SourceRecord generateSourceRecord(String message) throws Exception {
		return new SourceRecord(null, null, config.getString(ReplicatorConnectorConfig.CONNECTOR_DESTINATION_TOPIC),
				null, null, message);
	}
}