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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.function.Supplier;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientDataAuthentication;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverter;
import se.uu.ub.cora.clientdata.spies.ClientActionLinkSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataGroupSpy;
import se.uu.ub.cora.clientdata.spies.JsonToClientDataConverterFactorySpy;
import se.uu.ub.cora.clientdata.spies.JsonToClientDataConverterSpy;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonValue;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;

public class JsonToBasicClientDataAuthenticationConverterTest {

	private JsonToClientDataConverterFactorySpy factory;
	private JsonToBasicClientDataAuthenticationConverter jsonToDataConverter;
	private JsonToBasicClientDataActionLinkConverterFactorySpy actionLinkConverterFactory;
	private OrgJsonParser jsonParser = new OrgJsonParser();
	private JsonToClientDataFactories convertFactories;

	@BeforeMethod
	private void beforeMethod() {
		factory = new JsonToClientDataConverterFactorySpy();
		setUpJsonToDataActionLinkConverterFactory();

		convertFactories = new JsonToClientDataFactories(factory, actionLinkConverterFactory);
	}

	private void setUpJsonToDataActionLinkConverterFactory() {
		actionLinkConverterFactory = new JsonToBasicClientDataActionLinkConverterFactorySpy();
		JsonToBasicClientDataActionLinkConverter deleteConverter = createJsonToClientActionLinkConverter(
				ClientAction.DELETE);
		JsonToBasicClientDataActionLinkConverter renewConverter = createJsonToClientActionLinkConverter(
				ClientAction.RENEW);

		actionLinkConverterFactory.MRV.setDefaultReturnValuesSupplier("factor",
				createSupplierForList(List.of(deleteConverter, renewConverter)));
	}

	private JsonToBasicClientDataActionLinkConverter createJsonToClientActionLinkConverter(
			ClientAction clientAction) {
		JsonToBasicClientDataActionLinkConverterSpy linkConverter = new JsonToBasicClientDataActionLinkConverterSpy();
		ClientActionLinkSpy link = new ClientActionLinkSpy();
		link.MRV.setDefaultReturnValuesSupplier("getAction", () -> clientAction);
		linkConverter.MRV.setDefaultReturnValuesSupplier("toInstance", () -> link);
		return linkConverter;
	}

	private Supplier<?> createSupplierForList(List<JsonToBasicClientDataActionLinkConverter> list) {
		return new Supplier<JsonToBasicClientDataActionLinkConverter>() {
			int counter = -1;

			@Override
			public JsonToBasicClientDataActionLinkConverter get() {
				counter++;
				return list.get(counter);
			}
		};
	}

	@Test
	public void testImplementsJsonToClientDataConverter() throws Exception {
		JsonObject jsonObject = (JsonObject) jsonParser
				.parseString("{\"authentication\":\"authentication\"}");

		jsonToDataConverter = JsonToBasicClientDataAuthenticationConverter
				.usingConverterFactoriesAndJsonObject(convertFactories, jsonObject);

		assertTrue(jsonToDataConverter instanceof JsonToClientDataConverter);
	}

	@Test
	public void testFactoryIsSentAlong() {
		JsonObject jsonObject = (JsonObject) jsonParser
				.parseString("{\"authentication\":\"authentication\"}");

		jsonToDataConverter = JsonToBasicClientDataAuthenticationConverter
				.usingConverterFactoriesAndJsonObject(convertFactories, jsonObject);

		assertSame(jsonToDataConverter.onlyForTestGetConverterFactory(), factory);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Authentication data must contain key: authentication")
	public void testNotARecordString() {
		parseStringAndCreateConverter("{\"not\":\"authentication\"}");
	}

	private ClientConvertible parseStringAndCreateConverter(String json) {
		JsonValue jsonValue = jsonParser.parseString(json);

		jsonToDataConverter = JsonToBasicClientDataAuthenticationConverter
				.usingConverterFactoriesAndJsonObject(convertFactories, (JsonObject) jsonValue);

		return jsonToDataConverter.toInstance();
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Not an object")
	public void testRecordIsNotAnObject() {
		parseStringAndCreateConverter("{\"authentication\":\"authentication\"}");
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Authentication data must contain child with key: data")
	public void testAuthenticationDoesNotContainData() {
		String json = "{\"authentication\":{";
		json += "\"notData\":\"notData\"";
		json += ",\"actionLinks\":\"noActionLink\"";
		json += "}}";
		parseStringAndCreateConverter(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Not an object")
	public void testRecordDataNotAnObject() {
		String json = "{\"authentication\":{";
		json += "\"data\":\"notData\"";
		json += ",\"actionLinks\":\"noActionLink\"";
		json += "}}";
		parseStringAndCreateConverter(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Authentication data must contain only key: authentication")
	public void testRecordExtraKey() {
		String json = """
				{
					"authentication":{
						"data":{
							"name":"groupNameInData", "children":[]
						},
						"actionLinks":"noActionLink"
					},
					"someExtraKey":"someExtraData"
				}
				""";
		parseStringAndCreateConverter(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Authentication data must contain child with key: actionLinks")
	public void testAuthenticationNoActionLinks() {
		String json = """
				{
				  "authentication": {
				    "data": {
				      "name": "groupNameInData",
				      "children": []
				    },
				  "NOTactionLinks":"someExtraData"
				  }
				}""";

		parseStringAndCreateConverter(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Authentication data must contain keys: data and actionLinks")
	public void testRecordExtraKeyOnSecondLevel() {
		String json = """
				{
				  "authentication": {
				    "data": {
				      "name": "groupNameInData",
				      "children": []
				    },
				    "actionLinks": {
				      "read": {
				        "requestMethod": "GET",
				        "rel": "read"
				      }
				    },
				    "extraKey": {
				      "name": "groupNameInData"
				    }
				  }
				}""";
		parseStringAndCreateConverter(json);
	}

	@Test
	public void providedFactoryIsUsedForActionLinks() {
		setActionLinkConvereterToReturnClientDataGroup();
		String json = """
				{
				  "authentication": {
				    "data": {
				      "children": [
				        {"name": "token"     , "value": "someAuthToken"      },
				        {"name": "validUntil", "value": "100"                },
				        {"name": "renewUntil", "value": "200"                },
				        {"name": "userId"    , "value": "someIdInUserStorage"},
				        {"name": "loginId"   , "value": "someLoginId"        },
				        {"name": "firstName" , "value": "someFirstName"      },
				        {"name": "lastName"  , "value": "someLastName"       }
				      ],
				      "name": "authToken"
				    },
				    "actionLinks": {
				      "renew": {
				        "requestMethod": "POST",
				        "rel": "renew",
				        "url": "https://localhost:8080/login/rest/authToken/someTokenId",
				        "accept": "application/vnd.cora.authentication+json"
				      },
				      "delete": {
				        "requestMethod": "DELETE",
				        "rel": "delete",
				        "url": "https://localhost:8080/login/rest/authToken/someTokenId"
				      }
				    }
				  }
				}""";

		ClientDataAuthentication clientDataAuthentication = (ClientDataAuthentication) parseStringAndCreateConverter(
				json);

		JsonObject deleteActionLinkAsJsonObject = (JsonObject) actionLinkConverterFactory.MCR
				.getParameterForMethodAndCallNumberAndParameter("factor", 0, "jsonObject");
		String deleteAction = """
				{\
				"requestMethod":"DELETE",\
				"rel":"delete",\
				"url":"https://localhost:8080/login/rest/authToken/someTokenId"\
				}""";
		assertEquals(deleteActionLinkAsJsonObject.toJsonFormattedString(), deleteAction);

		JsonObject renewActionLinkAsJsonObject = (JsonObject) actionLinkConverterFactory.MCR
				.getParameterForMethodAndCallNumberAndParameter("factor", 1, "jsonObject");
		String renewAction = """
				{\
				"requestMethod":"POST",\
				"rel":"renew",\
				"url":"https://localhost:8080/login/rest/authToken/someTokenId",\
				"accept":"application/vnd.cora.authentication+json"\
				}""";
		assertEquals(renewActionLinkAsJsonObject.toJsonFormattedString(), renewAction);

		JsonToBasicClientDataActionLinkConverterSpy actionLinkConverter = (JsonToBasicClientDataActionLinkConverterSpy) actionLinkConverterFactory.MCR
				.getReturnValue("factor", 0);

		actionLinkConverter.MCR.assertReturn("toInstance", 0,
				clientDataAuthentication.getActionLink(ClientAction.DELETE).get());

		JsonToBasicClientDataActionLinkConverterSpy actionLinkConverter2 = (JsonToBasicClientDataActionLinkConverterSpy) actionLinkConverterFactory.MCR
				.getReturnValue("factor", 1);
		actionLinkConverter2.MCR.assertReturn("toInstance", 0,
				clientDataAuthentication.getActionLink(ClientAction.RENEW).get());
	}

	private void setActionLinkConvereterToReturnClientDataGroup() {
		ClientDataGroupSpy clientDataGroup = new ClientDataGroupSpy();

		JsonToClientDataConverterSpy jsonToDataConverter = new JsonToClientDataConverterSpy();
		jsonToDataConverter.MRV.setDefaultReturnValuesSupplier("toInstance", () -> clientDataGroup);
		factory.MRV.setDefaultReturnValuesSupplier("factorUsingJsonObject",
				() -> jsonToDataConverter);
	}
}
