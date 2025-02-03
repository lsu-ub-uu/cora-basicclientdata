/*
 * Copyright 2025 Uppsala University Library
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
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataAuthentication;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverter;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterFactory;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonValue;

public class JsonToBasicClientDataAuthenticationConverter implements JsonToClientDataConverter {
	private static final String DATA = "data";
	private static final String ACTION_LINKS = "actionLinks";
	private static final int NUM_OF_ALLOWED_KEYS = 2;

	private JsonObject json;
	private JsonToClientDataConverterFactory factory;
	private JsonToBasicClientDataActionLinkConverterFactory actionLinkConverterFactory;
	private BasicClientDataAuthentication clientDataAuthentication;

	public static JsonToBasicClientDataAuthenticationConverter usingConverterFactoriesAndJsonObject(
			JsonToClientDataFactories convertFactories, JsonObject jsonObject) {
		return new JsonToBasicClientDataAuthenticationConverter(
				convertFactories.dataConverterFactory(),
				convertFactories.actionLinkConverterFactory(), jsonObject);
	}

	private JsonToBasicClientDataAuthenticationConverter(JsonToClientDataConverterFactory factory,
			JsonToBasicClientDataActionLinkConverterFactory actionLinkConverterFactory,
			JsonObject json) {
		this.factory = factory;
		this.actionLinkConverterFactory = actionLinkConverterFactory;
		this.json = json;
	}

	@Override
	public ClientConvertible toInstance() {
		try {
			return tryToInstanciate();
		} catch (Exception e) {
			throw new JsonParseException("Error parsing jsonRecord: " + e.getMessage(), e);
		}
	}

	private BasicClientDataAuthentication tryToInstanciate() {
		validateOnlyRecordKeyAtTopLevel();
		JsonObject jsonAuthentication = readAuthentication();
		validateAuthenticationOnlyContainsDataAndActionLinks(jsonAuthentication);
		convertToClientDataAuthentication(jsonAuthentication);
		return clientDataAuthentication;
	}

	private void validateOnlyRecordKeyAtTopLevel() {
		if (!json.containsKey("authentication")) {
			throw new JsonParseException("Authentication data must contain key: authentication");
		}
		if (json.keySet().size() != 1) {
			throw new JsonParseException(
					"Authentication data must contain only key: authentication");
		}
	}

	private JsonObject readAuthentication() {
		JsonObject jsonAuthentication = json.getValueAsJsonObject("authentication");
		return jsonAuthentication;
	}

	private void convertToClientDataAuthentication(JsonObject jsonAuthentication) {
		clientDataAuthentication = createClientDataAuthenticationFromJson(jsonAuthentication);
		possiblyConvertAndAddActionLinks(jsonAuthentication);
	}

	private BasicClientDataAuthentication createClientDataAuthenticationFromJson(
			JsonObject jsonAuthentication) {
		JsonObject jsonDataObject = jsonAuthentication.getValueAsJsonObject(DATA);
		JsonToClientDataConverter converter = factory.factorUsingJsonObject(jsonDataObject);
		ClientDataGroup dataGroup = (ClientDataGroup) converter.toInstance();
		return BasicClientDataAuthentication.withDataGroup(dataGroup);
	}

	private void validateAuthenticationOnlyContainsDataAndActionLinks(
			JsonObject jsonAuthentication) {
		if (differentNoOfKeysThanAllowed(jsonAuthentication)) {
			throw new JsonParseException(
					"Authentication data must contain keys: data and actionLinks");

		}
		if (!jsonAuthentication.containsKey(DATA)) {
			throw new JsonParseException("Authentication data must contain child with key: data");
		}
		if (!jsonAuthentication.containsKey(ACTION_LINKS)) {
			throw new JsonParseException(
					"Authentication data must contain child with key: actionLinks");
		}
	}

	private boolean differentNoOfKeysThanAllowed(JsonObject jsonAuthentication) {
		return jsonAuthentication.keySet().size() != NUM_OF_ALLOWED_KEYS;
	}

	private void possiblyConvertAndAddActionLinks(JsonObject jsonAuthentication) {
		List<ClientActionLink> actionLinks = convertActionLinks(jsonAuthentication);
		addActionLinks(actionLinks);
	}

	private void addActionLinks(List<ClientActionLink> actionLinks) {
		for (ClientActionLink actionLink : actionLinks) {
			clientDataAuthentication.addActionLink(actionLink);
		}
	}

	private List<ClientActionLink> convertActionLinks(JsonObject jsonAuthentication) {
		List<ClientActionLink> cactionLinks = new ArrayList<>();
		JsonObject actionLinks = jsonAuthentication.getValueAsJsonObject(ACTION_LINKS);
		for (Map.Entry<String, JsonValue> actionLinkEntry : actionLinks.entrySet()) {
			cactionLinks.add(convertActionLink(actionLinkEntry));
		}
		return cactionLinks;
	}

	private ClientActionLink convertActionLink(Map.Entry<String, JsonValue> actionLinkEntry) {
		JsonToBasicClientDataActionLinkConverter actionLinkConverter = actionLinkConverterFactory
				.factor((JsonObject) actionLinkEntry.getValue());
		return actionLinkConverter.toInstance();
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
