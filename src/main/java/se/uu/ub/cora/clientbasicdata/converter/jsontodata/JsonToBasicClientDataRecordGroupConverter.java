package se.uu.ub.cora.clientbasicdata.converter.jsontodata;

import java.util.Map.Entry;

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataRecordGroup;
import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientDataChild;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverter;
import se.uu.ub.cora.json.parser.JsonArray;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonString;
import se.uu.ub.cora.json.parser.JsonValue;

public class JsonToBasicClientDataRecordGroupConverter implements JsonToClientDataConverter {
	private static final int ONE_OPTIONAL_KEY_IS_PRESENT = 3;
	private static final String CHILDREN = "children";
	private static final String ATTRIBUTES = "attributes";
	private static final int NUM_OF_ALLOWED_KEYS_AT_TOP_LEVEL = 3;
	protected BasicClientDataRecordGroup dataGroup;
	private JsonObject jsonObject;

	static JsonToClientDataConverter forJsonObject(JsonObject jsonObject) {
		return new JsonToBasicClientDataRecordGroupConverter(jsonObject);
	}

	private JsonToBasicClientDataRecordGroupConverter(JsonObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	@Override
	public ClientConvertible toInstance() {
		try {
			return tryToInstanciate();
		} catch (Exception e) {
			throw new JsonParseException("Error parsing jsonObject: " + e.getMessage(), e);
		}
	}

	private ClientConvertible tryToInstanciate() {
		validateOnlyCorrectKeysAtTopLevel();
		return createDataGroupInstance();
	}

	private String getNameInDataFromJsonObject() {
		return jsonObject.getValueAsJsonString("name").getStringValue();
	}

	protected void validateOnlyCorrectKeysAtTopLevel() {

		if (!jsonObject.containsKey("name")) {
			throw new JsonParseException("Group data must contain key: name");
		}

		if (!hasChildren()) {
			throw new JsonParseException("Group data must contain key: children");
		}

		validateNoOfKeysAtTopLevel();
	}

	private void validateNoOfKeysAtTopLevel() {
		if (threeKeysAtTopLevelButAttributeIsMissing()) {
			throw new JsonParseException(
					"Group data must contain name and children, and may contain attributes");
		}

		if (moreKeysAtTopLevelThanAllowed()) {
			throw new JsonParseException(
					"Group data can only contain keys: name, children and attributes");
		}
	}

	private boolean threeKeysAtTopLevelButAttributeIsMissing() {
		return jsonObject.keySet().size() == ONE_OPTIONAL_KEY_IS_PRESENT && !hasAttributes();
	}

	private boolean moreKeysAtTopLevelThanAllowed() {
		return jsonObject.keySet().size() > NUM_OF_ALLOWED_KEYS_AT_TOP_LEVEL;
	}

	private ClientConvertible createDataGroupInstance() {
		String nameInData = getNameInDataFromJsonObject();
		createInstanceOfDataElement(nameInData);
		if (hasAttributes()) {
			addAttributesToGroup();
		}
		addChildrenToGroup();
		return dataGroup;
	}

	protected void createInstanceOfDataElement(String nameInData) {
		dataGroup = BasicClientDataRecordGroup.withNameInData(nameInData);
	}

	private boolean hasAttributes() {
		return jsonObject.containsKey(ATTRIBUTES);
	}

	private void addAttributesToGroup() {
		JsonObject attributes = jsonObject.getValueAsJsonObject(ATTRIBUTES);
		for (Entry<String, JsonValue> attributeEntry : attributes.entrySet()) {
			addAttributeToGroup(attributeEntry);
		}
	}

	private void addAttributeToGroup(Entry<String, JsonValue> attributeEntry) {
		String value = ((JsonString) attributeEntry.getValue()).getStringValue();
		dataGroup.addAttributeByIdWithValue(attributeEntry.getKey(), value);
	}

	private boolean hasChildren() {
		return jsonObject.containsKey(CHILDREN);
	}

	private void addChildrenToGroup() {
		JsonArray children = jsonObject.getValueAsJsonArray(CHILDREN);
		for (JsonValue child : children) {
			addChildToGroup((JsonObject) child);
		}
	}

	private void addChildToGroup(JsonObject child) {
		JsonToBasicClientDataConverterFactoryImp jsonToDataConverterFactoryImp = new JsonToBasicClientDataConverterFactoryImp();
		JsonToClientDataConverter childJsonToDataConverter = jsonToDataConverterFactoryImp
				.factorUsingJsonObject(child);
		dataGroup.addChild((ClientDataChild) childJsonToDataConverter.toInstance());
	}

}
