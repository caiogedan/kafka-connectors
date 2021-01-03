package pt.isel.tfm;

import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicatorConnectorConfig extends AbstractConfig {

	private static final Logger log = LoggerFactory.getLogger(ReplicatorConnectorConfig.class);
	public static ConfigDef configuration = baseConfigDef();

	public static final String CONNECTOR_GROUP_NAME = "replicator.group.name";
	private static final String CONNECTOR_GROUP_NAME_DOC = "Replicator's ID.";

	public static final String CONNECTOR_ORIGIN_URI = "src.kafka.bootstrap.servers";
	private static final String CONNECTOR_ORIGIN_URI_DOC = "List of origin broker URI.";

	public static final String CONNECTOR_ORIGIN_TOPIC = "topic.whitelist";
	private static final String CONNECTOR_ORIGIN_TOPIC_DOC = "Topic to be replicated.";

	public static final String CONNECTOR_DESTINTION_URI = "dst.kafka.bootstrap.servers";
	private static final String CONNECTOR_DESTINATION_URI_DOC = "List of destination broker URI.";

	public static final String CONNECTOR_DESTINATION_TOPIC = "dst.kafka.topic";
	private static final String CONNECTOR_DESTINATION_TOPIC_DOC = "Topic to receive replicated messages.";

	public static final String CONNECTOR_REPLICATION_FACTOR = "topic.replication.factor";
	private static final String CONNECTOR_REPLICATION_FACTOR_DOC = "Kafka topic replication factor.";

	public static final String CONNECTOR_KEY_CONVERTER = "key.converter";
	private static final String CONNECTOR_KEY_CONVERTER_DOC = "Messge Key converter.";

	public static final String CONNECTOR_VALUE_CONVERTER = "value.converter";
	private static final String CONNECTOR_VALUE_CONVERTER_DOC = "Message Value converter.";

	public static final String CONNECTOR_DESTINATION_DEFAULT_URI = "confluent.topic.bootstrap.servers";
	public static final String CONNECTOR_REPLICATION_DEFAULT_FACTOR = "confluent.topic.replication.factor";

	public ReplicatorConnectorConfig(Map<String, String> parsedConfig) {
		super(configuration, parsedConfig);
	}

	public static ConfigDef baseConfigDef() {
		ConfigDef configDef = new ConfigDef();
		configDef.define(CONNECTOR_ORIGIN_TOPIC, Type.STRING, "locations", Importance.HIGH, CONNECTOR_ORIGIN_TOPIC_DOC)
				.define(CONNECTOR_ORIGIN_URI, Type.STRING, "localhost:9092", Importance.HIGH, CONNECTOR_ORIGIN_URI_DOC)
				.define(CONNECTOR_GROUP_NAME, Type.STRING, "locations", Importance.MEDIUM, CONNECTOR_GROUP_NAME_DOC)
				.define(CONNECTOR_DESTINATION_TOPIC, Type.STRING, "locations-replica", Importance.HIGH,
						CONNECTOR_DESTINATION_TOPIC_DOC)
				.define(CONNECTOR_DESTINTION_URI, Type.STRING, "teste:9092", Importance.HIGH,
						CONNECTOR_DESTINATION_URI_DOC)
				.define(CONNECTOR_REPLICATION_FACTOR, Type.INT, 1, Importance.MEDIUM, CONNECTOR_REPLICATION_FACTOR_DOC)
				.define(CONNECTOR_KEY_CONVERTER, Type.CLASS, "org.apache.kafka.connect.storage.StringConverter",
						Importance.MEDIUM, CONNECTOR_KEY_CONVERTER_DOC)
				.define(CONNECTOR_VALUE_CONVERTER, Type.CLASS, "org.apache.kafka.connect.storage.StringConverter",
						Importance.MEDIUM, CONNECTOR_VALUE_CONVERTER_DOC);

		log.debug("ConfigDef loaded: '{}'", configDef.toRst());
		return configDef;
	}
}
