/*
 * Copyright 2021, 2023, 2025 Uppsala University Library
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
import static org.testng.Assert.assertSame;

import java.text.MessageFormat;
import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterFactory;
import se.uu.ub.cora.clientdata.spies.ClientDataResourceLinkSpy;
import se.uu.ub.cora.json.builder.org.OrgJsonBuilderFactoryAdapter;

public class BasicClientDataResourceLinkToJsonConverterTest {

	BasicClientDataResourceLinkToJsonConverter resourceLinkToJsonConverter;
	ClientDataToJsonConverterFactory converterFactory;
	BasicClientJsonBuilderFactorySpy jsonBuilderFactorySpy;
	Optional<String> baseUrl;
	private ClientDataResourceLinkSpy dataResourceLink;
	private BasicClientDataResourceLinkToJsonConverter converter;

	@BeforeMethod
	public void beforeMethod() {
		dataResourceLink = new ClientDataResourceLinkSpy();
		jsonBuilderFactorySpy = new BasicClientJsonBuilderFactorySpy();
		converterFactory = new BasicClientDataToJsonConverterFactorySpy();

		constructWithRecordUrl();
	}

	private void constructWithRecordUrl() {
		dataResourceLink.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> "master");
		dataResourceLink.MRV.setDefaultReturnValuesSupplier("getType", () -> "someType");
		dataResourceLink.MRV.setDefaultReturnValuesSupplier("getId", () -> "someId");
		dataResourceLink.MRV.setDefaultReturnValuesSupplier("getMimeType", () -> "image/png");

		baseUrl = Optional.of("https://somesystem.org/rest/records/");
		converter = BasicClientDataResourceLinkToJsonConverter
				.usingConverterFactoryJsonBuilderFactoryAndDataResourceLinkAndRecordUrl(
						converterFactory, new OrgJsonBuilderFactoryAdapter(), dataResourceLink,
						baseUrl);
	}

	@Test
	public void testToJson() {
		String json = converter.toJson();
		String jsonCompacted = converter.toJsonCompactFormat();

		String expectedJson = """
				{
				    "children": [
				        {
				            "name": "linkedRecordType",
				            "value": "someType"
				        },
				        {
				            "name": "linkedRecordId",
				            "value": "someId"
				        },
				        {
				            "name": "mimeType",
				            "value": "image/png"
				        }
				    ],
				    "name": "master"
				}""";
		assertEquals(json, expectedJson);
		assertEquals(jsonCompacted, toCompactFormat(expectedJson));
	}

	private String toCompactFormat(String json) {
		return json.replaceAll("\\s+", "");

	}

	@Test
	public void testToJson2() {
		dataResourceLink.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> "thumbnail");
		dataResourceLink.MRV.setDefaultReturnValuesSupplier("getMimeType", () -> "image/jpeg");

		String json = converter.toJson();
		String jsonCompacted = converter.toJsonCompactFormat();

		String expectedJson = """
				{
				    "children": [
				        {
				            "name": "linkedRecordType",
				            "value": "someType"
				        },
				        {
				            "name": "linkedRecordId",
				            "value": "someId"
				        },
				        {
				            "name": "mimeType",
				            "value": "image/jpeg"
				        }
				    ],
				    "name": "thumbnail"
				}""";
		assertEquals(json, expectedJson);
		assertEquals(jsonCompacted, toCompactFormat(expectedJson));
	}

	@Test
	public void testJsonWithRepeatId() {
		dataResourceLink.MRV.setDefaultReturnValuesSupplier("hasRepeatId", () -> true);
		dataResourceLink.MRV.setDefaultReturnValuesSupplier("getRepeatId", () -> "1");

		String json = converter.toJson();
		String jsonCompacted = converter.toJsonCompactFormat();

		String expectedJson = """
				{
				    "repeatId": "1",
				    "children": [
				        {
				            "name": "linkedRecordType",
				            "value": "someType"
				        },
				        {
				            "name": "linkedRecordId",
				            "value": "someId"
				        },
				        {
				            "name": "mimeType",
				            "value": "image/png"
				        }
				    ],
				    "name": "master"
				}""";
		assertEquals(json, expectedJson);
		assertEquals(jsonCompacted, toCompactFormat(expectedJson));
	}

	@Test
	public void testConverterFactorySetInParent() {
		constructWithRecordUrl();

		assertSame(converter.onlyForTestGetConverterFactory(), converterFactory);
	}

	@Test
	public void testNoActions() {
		converter = BasicClientDataResourceLinkToJsonConverter
				.usingConverterFactoryJsonBuilderFactoryAndDataResourceLinkAndRecordUrl(
						converterFactory, jsonBuilderFactorySpy, dataResourceLink, baseUrl);

		converter.toJsonObjectBuilder();

		assertJsonBuilderNotUsed();
	}

	private void assertJsonBuilderNotUsed() {
		dataResourceLink.MCR.assertParameters("hasReadAction", 0);

		BasicClientJsonObjectBuilderSpy jsonObjectBuilderSpy = (BasicClientJsonObjectBuilderSpy) jsonBuilderFactorySpy.MCR
				.getReturnValue("createObjectBuilder", 0);

		jsonObjectBuilderSpy.MCR.assertMethodNotCalled("addKeyJsonObjectBuilder");
	}

	@Test
	public void testActionLinksBuilderAddedToMainBuilder() {
		dataResourceLink.MRV.setDefaultReturnValuesSupplier("hasReadAction", () -> true);
		converter = BasicClientDataResourceLinkToJsonConverter
				.usingConverterFactoryJsonBuilderFactoryAndDataResourceLinkAndRecordUrl(
						converterFactory, jsonBuilderFactorySpy, dataResourceLink, baseUrl);

		converter.toJsonObjectBuilder();

		dataResourceLink.MCR.assertParameters("hasReadAction", 0);
		assertActionLinksBuilderAddedToMainBuilder();
	}

	private void assertActionLinksBuilderAddedToMainBuilder() {
		BasicClientJsonObjectBuilderSpy mainBuilderSpy = (BasicClientJsonObjectBuilderSpy) jsonBuilderFactorySpy.MCR
				.getReturnValue("createObjectBuilder", 0);
		BasicClientJsonObjectBuilderSpy actionLinksBuilderSpy = getActionsBuilder();

		mainBuilderSpy.MCR.assertParameters("addKeyJsonObjectBuilder", 0, "actionLinks",
				actionLinksBuilderSpy);
	}

	@Test
	public void testActionAddedToActionBuilder() {
		dataResourceLink.MRV.setDefaultReturnValuesSupplier("hasReadAction", () -> true);
		converter = BasicClientDataResourceLinkToJsonConverter
				.usingConverterFactoryJsonBuilderFactoryAndDataResourceLinkAndRecordUrl(
						converterFactory, jsonBuilderFactorySpy, dataResourceLink, baseUrl);

		converter.toJsonObjectBuilder();

		BasicClientJsonObjectBuilderSpy actionLinksBuilderSpy = getActionsBuilder();
		BasicClientJsonObjectBuilderSpy internalLinkBuilderSpy = getJsonBuilderForActionLinksFields();
		actionLinksBuilderSpy.MCR.assertParameters("addKeyJsonObjectBuilder", 0, "read",
				internalLinkBuilderSpy);

		internalLinkBuilderSpy.MCR.assertParameters("addKeyString", 0, "rel", "read");
		internalLinkBuilderSpy.MCR.assertParameters("addKeyString", 1, "url",
				generateURL("someType", "someId", dataResourceLink.getNameInData()));
		internalLinkBuilderSpy.MCR.assertParameters("addKeyString", 2, "requestMethod", "GET");
		String mimeType = (String) dataResourceLink.MCR.getReturnValue("getMimeType", 0);
		internalLinkBuilderSpy.MCR.assertParameters("addKeyString", 3, "accept", mimeType);
		internalLinkBuilderSpy.MCR.assertNumberOfCallsToMethod("addKeyString", 4);

	}

	private BasicClientJsonObjectBuilderSpy getJsonBuilderForActionLinksFields() {
		return (BasicClientJsonObjectBuilderSpy) jsonBuilderFactorySpy.MCR
				.getReturnValue("createObjectBuilder", 5);
	}

	private BasicClientJsonObjectBuilderSpy getActionsBuilder() {
		return (BasicClientJsonObjectBuilderSpy) jsonBuilderFactorySpy.MCR
				.getReturnValue("createObjectBuilder", 4);
	}

	private String generateURL(String recordType, String recordId, String nameInData) {
		return MessageFormat.format("{0}{1}/{2}/{3}", baseUrl.get(), recordType, recordId,
				nameInData);
	}

	@Test
	public void testJsonWithActions() {
		dataResourceLink.MRV.setDefaultReturnValuesSupplier("hasReadAction", () -> true);
		converter = BasicClientDataResourceLinkToJsonConverter
				.usingConverterFactoryJsonBuilderFactoryAndDataResourceLinkAndRecordUrl(
						converterFactory, new OrgJsonBuilderFactoryAdapter(), dataResourceLink,
						baseUrl);

		String json = converter.toJson();

		String excpectedJson = """
				{
				    "children": [
				        {
				            "name": "linkedRecordType",
				            "value": "someType"
				        },
				        {
				            "name": "linkedRecordId",
				            "value": "someId"
				        },
				        {
				            "name": "mimeType",
				            "value": "image/png"
				        }
				    ],
				    "actionLinks": {"read": {
				        "requestMethod": "GET",
				        "rel": "read",
				        "url": "https://somesystem.org/rest/records/someType/someId/master",
				        "accept": "image/png"
				    }},
				    "name": "master"
				}""";
		assertEquals(json, excpectedJson);
	}

	@Test
	public void testJsonWithActionsButMissingURL() {
		dataResourceLink.MRV.setDefaultReturnValuesSupplier("hasReadAction", () -> true);
		converter = BasicClientDataResourceLinkToJsonConverter
				.usingConverterFactoryJsonBuilderFactoryAndDataResourceLinkAndRecordUrl(
						converterFactory, new OrgJsonBuilderFactoryAdapter(), dataResourceLink,
						Optional.ofNullable(null));

		String json = converter.toJson();

		String excpectedJson = """
				{
				    "children": [
				        {
				            "name": "linkedRecordType",
				            "value": "someType"
				        },
				        {
				            "name": "linkedRecordId",
				            "value": "someId"
				        },
				        {
				            "name": "mimeType",
				            "value": "image/png"
				        }
				    ],
				    "name": "master"
				}""";
		assertEquals(json, excpectedJson);
	}

	@Test
	public void testOnlyForTests() {
		var tmpConverter = BasicClientDataResourceLinkToJsonConverter
				.usingConverterFactoryJsonBuilderFactoryAndDataResourceLinkAndRecordUrl(
						converterFactory, jsonBuilderFactorySpy, dataResourceLink, baseUrl);

		assertEquals(tmpConverter.onlyForTestGetJsonBuilderFactory(), jsonBuilderFactorySpy);
		assertEquals(tmpConverter.onlyForTestGetBaseUrl(), baseUrl);
	}

}
