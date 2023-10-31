/*
 * Copyright 2019 Uppsala University Library
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

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataResourceLink;
import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientDataResourceLink;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverter;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;

public class JsonToBasicClientDataResourceLinkConverter implements JsonToClientDataConverter {

	private static final int MAX_JSON_KEYS_WITHOUT_REPEAT_ID = 2;
	private static final int MAX_JSON_KEYS_WITH_REPEAT_ID = 3;
	private static final String PARSING_ERROR_MSG = "Error parsing jsonObject: ResourceLink must "
			+ "contain name, mimeType and repeatId.";
	// private static final int NUM_OF_RESOURCELINK_CHILDREN = 4;
	private JsonObject jsonObject;

	private JsonToBasicClientDataResourceLinkConverter(JsonObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	@Override
	public ClientConvertible toInstance() {
		validateJson();
		return createResourceLinkFromJson();
	}

	private void validateJson() {
		if (validateJsonKeys()) {
			throw new JsonParseException(PARSING_ERROR_MSG);
		}
	}

	private boolean validateJsonKeys() {
		return exceedeFieldsRepeatIdExists() || exceedeFieldsRepeatIdNotExists()
				|| nameAndMimeTypeNotExists();
	}

	private boolean nameAndMimeTypeNotExists() {
		return !jsonObject.containsKey("name") || !jsonObject.containsKey("mimeType");
	}

	private boolean exceedeFieldsRepeatIdNotExists() {
		return !repeatIdExists() && jsonObject.keySet().size() > MAX_JSON_KEYS_WITHOUT_REPEAT_ID;
	}

	private boolean exceedeFieldsRepeatIdExists() {
		return repeatIdExists() && jsonObject.keySet().size() > MAX_JSON_KEYS_WITH_REPEAT_ID;
	}

	private ClientDataResourceLink createResourceLinkFromJson() {
		ClientDataResourceLink resourceLink = createResourceLinkWithNameAndMimeType();
		possiblySetRepeatId(resourceLink);
		return resourceLink;
	}

	private ClientDataResourceLink createResourceLinkWithNameAndMimeType() {
		String nameInData = getValueAsStringFromJsonObject("name");
		String mimeType = getValueAsStringFromJsonObject("mimeType");
		return BasicClientDataResourceLink.withNameInDataAndMimeType(nameInData, mimeType);
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

	private boolean repeatIdExists() {
		return jsonObject.containsKey("repeatId");
	}

	// @Override
	// public ClientConvertible toInstance() {
	// BasicClientDataResourceLink resourceLink = (BasicClientDataResourceLink) super.toInstance();
	// throwErrorIfLinkChildrenAreIncorrect(resourceLink);
	// return resourceLink;
	// }

	// private void throwErrorIfLinkChildrenAreIncorrect(ClientDataGroup recordLink) {
	// if (incorrectNumberOfChildren(recordLink) || incorrectChildren(recordLink)) {
	// throw new JsonParseException(
	// "ResourceLinkData must and can only contain children with name "
	// + "streamId and filename and filesize and mimeType");
	// }
	// }
	//
	// private boolean incorrectNumberOfChildren(ClientDataGroup recordLink) {
	// return recordLink.getChildren().size() != NUM_OF_RESOURCELINK_CHILDREN;
	// }
	//
	// private boolean incorrectChildren(ClientDataGroup recordLink) {
	// return !recordLink.containsChildWithNameInData("streamId")
	// || !recordLink.containsChildWithNameInData("filename")
	// || !recordLink.containsChildWithNameInData("filesize")
	// || !recordLink.containsChildWithNameInData("mimeType");
	// }
	//
	public static JsonToBasicClientDataResourceLinkConverter forJsonObject(JsonObject jsonObject) {
		return new JsonToBasicClientDataResourceLinkConverter(jsonObject);
	}
	//
	// @Override
	// protected void createInstanceOfDataElement(String nameInData) {
	// dataGroup = BasicClientDataResourceLink.withNameInData(nameInData);
	// }

}
