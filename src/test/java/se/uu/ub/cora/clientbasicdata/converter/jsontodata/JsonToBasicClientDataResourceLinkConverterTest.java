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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataResourceLink;
import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientDataLink;
import se.uu.ub.cora.clientdata.spies.ClientActionLinkSpy;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonValue;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;

public class JsonToBasicClientDataResourceLinkConverterTest {

	private JsonToBasicClientDataActionLinkConverterFactorySpy actionLinkConverterFactory;
	private JsonToBasicClientDataResourceLinkConverter converter;
	private JsonValue jsonValue;

	@BeforeMethod
	public void beforeMethod() {
		actionLinkConverterFactory = new JsonToBasicClientDataActionLinkConverterFactorySpy();

	}

	private BasicClientDataResourceLink getConvertedLink(String json) {
		OrgJsonParser jsonParser = new OrgJsonParser();
		jsonValue = jsonParser.parseString(json);
		converter = JsonToBasicClientDataResourceLinkConverter
				.usingActionLinkConverterFactoryforJsonObject(actionLinkConverterFactory,
						(JsonObject) jsonValue);

		return (BasicClientDataResourceLink) converter.toInstance();
	}

	@Test
	public void testToInstance() {
		BasicClientDataResourceLink resourceLink = getConvertedLink(minimalJsonOk());

		assertEquals(resourceLink.getNameInData(), "master");
		assertEquals(resourceLink.getType(), "someType");
		assertEquals(resourceLink.getId(), "someId");
		assertEquals(resourceLink.getMimeType(), "image/png");
		assertFalse(resourceLink.hasRepeatId());
		actionLinkConverterFactory.MCR.assertMethodNotCalled("factor");
	}

	private String minimalJsonOk() {
		return """
				{
				  "name": "master",
				  "children": [
				    {"name": "linkedRecordType", "value": "someType"                  },
				    {"name": "linkedRecordId",   "value": "someId"},
				    {"name": "mimeType",           "value": "image/png"               }
				  ]
				}
				""";
	}

	@Test
	public void testToInstanceWithActionLink() {
		String json = """
				{
				  "name": "master",
				  "children": [
				    {"name": "linkedRecordType", "value": "someType"                  },
				    {"name": "linkedRecordId",   "value": "someId"},
				    {"name": "mimeType",           "value": "image/png"               }
				  ],
				  "actionLinks": {
						"read": {
							"requestMethod": "GET",
							"rel": "read",
							"url": "http://localhost:38080/systemone/rest/record/someType/someId/master",
							"accept": "image/jpeg"
						}
					}
				}
				""";
		BasicClientDataResourceLink resourceLink = getConvertedLink(json);

		assertEquals(resourceLink.getNameInData(), "master");
		assertEquals(resourceLink.getType(), "someType");
		assertEquals(resourceLink.getId(), "someId");
		assertEquals(resourceLink.getMimeType(), "image/png");
		actionLinkConverterFactory.MCR.assertMethodWasCalled("factor");
		assertTrue(resourceLink.hasReadAction());
		assertActionReadForUrl(resourceLink,
				"http://localhost:38080/systemone/rest/record/someType/someId/master");
	}

	private void assertActionReadForUrl(BasicClientDataResourceLink resourceLink, String url) {
		Optional<ClientActionLink> actionLink = resourceLink.getActionLink(ClientAction.READ);
		var clientActionLink = (ClientActionLinkSpy) actionLink.get();
		var factoredActionLinkConverter = (JsonToBasicClientDataActionLinkConverterSpy) actionLinkConverterFactory.MCR
				.getReturnValue("factor", 0);
		factoredActionLinkConverter.MCR.assertReturn("toInstance", 0, clientActionLink);

		JsonObject valueForMethodNameAndCallNumberAndParameterName = (JsonObject) actionLinkConverterFactory.MCR
				.getParameterForMethodAndCallNumberAndParameter("factor", 0, "jsonObject");
		assertEquals(valueForMethodNameAndCallNumberAndParameterName.getValueAsJsonString("url")
				.getStringValue(), url);
	}

	@Test
	public void testToClassWithRepeatId() {
		String json = """
				{
				  "name": "master",
				  "children": [
				    {"name": "linkedRecordType", "value": "someType"                  },
				    {"name": "linkedRecordId",   "value": "someId"},
				    {"name": "mimeType",           "value": "image/png"               }
				  ],
				  "repeatId":"0"
				}
				""";

		ClientDataLink dataLink = getConvertedLink(json);

		assertEquals(dataLink.getRepeatId(), "0");
	}

	@Test(dataProvider = "testValidJson", expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonObject: ResourceLink must contain name,children\\[linkedRecordType,linkedRecordId,mimeType\\] and repeatId\\.")
	public void testValidJson(String json) {
		getConvertedLink(json);
	}

	@DataProvider(name = "testValidJson")
	public Object[][] testExceptionMimeTypeNotExist2() {
		String json0 = jsonWithOutNameInData();
		String json1 = jsonWithOutChildren();
		String json2 = jsonTooManyFields();
		String json3 = jsonTooManyFields_repeatIdMissing();
		String json4 = jsonTooManyFields_NameMissingAndRepeatIdMissing();
		String json5 = jsonManyChildrenMissing();
		String json6 = jsonLinkedRecordId();
		String json7 = jsonExtraChild();
		String json8 = jsonExtraChild_ActionLinksExist_RepeatIdMissing();
		String json9 = jsonExtraChild_ActionLinksMissing_RepeatIdExists();
		String json10 = jsonExtraChild_withActionLinks_TooMany();

		return new Object[][] { { json0 }, { json1 }, { json2 }, { json3 }, { json4 }, { json5 },
				{ json6 }, { json7 }, { json8 }, { json9 }, { json10 } };
	}

	private String jsonWithOutNameInData() {
		return """
					{
					"children": [
					             {"name": "linkedRecordType", "value": "someType"                  },
					             {"name": "linkedRecordId",   "value": "someId"},
					             {"name": "mimeType",           "value": "image/png"               }
					             ],
					"repeatId":"0"
						}
				""";
	}

	private String jsonWithOutChildren() {
		return """
				{
				  "name": "master",
				  "repeatId":"0"
				}
				""";
	}

	private String jsonTooManyFields() {
		return """
				{
				  "name": "master",
					"children": [
					             {"name": "linkedRecordType", "value": "someType"                  },
					             {"name": "linkedRecordId",   "value": "someId"},
					             {"name": "mimeType",           "value": "image/png"               }
					             ],
					"repeatId":"0",
					"someOther": "someOther"
				}
				""";
	}

	private String jsonTooManyFields_repeatIdMissing() {
		return """
				{
				  "name": "master",
					"children": [
					             {"name": "linkedRecordType", "value": "someType"                  },
					             {"name": "linkedRecordId",   "value": "someId"},
					             {"name": "mimeType",           "value": "image/png"               }
					             ],
					 "someOther": "someOther"
				}
				""";
	}

	private String jsonTooManyFields_NameMissingAndRepeatIdMissing() {
		return """
				{
				  "nameSpecial": "master",
					"children": [
					             {"name": "linkedRecordType", "value": "someType"                  },
					             {"name": "linkedRecordId",   "value": "someId"},
					             {"name": "mimeType",           "value": "image/png"               }
					             ],
					 "someOther": "someOther"
				}
				""";
	}

	private String jsonManyChildrenMissing() {
		return """
					{
					"name": "master",
					"children": [
					             {"name": "linkedRecordType", "value": "someType"                  }
					             ],
					"repeatId":"0"
						}
				""";
	}

	private String jsonLinkedRecordId() {
		return """
					{
					"name": "master",
					"children": [
					             {"name": "linkedRecordType", "value": "someType"                  },
					             {"name": "someOtherId",   "value": "someId"},
					              {"name": "mimeType",           "value": "image/png"               }
					             ],
					"repeatId":"0"
						}
				""";
	}

	private String jsonExtraChild() {
		return """
					{
					"name": "master",
					"children": [
					             {"name": "linkedRecordType", "value": "someType"                  },
					             {"name": "linkedRecordId",   "value": "someId"},
					             {"name": "mimeType",           "value": "image/png"               },
					             {"name": "another",           "value": "anotherValue"               }
					             ],
					"repeatId":"0"
						}
				""";
	}

	private String jsonExtraChild_ActionLinksExist_RepeatIdMissing() {
		return """
				{
					"name": "master",
					"children": [
					             {"name": "linkedRecordType", "value": "someType"                  },
					             {"name": "linkedRecordId",   "value": "someId"},
					             {"name": "mimeType",           "value": "image/png"               },
					             {"name": "another",           "value": "anotherValue"               }
					             ],
					"actionLinks": {
						"read": {
							"requestMethod": "GET",
							"rel": "read",
							"url": "http://localhost:38080/systemone/rest/record/someType/someId/master",
							"accept": "image/jpeg"
						}
					},
					"someOther": "someOther"
				}
				""";
	}

	private String jsonExtraChild_ActionLinksMissing_RepeatIdExists() {
		return """
				{
					"name": "master",
					"children": [
					             {"name": "linkedRecordType", "value": "someType"                  },
					             {"name": "linkedRecordId",   "value": "someId"},
					             {"name": "mimeType",           "value": "image/png"               },
					             {"name": "another",           "value": "anotherValue"               }
					             ],
					"repeatId":"0",
					"someOther": "someOther"
				}
				""";
	}

	private String jsonExtraChild_withActionLinks_TooMany() {
		return """
						{
					"name": "master",
					"children": [
					             {"name": "linkedRecordType", "value": "someType"                  },
					             {"name": "linkedRecordId",   "value": "someId"},
					             {"name": "mimeType",           "value": "image/png"               },
					             {"name": "another",           "value": "anotherValue"               }
					             ],
					"actionLinks": {
						"read": {
							"requestMethod": "GET",
							"rel": "read",
							"url": "http://localhost:38080/systemone/rest/record/someType/someId/master",
							"accept": "image/jpeg"
						}
					},
					"repeatId":"0",
					"someOther": "someOther"
						}
				""";
	}

	@Test
	public void onlyForTest() {
		String json = minimalJsonOk();
		getConvertedLink(json);

		assertonlyForTestGetActionLinkConverterFactory();
		assertOnlyForTestGetJsonObject();
	}

	private void assertonlyForTestGetActionLinkConverterFactory() {
		JsonToBasicClientDataActionLinkConverterFactory actionLinkConverter = converter
				.onlyForTestGetActionLinkConverterFactory();
		assertSame(actionLinkConverterFactory, actionLinkConverter);
	}

	private void assertOnlyForTestGetJsonObject() {
		JsonObject jsonObject = converter.onlyForTestGetJsonObject();
		assertSame(jsonObject, jsonValue);
	}
}
