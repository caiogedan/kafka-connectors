package pt.isel.tfm;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVSinkTask extends SinkTask {
	private static Logger log = LoggerFactory.getLogger(CSVSinkTask.class);
	private CSVSinkConnectorConfig config;
	private CSVutils csv;

	@Override
	public String version() {
		return VersionUtil.getVersion();
	}

	@Override
	public void start(Map<String, String> map) {
		this.config = new CSVSinkConnectorConfig(map);
		this.csv = new CSVutils(this.config);
		csv.createFile();
	}

	@Override
	public void put(Collection<SinkRecord> collection) {
		for (Iterator<SinkRecord> sinkRecordIterator = collection.iterator(); sinkRecordIterator.hasNext();) {
			SinkRecord sinkRecord = sinkRecordIterator.next();
			log.debug("Received record: '{}'", sinkRecord.value());
			try {
				if (sinkRecord.value().toString() != null)
					csv.appendCSV(sinkRecord.value().toString());

			} catch (Exception e) {
				log.error("Error on obtain SinkRecord.");
			}
		}
	}

	@Override
	public void flush(Map<TopicPartition, OffsetAndMetadata> map) {
		csv.flushCSV();
	}

	@Override
	public void stop() {
		csv.closeCSV();
	}

}
