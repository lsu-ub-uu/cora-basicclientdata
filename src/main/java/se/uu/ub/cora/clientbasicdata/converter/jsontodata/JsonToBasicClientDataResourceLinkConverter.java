/*
 * Copyright 2019, 2023, 2025 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.clientbasicdata.converter.jsontodata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataResourceLink;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientDataResourceLink;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverter;
import se.uu.ub.cora.json.parser.JsonArray;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonValue;

public class JsonToBasicClientDataResourceLinkConverter implements JsonToClientDataConverter {

	private static final int MAX_NUMBER_OF_FIELDS = 5;
	private static final int MIN_NUMBER_OF_FIELDS = 4;
	private static final String CHILDREN = "children";
	private static final String LINKED_RECORD_TYPE = "linkedRecordType";
	private static final String LINKED_RECORD_ID = "linkedRecordId";
	private static final String VALUE = "value";
	private static final String NAME = "name";
	private static final String REPEAT_ID = "repeatId";
	private static final String MIME_TYPE = "mimeType";
	private static final int NUMBER_OF_KEYS_ONE_OPTIONAL_KEY = 3;
	private static final int MAX_NUMBER_OF_JSON_KEYS = 4;
	private static final String PARSING_ERROR_MSG = "Error parsing jsonObject: ResourceLink must "
			+ "contain name,children[linkedRecordType,linkedRecordId,mimeType] and repeatId.";
	private JsonObject resourceLinkAsJson;
	private JsonToBasicClientDataActionLinkConverterFactory actionLinkConverterFactory;

	public static JsonToBasicClientDataResourceLinkConverter usingActionLinkConverterFactoryforJsonObject(
			JsonToBasicClientDataActionLinkConverterFactory actionLinkConverterFactory,
			JsonObject jsonObject) {
		return new JsonToBasicClientDataResourceLinkConverter(actionLinkConverterFactory,
				jsonObject);
	}

	private JsonToBasicClientDataResourceLinkConverter(
			JsonToBasicClientDataActionLinkConverterFactory actionLinkConverterFactory,
			JsonObject resourceLinkAsJson) {
		this.actionLinkConverterFactory = actionLinkConverterFactory;
		this.resourceLinkAsJson = resourceLinkAsJson;
	}

	@Override
	public ClientConvertible toInstance() {
		Map<String, String> fields = validateJsonAndCollectFields();
		ClientDataResourceLink resourceLink = createResourceLink(fields);
		possiblyConvertAndSetActionLink(resourceLink);
		return resourceLink;
	}

	private Map<String, String> validateJsonAndCollectFields() {
		validateMaxNumberOfJsonObjects();
		Map<String, String> fields = tryCollectResourceLinksFields();
		validateFields(fields);
		return fields;
	}

	private void validateMaxNumberOfJsonObjects() {
		if (validateJsonKeys()) {
			throw new JsonParseException(PARSING_ERROR_MSG);
		}
	}

	private boolean validateJsonKeys() {
		return threeKeysActionLinksOrRepeatIdIsMissing()
				|| maxNumberOfKeysActionLinksOrRepeatIdIsMissing() || moreThenMaxNumberOfKeys();
	}

	private boolean threeKeysActionLinksOrRepeatIdIsMissing() {
		return actionLinksIsMissing() && !repeatIdExists()
				&& resourceLinkAsJson.keySet().size() == NUMBER_OF_KEYS_ONE_OPTIONAL_KEY;
	}

	private boolean maxNumberOfKeysActionLinksOrRepeatIdIsMissing() {
		return (actionLinksIsMissing() || !repeatIdExists())
				&& resourceLinkAsJson.keySet().size() == MAX_NUMBER_OF_JSON_KEYS;
	}

	private boolean moreThenMaxNumberOfKeys() {
		return resourceLinkAsJson.keySet().size() > MAX_NUMBER_OF_JSON_KEYS;
	}

	private boolean actionLinksIsMissing() {
		return !actionLinkExists();
	}

	private boolean actionLinkExists() {
		return resourceLinkAsJson.containsKey("actionLinks");
	}

	private void possiblyConvertAndSetActionLink(ClientDataResourceLink resourceLink) {
		if (actionLinkExists()) {
			convertAndSetActionLink(resourceLink);
		}
	}

	private void convertAndSetActionLink(ClientDataResourceLink resourceLink) {
		JsonObject actionLinks = resourceLinkAsJson.getValueAsJsonObject("actionLinks");
		for (Map.Entry<String, JsonValue> actionLinkEntry : actionLinks.entrySet()) {
			convertAndAddActionLink(resourceLink, actionLinkEntry);
		}
	}

	private void convertAndAddActionLink(ClientDataResourceLink resourceLink,
			Map.Entry<String, JsonValue> actionLinkEntry) {
		JsonToBasicClientDataActionLinkConverter actionLinkConverter = actionLinkConverterFactory
				.factor((JsonObject) actionLinkEntry.getValue());
		ClientActionLink actionLink = actionLinkConverter.toInstance();
		resourceLink.addActionLink(actionLink);
	}

	private Map<String, String> tryCollectResourceLinksFields() {
		try {
			return collectResourceLinksFields();
		} catch (JsonParseException e) {
			throw new JsonParseException(PARSING_ERROR_MSG);
		}
	}

	private Map<String, String> collectResourceLinksFields() {
		Map<String, String> fields = new HashMap<>();
		fields.put(NAME, getValueAsStringFromJsonObject(resourceLinkAsJson, NAME));
		possiblyPutRepeatId(fields);
		fields.putAll(readChildren());
		return fields;
	}

	private void possiblyPutRepeatId(Map<String, String> fields) {
		if (repeatIdExists()) {
			fields.put(REPEAT_ID, getValueAsStringFromJsonObject(resourceLinkAsJson, REPEAT_ID));
		}
	}

	private Map<String, String> readChildren() {
		Map<String, String> childrenFields = new HashMap<>();
		JsonArray children = resourceLinkAsJson.getValueAsJsonArray(CHILDREN);
		for (JsonValue jsonValue : children) {
			JsonObject childrenObject = (JsonObject) jsonValue;
			String name = getValueAsStringFromJsonObject(childrenObject, NAME);
			String value = getValueAsStringFromJsonObject(childrenObject, VALUE);
			childrenFields.put(name, value);
		}
		return childrenFields;
	}

	private BasicClientDataResourceLink createResourceLink(Map<String, String> fields) {
		var resourceLink = BasicClientDataResourceLink.withNameInDataAndTypeAndIdAndMimeType(
				fields.get(NAME), fields.get(LINKED_RECORD_TYPE), fields.get(LINKED_RECORD_ID),
				fields.get(MIME_TYPE));
		possiblyAddRepeatIdToResourceLink(fields, resourceLink);
		return resourceLink;
	}

	private void possiblyAddRepeatIdToResourceLink(Map<String, String> fields,
			BasicClientDataResourceLink resourceLink) {
		if (fields.containsKey(REPEAT_ID)) {
			resourceLink.setRepeatId(fields.get(REPEAT_ID));
		}
	}

	private void validateFields(Map<String, String> fields) {
		if (minOrMaxNumberOfFieldsCollectedNotAsExpected(fields)) {
			throw new JsonParseException(PARSING_ERROR_MSG);
		}
		validateAllExpectedFieldsExists(fields);
	}

	private void validateAllExpectedFieldsExists(Map<String, String> fields) {
		List<String> expectedFields = generateExpectedFields(fields);
		for (String key : expectedFields) {
			if (!fields.containsKey(key)) {
				throw new JsonParseException(PARSING_ERROR_MSG);
			}
		}
	}

	private List<String> generateExpectedFields(Map<String, String> fields) {
		List<String> expectedFields = new ArrayList<>(
				List.of(NAME, LINKED_RECORD_TYPE, LINKED_RECORD_ID, MIME_TYPE));
		if (allFieldsIncludingRepeatId(fields)) {
			expectedFields.add(REPEAT_ID);
		}
		return expectedFields;
	}

	private boolean allFieldsIncludingRepeatId(Map<String, String> fields) {
		return fields.size() == MAX_NUMBER_OF_FIELDS;
	}

	private boolean minOrMaxNumberOfFieldsCollectedNotAsExpected(Map<String, String> fields) {
		return fields.size() < MIN_NUMBER_OF_FIELDS || fields.size() > MAX_NUMBER_OF_FIELDS;
	}

	private boolean repeatIdExists() {
		return resourceLinkAsJson.containsKey(REPEAT_ID);
	}

	private String getValueAsStringFromJsonObject(JsonObject jsonObject, String key) {
		return jsonObject.getValueAsJsonString(key).getStringValue();
	}

	public JsonToBasicClientDataActionLinkConverterFactory onlyForTestGetActionLinkConverterFactory() {
		return actionLinkConverterFactory;
	}

	public JsonObject onlyForTestGetJsonObject() {
		return resourceLinkAsJson;
	}
}