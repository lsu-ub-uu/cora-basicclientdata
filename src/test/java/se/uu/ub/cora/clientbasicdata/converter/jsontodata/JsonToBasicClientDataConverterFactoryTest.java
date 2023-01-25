/*
 * Copyright 2015 Uppsala University Library
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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.converter.JsonToDataConverter;
import se.uu.ub.cora.clientdata.converter.JsonToDataConverterFactory;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonParser;
import se.uu.ub.cora.json.parser.JsonValue;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;

public class JsonToBasicClientDataConverterFactoryTest {
	private JsonToDataConverterFactory jsonToDataConverterFactory;
	private JsonParser jsonParser;

	@BeforeMethod
	public void beforeMethod() {
		jsonToDataConverterFactory = new JsonToBasicClientDataConverterFactoryImp();
		jsonParser = new OrgJsonParser();
	}

	@Test
	public void testFactorOnJsonStringDataGroupEmptyChildren() {
		String json = "{\"name\":\"groupNameInData\", \"children\":[]}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
	}

	@Test
	public void testFactorOnJsonStringDataGroupAtomicChild() {
		String json = "{\"name\":\"id\",\"children\":[{\"name\":\"someNameInData\",\"value\":\"id2\"}]}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
	}

	@Test
	public void testFactorOnJsonStringOnlyRecordTypeFactorsGroupConverter() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"recordType\"},{\"name\":\"NOTlinkedRecordId\",\"value\":\"place\"}],\"name\":\"type\"}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
	}

	@Test
	public void testFactorOnJsonStringOnlyRecordIdFactorsGroupConverter() {
		String json = "{\"children\":[{\"name\":\"NOTlinkedRecordType\",\"value\":\"recordType\"},{\"name\":\"linkedRecordId\",\"value\":\"place\"}],\"name\":\"type\"}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
	}

	@Test
	public void testFactorOnJsonStringNoLinkedPathFactorsDataRecordLink() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"recordType\"},{\"name\":\"linkedRecordId\",\"value\":\"place\"}],\"name\":\"type\"}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataRecordLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringWithLinkedPathFactorsDataRecordLink() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"coraText\"},{\"name\":\"linkedRecordId\",\"value\":\"exampleGroupText\"},{\"children\":[{\"name\":\"nameInData\",\"value\":\"recordInfo\"},{\"children\":[{\"name\":\"nameInData\",\"value\":\"type\"}],\"name\":\"linkedPath\"}],\"name\":\"linkedPath\"}],\"name\":\"from\"}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataRecordLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringWithLinkedRepeatIdFactorsDataRecordLink() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"coraText\"},{\"name\":\"linkedRecordId\",\"value\":\"exampleGroupText\"},{\"name\":\"linkedRepeatId\",\"value\":\"one\"}],\"name\":\"from\"}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataRecordLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringWithLinkedRepeatIdAndLinkedPathFactorsDataRecordLink() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"coraText\"},{\"name\":\"linkedRecordId\",\"value\":\"exampleGroupText\"},{\"name\":\"linkedRepeatId\",\"value\":\"one\"},{\"children\":[{\"name\":\"nameInData\",\"value\":\"recordInfo\"},{\"children\":[{\"name\":\"nameInData\",\"value\":\"type\"}],\"name\":\"linkedPath\"}],\"name\":\"linkedPath\"}],\"name\":\"from\"}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataRecordLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringWithLinkedPathButNotLinkedRecordTypeFactorsDataGroup() {
		String json = "{\"children\":[{\"name\":\"NOTlinkedRecordType\",\"value\":\"coraText\"},{\"name\":\"linkedRecordId\",\"value\":\"exampleGroupText\"},{\"children\":[{\"name\":\"nameInData\",\"value\":\"recordInfo\"},{\"children\":[{\"name\":\"nameInData\",\"value\":\"type\"}],\"name\":\"linkedPath\"}],\"name\":\"linkedPath\"}],\"name\":\"from\"}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
		assertFalse(jsonToDataConverter instanceof JsonToBasicClientDataRecordLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringWithLinkedPathButNotLinkedRecordIdFactorsDataGroup() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"coraText\"},{\"name\":\"NOTlinkedRecordId\",\"value\":\"exampleGroupText\"},{\"children\":[{\"name\":\"nameInData\",\"value\":\"recordInfo\"},{\"children\":[{\"name\":\"nameInData\",\"value\":\"type\"}],\"name\":\"linkedPath\"}],\"name\":\"linkedPath\"}],\"name\":\"from\"}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
		assertFalse(jsonToDataConverter instanceof JsonToBasicClientDataRecordLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringWithThreeChildrenButNoLinkedRepatIdAndNoLinkedPathFactorsDataGroup() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"coraText\"},{\"name\":\"linkedRecordId\",\"value\":\"exampleGroupText\"},{\"children\":[{\"name\":\"nameInData\",\"value\":\"recordInfo\"},{\"children\":[{\"name\":\"nameInData\",\"value\":\"type\"}],\"name\":\"linkedPath\"}],\"name\":\"NOTlinkedPath\"}],\"name\":\"from\"}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
		assertFalse(jsonToDataConverter instanceof JsonToBasicClientDataRecordLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringWithMaxNumOfChildrenBUtNOLinkedRepeatIdFactorsDataGroup() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"coraText\"},{\"name\":\"linkedRecordId\",\"value\":\"exampleGroupText\"},{\"name\":\"NOTlinkedRepeatId\",\"value\":\"one\"},{\"children\":[{\"name\":\"nameInData\",\"value\":\"recordInfo\"},{\"children\":[{\"name\":\"nameInData\",\"value\":\"type\"}],\"name\":\"linkedPath\"}],\"name\":\"linkedPath\"}],\"name\":\"from\"}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
		assertFalse(jsonToDataConverter instanceof JsonToBasicClientDataRecordLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringWithMaxNumOfChildrenBUtNOLinkedPathIdFactorsDataGroup() {
		String json = "{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"coraText\"},{\"name\":\"linkedRecordId\",\"value\":\"exampleGroupText\"},{\"name\":\"linkedRepeatId\",\"value\":\"one\"},{\"children\":[{\"name\":\"nameInData\",\"value\":\"recordInfo\"},{\"children\":[{\"name\":\"nameInData\",\"value\":\"type\"}],\"name\":\"linkedPath\"}],\"name\":\"NOTlinkedPath\"}],\"name\":\"from\"}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
		assertFalse(jsonToDataConverter instanceof JsonToBasicClientDataRecordLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringOnlyStreamIdFactorsGroupConverter() {
		String json = "{\"children\":[{\"name\":\"streamId\",\"value\":\"soundBinary:18269669168741\"},{\"name\":\"NOTfilename\",\"value\":\"adele.png\"},{\"name\":\"NOTfilesize\",\"value\":\"8\"},{\"name\":\"NOTmimeType\",\"value\":\"application/octet-stream\"}],\"name\":\"master\"}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
		assertFalse(jsonToDataConverter instanceof JsonToBasicClientDataResourceLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringNOTStreamIdFactorsGroupConverter() {
		String json = "{\"children\":[{\"name\":\"NOTstreamId\",\"value\":\"soundBinary:18269669168741\"},{\"name\":\"NOTfilename\",\"value\":\"adele.png\"},{\"name\":\"NOTfilesize\",\"value\":\"8\"},{\"name\":\"NOTmimeType\",\"value\":\"application/octet-stream\"}],\"name\":\"master\"}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
		assertFalse(jsonToDataConverter instanceof JsonToBasicClientDataResourceLinkConverter);
	}

	@Test
	public void testFactorOnlyStreamIdAndFileNameFactorsGroupConverter() {
		String json = "{\"children\":[{\"name\":\"streamId\",\"value\":\"soundBinary:18269669168741\"},{\"name\":\"filename\",\"value\":\"adele.png\"},{\"name\":\"NOTfilesize\",\"value\":\"8\"},{\"name\":\"NOTmimeType\",\"value\":\"application/octet-stream\"}],\"name\":\"master\"}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
		assertFalse(jsonToDataConverter instanceof JsonToBasicClientDataResourceLinkConverter);
	}

	@Test
	public void testFactorOnlyStreamIdAndFileNameAndFileSizeFactorsGroupConverter() {
		String json = "{\"children\":[{\"name\":\"streamId\",\"value\":\"soundBinary:18269669168741\"},{\"name\":\"filename\",\"value\":\"adele.png\"},{\"name\":\"filesize\",\"value\":\"8\"},{\"name\":\"NOTmimeType\",\"value\":\"application/octet-stream\"}],\"name\":\"master\"}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataGroupConverter);
		assertFalse(jsonToDataConverter instanceof JsonToBasicClientDataResourceLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringCompleteSetupFactorsDataResourceLink() {
		String json = "{\"children\":[{\"name\":\"streamId\",\"value\":\"soundBinary:18269669168741\"},{\"name\":\"filename\",\"value\":\"adele.png\"},{\"name\":\"filesize\",\"value\":\"8\"},{\"name\":\"mimeType\",\"value\":\"application/octet-stream\"}],\"name\":\"master\"}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataResourceLinkConverter);
	}

	@Test
	public void testFactorOnJsonStringDataAtomic() {
		String json = "{\"name\":\"atomicNameInData\",\"value\":\"atomicValue\"}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataAtomicConverter);
	}

	@Test
	public void testFactorOnJsonStringDataAttribute() {
		String json = "{\"attributeNameInData\":\"attributeValue\"}";
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToDataConverter jsonToDataConverter = jsonToDataConverterFactory
				.createForJsonObject(jsonValue);
		assertTrue(jsonToDataConverter instanceof JsonToBasicClientDataAttributeConverter);
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testFactorOnJsonObjectNullJson() {
		jsonToDataConverterFactory.createForJsonObject(null);
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void testClassCreatorGroupNotAGroup() {
		String json = "[{\"id\":{\"id2\":\"value\"}}]";
		JsonValue jsonValue = jsonParser.parseString(json);
		jsonToDataConverterFactory.createForJsonObject(jsonValue);
	}

}