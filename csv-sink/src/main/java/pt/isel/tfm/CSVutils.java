package pt.isel.tfm;

import static pt.isel.tfm.CSVSinkConnectorConfig.CSV_FILE;
import static pt.isel.tfm.CSVSinkConnectorConfig.CSV_SEP;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class CSVutils {

	private static Logger log = LoggerFactory.getLogger(CSVutils.class);

	private String filename;
	private CSVPrinter csvPrinter = null;
	private PrintStream fileWriter = null;
	private CSVSinkConnectorConfig config;
	private boolean hasHeader = false;
	private String[] line = null;
	private char sep;

	public CSVutils(CSVSinkConnectorConfig config) {
		this.config = config;
		filename = config.getString(CSV_FILE);
		sep = config.getString(CSV_SEP).charAt(0);
	}

	/* Apenas para a ter, caso necessite de sua utilização no futuro. */
//	private Date parseDateTime(String dateString) {
//		if (dateString == null)
//			return null;
//		DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
//		if (dateString.contains("T"))
//			dateString = dateString.replace('T', ' ');
//		if (dateString.contains("Z"))
//			dateString = dateString.replace("Z", "+0000");
//		else
//			dateString = dateString.substring(0, dateString.lastIndexOf(':'))
//					+ dateString.substring(dateString.lastIndexOf(':') + 1);
//		try {
//			return fmt.parse(dateString);
//		} catch (ParseException e) {
//			log.error("Could not parse datetime: " + dateString);
//			return null;
//		}
//	}

	private String[] getCSVHeader(String jSon) {
		JsonObject jobj = new Gson().fromJson(jSon, JsonObject.class);
		Set<String> columns = jobj.keySet();

		String[] CSV_HEADER = null;
		/* Fields to CSV_HEADER */
		CSV_HEADER = new String[columns.size()];
		CSV_HEADER = columns.toArray(CSV_HEADER);

		return CSV_HEADER;
	}

	public void createFile() {
		if (filename == null)
			fileWriter = System.out;
		else {
			try {
				fileWriter = new PrintStream(Files.newOutputStream(Paths.get(filename), StandardOpenOption.CREATE,
						StandardOpenOption.APPEND), false, StandardCharsets.UTF_8.name());

			} catch (IOException e) {
				log.error("Couldn't find or create file '" + filename + "' for Kafka-CSV-Sink-Task");
			}
		}
	}

	public void appendCSV(String jSon) {
		JsonObject o = new Gson().fromJson(jSon, JsonObject.class);

		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			if (br.readLine() != null)
				hasHeader = true;

			if (!hasHeader) {
				final String[] CSV_HEADER = getCSVHeader(jSon);
				csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader(CSV_HEADER).withDelimiter(sep));
			}
		} catch (IOException e) {
			log.error("Couldn't find file '" + filename);
			e.printStackTrace();
		}

		Set<String> columns = o.keySet();
		line = new String[columns.size()];
		StringBuilder str = new StringBuilder();
		columns.forEach((c) -> {
			str.append(o.get(c).toString());
			str.append(sep);
		});
		str.deleteCharAt(str.length() - 1);
		if (str != null)
			this.fileWriter.println(str.toString());
	}

	public void flushCSV() {
		this.fileWriter.flush();
	}

	public void closeCSV() {
		this.fileWriter.close();
	}

}
