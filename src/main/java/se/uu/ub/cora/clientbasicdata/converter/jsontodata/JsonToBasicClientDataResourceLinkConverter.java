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

import java.util.Map;

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataResourceLink;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientDataResourceLink;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverter;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonValue;

public class JsonToBasicClientDataResourceLinkConverter implements JsonToClientDataConverter {

	private static final int NUMBER_OF_KEYS_ONE_OPTIONAL_KEY = 3;
	private static final int MAX_NUMBER_OF_JSON_KEYS = 4;
	private static final String PARSING_ERROR_MSG = "Error parsing jsonObject: ResourceLink must "
			+ "contain name, mimeType and may contain actionLinks and/or repeatId.";
	private JsonObject jsonObject;
	private JsonToBasicClientDataActionLinkConverterFactory actionLinkConverterFactory;

	public static JsonToBasicClientDataResourceLinkConverter usingActionLinkConverterFactoryforJsonObject(
			JsonToBasicClientDataActionLinkConverterFactory actionLinkConverterFactory,
			JsonObject jsonObject) {
		return new JsonToBasicClientDataResourceLinkConverter(actionLinkConverterFactory,
				jsonObject);
	}

	private JsonToBasicClientDataResourceLinkConverter(
			JsonToBasicClientDataActionLinkConverterFactory actionLinkConverterFactory,
			JsonObject jsonObject) {
		this.actionLinkConverterFactory = actionLinkConverterFactory;
		this.jsonObject = jsonObject;
	}

	@Override
	public ClientConvertible toInstance() {
		validateJson();
		return createResourceLinkFromJson();
	}

	private void validateJson() {
		if (validateJsonKeysFail()) {
			throw new JsonParseException(PARSING_ERROR_MSG);
		}
	}

	private boolean validateJsonKeysFail() {
		return nameOrMimeTypeIsMissing() || threeKeysActionLinksOrRepeatIdIsMissing()
				|| maxNumberOfKeysActionLinksOrRepeatIdIsMissing() || moreThenMaxNumberOfKeys();
	}

	private boolean nameOrMimeTypeIsMissing() {
		return !jsonObject.containsKey("name") || !jsonObject.containsKey("mimeType");
	}

	private boolean threeKeysActionLinksOrRepeatIdIsMissing() {
		return actionLinksIsMissing() && !repeatIdExists()
				&& jsonObject.keySet().size() == NUMBER_OF_KEYS_ONE_OPTIONAL_KEY;
	}

	private boolean maxNumberOfKeysActionLinksOrRepeatIdIsMissing() {
		return (actionLinksIsMissing() || !repeatIdExists())
				&& jsonObject.keySet().size() == MAX_NUMBER_OF_JSON_KEYS;
	}

	private boolean moreThenMaxNumberOfKeys() {
		return jsonObject.keySet().size() > MAX_NUMBER_OF_JSON_KEYS;
	}

	private boolean actionLinksIsMissing() {
		return !actionLinkExists();
	}

	private boolean actionLinkExists() {
		return jsonObject.containsKey("actionLinks");
	}

	private boolean repeatIdExists() {
		return jsonObject.containsKey("repeatId");
	}

	private ClientDataResourceLink createResourceLinkFromJson() {
		ClientDataResourceLink resourceLink = createResourceLinkWithNameAndMimeType();
		possiblySetRepeatId(resourceLink);
		possiblyConvertAndSetActionLink(resourceLink);
		return resourceLink;
	}

	private void possiblyConvertAndSetActionLink(ClientDataResourceLink resourceLink) {
		if (actionLinkExists()) {
			convertAndSetActionLink(resourceLink);
		}
	}

	private void convertAndSetActionLink(ClientDataResourceLink resourceLink) {
		JsonObject actionLinks = jsonObject.getValueAsJsonObject("actionLinks");
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

	private ClientDataResourceLink createResourceLinkWithNameAndMimeType() {
		String nameInData = getValueAsStringFromJsonObject("name");
		String mimeType = getValueAsStringFromJsonObject("mimeType");
		return BasicClientDataResourceLink.withNameInDataAndTypeAndIdAndMimeType(nameInData,
				"someType", "someId", mimeType);
	}

	private String getValueAsStringFromJsonObject(String key) {
		return jsonObject.getValueAsJsonString(key).getStringValue();
	}

	private void possiblySetRepeatId(ClientDataResourceLink resourceLink) {
		if (repeatIdExists()) {
			String repeatId = getValueAsStringFromJsonObject("repeatId");
			resourceLink.setRepeatId(repeatId);
		}
	}

	public JsonToBasicClientDataActionLinkConverterFactory onlyForTestGetActionLinkConverterFactory() {
		return actionLinkConverterFactory;
	}

	public JsonObject onlyForTestGetJsonObject() {
		return jsonObject;
	}
}