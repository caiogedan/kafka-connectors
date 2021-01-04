package pt.isel.tfm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.isel.tfm.mqtt.MqttSinkConnectorConfig;
import pt.isel.tfm.mqtt.MqttSinkTask;
import pt.isel.tfm.utils.VersionUtil;

public class MqttSinkConnector extends SinkConnector {
	private static Logger log = LoggerFactory.getLogger(MqttSinkConnector.class);
	private MqttSinkConnectorConfig config;

	@Override
	public String version() {
		return VersionUtil.getVersion();
	}

	@Override
	public void start(Map<String, String> map) {
		log.info("Starting up Mqtt connector");
		try {
			config = new MqttSinkConnectorConfig(map);

		} catch (ConfigException e) {
			throw new ConnectException("Couldn't start MqttSinkConnector due to configuration error", e);
		}
	}

	@Override
	public Class<? extends Task> taskClass() {
		return MqttSinkTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		List<Map<String, String>> taskConfigs = new ArrayList<>(1);
		taskConfigs.add(config.originalsStrings());
		return taskConfigs;
	}

	@Override
	public void stop() {
		log.debug("STOPPING mqtt sink connector.");
	}

	@Override
	public ConfigDef config() {
		return MqttSinkConnectorConfig.CONFIG;
	}
}
