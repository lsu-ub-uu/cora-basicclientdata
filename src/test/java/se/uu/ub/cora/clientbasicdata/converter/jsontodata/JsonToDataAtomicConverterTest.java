/*
 * Copyright 2015, 2019, 2022 Uppsala University Library
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

import org.testng.annotations.Test;

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataAtomic;
import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonValue;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;

public class JsonToDataAtomicConverterTest {

	@Test
	public void testToClass() {
		String json = "{\"name\":\"atomicNameInData\",\"value\":\"atomicValue\"}";
		BasicClientDataAtomic dataAtomic = createDataAtomicForJsonString(json);
		assertEquals(dataAtomic.getNameInData(), "atomicNameInData");
		assertEquals(dataAtomic.getValue(), "atomicValue");
	}

	private BasicClientDataAtomic createDataAtomicForJsonString(String json) {
		OrgJsonParser jsonParser = new OrgJsonParser();
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataAtomicConverter jsonToDataConverter = JsonToDataAtomicConverter
				.forJsonObject((JsonObject) jsonValue);
		ClientConvertible dataPart = jsonToDataConverter.toInstance();

		BasicClientDataAtomic dataAtomic = (BasicClientDataAtomic) dataPart;
		return dataAtomic;
	}

	@Test
	public void testToClassWithRepeatId() {
		String json = "{\"name\":\"atomicNameInData\",\"value\":\"atomicValue\",\"repeatId\":\"5\"}";
		BasicClientDataAtomic dataAtomic = createDataAtomicForJsonString(json);
		assertEquals(dataAtomic.getNameInData(), "atomicNameInData");
		assertEquals(dataAtomic.getValue(), "atomicValue");
		assertEquals(dataAtomic.getRepeatId(), "5");
	}

	@Test
	public void testToClassEmptyValue() {
		String json = "{\"name\":\"atomicNameInData\",\"value\":\"\"}";
		BasicClientDataAtomic dataAtomic = createDataAtomicForJsonString(json);
		assertEquals(dataAtomic.getNameInData(), "atomicNameInData");
		assertEquals(dataAtomic.getValue(), "");
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonValueIsNotString() {
		String json = "{\"name\":\"id\",\"value\":[]}";
		createDataAtomicForJsonString(json);
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonNameIsNotString() {
		String json = "{\"name\":{},\"value\":\"atomicValue\"}";
		createDataAtomicForJsonString(json);
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonNotName() {
		String json = "{\"nameNOT\":\"id\",\"value\":\"atomicValue\"}";
		createDataAtomicForJsonString(json);
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonMissingValue() {
		String json = "{\"name\":\"id\",\"valueNOT\":\"atomicValue\"}";
		createDataAtomicForJsonString(json);
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonExtraKey() {
		String json = "{\"name\":\"id\",\"value\":\"atomicValue\",\"repeatId\":\"5\""
				+ ",\"extra\":\"extra\", \"extra2\":\"extra\"}";
		createDataAtomicForJsonString(json);
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonOneExtraKeyMissingRepeatIdAndMissingAttributes() {
		String json = "{\"name\":\"id\",\"value\":\"atomicValue\",\"NOTrepeatId\":\"5\"}";
		createDataAtomicForJsonString(json);
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonTwoExtraKeyMissingRepeatIdAndMissingAttributes() {
		String json = "{\"name\":\"id\",\"value\":\"atomicValue\",\"NOTrepeatId\":\"5\",\"NOTattributes\":\"5\"}";
		createDataAtomicForJsonString(json);
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonMaxKeysMissingAttributes() {
		String json = "{\"name\":\"id\",\"value\":\"atomicValue\",\"repeatId\":\"5\",\"NOTattributes\":\"5\"}";
		createDataAtomicForJsonString(json);
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonExtraKeyValuePair() {
		String json = "{\"name\":\"atomicNameInData\",\"value\":\"atomicValue\","
				+ "\"name\":\"id2\",\"value\":\"value2\"}";
		createDataAtomicForJsonString(json);
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testToClassWrongJsonExtraArray() {
		String json = "{\"name\":\"atomicNameInData\",\"value\":\"atomicValue\","
				+ "\"name\":\"id2\",\"value\":[]}";
		createDataAtomicForJsonString(json);
	}

	@Test
	public void testWithOneAttribute() {
		String json = "{\"name\":\"mode\",\"value\":\"output\",\"attributes\":{\"type\":\"container\"}}";
		BasicClientDataAtomic dataAtomic = createDataAtomicForJsonString(json);
		assertEquals(dataAtomic.getNameInData(), "mode");
		assertEquals(dataAtomic.getValue(), "output");
		assertEquals(dataAtomic.getAttributes().size(), 1);
		assertEquals(dataAtomic.getAttribute("type").getValue(), "container");
	}

	@Test
	public void testWithTwoAttributesAndRepeatId() {
		String json = "{\"name\":\"mode\",\"value\":\"output\",\"attributes\":{\"type\":\"container\",\"repeat\":\"children\"},\"repeatId\":\"0\"}";
		BasicClientDataAtomic dataAtomic = createDataAtomicForJsonString(json);
		assertEquals(dataAtomic.getNameInData(), "mode");
		assertEquals(dataAtomic.getValue(), "output");
		assertEquals(dataAtomic.getRepeatId(), "0");
		assertEquals(dataAtomic.getAttributes().size(), 2);
		assertEquals(dataAtomic.getAttribute("type").getValue(), "container");
		assertEquals(dataAtomic.getAttribute("repeat").getValue(), "children");
	}

}
