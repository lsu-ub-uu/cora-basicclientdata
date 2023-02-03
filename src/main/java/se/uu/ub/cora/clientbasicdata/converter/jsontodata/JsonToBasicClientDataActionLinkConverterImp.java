/*
 * Copyright 2015, 2018 Uppsala University Library
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

import se.uu.ub.cora.clientbasicdata.data.BasicClientActionLink;
import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverter;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterFactory;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;

public class JsonToBasicClientDataActionLinkConverterImp
		implements JsonToBasicClientDataActionLinkConverter {

	private JsonObject jsonObject;
	protected JsonToClientDataConverterFactory factory;
	private ClientActionLink actionLink;

	public JsonToBasicClientDataActionLinkConverterImp(JsonObject jsonObject,
			JsonToClientDataConverterFactory factory) {
		this.jsonObject = jsonObject;
		this.factory = factory;
	}

	public static JsonToBasicClientDataActionLinkConverterImp forJsonObjectUsingFactory(
			JsonObject jsonObject, JsonToClientDataConverterFactory factory) {
		return new JsonToBasicClientDataActionLinkConverterImp(jsonObject, factory);
	}

	@Override
	public ClientActionLink toInstance() {
		validateRequiredKeys();
		createActionLinkAndAddRequiredValues();

		possiblyAddExtraValues();
		return actionLink;
	}

	private void validateRequiredKeys() {
		validateKey("rel");
		validateKey("url");
		validateKey("requestMethod");
	}

	private void validateKey(String key) {
		if (!jsonObject.containsKey(key)) {
			throw new JsonParseException("Action link data must contain key: " + key);
		}
	}

	private void createActionLinkAndAddRequiredValues() {
		actionLink = createActionLinkUsingRelInJsonObject();
		setUrl();
		setRequestMethod();
	}

	private ClientActionLink createActionLinkUsingRelInJsonObject() {
		String rel = getStringValueFromJsonObjectUsingKey("rel");
		return BasicClientActionLink.withAction(ClientAction.valueOf(rel.toUpperCase()));
	}

	private String getStringValueFromJsonObjectUsingKey(String key) {
		return jsonObject.getValueAsJsonString(key).getStringValue();
	}

	private void setUrl() {
		String url = getStringValueFromJsonObjectUsingKey("url");
		actionLink.setURL(url);
	}

	private void setRequestMethod() {
		String requestMethod = getStringValueFromJsonObjectUsingKey("requestMethod");
		actionLink.setRequestMethod(requestMethod);
	}

	private void possiblyAddExtraValues() {
		possiblySetAccept();
		possiblySetContentType();
		possiblyConvertAndSetBody();
	}

	private void possiblySetContentType() {
		if (jsonObject.containsKey("contentType")) {
			String contentType = getStringValueFromJsonObjectUsingKey("contentType");
			actionLink.setContentType(contentType);
		}
	}

	private void possiblySetAccept() {
		if (jsonObject.containsKey("accept")) {
			String accept = getStringValueFromJsonObjectUsingKey("accept");
			actionLink.setAccept(accept);
		}
	}

	private void possiblyConvertAndSetBody() {
		if (jsonObject.containsKey("body")) {
			JsonToClientDataConverter converter = factory
					.factorUsingJsonObject((JsonObject) jsonObject.getValue("body"));
			ClientDataGroup bodyAsClientDataGroup = (ClientDataGroup) converter.toInstance();
			actionLink.setBody(bodyAsClientDataGroup);
		}
	}

	public JsonObject onlyForTestGetJsonObject() {
		return jsonObject;
	}

	public JsonToClientDataConverterFactory onlyForTestGetJsonToClientDataConverterFactory() {
		return factory;
	}

}
