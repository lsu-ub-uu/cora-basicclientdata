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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataRecord;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
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
	private static final int NUM_OF_ALLOWED_KEYS = 4;
	private JsonObject json;
	private JsonObject jsonRecord;
	private JsonToClientDataConverterFactory factory;
	private ClientDataRecord clientDataRecord;
	private JsonToBasicClientDataActionLinkConverterFactory actionLinkConverterFactory;

	private JsonToBasicClientDataRecordConverter(JsonToClientDataConverterFactory factory,
			JsonToBasicClientDataActionLinkConverterFactory actionLinkConverterFactory,
			JsonObject json) {
		this.factory = factory;
		this.actionLinkConverterFactory = actionLinkConverterFactory;
		this.json = json;
	}

	public static JsonToBasicClientDataRecordConverter usingConverterFactoriesAndJsonObject(
			JsonToClientDataFactories factories, JsonObject json) {
		return new JsonToBasicClientDataRecordConverter(factories.dataConverterFactory(),
				factories.actionLinkConverterFactory(), json);
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
		jsonRecord = json.getValueAsJsonObject("record");
		validateOnlyCorrectKeysAtSecondLevel();

		ClientDataRecordGroup clientDataRecordGroup = convertDataRecordGroup();

		clientDataRecord = BasicClientDataRecord.withDataRecordGroup(clientDataRecordGroup);
		possiblyAddActionLinks();
		possiblyAddPermissions();
		possiblyOtherProtocols();
		return clientDataRecord;
	}

	private void validateOnlyRecordKeyAtTopLevel() {
		if (!json.containsKey("record")) {
			throw new JsonParseException("Record data must contain key: record");
		}
		if (json.keySet().size() != 1) {
			throw new JsonParseException("Record data must contain only key: record");
		}
	}

	private void validateOnlyCorrectKeysAtSecondLevel() {
		if (moreKeysThanAllowed() || maxNumOfKeysButPermissionsIsMissing()) {
			throw new JsonParseException(
					"Record data must contain keys: data and actionLinks and possibly "
							+ "permissions and otherProtocols");

		}
		if (!jsonRecord.containsKey("data")) {
			throw new JsonParseException("Record data must contain child with key: data");
		}
	}

	private boolean moreKeysThanAllowed() {
		return jsonRecord.keySet().size() > NUM_OF_ALLOWED_KEYS;
	}

	private boolean maxNumOfKeysButPermissionsIsMissing() {
		return jsonRecord.keySet().size() == NUM_OF_ALLOWED_KEYS
				&& !jsonRecord.containsKey(PERMISSIONS);
	}

	private ClientDataRecordGroup convertDataRecordGroup() {
		JsonObject jsonDataObject = jsonRecord.getValueAsJsonObject("data");
		JsonToClientDataConverter converter = factory.factorUsingJsonObject(jsonDataObject);
		return (ClientDataRecordGroup) converter.toInstance();
	}

	private void possiblyAddActionLinks() {
		if (jsonRecord.containsKey(ACTION_LINKS)) {
			JsonObject actionLinks = jsonRecord.getValueAsJsonObject(ACTION_LINKS);
			for (Map.Entry<String, JsonValue> actionLinkEntry : actionLinks.entrySet()) {
				convertAndAddActionLink(actionLinkEntry);
			}
		}
	}

	private void convertAndAddActionLink(Map.Entry<String, JsonValue> actionLinkEntry) {
		JsonToBasicClientDataActionLinkConverter actionLinkConverter = actionLinkConverterFactory
				.factor((JsonObject) actionLinkEntry.getValue());
		ClientActionLink actionLink = actionLinkConverter.toInstance();
		clientDataRecord.addActionLink(actionLink);
	}

	private void possiblyAddPermissions() {
		if (jsonRecord.containsKey(PERMISSIONS)) {
			JsonObject permissions = jsonRecord.getValueAsJsonObject(PERMISSIONS);
			possiblyAddReadPermissions(permissions);
			possiblyAddWritePermissions(permissions);
		}
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

	private void possiblyOtherProtocols() {
		if (jsonRecord.containsKey("otherProtocols")) {
			JsonObject otherProtocols = jsonRecord.getValueAsJsonObject("otherProtocols");
			for (Entry<String, JsonValue> protocolEntry : otherProtocols.entrySet()) {
				addProtocolToDataRecord(protocolEntry);
			}
		}

	}

	private void addProtocolToDataRecord(Entry<String, JsonValue> protocolEntry) {
		Map<String, String> properites = readProtocolProperties(protocolEntry);
		clientDataRecord.putProtocol(protocolEntry.getKey(), properites);
	}

	private Map<String, String> readProtocolProperties(Entry<String, JsonValue> protocolEntry) {
		var setOfPropertyEntries = getPropertiesSet(protocolEntry);
		Map<String, String> properites = new HashMap<>();
		for (Entry<String, JsonValue> property : setOfPropertyEntries) {
			properites.put(property.getKey(), readPropertyValue(property));
		}
		return properites;
	}

	private Set<Entry<String, JsonValue>> getPropertiesSet(Entry<String, JsonValue> protocolEntry) {
		return ((JsonObject) protocolEntry.getValue()).entrySet();
	}

	private String readPropertyValue(Entry<String, JsonValue> propertiesEntry) {
		return ((JsonString) propertiesEntry.getValue()).getStringValue();
	}

	public JsonToClientDataConverterFactory onlyForTestGetConverterFactory() {
		return factory;
	}

	public JsonToBasicClientDataActionLinkConverterFactory onlyForTestGetActionLinkConverterFactory() {
		return actionLinkConverterFactory;
	}

	public JsonObject onlyForTestGetJsonObject() {
		return json;
	}

}
