package se.uu.ub.cora.clientbasicdata.converter.jsontodata;

import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverter;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterFactory;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;

public class JsonToBasicClientDataListConverter implements JsonToClientDataConverter {

	private JsonToClientDataConverterFactory dataConverterFactory;
	private JsonObject json;
	private JsonObject jsonDataList;

	public JsonToBasicClientDataListConverter(JsonToClientDataConverterFactory dataConverterFactory,
			JsonObject json) {
		this.dataConverterFactory = dataConverterFactory;
		this.json = json;
	}

	public static JsonToBasicClientDataListConverter usingDataConverterAndJsonObject(
			JsonToClientDataConverterFactory dataConverterFactory, JsonObject json) {
		return new JsonToBasicClientDataListConverter(dataConverterFactory, json);

	}

	@Override
	public ClientConvertible toInstance() {
		try {
			tryToValidateJson();
		} catch (Exception e) {
			throw new JsonParseException("Error parsing json: " + e.getMessage(), e);
		}
		return null;
		// throw new JsonParseException(
		// "Error parsing jsonRecord: Record data must contain key: dataList");
	}

	private void tryToValidateJson() {
		validateFirstLevel();
		tryToExtractDataListObject();
		validateSecondLevel();
		tryToExtractDataObjectFromDataList();

	}

	private void validateFirstLevel() {
		if (!json.containsKey("dataList")) {
			throw new JsonParseException("It must contains the key: dataList");
		}
	}

	private void tryToExtractDataObjectFromDataList() {
		try {
			jsonDataList.getValueAsJsonObject("data");
		} catch (JsonParseException e) {
			throw new JsonParseException("Datalist must have a child data that is an object.");
		}
	}

	private void tryToExtractDataListObject() {
		try {
			jsonDataList = json.getValueAsJsonObject("dataList");
		} catch (JsonParseException e) {
			throw new JsonParseException("It must exist a datalist object on the top.");
		}
	}

	private void validateSecondLevel() {
		// if (moreKeysThanAllowed() || maxNumOfKeysButPermissionsIsMissing()) {
		// throw new JsonParseException(
		// "Record data must contain only keys: data and actionLinks and permissions");
		//
		// }
		if (!jsonDataList.containsKey("data")) {
			throw new JsonParseException("It must contains child with key: data");
		}
	}

	public Object onlyForTestGetConverterFactory() {
		return dataConverterFactory;
	}

	public JsonObject onlyForTestGetJsonObject() {
		return json;
	}

}
