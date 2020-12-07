package pt.isel.tfm;

import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVSinkConnectorConfig extends AbstractConfig {

	private static final Logger log = LoggerFactory.getLogger(CSVSinkConnectorConfig.class);
	public static ConfigDef configuration = baseConfigDef();

	public static final String CSV_FILE = "csv.file";
	public static final String CSV_SEP = "csv.sep";

	public CSVSinkConnectorConfig(ConfigDef config, Map<String, String> parsedConfig) {
		super(config, parsedConfig);
	}

	public CSVSinkConnectorConfig(Map<String, String> parsedConfig) {
		this(configuration, parsedConfig);

	}

	public static ConfigDef baseConfigDef() {
		ConfigDef configDef = new ConfigDef();
		configDef
		.define(CSV_FILE, Type.STRING, "/tmp/diff.csv", Importance.HIGH, "csv output.")
		.define(CSV_SEP, Type.STRING, ";", Importance.MEDIUM, "csv separator.");

		log.debug("ConfigDef loaded: '{}'", configDef.toRst());
		return configDef;
	}
}
