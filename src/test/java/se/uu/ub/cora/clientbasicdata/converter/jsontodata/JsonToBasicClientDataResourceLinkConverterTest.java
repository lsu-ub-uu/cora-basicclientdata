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

import org.testng.annotations.Test;

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataResourceLink;
import se.uu.ub.cora.clientdata.ClientDataLink;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonValue;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;

public class JsonToBasicClientDataResourceLinkConverterTest {

	@Test
	public void testToInstance() {
		String json = "{\"children\":[{\"name\":\"streamId\",\"value\":\"aStreamId\"},{\"name\":\"filename\",\"value\":\"aFilename\"},{\"name\":\"filesize\",\"value\":\"12345\"},{\"name\":\"mimeType\",\"value\":\"application/png\"}],\"name\":\"someResourceLink\"}";
		BasicClientDataResourceLink resourceLink = (BasicClientDataResourceLink) getConverterdLink(
				json);
		assertEquals(resourceLink.getNameInData(), "someResourceLink");
		assertEquals(resourceLink.getFirstAtomicValueWithNameInData("streamId"), "aStreamId");
		assertEquals(resourceLink.getFirstAtomicValueWithNameInData("filename"), "aFilename");
		assertEquals(resourceLink.getFirstAtomicValueWithNameInData("filesize"), "12345");
		assertEquals(resourceLink.getFirstAtomicValueWithNameInData("mimeType"), "application/png");
	}

	private ClientDataLink getConverterdLink(String json) {
		OrgJsonParser jsonParser = new OrgJsonParser();
		JsonValue jsonValue = jsonParser.parseString(json);
		JsonToBasicClientDataResourceLinkConverter converter = JsonToBasicClientDataResourceLinkConverter
				.forJsonObject((JsonObject) jsonValue);

		ClientDataLink dataLink = (ClientDataLink) converter.toInstance();
		return dataLink;
	}

	@Test
	public void testToClassWithRepeatId() {
		String json = "{\"children\":[{\"name\":\"streamId\",\"value\":\"aStreamId\"},{\"name\":\"filename\",\"value\":\"aFilename\"},{\"name\":\"filesize\",\"value\":\"12345\"},{\"name\":\"mimeType\",\"value\":\"application/png\"}],\"repeatId\":\"0\",\"name\":\"someResourceLink\"}";
		ClientDataLink dataLink = getConverterdLink(json);
		assertEquals(dataLink.getNameInData(), "someResourceLink");
		assertEquals(dataLink.getRepeatId(), "0");
	}

	@Test
	public void testToClassWithAttribute() {
		String json = "{\"children\":[{\"name\":\"streamId\",\"value\":\"aStreamId\"},{\"name\":\"filename\",\"value\":\"aFilename\"},{\"name\":\"filesize\",\"value\":\"12345\"},{\"name\":\"mimeType\",\"value\":\"application/png\"}],\"attributes\":{\"type\":\"someType\"},\"name\":\"someResourceLink\"}";
		ClientDataLink dataLink = getConverterdLink(json);

		assertEquals(dataLink.getNameInData(), "someResourceLink");
		String attributeValue = dataLink.getAttribute("type").getValue();
		assertEquals(attributeValue, "someType");
	}

	@Test
	public void testToClassWithRepeatIdAndAttribute() {
		String json = "{\"children\":[{\"name\":\"streamId\",\"value\":\"aStreamId\"},{\"name\":\"filename\",\"value\":\"aFilename\"},{\"name\":\"filesize\",\"value\":\"12345\"},{\"name\":\"mimeType\",\"value\":\"application/png\"}],\"repeatId\":\"0\",\"attributes\":{\"type\":\"someType\"},\"name\":\"someResourceLink\"}";
		ClientDataLink dataLink = getConverterdLink(json);

		assertEquals(dataLink.getNameInData(), "someResourceLink");
		String attributeValue = dataLink.getAttribute("type").getValue();
		assertEquals(attributeValue, "someType");
		assertEquals(dataLink.getRepeatId(), "0");
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonObject: Group data can only contain keys: name, children and attributes")
	public void testToClassWithRepeatIdAndAttributeAndExtra() {
		String json = "{\"children\":[{\"name\":\"streamId\",\"value\":\"aStreamId\"},{\"name\":\"filename\",\"value\":\"aFilename\"},{\"name\":\"filesize\",\"value\":\"12345\"},{\"name\":\"mimeType\",\"value\":\"application/png\"}],\"repeatId\":\"0\",\"attributes\":{\"type\":\"someType\"}, \"extra\":\"extraValue\", \"name\":\"someResourceLink\"}";
		getConverterdLink(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonObject: Group data must contain name and children, and may contain attributes or repeatId")
	public void testToClassWithIncorrectAttributeNameInData() {
		String json = "{\"children\":[{\"name\":\"streamId\",\"value\":\"aStreamId\"},{\"name\":\"filename\",\"value\":\"aFilename\"},{\"name\":\"filesize\",\"value\":\"12345\"},{\"name\":\"mimeType\",\"value\":\"application/png\"}],\"NOTattributes\":{\"type\":\"someType\"},\"name\":\"someResourceLink\"}";
		getConverterdLink(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "ResourceLinkData must and can only contain children with name streamId and filename and filesize and mimeType")
	public void testToClassWithNoStreamId() {
		String json = "{\"children\":[{\"name\":\"filename\",\"value\":\"aFilename\"},{\"name\":\"filesize\",\"value\":\"12345\"},{\"name\":\"mimeType\",\"value\":\"application/png\"}],\"name\":\"someResourceLink\"}";
		getConverterdLink(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "ResourceLinkData must and can only contain children with name streamId and filename and filesize and mimeType")
	public void testToClassWithNoStreamIdButOtherChild() {
		String json = "{\"children\":[{\"name\":\"NOTstreamId\",\"value\":\"aStreamId\"},{\"name\":\"filename\",\"value\":\"aFilename\"},{\"name\":\"filesize\",\"value\":\"12345\"},{\"name\":\"mimeType\",\"value\":\"application/png\"}],\"name\":\"someResourceLink\"}";
		getConverterdLink(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "ResourceLinkData must and can only contain children with name streamId and filename and filesize and mimeType")
	public void testToClassWithNoFilenameButOtherChild() {
		String json = "{\"children\":[{\"name\":\"streamId\",\"value\":\"aStreamId\"},{\"name\":\"NOTfilename\",\"value\":\"aFilename\"},{\"name\":\"filesize\",\"value\":\"12345\"},{\"name\":\"mimeType\",\"value\":\"application/png\"}],\"name\":\"someResourceLink\"}";
		getConverterdLink(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "ResourceLinkData must and can only contain children with name streamId and filename and filesize and mimeType")
	public void testToClassWithNoFilesizeButOtherChild() {
		String json = "{\"children\":[{\"name\":\"streamId\",\"value\":\"aStreamId\"},{\"name\":\"filename\",\"value\":\"aFilename\"},{\"name\":\"NOTfilesize\",\"value\":\"12345\"},{\"name\":\"mimeType\",\"value\":\"application/png\"}],\"name\":\"someResourceLink\"}";
		getConverterdLink(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "ResourceLinkData must and can only contain children with name streamId and filename and filesize and mimeType")
	public void testToClassWithNoMimeTypeButOtherChild() {
		String json = "{\"children\":[{\"name\":\"streamId\",\"value\":\"aStreamId\"},{\"name\":\"filename\",\"value\":\"aFilename\"},{\"name\":\"filesize\",\"value\":\"12345\"},{\"name\":\"NOTmimeType\",\"value\":\"application/png\"}],\"name\":\"someResourceLink\"}";
		getConverterdLink(json);
	}
}
