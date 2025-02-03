/*
 * Copyright 2015, 2023 Uppsala University Library
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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverter;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterFactory;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonParser;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;

public class JsonToBasicClientDataConverterFactoryTest {
	private JsonToClientDataConverterFactory jsonToDataConverterFactory;
	private String json;

	@BeforeMethod
	public void beforeMethod() {
		jsonToDataConverterFactory = new JsonToBasicClientDataConverterFactoryImp();
	}

	@Test
	public void testFactorOnJsonStringDataRecord() {

		String json = """
				{"record":{
					"data":{
						"name":"groupNameInData"
						, "children":[]
					},
					"actionLinks":{
				 		"read":{
				 			"requestMethod":"GET",
				 			"rel":"read",
				 			"url":"https://cora.example.org/somesystem/rest/record/somerecordtype/somerecordid",
				 			"accept":"application/vnd.uub.record+json"
						}
					}
				}
				}""";

		JsonToBasicClientDataRecordConverter jsonToDataConverter = (JsonToBasicClientDataRecordConverter) jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataRecordConverter);

		assertSame(jsonToDataConverter.onlyForTestGetConverterFactory(),
				jsonToDataConverterFactory);
		assertTrue(jsonToDataConverter
				.onlyForTestGetActionLinkConverterFactory() instanceof JsonToBasicClientDataActionLinkConverterFactoryImp);

		assertEqualJson(jsonToDataConverter.onlyForTestGetJsonObject(), parseToJsonObject(json));

	}

	private void assertEqualJson(JsonObject actual, JsonObject expected) {
		assertEquals(actual.toJsonFormattedString(), expected.toJsonFormattedString());
	}

	private JsonObject parseToJsonObject(String json) {
		JsonParser jsonParser = new OrgJsonParser();
		JsonObject jsonObject = jsonParser.parseStringAsObject(json);
		return jsonObject;
	}

	@Test
	public void testFactorOnJsonStringDataAuthentication() {
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
				        "url": "{protocol}://localhost:8080/login/rest/authToken/someTokenId",
				        "accept": "application/vnd.uub.authentication+json"
				      },
				      "delete": {
				        "requestMethod": "DELETE",
				        "rel": "delete",
				        "url": "{protocol}://localhost:8080/login/rest/authToken/someTokenId"
				      }
				    }
				  }
				}""";

		JsonToBasicClientDataAuthenticationConverter jsonToDataConverter = (JsonToBasicClientDataAuthenticationConverter) jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataAuthenticationConverter);
		assertFactoriesForAuthenticationConverter(jsonToDataConverter);
		assertEqualJson(jsonToDataConverter.onlyForTestGetJsonObject(), parseToJsonObject(json));

	}

	private void assertFactoriesForAuthenticationConverter(
			JsonToBasicClientDataAuthenticationConverter jsonToDataConverter) {
		assertSame(jsonToDataConverter.onlyForTestGetConverterFactory(),
				jsonToDataConverterFactory);
		assertTrue(jsonToDataConverter
				.onlyForTestGetActionLinkConverterFactory() instanceof JsonToBasicClientDataActionLinkConverterFactoryImp);
	}

	@Test
	public void testFactorDataListConverter() {
		String json = """
				{
				  "dataList": {
				    "fromNo": "0",
				    "data": [
				      {
				        "record": {
				          "data": {
				            "name": "groupNameInData",
				            "children": [
				            ]
				          },
				          "actionLinks": {
				            "read": {
				              "requestMethod": "GET",
				              "rel": "read",
				              "url": "https://cora.example.org/somesystem/rest/record/somerecordtype/somerecordid",
				              "accept": "application/vnd.uub.record+json"
				            }
				          }
				        }
				      },
				      {
				        "record": {
				          "data": {
				            "name": "groupNameInData",
				            "children": [
				            ]
				          },
				          "actionLinks": {
				            "read": {
				              "requestMethod": "GET",
				              "rel": "read",
				              "url": "https://cora.example.org/somesystem/rest/record/somerecordtype/somerecordid",
				              "accept": "application/vnd.uub.record+json"
				            }
				          }

				        }
				      }
				    ],
				    "totalNo": "2",
				    "containDataOfType": "demo",
				    "toNo": "2"
				  }
				}""";
		JsonToBasicClientDataListConverter jsonToDataListConverter = (JsonToBasicClientDataListConverter) jsonToDataConverterFactory
				.factorUsingString(json);

		assertTrue(jsonToDataListConverter instanceof JsonToBasicClientDataListConverter);
		assertSame(jsonToDataListConverter.onlyForTestGetConverterFactory(),
				jsonToDataConverterFactory);
		assertEqualJson(jsonToDataListConverter.onlyForTestGetJsonObject(),
				parseToJsonObject(json));
	}

	@Test
	public void testFactorOnJsonStringDataRecordGroup() {
		String json = """
				{"name":"id","children":[
					{"name":"recordInfo","children":[
						{"name":"id","value":"id2"}]}]
					}""";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataRecordGroupConverter);
	}

	@Test
	public void testFactorOnJsonStringDataGroupEmptyChildren() {
		json = """
				{"name":"groupNameInData", "children":[]}""";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
	}

	@Test
	public void testFactorOnJsonStringDataGroupAtomicChild() {
		String json = """
				{"name":"id","children":[{"name":"someNameInData","value":"id2"}]}
				""";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
	}

	@Test
	public void testFactorOnJsonStringOnlyRecordTypeFactorsGroupConverter() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"recordType\"}"
				+ ",{\"name\":\"NOTlinkedRecordId\",\"value\":\"place\"}],\"name\":\"type\"}";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
	}

	@Test
	public void testFactorOnJsonStringOnlyRecordIdFactorsGroupConverter() {
		String json = "{\"children\":[{\"name\":\"NOTlinkedRecordType\",\"value\":\"recordType\"}"
				+ ",{\"name\":\"linkedRecordId\",\"value\":\"place\"}],\"name\":\"type\"}";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
	}

	@Test
	public void testFactorOnJsonStringNoLinkedPathFactorsDataRecordLink() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"recordType\"}"
				+ ",{\"name\":\"linkedRecordId\",\"value\":\"place\"}],\"name\":\"type\"}";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataRecordLinkConverter);
		JsonToBasicClientDataRecordLinkConverter jsonToDataConverter2 = (JsonToBasicClientDataRecordLinkConverter) jsonToDataConverter;
		assertTrue(jsonToDataConverter2
				.onlyForTestGetActionLinkConverterFactory() instanceof JsonToBasicClientDataActionLinkConverterFactoryImp);
	}

	@Test
	public void testFactorOnJsonStringWithLinkedPathFactorsDataRecordLink() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"coraText\"}"
				+ ",{\"name\":\"linkedRecordId\",\"value\":\"exampleGroupText\"}"
				+ ",{\"children\":[{\"name\":\"nameInData\",\"value\":\"recordInfo\"}"
				+ ",{\"children\":[{\"name\":\"nameInData\",\"value\":\"type\"}],\"name\":\"linkedPath\"}]"
				+ ",\"name\":\"linkedPath\"}],\"name\":\"from\"}";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataRecordLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringWithLinkedRepeatIdFactorsDataRecordLink() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"coraText\"}"
				+ ",{\"name\":\"linkedRecordId\",\"value\":\"exampleGroupText\"}"
				+ ",{\"name\":\"linkedRepeatId\",\"value\":\"one\"}],\"name\":\"from\"}";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataRecordLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringWithLinkedRepeatIdAndLinkedPathFactorsDataRecordLink() {
		String json = """
				{"children": [
				  {
				    "name": "linkedRecordType",
				    "value": "coraText"
				  },
				  {
				    "name": "linkedRecordId",
				    "value": "exampleGroupText"
				  },
				  {
				    "name": "linkedRepeatId",
				    "value": "one"
				  },
				  {
				    "children": [
				      {
				        "name": "nameInData",
				        "value": "recordInfo"
				      },
				      {
				        "children": [
				          {
				            "name": "nameInData",
				            "value": "type"
				          }
				        ],
				        "name": "linkedPath"
				      }
				    ],
				    "name": "linkedPath"
				  }
				],
				"name": "from"}""";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataRecordLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringWithLinkedPathButNotLinkedRecordTypeFactorsDataGroup() {
		String json = """
				{"children": [
				  {
				    "name": "NOTlinkedRecordType",
				    "value": "coraText"
				  },
				  {
				    "name": "linkedRecordId",
				    "value": "exampleGroupText"
				  },
				  {
				    "children": [
				      {
				        "name": "nameInData",
				        "value": "recordInfo"
				      },
				      {
				        "children": [
				          {
				            "name": "nameInData",
				            "value": "type"
				          }
				        ],
				        "name": "linkedPath"
				      }
				    ],
				    "name": "linkedPath"
				  }
				],
				"name": "from"}""";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
		assertFalse(jsonToDataConverter instanceof JsonToBasicClientDataRecordLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringWithLinkedPathButNotLinkedRecordIdFactorsDataGroup() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"coraText\"},"
				+ "{\"name\":\"NOTlinkedRecordId\",\"value\":\"exampleGroupText\"},"
				+ "{\"children\":[{\"name\":\"nameInData\",\"value\":\"recordInfo\"},"
				+ "{\"children\":[{\"name\":\"nameInData\",\"value\":\"type\"}],"
				+ "\"name\":\"linkedPath\"}],\"name\":\"linkedPath\"}],\"name\":\"from\"}";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
		assertFalse(jsonToDataConverter instanceof JsonToBasicClientDataRecordLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringWithThreeChildrenButNoLinkedRepatIdAndNoLinkedPathFactorsDataGroup() {
		String json = """
				{"children": [
				  {
				    "name": "linkedRecordType",
				    "value": "coraText"
				  },
				  {
				    "name": "linkedRecordId",
				    "value": "exampleGroupText"
				  },
				  {
				    "children": [
				      {
				        "name": "nameInData",
				        "value": "recordInfo"
				      },
				      {
				        "children": [
				          {
				            "name": "nameInData",
				            "value": "type"
				          }
				        ],
				        "name": "linkedPath"
				      }
				    ],
				    "name": "NOTlinkedPath"
				  }
				],
				"name": "from"}""";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataRecordLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringWithMaxNumOfChildrenBUtNOLinkedRepeatIdFactorsDataGroup() {
		String json = """
				{"children": [
				   {
				     "name": "linkedRecordType",
				     "value": "coraText"
				   },
				   {
				     "name": "linkedRecordId",
				     "value": "exampleGroupText"
				   },
				   {
				     "name": "NOTlinkedRepeatId",
				     "value": "one"
				   },
				   {
				     "children": [
				       {
				         "name": "nameInData",
				         "value": "recordInfo"
				       },
				       {
				         "children": [
				           {
				             "name": "nameInData",
				             "value": "type"
				           }
				         ],
				         "name": "linkedPath"
				       }
				     ],
				     "name": "linkedPath"
				   }
				 ],
				 "name": "from"}""";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataRecordLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringWithMaxNumOfChildrenBUtNOLinkedPathIdFactorsDataGroup() {
		String json = """
				{"children": [
				    {
				      "name": "linkedRecordType",
				      "value": "coraText"
				    },
				    {
				      "name": "linkedRecordId",
				      "value": "exampleGroupText"
				    },
				    {
				      "name": "linkedRepeatId",
				      "value": "one"
				    },
				    {
				      "children": [
				        {
				          "name": "nameInData",
				          "value": "recordInfo"
				        },
				        {
				          "children": [
				            {
				              "name": "nameInData",
				              "value": "type"
				            }
				          ],
				          "name": "linkedPath"
				        }
				      ],
				      "name": "NOTlinkedPath"
				    }
				  ],
				  "name": "from"}""";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataRecordLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringOnlyStreamIdFactorsGroupConverter() {
		String json = """
				{"children": [
				  {
				    "name": "streamId",
				    "value": "soundBinary:18269669168741"
				  },
				  {
				    "name": "NOTfilename",
				    "value": "adele.png"
				  },
				  {
				    "name": "NOTfilesize",
				    "value": "8"
				  },
				  {
				    "name": "NOTmimeType",
				    "value": "application/octet-stream"
				  }
				],
				"name": "master"}""";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
		assertFalse(jsonToDataConverter instanceof JsonToBasicClientDataResourceLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringNOTStreamIdFactorsGroupConverter() {
		String json = "{\"children\":[{\"name\":\"NOTstreamId\",\"value\":\"soundBinary:18269669168741\"},{\"name\":\"NOTfilename\",\"value\":\"adele.png\"},{\"name\":\"NOTfilesize\",\"value\":\"8\"},{\"name\":\"NOTmimeType\",\"value\":\"application/octet-stream\"}],\"name\":\"master\"}";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
		assertFalse(jsonToDataConverter instanceof JsonToBasicClientDataResourceLinkConverter);
	}

	@Test
	public void testFactorOnlyStreamIdAndFileNameFactorsGroupConverter() {
		String json = "{\"children\":[{\"name\":\"streamId\",\"value\":\"soundBinary:18269669168741\"},{\"name\":\"filename\",\"value\":\"adele.png\"},{\"name\":\"NOTfilesize\",\"value\":\"8\"},{\"name\":\"NOTmimeType\",\"value\":\"application/octet-stream\"}],\"name\":\"master\"}";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
		assertFalse(jsonToDataConverter instanceof JsonToBasicClientDataResourceLinkConverter);
	}

	@Test
	public void testFactorOnlyStreamIdAndFileNameAndFileSizeFactorsGroupConverter() {
		String json = "{\"children\":[{\"name\":\"streamId\",\"value\":\"soundBinary:18269669168741\"},{\"name\":\"filename\",\"value\":\"adele.png\"},{\"name\":\"filesize\",\"value\":\"8\"},{\"name\":\"NOTmimeType\",\"value\":\"application/octet-stream\"}],\"name\":\"master\"}";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
		assertFalse(jsonToDataConverter instanceof JsonToBasicClientDataResourceLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringCompleteSetupFactorsDataResourceLink() {
		String json = """
				{
					"mimeType": "application/octet-stream",
					"name": "master"
				}
				""";

		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);

		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataResourceLinkConverter);
		JsonToBasicClientDataResourceLinkConverter converter = (JsonToBasicClientDataResourceLinkConverter) jsonToDataConverter;
		JsonToBasicClientDataActionLinkConverterFactory createdActionLinkConverterFactory = converter
				.onlyForTestGetActionLinkConverterFactory();
		assertTrue(
				createdActionLinkConverterFactory instanceof JsonToBasicClientDataActionLinkConverterFactoryImp);
	}

	@Test

	public void testFactorOnJsonStringDataAtomic() {
		String json = "{\"name\":\"atomicNameInData\",\"value\":\"atomicValue\"}";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataAtomicConverter);
	}

	@Test
	public void testFactorOnJsonStringDataAttribute() {
		String json = "{\"attributeNameInData\":\"attributeValue\"}";
		JsonToClientDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.factorUsingString(json);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataAttributeConverter);
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testFactorOnJsonObjectNullJson() {
		jsonToDataConverterFactory.factorUsingJsonObject(null);
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testFactorOnStringNullJson() {
		jsonToDataConverterFactory.factorUsingString(null);
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testClassCreatorGroupNotAGroup() {
		String json = "[{\"id\":{\"id2\":\"value\"}}]";
		jsonToDataConverterFactory.factorUsingString(json);
	}

}
