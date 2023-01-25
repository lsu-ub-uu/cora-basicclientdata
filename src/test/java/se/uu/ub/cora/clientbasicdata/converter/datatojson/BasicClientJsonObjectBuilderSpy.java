package se.uu.ub.cora.clientbasicdata.converter.datatojson;

import se.uu.ub.cora.json.builder.JsonArrayBuilder;
import se.uu.ub.cora.json.builder.JsonObjectBuilder;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class BasicClientJsonObjectBuilderSpy implements JsonObjectBuilder {

	MethodCallRecorder MCR = new MethodCallRecorder();

	@Override
	public void addKeyJsonArrayBuilder(String key, JsonArrayBuilder jsonArrayBuilder) {
		MCR.addCall("key", key, "jsonArrayBuilder", jsonArrayBuilder);
	}

	@Override
	public void addKeyJsonObjectBuilder(String key, JsonObjectBuilder jsonObjectBuilder) {
		MCR.addCall("key", key, "jsonObjectBuilder", jsonObjectBuilder);
	}

	@Override
	public void addKeyString(String key, String value) {
		MCR.addCall("key", key, "value", value);
	}

	@Override
	public String toJsonFormattedPrettyString() {
		String out = "fake json from JsonObjectBuilderSpy toJsonFormattedPrettyString";
		MCR.addCall();
		MCR.addReturned(out);
		return out;
	}

	@Override
	public String toJsonFormattedString() {
		String out = "fake json from JsonObjectBuilderSpy toJsonFormattedString";
		MCR.addCall();
		MCR.addReturned(out);
		return out;
	}

	@Override
	public JsonObject toJsonObject() {
		JsonObject out = null;
		MCR.addCall();
		MCR.addReturned(out);
		return out;
	}

}
