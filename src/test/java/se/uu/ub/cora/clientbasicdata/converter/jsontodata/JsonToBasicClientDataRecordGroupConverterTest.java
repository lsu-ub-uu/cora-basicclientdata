/*
 * Copyright 2015, 2019 Uppsala University Library
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

import java.util.Iterator;

import org.testng.annotations.Test;

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataAtomic;
import se.uu.ub.cora.clientdata.ClientDataChild;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverter;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonValue;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;

public class JsonToBasicClientDataRecordGroupConverterTest {
	OrgJsonParser jsonParser = new OrgJsonParser();
	private JsonToClientDataConverter converter;
	private ClientDataRecordGroup dataRecordGroup;

	@Test
	public void testInit() {
		String json = "{\"name\":\"groupNameInData\", \"children\":[]}";
		converter = createRecordGroupConverterForJsonString(json);

		assertTrue(converter instanceof JsonToBasicClientDataRecordGroupConverter);
	}

	private JsonToClientDataConverter createRecordGroupConverterForJsonString(String json) {
		JsonValue jsonValue = jsonParser.parseString(json);
		return JsonToBasicClientDataRecordGroupConverter.forJsonObject((JsonObject) jsonValue);
	}

	@Test
	public void testToClassWithAttribute() {
		String json = """
				{"name":"groupNameInData","attributes":{"attributeNameInData":"attributeValue"},\
				"children":[]}""";
		converter = createRecordGroupConverterForJsonString(json);

		dataRecordGroup = (ClientDataRecordGroup) converter.toInstance();

		String attributeValue = dataRecordGroup.getAttribute("attributeNameInData").getValue();
		assertEquals(attributeValue, "attributeValue");
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWithRepeatIdAndAttributeAndExtra() {
		String json = """
				{"name":"groupNameInData", "children":[]
				 ,"attributes":{"attributeNameInData":"attributeValue"}"
				 ,"extraKey":"extra"}""";
		converter = createRecordGroupConverterForJsonString(json);

		converter.toInstance();
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWithRepeatIdMissingAttribute() {
		String json = """
				{"name":"groupNameInData", "children":[],
				"NOTattributes":{"attributeNameInData":"attributeValue"}}""";
		converter = createRecordGroupConverterForJsonString(json);

		converter.toInstance();
	}

	@Test
	public void testToClassWithAttributes() {
		String json = """
				{"name":"groupNameInData","attributes":{
				"attributeNameInData":"attributeValue",
				"attributeNameInData2":"attributeValue2"},"children":[]}""";
		converter = createRecordGroupConverterForJsonString(json);

		dataRecordGroup = (ClientDataRecordGroup) converter.toInstance();

		assertEquals(dataRecordGroup.getNameInData(), "groupNameInData");
		String attributeValue = dataRecordGroup.getAttribute("attributeNameInData").getValue();
		assertEquals(attributeValue, "attributeValue");
		String attributeValue2 = dataRecordGroup.getAttribute("attributeNameInData2").getValue();
		assertEquals(attributeValue2, "attributeValue2");
	}

	@Test
	public void testToClassWithAtomicChild() {
		String json = """
				{"name":"groupNameInData",
				"children":[{"name":"atomicNameInData","value":"atomicValue"}]}""";
		converter = createRecordGroupConverterForJsonString(json);

		dataRecordGroup = (ClientDataRecordGroup) converter.toInstance();

		assertEquals(dataRecordGroup.getNameInData(), "groupNameInData");
		BasicClientDataAtomic child = (BasicClientDataAtomic) dataRecordGroup.getChildren()
				.iterator().next();
		assertEquals(child.getNameInData(), "atomicNameInData");
		assertEquals(child.getValue(), "atomicValue");
	}

	@Test
	public void testToClassGroupWithAtomicChildAndGroupChildWithAtomicChild() {
		String json = """
				{"name":"groupNameInData","children":[
					{"name":"atomicNameInData","value":"atomicValue"},
					{"name":"groupNameInData2","children":
					[{"name":"atomicNameInData2","value":"atomicValue2"}]}
					]}""";
		converter = createRecordGroupConverterForJsonString(json);

		dataRecordGroup = (ClientDataRecordGroup) converter.toInstance();

		assertEquals(dataRecordGroup.getNameInData(), "groupNameInData");
		Iterator<ClientDataChild> iterator = dataRecordGroup.getChildren().iterator();
		BasicClientDataAtomic child = (BasicClientDataAtomic) iterator.next();
		assertEquals(child.getNameInData(), "atomicNameInData");
		assertEquals(child.getValue(), "atomicValue");
		ClientDataGroup child2 = (ClientDataGroup) iterator.next();
		assertEquals(child2.getNameInData(), "groupNameInData2");
		BasicClientDataAtomic subChild = (BasicClientDataAtomic) child2.getChildren().iterator()
				.next();
		assertEquals(subChild.getNameInData(), "atomicNameInData2");
		assertEquals(subChild.getValue(), "atomicValue2");
	}

	@Test
	public void testToClassGroupWithAttributesAndAtomicChildAndGroupChildWithAtomicChild() {
		String json = """
				{"name":"groupNameInData",	"attributes":{"attributeNameInData":"attributeValue",
				"attributeNameInData2":"attributeValue2"},"children":[
					{"name":"atomicNameInData","value":"atomicValue"},{"name":"groupNameInData2",
					"attributes":{"g2AttributeNameInData":"g2AttributeValue"},
					"children":[{"name":"atomicNameInData2","value":"atomicValue2"}]}
				]}""";
		converter = createRecordGroupConverterForJsonString(json);

		dataRecordGroup = (ClientDataRecordGroup) converter.toInstance();

		assertEquals(dataRecordGroup.getNameInData(), "groupNameInData");

		String attributeValue2 = dataRecordGroup.getAttribute("attributeNameInData").getValue();
		assertEquals(attributeValue2, "attributeValue");

		Iterator<ClientDataChild> iterator = dataRecordGroup.getChildren().iterator();
		BasicClientDataAtomic child = (BasicClientDataAtomic) iterator.next();
		assertEquals(child.getNameInData(), "atomicNameInData");
		assertEquals(child.getValue(), "atomicValue");
		ClientDataGroup child2 = (ClientDataGroup) iterator.next();
		assertEquals(child2.getNameInData(), "groupNameInData2");
		BasicClientDataAtomic subChild = (BasicClientDataAtomic) child2.getChildren().iterator()
				.next();
		assertEquals(subChild.getNameInData(), "atomicNameInData2");
		assertEquals(subChild.getValue(), "atomicValue2");

		String attributeValue = child2.getAttribute("g2AttributeNameInData").getValue();
		assertEquals(attributeValue, "g2AttributeValue");
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonTopLevelNoName() {
		String json = "{\"children\":[],\"extra\":{\"id2\":\"value2\"}}";
		converter = createRecordGroupConverterForJsonString(json);

		converter.toInstance();
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonTopLevelNoChildren() {
		String json = "{\"name\":\"id\",\"attributes\":{}}";
		converter = createRecordGroupConverterForJsonString(json);

		converter.toInstance();
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonKeyTopLevel() {
		String json = "{\"name\":\"id\",\"children\":[],\"extra\":{\"id2\":\"value2\"}}";
		converter = createRecordGroupConverterForJsonString(json);

		converter.toInstance();
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonKeyTopLevelWithAttributes() {
		String json = "{\"name\":\"id\",\"children\":[], \"attributes\":{},\"extra\":{\"id2\":\"value2\"}}";
		converter = createRecordGroupConverterForJsonString(json);

		converter.toInstance();
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonAttributesIsGroup() {
		String json = "{\"name\":\"groupNameInData\", \"attributes\":{\"attributeNameInData\":\"attributeValue\",\"bla\":{} }}";
		converter = createRecordGroupConverterForJsonString(json);

		converter.toInstance();
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonTwoAttributes() {
		String json = "{\"name\":\"groupNameInData\",\"children\":[],\"attributes\":{\"attributeNameInData\":\"attributeValue\"}"
				+ ",\"attributes\":{\"attributeNameInData2\":\"attributeValue2\"}}";
		converter = createRecordGroupConverterForJsonString(json);

		converter.toInstance();
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonOneAttributesIsArray() {
		String json = "{\"name\":\"groupNameInData\",\"children\":[],\"attributes\":{\"attributeNameInData\":\"attributeValue\",\"bla\":[true] }}";
		converter = createRecordGroupConverterForJsonString(json);

		converter.toInstance();
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonAttributesIsArray() {
		String json = "{\"name\":\"groupNameInData\",\"children\":[],\"attributes\":[{\"attributeNameInData\":\"attributeValue\"}]}";
		converter = createRecordGroupConverterForJsonString(json);

		converter.toInstance();
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonOneChildIsArray() {
		String json = "{\"name\":\"groupNameInData\",\"children\":[{\"atomicNameInData\":\"atomicValue\"},[]]}";
		converter = createRecordGroupConverterForJsonString(json);

		converter.toInstance();
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonOneChildIsString() {
		String json = "{\"name\":\"groupNameInData\",\"children\":[{\"atomicNameInData\":\"atomicValue\"},\"string\"]}";
		converter = createRecordGroupConverterForJsonString(json);

		converter.toInstance();
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonChildrenIsNotCorrectObject() {
		String json = "{\"name\":\"groupNameInData\",\"children\":[{\"atomicNameInData\":\"atomicValue\""
				+ ",\"atomicNameInData2\":\"atomicValue2\"}]}";
		converter = createRecordGroupConverterForJsonString(json);

		converter.toInstance();
	}
}
