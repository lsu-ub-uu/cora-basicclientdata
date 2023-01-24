/*
 * Copyright 2021 Uppsala University Library
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

import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientbasicdata.converter.datatojson.DataGroupToJsonConverter;
import se.uu.ub.cora.clientbasicdata.converter.datatojson.DataRecordLinkToJsonConverter;
import se.uu.ub.cora.clientbasicdata.data.spy.DataRecordLinkOldSpy;
import se.uu.ub.cora.data.converter.DataToJsonConverter;
import se.uu.ub.cora.data.converter.DataToJsonConverterFactory;

public class DataRecordLinkToJsonConverterTest {
	DataRecordLinkToJsonConverter recordLinkToJsonConverter;
	DataToJsonConverterFactory converterFactory;
	JsonBuilderFactorySpy jsonBuilderFactorySpy;
	String baseURL;
	DataRecordLinkOldSpy dataRecordLink;

	@BeforeMethod
	public void beforeMethod() {
		baseURL = "https://somesystem.org/rest/records/";
		dataRecordLink = new DataRecordLinkOldSpy("someNameInData");

		jsonBuilderFactorySpy = new JsonBuilderFactorySpy();
		converterFactory = new DataToJsonConverterFactorySpy();
		recordLinkToJsonConverter = DataRecordLinkToJsonConverter
				.usingConverterFactoryAndJsonBuilderFactoryAndDataRecordLinkAndBaseUrl(
						converterFactory, jsonBuilderFactorySpy, dataRecordLink, baseURL);
	}

	@Test
	public void testConverterFactorySetInParent() throws Exception {
		assertSame(recordLinkToJsonConverter.converterFactory, converterFactory);
	}

	@Test
	public void testRecordLinkConverterExtendsGroupConverter() throws Exception {
		assertTrue(recordLinkToJsonConverter instanceof DataToJsonConverter);
		assertTrue(recordLinkToJsonConverter instanceof DataGroupToJsonConverter);
	}

	@Test
	public void testNoActions() throws Exception {
		recordLinkToJsonConverter.hookForSubclassesToImplementExtraConversion();

		JsonObjectBuilderSpy jsonObjectBuilderSpy = (JsonObjectBuilderSpy) jsonBuilderFactorySpy.MCR
				.getReturnValue("createObjectBuilder", 0);

		jsonObjectBuilderSpy.MCR.assertMethodNotCalled("addKeyJsonObjectBuilder");
	}

	@Test
	public void testActionLinksBuilderAddedToMainBuilder() throws Exception {
		dataRecordLink.hasReadAction = true;

		recordLinkToJsonConverter.hookForSubclassesToImplementExtraConversion();

		JsonObjectBuilderSpy mainBuilderSpy = (JsonObjectBuilderSpy) jsonBuilderFactorySpy.MCR
				.getReturnValue("createObjectBuilder", 0);
		JsonObjectBuilderSpy actionLinksBuilderSpy = getActionsBuilder();

		mainBuilderSpy.MCR.assertParameters("addKeyJsonObjectBuilder", 0, "actionLinks",
				actionLinksBuilderSpy);

	}

	private JsonObjectBuilderSpy getActionsBuilder() {
		return (JsonObjectBuilderSpy) jsonBuilderFactorySpy.MCR
				.getReturnValue("createObjectBuilder", 1);
	}

	@Test
	public void testActionAddedToActionBuilder() throws Exception {
		dataRecordLink.hasReadAction = true;

		recordLinkToJsonConverter.hookForSubclassesToImplementExtraConversion();

		JsonObjectBuilderSpy actionLinksBuilderSpy = getActionsBuilder();
		JsonObjectBuilderSpy internalLinkBuilderSpy = (JsonObjectBuilderSpy) jsonBuilderFactorySpy.MCR
				.getReturnValue("createObjectBuilder", 2);

		actionLinksBuilderSpy.MCR.assertParameters("addKeyJsonObjectBuilder", 0, "read",
				internalLinkBuilderSpy);
		internalLinkBuilderSpy.MCR.assertParameters("addKeyString", 0, "rel", "read");
		internalLinkBuilderSpy.MCR.assertParameters("addKeyString", 1, "url", baseURL
				+ dataRecordLink.getLinkedRecordType() + "/" + dataRecordLink.getLinkedRecordId());
		internalLinkBuilderSpy.MCR.assertParameters("addKeyString", 2, "requestMethod", "GET");
		internalLinkBuilderSpy.MCR.assertParameters("addKeyString", 3, "accept",
				"application/vnd.uub.record+json");
		internalLinkBuilderSpy.MCR.assertNumberOfCallsToMethod("addKeyString", 4);
	}
}
