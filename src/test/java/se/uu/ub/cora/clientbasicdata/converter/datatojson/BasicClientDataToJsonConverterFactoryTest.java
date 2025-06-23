/*
 * Copyright 2015, 2019, 2021, 2025 Uppsala University Library
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

import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataAtomic;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataAttribute;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataGroup;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataList;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataRecordLink;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataResourceLink;
import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverter;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterFactory;
import se.uu.ub.cora.json.builder.JsonBuilderFactory;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class BasicClientDataToJsonConverterFactoryTest {
	private ClientDataToJsonConverterFactory converterFactory;
	private BasicClientJsonBuilderFactorySpy builderFactory;
	private BasicClientDataGroup coraDataGroup;
	private BasicClientDataAtomic dataAtomic;
	private BasicClientDataAttribute dataAttribute;
	private BasicClientDataRecordLink dataRecordLink;
	private BasicClientDataResourceLink dataResourceLink;
	private String baseUrl;

	@BeforeMethod
	public void beforeMethod() {
		createConvertibles();
		builderFactory = new BasicClientJsonBuilderFactorySpy();
		converterFactory = BasicClientDataToJsonConverterFactory
				.usingBuilderFactory(builderFactory);
		baseUrl = "some/url/";
	}

	private void createConvertibles() {
		coraDataGroup = BasicClientDataGroup.withNameInData("groupNameInData");
		dataAtomic = BasicClientDataAtomic.withNameInDataAndValue("atomicNameInData",
				"atomicValue");
		dataAttribute = BasicClientDataAttribute.withNameInDataAndValue("attributeNameInData",
				"attributeValue");
		dataRecordLink = BasicClientDataRecordLink.withNameInData("recordLinkNameInData");
		dataResourceLink = BasicClientDataResourceLink.withNameInDataAndTypeAndIdAndMimeType(
				"recordLinkNameInData", "someType", "someId", "someMimeType");
	}

	@Test
	public void testDataList() {
		BasicClientDataList coraDataList = BasicClientDataList.withContainDataOfType("someType");
		BasicClientDataListToJsonConverter dataToJsonConverter = (BasicClientDataListToJsonConverter) converterFactory
				.factorUsingConvertible(coraDataList);
		assertSame(dataToJsonConverter.converterFactory, converterFactory);
		assertSame(dataToJsonConverter.builderFactory, builderFactory);
		assertSame(dataToJsonConverter.dataList, coraDataList);

	}

	@Test
	public void testDataGroupNoUrl() {
		BasicClientDataGroupToJsonConverter dataToJsonConverter = (BasicClientDataGroupToJsonConverter) converterFactory
				.factorUsingConvertible(coraDataGroup);

		JsonBuilderFactory jsonBuilderFactory = dataToJsonConverter.jsonBuilderFactory;
		assertSame(jsonBuilderFactory, builderFactory);
	}

	@Test
	public void testDataGroupWithUrl() {
		BasicClientDataGroupToJsonConverter dataToJsonConverter = (BasicClientDataGroupToJsonConverter) converterFactory
				.factorUsingConvertible(coraDataGroup);

		JsonBuilderFactory jsonBuilderFactory = dataToJsonConverter.jsonBuilderFactory;
		assertSame(jsonBuilderFactory, builderFactory);
	}

	@Test
	public void testDataAtomicNoUrl() {
		BasicClientDataAtomicToJsonConverter dataToJsonConverter = (BasicClientDataAtomicToJsonConverter) converterFactory
				.factorUsingConvertible(dataAtomic);

		JsonBuilderFactory jsonBuilderFactory = dataToJsonConverter.factory;
		assertSame(jsonBuilderFactory, builderFactory);
	}

	@Test
	public void testDataAtomicWithUrl() {
		BasicClientDataAtomicToJsonConverter dataToJsonConverter = (BasicClientDataAtomicToJsonConverter) converterFactory
				.factorUsingConvertible(dataAtomic);

		JsonBuilderFactory jsonBuilderFactory = dataToJsonConverter.factory;
		assertSame(jsonBuilderFactory, builderFactory);
	}

	@Test
	public void testDataAttributeNoUrl() {
		BasicClientDataAttributeToJsonConverter dataToJsonConverter = (BasicClientDataAttributeToJsonConverter) converterFactory
				.factorUsingConvertible(dataAttribute);

		JsonBuilderFactory jsonBuilderFactory = dataToJsonConverter.factory;
		assertSame(jsonBuilderFactory, builderFactory);
	}

	@Test
	public void testRecordLinkNoUrl() {
		BasicClientDataGroupToJsonConverter dataToJsonConverter = (BasicClientDataGroupToJsonConverter) converterFactory
				.factorUsingConvertible(dataRecordLink);

		JsonBuilderFactory jsonBuilderFactory = dataToJsonConverter.jsonBuilderFactory;
		assertSame(jsonBuilderFactory, builderFactory);
		assertFalse(dataToJsonConverter instanceof BasicClientDataRecordLinkToJsonConverter);
	}

	@Test
	public void testDataAttributeWithUrl() {
		BasicClientDataAttributeToJsonConverter converter = (BasicClientDataAttributeToJsonConverter) converterFactory
				.factorUsingConvertible(dataAttribute);

		JsonBuilderFactory jsonBuilderFactory = converter.factory;
		assertSame(jsonBuilderFactory, builderFactory);
	}

	@Test
	public void testRecordLinkWithUrl() {
		BasicClientDataRecordLinkToJsonConverter converter = (BasicClientDataRecordLinkToJsonConverter) converterFactory
				.factorUsingBaseUrlAndConvertible(baseUrl, dataRecordLink);

		JsonBuilderFactory jsonBuilderFactory = converter.jsonBuilderFactory;
		assertSame(jsonBuilderFactory, builderFactory);
		assertSame(converter.converterFactory, converterFactory);
		assertEquals(converter.baseURL, baseUrl);
	}

	@Test
	public void testRectorDownToRecordLink() {
		converterFactory.factorUsingBaseUrlAndConvertible(baseUrl, dataRecordLink);
		ClientDataToJsonConverter converter = converterFactory
				.factorUsingConvertible(dataRecordLink);

		assertTrue(converter instanceof BasicClientDataRecordLinkToJsonConverter);
	}

	@Test
	public void testDataResourceLinkWithUrl() {
		BasicClientDataResourceLinkToJsonConverter converter = (BasicClientDataResourceLinkToJsonConverter) converterFactory
				.factorUsingBaseUrlAndConvertible(baseUrl, dataResourceLink);

		assertSame(converter.onlyForTestGetJsonBuilderFactory(), builderFactory);
		assertSame(converter.onlyForTestGetConverterFactory(), converterFactory);
		assertEquals(converter.onlyForTestGetBaseUrl(), Optional.of(baseUrl));
	}

	@Test
	public void testDataResourceLinkWithoutUrl() {
		BasicClientDataResourceLinkToJsonConverter converter = (BasicClientDataResourceLinkToJsonConverter) converterFactory
				.factorUsingConvertible(dataResourceLink);

		assertSame(converter.onlyForTestGetJsonBuilderFactory(), builderFactory);
		assertSame(converter.onlyForTestGetConverterFactory(), converterFactory);
		assertTrue(converter.onlyForTestGetBaseUrl().isEmpty());
	}

	@Test
	public void testRectorDownToRecordLink2() {

		converterFactory.factorUsingBaseUrlAndConvertible(baseUrl, dataRecordLink);
		ClientDataToJsonConverter converter = converterFactory
				.factorUsingConvertible(dataRecordLink);

		assertTrue(converter instanceof BasicClientDataGroupToJsonConverter);
	}

	@Test
	public void testGenerateLinksForResourceWithoutRecordUrlSetShouldReturnDataGroup() {

		converterFactory.factorUsingBaseUrlAndConvertible(baseUrl, dataResourceLink);
		ClientDataToJsonConverter converter = converterFactory
				.factorUsingConvertible(dataResourceLink);

		assertFalse(converter instanceof BasicClientDataGroupToJsonConverter);
		assertTrue(converter instanceof BasicClientDataResourceLinkToJsonConverter);
	}

	@Test
	public void testFactorUsingBaseUrlAndConvertibleUsesFactorUsingConvertible() {
		BasicDataToJsonConverterFactoryForTest forTest = new BasicDataToJsonConverterFactoryForTest();

		ClientDataToJsonConverter converter = forTest.factorUsingBaseUrlAndConvertible(baseUrl,
				dataRecordLink);

		assertEquals(forTest.baseUrl, baseUrl);
		forTest.MCR.assertParameters("factorUsingConvertible", 0, dataRecordLink);
		forTest.MCR.assertReturn("factorUsingConvertible", 0, converter);

	}

	class BasicDataToJsonConverterFactoryForTest extends BasicClientDataToJsonConverterFactory {
		BasicDataToJsonConverterFactoryForTest() {
			super(null);
		}

		MethodCallRecorder MCR = new MethodCallRecorder();

		@Override
		public ClientDataToJsonConverter factorUsingConvertible(ClientConvertible convertible) {
			MCR.addCall("convertible", convertible);
			ClientDataToJsonConverter converter = new BasicClientDataToJsonConverterSpy();
			MCR.addReturned(converter);
			return converter;
		}
	}
}
