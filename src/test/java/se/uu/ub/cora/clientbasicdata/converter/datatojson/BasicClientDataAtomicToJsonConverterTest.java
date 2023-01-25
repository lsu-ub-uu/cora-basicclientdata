/*
 * Copyright 2015, 2019 Uppsala University Library
 * Copyright 2016 Olov McKie
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

package se.uu.ub.cora.clientbasicdata.converter.datatojson;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataAtomic;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverter;
import se.uu.ub.cora.json.builder.org.OrgJsonBuilderFactoryAdapter;

public class BasicClientDataAtomicToJsonConverterTest {
	private BasicClientDataAtomic dataAtomic;
	private ClientDataToJsonConverter converter;

	@BeforeMethod
	public void beforeMethod() {
		dataAtomic = BasicClientDataAtomic.withNameInDataAndValue("atomicNameInData",
				"atomicValue");
		OrgJsonBuilderFactoryAdapter factory = new OrgJsonBuilderFactoryAdapter();
		converter = BasicClientDataAtomicToJsonConverter.usingJsonBuilderFactoryAndDataAtomic(factory,
				dataAtomic);
	}

	@Test
	public void testToJson() {
		String json = converter.toJson();

		String expectedJson = "{\n";
		expectedJson += "    \"name\": \"atomicNameInData\",\n";
		expectedJson += "    \"value\": \"atomicValue\"\n";
		expectedJson += "}";
		assertEquals(json, expectedJson);
	}

	@Test
	public void testToJsonWithRepeatId() {
		dataAtomic.setRepeatId("2");
		String json = converter.toJson();
		String expectedJson = "{\n";
		expectedJson += "    \"repeatId\": \"2\",\n";
		expectedJson += "    \"name\": \"atomicNameInData\",\n";
		expectedJson += "    \"value\": \"atomicValue\"\n";
		expectedJson += "}";
		assertEquals(json, expectedJson);
	}

	@Test
	public void testToJsonWithEmptyRepeatId() {
		dataAtomic.setRepeatId("");
		String json = converter.toJson();

		String expectedJson = "{\n";
		expectedJson += "    \"name\": \"atomicNameInData\",\n";
		expectedJson += "    \"value\": \"atomicValue\"\n";
		expectedJson += "}";
		assertEquals(json, expectedJson);
	}

	@Test
	public void testToJsonEmptyValue() {
		BasicClientDataAtomic dataAtomic = BasicClientDataAtomic
				.withNameInDataAndValue("atomicNameInData", "");
		OrgJsonBuilderFactoryAdapter factory = new OrgJsonBuilderFactoryAdapter();
		converter = BasicClientDataAtomicToJsonConverter.usingJsonBuilderFactoryAndDataAtomic(factory,
				dataAtomic);
		String json = converter.toJson();

		String expectedJson = "{\n";
		expectedJson += "    \"name\": \"atomicNameInData\",\n";
		expectedJson += "    \"value\": \"\"\n";
		expectedJson += "}";
		assertEquals(json, expectedJson);
	}

	@Test
	public void testToJsonCompactFormat() {
		String json = converter.toJsonCompactFormat();

		String expectedJson = "{\"name\":\"atomicNameInData\",\"value\":\"atomicValue\"}";
		assertEquals(json, expectedJson);
	}

	@Test
	public void testToJsonOneAttribute() {
		dataAtomic.addAttributeByIdWithValue("type", "container");
		String json = converter.toJson();

		String expectedJson = "{\n" + "    \"name\": \"atomicNameInData\",\n"
				+ "    \"attributes\": {\"type\": \"container\"},\n"
				+ "    \"value\": \"atomicValue\"\n" + "}";
		assertEquals(json, expectedJson);
	}

	@Test
	public void testToJsonTwoAttributes() {
		dataAtomic.addAttributeByIdWithValue("type", "container");
		dataAtomic.addAttributeByIdWithValue("repeat", "children");
		String json = converter.toJson();

		String expectedJson = "{\n" + "    \"name\": \"atomicNameInData\",\n"
				+ "    \"attributes\": {\n" + "        \"repeat\": \"children\",\n"
				+ "        \"type\": \"container\"\n" + "    },\n"
				+ "    \"value\": \"atomicValue\"\n" + "}";
		assertEquals(json, expectedJson);
	}

	@Test
	public void testToJsonTwoAttributesCompactFormat() {
		dataAtomic.addAttributeByIdWithValue("type", "container");
		dataAtomic.addAttributeByIdWithValue("repeat", "children");
		String json = converter.toJsonCompactFormat();

		String expectedJson = "{\"name\":\"atomicNameInData\",\"attributes\":{\"repeat\":\"children\",\"type\":\"container\"},\"value\":\"atomicValue\"}";
		assertEquals(json, expectedJson);
	}
}
