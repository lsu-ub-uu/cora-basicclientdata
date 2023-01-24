/*
 * Copyright 2015, 2019, 2021 Uppsala University Library
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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientbasicdata.converter.datatojson.ClientDataAtomicToJsonConverter;
import se.uu.ub.cora.clientbasicdata.converter.datatojson.ClientDataAttributeToJsonConverter;
import se.uu.ub.cora.clientbasicdata.converter.datatojson.ClientDataListToJsonConverter;
import se.uu.ub.cora.clientbasicdata.converter.datatojson.ClientDataRecordLinkToJsonConverter;
import se.uu.ub.cora.clientbasicdata.converter.datatojson.ClientDataRecordToJsonConverter;
import se.uu.ub.cora.clientbasicdata.converter.datatojson.ClientDataResourceLinkToJsonConverter;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataAtomic;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataAttribute;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataGroup;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataList;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataRecord;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataRecordLink;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataResourceLink;
import se.uu.ub.cora.data.Convertible;
import se.uu.ub.cora.data.converter.ClientDataToJsonConverter;
import se.uu.ub.cora.data.converter.ClientDataToJsonConverterFactory;
import se.uu.ub.cora.json.builder.JsonBuilderFactory;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class BasicDataToJsonConverterFactoryTest {
	private ClientDataToJsonConverterFactory converterFactory;
	private JsonBuilderFactorySpy builderFactory;
	private BasicClientDataGroup coraDataGroup;
	private BasicClientDataAtomic dataAtomic;
	private BasicClientDataAttribute dataAttribute;
	private BasicClientDataRecordLink dataRecordLink;
	private BasicClientDataResourceLink dataResourceLink;
	private String recordUrl;
	private String baseUrl;

	@BeforeMethod
	public void beforeMethod() {
		createConvertibles();
		builderFactory = new JsonBuilderFactorySpy();
		converterFactory = BasicDataToJsonConverterFactory.usingBuilderFactory(builderFactory);
		baseUrl = "some/url/";
		recordUrl = "some/url/type/id";
	}

	private void createConvertibles() {
		coraDataGroup = BasicClientDataGroup.withNameInData("groupNameInData");
		dataAtomic = BasicClientDataAtomic.withNameInDataAndValue("atomicNameInData",
				"atomicValue");
		dataAttribute = BasicClientDataAttribute.withNameInDataAndValue("attributeNameInData",
				"attributeValue");
		dataRecordLink = BasicClientDataRecordLink.withNameInData("recordLinkNameInData");
		dataResourceLink = BasicClientDataResourceLink.withNameInData("recordLinkNameInData");
	}

	@Test
	public void testDataList() throws Exception {
		BasicClientDataList coraDataList = BasicClientDataList.withContainDataOfType("someType");
		ClientDataListToJsonConverter dataToJsonConverter = (ClientDataListToJsonConverter) converterFactory
				.factorUsingConvertible(coraDataList);
		assertSame(dataToJsonConverter.converterFactory, converterFactory);
		assertSame(dataToJsonConverter.builderFactory, builderFactory);
		assertSame(dataToJsonConverter.dataList, coraDataList);

	}

	@Test
	public void testRecordNoUrl() throws Exception {
		BasicClientDataRecord coraDataRecord = BasicClientDataRecord.withDataGroup(null);
		ClientDataRecordToJsonConverter dataToJsonConverter = (ClientDataRecordToJsonConverter) converterFactory
				.factorUsingConvertible(coraDataRecord);
		assertSame(dataToJsonConverter.converterFactory, converterFactory);
		assertTrue(dataToJsonConverter.actionsConverter instanceof RecordActionsToJsonConverterImp);
		assertSame(dataToJsonConverter.builderFactory, builderFactory);
		assertSame(dataToJsonConverter.baseUrl, null);
		assertSame(dataToJsonConverter.dataRecord, coraDataRecord);
	}

	@Test
	public void testDependenciesOfActionConverterNoUrl() throws Exception {
		BasicClientDataRecord coraDataRecord = BasicClientDataRecord.withDataGroup(null);
		ClientDataRecordToJsonConverter dataToJsonConverter = (ClientDataRecordToJsonConverter) converterFactory
				.factorUsingConvertible(coraDataRecord);
		RecordActionsToJsonConverterImp actionsConverter = (RecordActionsToJsonConverterImp) dataToJsonConverter.actionsConverter;
		assertSame(actionsConverter.converterFactory, converterFactory);
		assertSame(actionsConverter.builderFactory, builderFactory);
		assertSame(dataToJsonConverter.baseUrl, null);
	}

	@Test
	public void testRecordWithUrl() throws Exception {
		BasicClientDataRecord coraDataRecord = BasicClientDataRecord.withDataGroup(null);
		ClientDataRecordToJsonConverter dataToJsonConverter = (ClientDataRecordToJsonConverter) converterFactory
				.factorUsingBaseUrlAndConvertible(baseUrl, coraDataRecord);
		assertSame(dataToJsonConverter.converterFactory, converterFactory);
		assertTrue(dataToJsonConverter.actionsConverter instanceof RecordActionsToJsonConverterImp);
		assertSame(dataToJsonConverter.builderFactory, builderFactory);
		assertSame(dataToJsonConverter.baseUrl, baseUrl);
		assertSame(dataToJsonConverter.dataRecord, coraDataRecord);
	}

	@Test
	public void testDependenciesOfActionConverterWithUrl() throws Exception {
		BasicClientDataRecord coraDataRecord = BasicClientDataRecord.withDataGroup(null);
		ClientDataRecordToJsonConverter dataToJsonConverter = (ClientDataRecordToJsonConverter) converterFactory
				.factorUsingBaseUrlAndConvertible(baseUrl, coraDataRecord);
		RecordActionsToJsonConverterImp actionsConverter = (RecordActionsToJsonConverterImp) dataToJsonConverter.actionsConverter;
		assertSame(actionsConverter.converterFactory, converterFactory);
		assertSame(actionsConverter.builderFactory, builderFactory);
		assertSame(dataToJsonConverter.baseUrl, baseUrl);
	}

	@Test
	public void testDataGroupNoUrl() {
		ClientDataGroupToJsonConverter dataToJsonConverter = (ClientDataGroupToJsonConverter) converterFactory
				.factorUsingConvertible(coraDataGroup);

		JsonBuilderFactory jsonBuilderFactory = dataToJsonConverter.jsonBuilderFactory;
		assertSame(jsonBuilderFactory, builderFactory);

	}

	@Test
	public void testDataAtomicNoUrl() {
		ClientDataAtomicToJsonConverter dataToJsonConverter = (ClientDataAtomicToJsonConverter) converterFactory
				.factorUsingConvertible(dataAtomic);

		JsonBuilderFactory jsonBuilderFactory = dataToJsonConverter.factory;
		assertSame(jsonBuilderFactory, builderFactory);
	}

	@Test
	public void testDataAttributeNoUrl() {
		ClientDataAttributeToJsonConverter dataToJsonConverter = (ClientDataAttributeToJsonConverter) converterFactory
				.factorUsingConvertible(dataAttribute);

		JsonBuilderFactory jsonBuilderFactory = dataToJsonConverter.factory;
		assertSame(jsonBuilderFactory, builderFactory);
	}

	@Test
	public void testRecordLinkNoUrl() throws Exception {
		ClientDataGroupToJsonConverter dataToJsonConverter = (ClientDataGroupToJsonConverter) converterFactory
				.factorUsingConvertible(dataRecordLink);

		JsonBuilderFactory jsonBuilderFactory = dataToJsonConverter.jsonBuilderFactory;
		assertSame(jsonBuilderFactory, builderFactory);
		assertFalse(dataToJsonConverter instanceof ClientDataRecordLinkToJsonConverter);
	}

	@Test
	public void testDataResourceLinkNoUrl() throws Exception {
		ClientDataGroupToJsonConverter dataToJsonConverter = (ClientDataGroupToJsonConverter) converterFactory
				.factorUsingConvertible(dataResourceLink);

		JsonBuilderFactory jsonBuilderFactory = dataToJsonConverter.jsonBuilderFactory;
		assertSame(jsonBuilderFactory, builderFactory);
		assertFalse(dataToJsonConverter instanceof ClientDataResourceLinkToJsonConverter);
	}

	// TODO: Implement Converter for ClientDataList add test
	// TODO: Implement Converter for ClientDataRecord add test

	@Test
	public void testDataGroupWithUrl() {
		ClientDataGroupToJsonConverter dataToJsonConverter = (ClientDataGroupToJsonConverter) converterFactory
				.factorUsingConvertible(coraDataGroup);

		JsonBuilderFactory jsonBuilderFactory = dataToJsonConverter.jsonBuilderFactory;
		assertSame(jsonBuilderFactory, builderFactory);
	}

	@Test
	public void testDataAtomicWithUrl() {
		ClientDataAtomicToJsonConverter dataToJsonConverter = (ClientDataAtomicToJsonConverter) converterFactory
				.factorUsingConvertible(dataAtomic);

		JsonBuilderFactory jsonBuilderFactory = dataToJsonConverter.factory;
		assertSame(jsonBuilderFactory, builderFactory);
	}

	@Test
	public void testDataAttributeWithUrl() {
		ClientDataAttributeToJsonConverter converter = (ClientDataAttributeToJsonConverter) converterFactory
				.factorUsingConvertible(dataAttribute);

		JsonBuilderFactory jsonBuilderFactory = converter.factory;
		assertSame(jsonBuilderFactory, builderFactory);
	}

	@Test
	public void testRecordLinkWithUrl() throws Exception {
		ClientDataRecordLinkToJsonConverter converter = (ClientDataRecordLinkToJsonConverter) converterFactory
				.factorUsingBaseUrlAndRecordUrlAndConvertible(baseUrl, recordUrl, dataRecordLink);

		JsonBuilderFactory jsonBuilderFactory = converter.jsonBuilderFactory;
		assertSame(jsonBuilderFactory, builderFactory);
		assertSame(converter.converterFactory, converterFactory);
		assertEquals(converter.baseURL, baseUrl);
	}

	@Test
	public void testRectorDownToRecordLink() throws Exception {

		converterFactory.factorUsingBaseUrlAndRecordUrlAndConvertible(baseUrl, recordUrl,
				dataRecordLink);
		ClientDataToJsonConverter converter = converterFactory
				.factorUsingConvertible(dataRecordLink);

		assertTrue(converter instanceof ClientDataRecordLinkToJsonConverter);
	}

	@Test
	public void testDataResourceLinkWithUrl() throws Exception {
		ClientDataResourceLinkToJsonConverter converter = (ClientDataResourceLinkToJsonConverter) converterFactory
				.factorUsingBaseUrlAndRecordUrlAndConvertible(baseUrl, recordUrl, dataResourceLink);

		JsonBuilderFactory jsonBuilderFactory = converter.resourceLinkBuilderFactory;
		assertSame(jsonBuilderFactory, builderFactory);
		assertSame(converter.converterFactory, converterFactory);
		assertEquals(converter.recordURL, recordUrl);
	}

	@Test
	public void testCallFactorUsingBaseUrlAndConvertible() throws Exception {

	}

	@Test
	public void testRectorDownToRecordLink2() throws Exception {

		converterFactory.factorUsingBaseUrlAndConvertible(baseUrl, dataRecordLink);
		ClientDataToJsonConverter converter = converterFactory
				.factorUsingConvertible(dataRecordLink);

		assertTrue(converter instanceof ClientDataGroupToJsonConverter);
	}

	@Test
	public void testGenerateLinksForResourceWithoutRecordUrlSetShouldReturnDataGroup()
			throws Exception {

		converterFactory.factorUsingBaseUrlAndConvertible(baseUrl, dataResourceLink);
		ClientDataToJsonConverter converter = converterFactory
				.factorUsingConvertible(dataResourceLink);

		assertTrue(converter instanceof ClientDataGroupToJsonConverter);
		assertFalse(converter instanceof ClientDataResourceLinkToJsonConverter);
	}

	@Test
	public void testFactorUsingBaseUrlAndConvertibleUsesFactorUsingConvertible() throws Exception {
		BasicDataToJsonConverterFactoryForTest forTest = new BasicDataToJsonConverterFactoryForTest();

		ClientDataToJsonConverter converter = forTest.factorUsingBaseUrlAndConvertible(baseUrl,
				dataRecordLink);

		assertEquals(forTest.baseUrl, baseUrl);
		forTest.MCR.assertParameters("factorUsingConvertible", 0, dataRecordLink);
		forTest.MCR.assertReturn("factorUsingConvertible", 0, converter);

	}

	@Test
	public void testFactorUsingRecordUrlAndConvertibleUsesFactorUsingConvertible()
			throws Exception {
		BasicDataToJsonConverterFactoryForTest forTest = new BasicDataToJsonConverterFactoryForTest();
		ClientDataToJsonConverter converter = forTest
				.factorUsingBaseUrlAndRecordUrlAndConvertible(baseUrl, recordUrl, dataRecordLink);

		assertEquals(forTest.baseUrl, baseUrl);
		assertEquals(forTest.recordUrl, recordUrl);
		forTest.MCR.assertParameters("factorUsingConvertible", 0, dataRecordLink);
		forTest.MCR.assertReturn("factorUsingConvertible", 0, converter);
	}

	class BasicDataToJsonConverterFactoryForTest extends BasicDataToJsonConverterFactory {
		BasicDataToJsonConverterFactoryForTest() {
			super(null);
		}

		MethodCallRecorder MCR = new MethodCallRecorder();

		@Override
		public ClientDataToJsonConverter factorUsingConvertible(Convertible convertible) {
			MCR.addCall("convertible", convertible);
			ClientDataToJsonConverter converter = new ClientDataToJsonConverterSpy();
			MCR.addReturned(converter);
			return converter;
		}
	}
}
