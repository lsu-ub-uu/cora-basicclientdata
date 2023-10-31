/*
 * Copyright 2019, 2023 Uppsala University Library
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

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataResourceLink;
import se.uu.ub.cora.clientdata.ClientDataLink;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonValue;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;

public class JsonToBasicClientDataResourceLinkConverterTest {

	@Test
	public void testToInstance() {
		String json = """
				{
				  "name": "master",
				  "mimeType": "application/vnd.uub.record+json"
				}
				""";
		BasicClientDataResourceLink resourceLink = (BasicClientDataResourceLink) getConverterdLink(
				json);

		assertEquals(resourceLink.getNameInData(), "master");
		assertEquals(resourceLink.getMimeType(), "application/vnd.uub.record+json");
	}

	@Test
	public void testToInstanceWithActionLink() {
		String json = """
				{
					"actionLinks": {
						"read": {
							"requestMethod": "GET",
							"rel": "read",
							"url": "http://localhost:38080/systemone/rest/record/binary/binary:14826085103360/master",
							"accept": "image/jpeg"
						}
					},
					"name": "master",
					"mimeType": "image/jpeg"
				}
				""";
		BasicClientDataResourceLink resourceLink = (BasicClientDataResourceLink) getConverterdLink(
				json);

		assertEquals(resourceLink.getNameInData(), "master");
		assertEquals(resourceLink.getMimeType(), "image/jpeg");
		assertTrue(resourceLink.hasReadAction());
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
		String json = """
				{
				  "name": "master",
				  "mimeType": "application/vnd.uub.record+json",
				  "repeatId":"0"
				}
				""";

		ClientDataLink dataLink = getConverterdLink(json);

		assertEquals(dataLink.getNameInData(), "master");
		assertEquals(dataLink.getRepeatId(), "0");
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonObject: ResourceLink must "
			+ "contain name, mimeType and may contain actionLinks and/or repeatId.")
	public void testExceptionMimeTypeNotExist() {
		String json = """
				{
				  "name": "master",
				  "repeatId":"0"
				}
				""";
		getConverterdLink(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonObject: ResourceLink must "
			+ "contain name, mimeType and may contain actionLinks and/or repeatId.")
	public void testExceptionNameNotExist() {
		String json = """
				{
				  "mimeType": "application/vnd.uub.record+json",
				  "repeatId":"0"
				}
				""";
		getConverterdLink(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonObject: ResourceLink must "
			+ "contain name, mimeType and may contain actionLinks and/or repeatId.")
	public void testExceptionIfTooManyFields() {
		String json = """
				{
				  "name": "master",
				  "mimeType": "application/vnd.uub.record+json",
				  "repeatId":"0",
				  "someOther": "someOther",
				  "someOther2": "someOther"
				}
				""";
		getConverterdLink(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonObject: ResourceLink must "
			+ "contain name, mimeType and may contain actionLinks and/or repeatId.")
	public void testExceptionIfTooManyFields2() {
		String json = """
				{
				  "name": "master",
				  "mimeType": "application/vnd.uub.record+json",
				  "someOther": "someOther"
				}
				""";
		getConverterdLink(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonObject: ResourceLink must "
			+ "contain name, mimeType and may contain actionLinks and/or repeatId.")
	public void testExceptionIfTooManyFields3() {
		String json = """
				{
				  "nameSpecial": "master",
				  "mimeType": "application/vnd.uub.record+json",
				  "someOther": "someOther"
				}
				""";
		getConverterdLink(json);
	}
}
