/*
 * Copyright 2015, 2018, 2023 Uppsala University Library
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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.spies.ClientDataGroupSpy;
import se.uu.ub.cora.clientdata.spies.JsonToClientDataConverterFactorySpy;
import se.uu.ub.cora.clientdata.spies.JsonToClientDataConverterSpy;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonParser;
import se.uu.ub.cora.json.parser.JsonValue;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;

public class JsonToBasicClientDataActionLinkConverterTest {

	private JsonParser jsonParser;
	private JsonToClientDataConverterFactorySpy factory;
	JsonToBasicClientDataActionLinkConverterImp jsonToDataConverter;

	@BeforeMethod
	public void beforeMethod() {
		jsonParser = new OrgJsonParser();
		factory = new JsonToClientDataConverterFactorySpy();
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Action link data must contain key: rel")
	public void testToClassWithNoAction() {
		String json = "{}";
		createClientDataActionLinkForJsonString(json);
	}

	private ClientActionLink createClientDataActionLinkForJsonString(String json) {
		JsonValue jsonValue = jsonParser.parseString(json);

		jsonToDataConverter = JsonToBasicClientDataActionLinkConverterImp
				.forJsonObjectUsingFactory((JsonObject) jsonValue, factory);
		return jsonToDataConverter.toInstance();
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Action link data must contain key: url")
	public void testToClassWithNoURL() {
		String json = """
				{"requestMethod":"GET","rel":"read"}""";
		createClientDataActionLinkForJsonString(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Action link data must contain key: requestMethod")
	public void testToClassWithNoRequestMethod() {
		String json = """
				{"rel":"read",\
				"url":"https://cora.epc.ub.uu.se/systemone/rest/record/presentationGroup/loginFormNewPGroup"}""";
		createClientDataActionLinkForJsonString(json);
	}

	@Test
	public void testToClassWithNecessaryContent() {
		String json = """
				{"requestMethod":"GET",\
				"rel":"read",\
				"url":"https://cora.epc.ub.uu.se/systemone/rest/record/presentationGroup/loginFormNewPGroup"}""";
		ClientActionLink clientDataActionLink = createClientDataActionLinkForJsonString(json);
		assertEquals(clientDataActionLink.getAction(), ClientAction.READ);
		assertEquals(clientDataActionLink.getURL(),
				"https://cora.epc.ub.uu.se/systemone/rest/record/presentationGroup/loginFormNewPGroup");
		assertEquals(clientDataActionLink.getRequestMethod(), "GET");
	}

	@Test
	public void testToClassWithExtraContent() {
		setJsonToClientDataConverterFactorySpyToReturnAClientDataGroupSpy();

		String json = """
				{
					"requestMethod": "POST",
				    "rel": "index",
					"body": {
				        "children": [
				            {
				                "children": [
				                    {
				                        "name": "linkedRecordType",
				                        "value": "recordType"
				                    },
				                    {
				                        "name": "linkedRecordId",
				                        "value": "textSystemOne"
				                    }
				                ],
				                "name": "recordType"
				            },
				            {
				                "name": "recordId",
				                "value": "refItemText"
				            },
				            {
				                "name": "type",
				                "value": "index"
				            }
				        ],
				        "name": "workOrder"
				    },
				    "contentType": "application/vnd.uub.record+json",
				    "url": "https://cora.epc.ub.uu.se/systemone/rest/record/workOrder/",
				    "accept": "application/vnd.uub.record+json"
				}""";
		ClientActionLink clientDataActionLink = createClientDataActionLinkForJsonString(json);
		assertEquals(clientDataActionLink.getAction(), ClientAction.INDEX);
		assertEquals(clientDataActionLink.getURL(),
				"https://cora.epc.ub.uu.se/systemone/rest/record/workOrder/");
		assertEquals(clientDataActionLink.getRequestMethod(), "POST");
		assertEquals(clientDataActionLink.getAccept(), "application/vnd.uub.record+json");
		assertEquals(clientDataActionLink.getContentType(), "application/vnd.uub.record+json");

		factory.MCR.assertMethodWasCalled("factorUsingJsonObject");
		JsonToClientDataConverterSpy bodyConverter = (JsonToClientDataConverterSpy) factory.MCR
				.getReturnValue("factorUsingJsonObject", 0);
		bodyConverter.MCR.assertReturn("toInstance", 0, clientDataActionLink.getBody());
	}

	private void setJsonToClientDataConverterFactorySpyToReturnAClientDataGroupSpy() {
		ClientDataGroupSpy clientDataGroupSpy = new ClientDataGroupSpy();
		JsonToClientDataConverterSpy converter = new JsonToClientDataConverterSpy();
		converter.MRV.setDefaultReturnValuesSupplier("toInstance", () -> clientDataGroupSpy);
		factory.MRV.setDefaultReturnValuesSupplier("factorUsingJsonObject", () -> converter);
	}
}
