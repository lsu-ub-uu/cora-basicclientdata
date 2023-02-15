package se.uu.ub.cora.clientbasicdata.converter.jsontodata;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataList;
import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientData;
import se.uu.ub.cora.clientdata.ClientDataList;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverter;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterFactory;
import se.uu.ub.cora.json.parser.JsonArray;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonValue;

public class JsonToBasicClientDataListConverter implements JsonToClientDataConverter {

	private static final String ERROR_DATALIST_MUST_EXISTS = "DataList must exist and must be an object.";
	private static final String ERROR_DATALIST_NOT_ARRAY = "Data in Datalist is not an Array.";
	private static final int ALLOWED_KEYS_FISRT_LEVEL = 1;
	private static final int ALLOWED_KEYS_SECOND_LEVEL = 5;
	private static final String ERROR_DATALIST_ONLY_ONE_KEY = "DataList must have only one key: datalist";
	private static final String ERROR_PARSING_GENERAL = "Error parsing json: ";
	private JsonToClientDataConverterFactory dataConverterFactory;
	private JsonObject json;
	private JsonObject jsonDataList;
	private static final List<String> DATA_LIST_KEYS = List.of("fromNo", "data", "totalNo",
			"containDataOfType", "toNo");
	private JsonArray recordsArray;
	private ClientDataList dataList;

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
		tryToValidateJsonAndSetDataList();
		return dataList;
	}

	private void tryToValidateJsonAndSetDataList() {
		try {
			validateJsonAndSetDataList();
		} catch (Exception e) {
			throw new JsonParseException(ERROR_PARSING_GENERAL + e.getMessage(), e);
		}
	}

	private void validateJsonAndSetDataList() {
		validateFirstLevel();
		tryToExtractDataListFromJson();
		validateSecondLevel();
		createAndSetDataList();
		tryToRecordsFromJson();
		addDataRecords();
	}

	private void validateFirstLevel() {
		if (json.keySet().size() != ALLOWED_KEYS_FISRT_LEVEL) {
			throw new JsonParseException(ERROR_DATALIST_ONLY_ONE_KEY);
		}
		if (!json.containsKey("dataList")) {
			throw new JsonParseException(ERROR_DATALIST_MUST_EXISTS);
		}
	}

	private void tryToExtractDataListFromJson() {
		try {
			jsonDataList = json.getValueAsJsonObject("dataList");
		} catch (JsonParseException e) {
			throw new JsonParseException(ERROR_DATALIST_MUST_EXISTS);
		}
	}

	private void validateSecondLevel() {
		validateNumberOfKeyAllowedInDataList();
		validateAllKeysInsideDataListExists();
	}

	private void validateNumberOfKeyAllowedInDataList() {
		if (jsonDataList.keySet().size() != ALLOWED_KEYS_SECOND_LEVEL) {
			throw new JsonParseException(
					"Datalist must have " + ALLOWED_KEYS_SECOND_LEVEL + " key childs.");
		}
	}

	private void validateAllKeysInsideDataListExists() {
		List<String> missingkeys = validateDataListKeysExists();
		throwExceptionIfMissingKeys(missingkeys);
	}

	private void throwExceptionIfMissingKeys(List<String> missingkeys) {
		if (!missingkeys.isEmpty()) {
			throw new JsonParseException(
					"It must contains child with key: " + String.join(", ", missingkeys));
		}
	}

	private List<String> validateDataListKeysExists() {
		List<String> missingkeys = new ArrayList<>();
		for (String key : DATA_LIST_KEYS) {
			addKeyIfMissing(missingkeys, key);
		}
		return missingkeys;
	}

	private void addKeyIfMissing(List<String> missingkeys, String key) {
		if (!jsonDataList.containsKey(key)) {
			missingkeys.add(key);
		}
	}

	private void createAndSetDataList() {
		createAndSetType();
		setFromNo();
		setTotalNo();
		setToNo();
	}

	private void addDataRecords() {
		for (JsonValue jsonValue : recordsArray) {
			JsonToClientDataConverter recordConverter = dataConverterFactory
					.factorUsingJsonObject((JsonObject) jsonValue);
			dataList.addData((ClientData) recordConverter.toInstance());
		}
	}

	private void createAndSetType() {
		String type = getStringValueFromJsonObjectUsingKey(jsonDataList, "containDataOfType");
		dataList = BasicClientDataList.withContainDataOfType(type);
	}

	private void setFromNo() {
		String fromNo = getStringValueFromJsonObjectUsingKey(jsonDataList, "fromNo");
		dataList.setFromNo(fromNo);
	}

	private void setTotalNo() {
		String totalNo = getStringValueFromJsonObjectUsingKey(jsonDataList, "totalNo");
		dataList.setTotalNo(totalNo);
	}

	private void setToNo() {
		String toNo = getStringValueFromJsonObjectUsingKey(jsonDataList, "toNo");
		dataList.setToNo(toNo);
	}

	private String getStringValueFromJsonObjectUsingKey(JsonObject jsonObject, String key) {
		return jsonObject.getValueAsJsonString(key).getStringValue();
	}

	private void tryToRecordsFromJson() {
		try {
			recordsArray = jsonDataList.getValueAsJsonArray("data");
		} catch (JsonParseException e) {
			throw new JsonParseException(ERROR_DATALIST_NOT_ARRAY);
		}
	}

	public Object onlyForTestGetConverterFactory() {
		return dataConverterFactory;
	}

	public JsonObject onlyForTestGetJsonObject() {
		return json;
	}

}
