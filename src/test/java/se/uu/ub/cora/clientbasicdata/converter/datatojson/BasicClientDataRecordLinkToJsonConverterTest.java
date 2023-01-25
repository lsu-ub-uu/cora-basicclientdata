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

import se.uu.ub.cora.clientbasicdata.data.spy.BasicClientDataRecordLinkOldSpy;
import se.uu.ub.cora.clientdata.converter.DataToJsonConverter;
import se.uu.ub.cora.clientdata.converter.DataToJsonConverterFactory;

public class BasicClientDataRecordLinkToJsonConverterTest {
	BasicClientDataRecordLinkToJsonConverter recordLinkToJsonConverter;
	DataToJsonConverterFactory converterFactory;
	BasicClientJsonBuilderFactorySpy jsonBuilderFactorySpy;
	String baseURL;
	BasicClientDataRecordLinkOldSpy dataRecordLink;

	@BeforeMethod
	public void beforeMethod() {
		baseURL = "https://somesystem.org/rest/records/";
		dataRecordLink = new BasicClientDataRecordLinkOldSpy("someNameInData");

		jsonBuilderFactorySpy = new BasicClientJsonBuilderFactorySpy();
		converterFactory = new BasicClientDataToJsonConverterFactorySpy();
		recordLinkToJsonConverter = BasicClientDataRecordLinkToJsonConverter
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
		assertTrue(recordLinkToJsonConverter instanceof BasicClientDataGroupToJsonConverter);
	}

	@Test
	public void testNoActions() throws Exception {
		recordLinkToJsonConverter.hookForSubclassesToImplementExtraConversion();

		BasicClientJsonObjectBuilderSpy jsonObjectBuilderSpy = (BasicClientJsonObjectBuilderSpy) jsonBuilderFactorySpy.MCR
				.getReturnValue("createObjectBuilder", 0);

		jsonObjectBuilderSpy.MCR.assertMethodNotCalled("addKeyJsonObjectBuilder");
	}

	@Test
	public void testActionLinksBuilderAddedToMainBuilder() throws Exception {
		dataRecordLink.hasReadAction = true;

		recordLinkToJsonConverter.hookForSubclassesToImplementExtraConversion();

		BasicClientJsonObjectBuilderSpy mainBuilderSpy = (BasicClientJsonObjectBuilderSpy) jsonBuilderFactorySpy.MCR
				.getReturnValue("createObjectBuilder", 0);
		BasicClientJsonObjectBuilderSpy actionLinksBuilderSpy = getActionsBuilder();

		mainBuilderSpy.MCR.assertParameters("addKeyJsonObjectBuilder", 0, "actionLinks",
				actionLinksBuilderSpy);

	}

	private BasicClientJsonObjectBuilderSpy getActionsBuilder() {
		return (BasicClientJsonObjectBuilderSpy) jsonBuilderFactorySpy.MCR
				.getReturnValue("createObjectBuilder", 1);
	}

	@Test
	public void testActionAddedToActionBuilder() throws Exception {
		dataRecordLink.hasReadAction = true;

		recordLinkToJsonConverter.hookForSubclassesToImplementExtraConversion();

		BasicClientJsonObjectBuilderSpy actionLinksBuilderSpy = getActionsBuilder();
		BasicClientJsonObjectBuilderSpy internalLinkBuilderSpy = (BasicClientJsonObjectBuilderSpy) jsonBuilderFactorySpy.MCR
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
