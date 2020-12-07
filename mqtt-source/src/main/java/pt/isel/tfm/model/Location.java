package pt.isel.tfm.model;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class Location {
	static final Logger log = LoggerFactory.getLogger(Location.class);

	private String profileId;
	private Double latitude;
	private Double longitude;
	
	public Location(String profileId, Double latitude, Double longitude) {
		this.profileId = profileId;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getProfileId() {
		return profileId;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public static String jsonFormat(Location loc) {
		return new Gson().toJson(loc);
	}
	
	// Schema Definition - (Schema Registry)
	// Locations Fields
	private static final String PROFILEID_FIELD = "profileid";
	private static final String LATITUDE_FIELD = "latitude";
	private static final String LONGITUDE_FIELD = "longitude";
	
	private static final String SCHEMA_VALUE_LOCATION = "Location";

	public static final Schema VALUE_SCHEMA = SchemaBuilder.struct()
			.version(1)
			.name(SCHEMA_VALUE_LOCATION)
			.field(PROFILEID_FIELD, Schema.STRING_SCHEMA)
			.field(LATITUDE_FIELD, Schema.FLOAT64_SCHEMA)
			.field(LONGITUDE_FIELD, Schema.FLOAT64_SCHEMA)
			.build();

	public Struct toStruct() {
		Struct valueStruct = new Struct(VALUE_SCHEMA)
				.put(PROFILEID_FIELD, getProfileId())
				.put(LATITUDE_FIELD, getLatitude())
				.put(LONGITUDE_FIELD, getLongitude());

		return valueStruct;
	}

}
