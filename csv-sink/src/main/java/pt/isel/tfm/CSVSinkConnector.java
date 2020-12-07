package pt.isel.tfm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVSinkConnector extends SinkConnector {
	private static Logger log = LoggerFactory.getLogger(CSVSinkConnector.class);
	private CSVSinkConnectorConfig config;

	@Override
	public String version() {
		return VersionUtil.getVersion();
	}

	@Override
	public void start(Map<String, String> map) {
		config = new CSVSinkConnectorConfig(map);
		log.debug("STARTING mqtt sink connector");
	}

	@Override
	public Class<? extends Task> taskClass() {
		// TODO: Return your task implementation.
		return CSVSinkTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		// TODO: Define the individual task configurations that will be executed.

		/**
		 * This is used to schedule the number of tasks that will be running. This
		 * should not exceed maxTasks.
		 */

		// throw new UnsupportedOperationException("This has not been implemented.");
		List<Map<String, String>> taskConfigs = new ArrayList<>(1);
		taskConfigs.add(config.originalsStrings());
		return taskConfigs;
	}

	@Override
	public void stop() {
		log.debug("STOPPING CSV sink connector.");
	}

	@Override
	public ConfigDef config() {
		return CSVSinkConnectorConfig.configuration;
	}
}
