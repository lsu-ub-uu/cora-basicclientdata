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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataRecordLink;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataLink;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonValue;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;

public class JsonToBasicClientDataRecordLinkConverterTest {

	@Test
	public void testToInstance() {
		String json = """
				{"children": [
				  {
				    "name": "linkedRecordType",
				    "value": "recordType"
				  },
				  {
				    "name": "linkedRecordId",
				    "value": "place"
				  }
				],
				"name": "someLink"}""";
		BasicClientDataRecordLink dataLink = (BasicClientDataRecordLink) getConverterdLink(json);
		assertEquals(dataLink.getNameInData(), "someLink");
		assertEquals(dataLink.getFirstAtomicValueWithNameInData("linkedRecordType"), "recordType");
		assertEquals(dataLink.getFirstAtomicValueWithNameInData("linkedRecordId"), "place");

	}

	@Test
	public void testToInstanceWithLinkedRepeatId() {
		String json = """
				{"children": [
				    {
				      "name": "linkedRecordType",
				      "value": "recordType"
				    },
				    {
				      "name": "linkedRecordId",
				      "value": "place"
				    },
				    {
				      "name": "linkedRepeatId",
				      "value": "one"
				    }
				  ],
				  "name": "someLink"
				}""";
		BasicClientDataRecordLink dataLink = (BasicClientDataRecordLink) getConverterdLink(json);
		assertEquals(dataLink.getNameInData(), "someLink");
		assertEquals(dataLink.getFirstAtomicValueWithNameInData("linkedRecordType"), "recordType");
		assertEquals(dataLink.getFirstAtomicValueWithNameInData("linkedRecordId"), "place");
		assertEquals(dataLink.getFirstAtomicValueWithNameInData("linkedRepeatId"), "one");

	}

	@Test
	public void testToInstanceWithLinkedPath() {
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
				      "name": "linkedPath"
				    }
				  ],
				  "name": "from"
				}""";
		BasicClientDataRecordLink dataLink = (BasicClientDataRecordLink) getConverterdLink(json);
		assertEquals(dataLink.getNameInData(), "from");
		assertEquals(dataLink.getFirstAtomicValueWithNameInData("linkedRecordType"), "coraText");
		assertEquals(dataLink.getFirstAtomicValueWithNameInData("linkedRecordId"),
				"exampleGroupText");
		assertTrue(dataLink.containsChildWithNameInData("linkedPath"));
		assertCorrectDataInLinkedPath(dataLink);

	}

	@Test
	public void testToInstanceWithLinkedRepeatIdAndLinkedPath() {
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
				  "name": "from"
				}""";
		BasicClientDataRecordLink dataLink = (BasicClientDataRecordLink) getConverterdLink(json);
		assertEquals(dataLink.getNameInData(), "from");
		assertEquals(dataLink.getFirstAtomicValueWithNameInData("linkedRecordType"), "coraText");
		assertEquals(dataLink.getFirstAtomicValueWithNameInData("linkedRecordId"),
				"exampleGroupText");
		assertEquals(dataLink.getFirstAtomicValueWithNameInData("linkedRepeatId"), "one");
		assertTrue(dataLink.containsChildWithNameInData("linkedPath"));
		assertCorrectDataInLinkedPath(dataLink);

	}

	private void assertCorrectDataInLinkedPath(BasicClientDataRecordLink dataLink) {
		ClientDataGroup outerLinkedPath = dataLink.getFirstGroupWithNameInData("linkedPath");
		assertEquals(outerLinkedPath.getFirstAtomicValueWithNameInData("nameInData"), "recordInfo");
		ClientDataGroup innerLinkedPath = outerLinkedPath.getFirstGroupWithNameInData("linkedPath");
		assertEquals(innerLinkedPath.getFirstAtomicValueWithNameInData("nameInData"), "type");
	}

	private ClientDataLink getConverterdLink(String json) {
		OrgJsonParser jsonParser = new OrgJsonParser();
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToBasicClientDataRecordLinkConverter converter = JsonToBasicClientDataRecordLinkConverter
				.forJsonObject((JsonObject) jsonValue);

		ClientDataLink dataLink = (ClientDataLink) converter.toInstance();
		return dataLink;
	}

	@Test
	public void testToClassWithRepeatId() {
		String json = """
				{"children": [
				  {
				    "name": "linkedRecordType",
				    "value": "recordType"
				  },
				  {
				    "name": "linkedRecordId",
				    "value": "place"
				  }
				],
				"repeatId": "0",
				"name": "someLink"}""";
		ClientDataLink dataLink = getConverterdLink(json);
		assertEquals(dataLink.getNameInData(), "someLink");
		assertEquals(dataLink.getRepeatId(), "0");
	}

	@Test
	public void testToClassWithAttribute() {
		String json = """
				{"children": [
				  {
				    "name": "linkedRecordType",
				    "value": "recordType"
				  },
				  {
				    "name": "linkedRecordId",
				    "value": "place"
				  }
				],
				"attributes": {
				  "type": "someType"
				},
				"name": "someLink"}""";
		ClientDataLink dataLink = getConverterdLink(json);

		assertEquals(dataLink.getNameInData(), "someLink");
		String attributeValue = dataLink.getAttribute("type").getValue();
		assertEquals(attributeValue, "someType");
	}

	@Test
	public void testToClassWithRepeatIdAndAttribute() {
		String json = """
				{"children": [
				  {
				    "name": "linkedRecordType",
				    "value": "recordType"
				  },
				  {
				    "name": "linkedRecordId",
				    "value": "place"
				  }
				],
				"repeatId": "0",
				"attributes": {
				  "type": "someType"
				},
				"name": "someLink"}""";
		ClientDataLink dataLink = getConverterdLink(json);

		assertEquals(dataLink.getNameInData(), "someLink");
		String attributeValue = dataLink.getAttribute("type").getValue();
		assertEquals(attributeValue, "someType");
		assertEquals(dataLink.getRepeatId(), "0");
	}

	@Test
	public void testToRecordLinkWithActions() {
		String json = """
				{"children": [
				    {
				      "name": "linkedRecordType",
				      "value": "recordType"
				    },
				    {
				      "name": "linkedRecordId",
				      "value": "demo"
				    }
				  ],
				  "actionLinks": {
				    "read": {
				      "requestMethod": "GET",
				      "rel": "read",
				      "url": "https://cora.epc.ub.uu.se/systemone/rest/record/recordType/demo",
				      "accept": "application/vnd.uub.record+json"
				    }
				  },
				  "name": "type"
				}""";
		ClientDataLink dataLink = getConverterdLink(json);

		assertEquals(dataLink.getNameInData(), "type");
		assertTrue(dataLink.hasReadAction());

	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonObject: Group data can only contain keys: name, children and attributes")
	public void testToClassWithRepeatIdAndAttributeAndExtra() {
		String json = """
				{"children": [
				  {
				    "name": "linkedRecordType",
				    "value": "recordType"
				  },
				  {
				    "name": "linkedRecordId",
				    "value": "place"
				  }
				],
				"repeatId": "0",
				"attributes": {
				  "type": "someType"
				},
				"name": "someLink",
				"extra": "extraValue"}""";
		getConverterdLink(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonObject: Group data must contain name and children, and may contain attributes or repeatId")
	public void testToClassWithIncorrectAttributeNameInData() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"recordType\"},{\"name\":\"linkedRecordId\",\"value\":\"place\"}],\"NOTattributes\":{\"type\":\"someType\"},\"name\":\"someLink\"}";
		getConverterdLink(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "RecordLinkData must contain children with name linkedRecordType and linkedRecordId "
			+ "and might contain child with name linkedRepeatId and linkedPath")
	public void testToClassWithNoLinkedRecordType() {
		String json = "{\"children\":[{\"name\":\"linkedRecordId\",\"value\":\"place\"}],\"name\":\"someLink\"}";
		getConverterdLink(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "RecordLinkData must contain children with name linkedRecordType and linkedRecordId "
			+ "and might contain child with name linkedRepeatId and linkedPath")
	public void testToClassWithNoLinkedRecordTypeButOtherChild() {
		String json = "{\"children\":[{\"name\":\"NOTlinkedRecordType\",\"value\":\"recordType\"},{\"name\":\"linkedRecordId\",\"value\":\"place\"}],\"name\":\"someLink\"}";
		getConverterdLink(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "RecordLinkData must contain children with name linkedRecordType and linkedRecordId "
			+ "and might contain child with name linkedRepeatId and linkedPath")
	public void testToClassWithNoLinkedRecordId() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"recordType\"}],\"name\":\"someLink\"}";
		getConverterdLink(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "RecordLinkData must contain children with name linkedRecordType and linkedRecordId "
			+ "and might contain child with name linkedRepeatId and linkedPath")
	public void testToClassWithNoLinkedRecordIdButOtherChild() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"recordType\"},{\"name\":\"NOTlinkedRecordId\",\"value\":\"place\"}],\"name\":\"someLink\"}";
		getConverterdLink(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "RecordLinkData must contain children with name linkedRecordType and linkedRecordId "
			+ "and might contain child with name linkedRepeatId and linkedPath")
	public void testToClassWithTooManyChildren() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"recordType\"},{\"name\":\"linkedRecordId\",\"value\":\"place\"},{\"name\":\"linkedRepeatId\",\"value\":\"one\"},{\"name\":\"someExtra\",\"value\":\"one\"},{\"children\":[{\"name\":\"nameInData\",\"value\":\"recordInfo\"},{\"children\":[{\"name\":\"nameInData\",\"value\":\"type\"}],\"name\":\"linkedPath\"}],\"name\":\"linkedPath\"}],\"name\":\"someLink\"}";
		getConverterdLink(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "RecordLinkData must contain children with name linkedRecordType and linkedRecordId "
			+ "and might contain child with name linkedRepeatId and linkedPath")
	public void testToClassWithOkNumberOfChildrenButNoLinkedRepeatIdAndNoLinkedPath() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"coraText\"},{\"name\":\"linkedRecordId\",\"value\":\"exampleGroupText\"},{\"name\":\"someOtherChild\",\"value\":\"one\"}],\"name\":\"from\"}";
		getConverterdLink(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "RecordLinkData must contain children with name linkedRecordType and linkedRecordId "
			+ "and might contain child with name linkedRepeatId and linkedPath")
	public void testToClassWithMaxNumberOfChildrenButNoLinkedRepeatId() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"recordType\"},{\"name\":\"linkedRecordId\",\"value\":\"place\"},{\"name\":\"someExtra\",\"value\":\"one\"},{\"children\":[{\"name\":\"nameInData\",\"value\":\"recordInfo\"},{\"children\":[{\"name\":\"nameInData\",\"value\":\"type\"}],\"name\":\"linkedPath\"}],\"name\":\"linkedPath\"}],\"name\":\"someLink\"}";
		getConverterdLink(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "RecordLinkData must contain children with name linkedRecordType and linkedRecordId "
			+ "and might contain child with name linkedRepeatId and linkedPath")
	public void testToClassWithMaxNumberOfChildrenButNoLinkedPath() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"recordType\"},{\"name\":\"linkedRecordId\",\"value\":\"place\"},{\"name\":\"linkedRepeatId\",\"value\":\"one\"},{\"name\":\"someExtra\",\"value\":\"one\"}],\"name\":\"someLink\"}";
		getConverterdLink(json);
	}
}
