package pt.isel.tfm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.isel.tfm.mqtt.MqttSourceConnectorConfig;
import pt.isel.tfm.mqtt.MqttSourceTask;
import pt.isel.tfm.utils.VersionUtil;

public class MqttSourceConnector extends SourceConnector {
	private static Logger log = LoggerFactory.getLogger(MqttSourceConnector.class);
	private MqttSourceConnectorConfig config;

	@Override
	public String version() {
		return VersionUtil.getVersion();
	}

	@Override
	public void start(Map<String, String> map) {
		log.info("Starting up Mqtt connector");
		try {
			config = new MqttSourceConnectorConfig(map);
		} catch (ConfigException e) {
			throw new ConnectException("Couldn't start MqttSourceConnector due to configuration error", e);
		}
	}

	@Override
	public Class<? extends Task> taskClass() {
		// TODO: Return your task implementation.
		return MqttSourceTask.class;
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
		log.info("STOPPING MQTT source connector");
	}

	@Override
	public ConfigDef config() {
		return MqttSourceConnectorConfig.configuration;
	}
}
