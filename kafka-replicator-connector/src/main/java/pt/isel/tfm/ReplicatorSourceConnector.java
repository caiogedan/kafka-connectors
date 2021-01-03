package pt.isel.tfm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicatorSourceConnector extends SourceConnector {
	private static Logger log = LoggerFactory.getLogger(ReplicatorSourceConnector.class);
	private ReplicatorConnectorConfig config;

	@Override
	public String version() {
		return VersionUtil.getVersion();
	}

	@Override
	public void start(Map<String, String> map) {
		log.info("Starting up Replicator Source Connector");
		try {
			config = new ReplicatorConnectorConfig(map);

			/*
			 * REMOVER PROPRIEDADES AFETADAS POR DEFAULT E VOLTAR ÀS ESCREVER
			 */

			ReplicatorConnectorConfig.configuration.configKeys().keySet()
					.remove(ReplicatorConnectorConfig.CONNECTOR_DESTINATION_DEFAULT_URI);
			ReplicatorConnectorConfig.configuration.configKeys().keySet()
					.remove(ReplicatorConnectorConfig.CONNECTOR_REPLICATION_DEFAULT_FACTOR);

			ReplicatorConnectorConfig.configuration
					.define(ReplicatorConnectorConfig.CONNECTOR_DESTINATION_DEFAULT_URI, Type.STRING,
							config.getString(ReplicatorConnectorConfig.CONNECTOR_DESTINTION_URI), Importance.HIGH, "")
					.define(ReplicatorConnectorConfig.CONNECTOR_REPLICATION_DEFAULT_FACTOR, Type.INT,
							config.getInt(ReplicatorConnectorConfig.CONNECTOR_REPLICATION_FACTOR), Importance.MEDIUM,
							"");

		} catch (ConfigException e) {
			throw new ConnectException("Couldn't start ReplicatorSourceConnector due to configuration error", e);
		}
	}

	@Override
	public Class<? extends Task> taskClass() {
		// TODO: Return your task implementation.
		return ReplicatorSourceTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		if (maxTasks != 1) {
			log.info("Ignoring maxTasks as there can only be one.");
		}
		List<Map<String, String>> configs = new ArrayList<>(maxTasks);
		configs.add(config.originalsStrings());
		return configs;
	}

	@Override
	public void stop() {
		log.info("STOPPING Replicator source connector");
	}

	@Override
	public ConfigDef config() {
		return ReplicatorConnectorConfig.configuration;
	}
}
