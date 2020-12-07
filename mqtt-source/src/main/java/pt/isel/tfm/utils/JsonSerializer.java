package pt.isel.tfm.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import pt.isel.tfm.model.Location;

public class JsonSerializer {

	static final Logger log = LoggerFactory.getLogger(JsonSerializer.class);

	public Object serializer(byte[] payload) {

		String msg = new String(payload);
		Gson g = new Gson();

		return g.fromJson(msg, Location.class);
	}
}
