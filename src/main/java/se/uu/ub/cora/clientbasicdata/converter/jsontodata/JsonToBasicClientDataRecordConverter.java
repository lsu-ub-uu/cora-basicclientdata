/*
 * Copyright 2023 Uppsala University Library
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

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataRecord;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverter;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterFactory;
import se.uu.ub.cora.json.parser.JsonArray;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonString;
import se.uu.ub.cora.json.parser.JsonValue;

public class JsonToBasicClientDataRecordConverter implements JsonToClientDataConverter {

	private static final String PERMISSIONS = "permissions";
	private static final String ACTION_LINKS = "actionLinks";
	private static final int NUM_OF_ALLOWED_KEYS = 3;
	private JsonObject jsonObject;
	private JsonObject jsonObjectRecord;
	private JsonToClientDataConverterFactory factory;
	private ClientDataRecord clientDataRecord;
	private JsonToBasicClientDataActionLinkConverterFactory actionLinkConverterFactory;

	private JsonToBasicClientDataRecordConverter(JsonToClientDataConverterFactory factory,
			JsonToBasicClientDataActionLinkConverterFactory actionLinkConverterFactory,
			JsonObject jsonObject) {
		this.factory = factory;
		this.actionLinkConverterFactory = actionLinkConverterFactory;
		this.jsonObject = jsonObject;
	}

	public static JsonToBasicClientDataRecordConverter usingConverterFactoryAndJsonObject(
			JsonToClientDataConverterFactory factory,
			JsonToBasicClientDataActionLinkConverterFactory actionLinkConverterFactory,
			JsonObject jsonObject) {
		return new JsonToBasicClientDataRecordConverter(factory, actionLinkConverterFactory,
				jsonObject);
	}

	@Override
	public ClientConvertible toInstance() {
		try {
			return tryToInstanciate();
		} catch (Exception e) {
			throw new JsonParseException("Error parsing jsonRecord: " + e.getMessage(), e);
		}
	}

	private ClientDataRecord tryToInstanciate() {
		validateOnlyRecordKeyAtTopLevel();
		jsonObjectRecord = jsonObject.getValueAsJsonObject("record");
		validateOnlyCorrectKeysAtSecondLevel();

		ClientDataGroup clientDataGroup = convertDataGroup();

		clientDataRecord = BasicClientDataRecord.withDataGroup(clientDataGroup);
		possiblyAddActionLinks();
		possiblyAddPermissions();
		return clientDataRecord;
	}

	private void possiblyAddPermissions() {
		if (jsonObjectRecord.containsKey(PERMISSIONS)) {
			JsonObject permissions = jsonObjectRecord.getValueAsJsonObject(PERMISSIONS);
			possiblyAddReadPermissions(permissions);
			possiblyAddWritePermissions(permissions);
		}
	}

	private void possiblyAddReadPermissions(JsonObject permissions) {
		if (permissions.containsKey("read")) {
			JsonArray readPermissions = permissions.getValueAsJsonArray("read");
			addReadPermissions(readPermissions);
		}
	}

	private void addReadPermissions(JsonArray readPermissions) {
		for (JsonValue value : readPermissions) {
			String permission = getJsonValueAsString((JsonString) value);
			clientDataRecord.addReadPermission(permission);
		}
	}

	private String getJsonValueAsString(JsonString value) {
		return value.getStringValue();
	}

	private void possiblyAddWritePermissions(JsonObject permissions) {
		if (permissions.containsKey("write")) {
			JsonArray writePermissions = permissions.getValueAsJsonArray("write");
			addWritePermissions(writePermissions);
		}
	}

	private void addWritePermissions(JsonArray writePermissions) {
		for (JsonValue value : writePermissions) {
			String permission = getJsonValueAsString((JsonString) value);
			clientDataRecord.addWritePermission(permission);
		}
	}

	private void validateOnlyRecordKeyAtTopLevel() {
		if (!jsonObject.containsKey("record")) {
			throw new JsonParseException("Record data must contain key: record");
		}
		if (jsonObject.keySet().size() != 1) {
			throw new JsonParseException("Record data must contain only key: record");
		}
	}

	private void validateOnlyCorrectKeysAtSecondLevel() {
		if (moreKeysThanAllowed() || maxNumOfKeysButPermissionsIsMissing()) {
			throw new JsonParseException(
					"Record data must contain only keys: data and actionLinks and permissions");

		}
		if (!jsonObjectRecord.containsKey("data")) {
			throw new JsonParseException("Record data must contain child with key: data");
		}
		if (!jsonObjectRecord.containsKey(ACTION_LINKS)) {
			throw new JsonParseException("Record data must contain child with key: actionLinks");
		}
	}

	private boolean moreKeysThanAllowed() {
		return jsonObjectRecord.keySet().size() > NUM_OF_ALLOWED_KEYS;
	}

	private boolean maxNumOfKeysButPermissionsIsMissing() {
		return jsonObjectRecord.keySet().size() == NUM_OF_ALLOWED_KEYS
				&& !jsonObjectRecord.containsKey(PERMISSIONS);
	}

	private ClientDataGroup convertDataGroup() {
		JsonObject jsonDataObject = jsonObjectRecord.getValueAsJsonObject("data");
		JsonToClientDataConverter converter = factory.factorUsingJsonObject(jsonDataObject);
		return (ClientDataGroup) converter.toInstance();
	}

	private void possiblyAddActionLinks() {
		JsonObject actionLinks = jsonObjectRecord.getValueAsJsonObject(ACTION_LINKS);
		for (Map.Entry<String, JsonValue> actionLinkEntry : actionLinks.entrySet()) {
			convertAndAddActionLink(actionLinkEntry);
		}
	}

	private void convertAndAddActionLink(Map.Entry<String, JsonValue> actionLinkEntry) {
		JsonToBasicClientDataActionLinkConverter actionLinkConverter = actionLinkConverterFactory
				.factor((JsonObject) actionLinkEntry.getValue());
		ClientActionLink actionLink = actionLinkConverter.toInstance();
		clientDataRecord.addActionLink(actionLink);
	}

	public JsonToClientDataConverterFactory onlyForTestGetConverterFactory() {
		return factory;
	}
}
