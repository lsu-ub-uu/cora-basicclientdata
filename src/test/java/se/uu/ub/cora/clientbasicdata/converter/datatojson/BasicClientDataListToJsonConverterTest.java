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

import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataList;
import se.uu.ub.cora.clientdata.ClientData;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.converter.DataToJsonConverter;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordSpy;
import se.uu.ub.cora.json.builder.JsonObjectBuilder;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class BasicClientDataListToJsonConverterTest {

	private BasicClientDataToJsonConverterFactorySpy converterFactory;
	private BasicClientDataListToJsonConverter recordListToJsonConverter;
	private BasicClientJsonBuilderFactorySpy builderFactory;
	private BasicClientDataList dataList;

	@BeforeMethod
	public void beforeMethod() throws Exception {
		converterFactory = new BasicClientDataToJsonConverterFactorySpy();
		builderFactory = new BasicClientJsonBuilderFactorySpy();
		dataList = createDataList();
		recordListToJsonConverter = BasicClientDataListToJsonConverter
				.usingJsonFactoryForDataList(converterFactory, builderFactory, dataList);

	}

	@Test
	public void testRecordListConverterImplementsDataToJsonConverter() throws Exception {
		assertTrue(recordListToJsonConverter instanceof DataToJsonConverter);
	}

	@Test
	public void testToJsonObjectBuilderRootBuilderAndListBuilderCreatedWithBuilderFactory()
			throws Exception {
		JsonObjectBuilder returnedObjectBuilder = recordListToJsonConverter.toJsonObjectBuilder();
		BasicClientJsonObjectBuilderSpy rootWrappingBuilder = getRootWrappingBuilderFromSpy();

		assertSame(returnedObjectBuilder, rootWrappingBuilder);

		BasicClientJsonObjectBuilderSpy recordListBuilder = getRecordListBuilderFromSpy();
		rootWrappingBuilder.MCR.assertParameters("addKeyJsonObjectBuilder", 0, "dataList",
				recordListBuilder);
	}

	private BasicClientJsonObjectBuilderSpy getRecordListBuilderFromSpy() {
		return (BasicClientJsonObjectBuilderSpy) builderFactory.MCR.getReturnValue("createObjectBuilder", 0);
	}

	private BasicClientJsonObjectBuilderSpy getRootWrappingBuilderFromSpy() {
		return (BasicClientJsonObjectBuilderSpy) builderFactory.MCR.getReturnValue("createObjectBuilder", 1);
	}

	@Test
	public void testToJsonObjectBuilderBasicListInfoAdded() throws Exception {
		recordListToJsonConverter.toJsonObjectBuilder();
		BasicClientJsonObjectBuilderSpy recordListBuilder = getRecordListBuilderFromSpy();
		recordListBuilder.MCR.assertParameters("addKeyString", 0, "totalNo",
				dataList.getTotalNumberOfTypeInStorage());
		recordListBuilder.MCR.assertParameters("addKeyString", 1, "fromNo", dataList.getFromNo());
		recordListBuilder.MCR.assertParameters("addKeyString", 2, "toNo", dataList.getToNo());
		recordListBuilder.MCR.assertParameters("addKeyString", 3, "containDataOfType",
				dataList.getContainDataOfType());
	}

	@Test
	public void testToJsonObjectBuilderDataBuilderCreatedWithBuilderFactoryAndAddedToListBuilder()
			throws Exception {
		recordListToJsonConverter.toJsonObjectBuilder();
		BasicClientJsonObjectBuilderSpy recordListBuilder = getRecordListBuilderFromSpy();
		BasicClientJsonArrayBuilderSpy dataArrayBuilder = getDataArrayBuilderFromSpy();
		recordListBuilder.MCR.assertParameters("addKeyJsonArrayBuilder", 0, "data",
				dataArrayBuilder);
	}

	private BasicClientJsonArrayBuilderSpy getDataArrayBuilderFromSpy() {
		return (BasicClientJsonArrayBuilderSpy) builderFactory.MCR.getReturnValue("createArrayBuilder", 0);
	}

	@Test
	public void testToJsonObjectBuilderNoConvertersCreatedAndNothingAddedToDataIfNoRecordsInList()
			throws Exception {
		dataList.getDataList().clear();

		recordListToJsonConverter.toJsonObjectBuilder();
		BasicClientJsonArrayBuilderSpy dataArrayBuilder = getDataArrayBuilderFromSpy();
		dataArrayBuilder.MCR.assertMethodNotCalled("addJsonObjectBuilder");
	}

	@Test
	public void testToJsonObjectBuilderConvertersCreatedFromFactoryAndResultAddedToDataForEachRecordInList()
			throws Exception {
		recordListToJsonConverter.toJsonObjectBuilder();

		List<ClientData> listOfData = dataList.getDataList();

		converterFactory.MCR.assertParameters("factorUsingConvertible", 0, listOfData.get(0));
		assertCorrectFactoryAndConvertAndAddingToDataForDataNumberFromList(0);
		assertCorrectFactoryAndConvertAndAddingToDataForDataNumberFromList(1);

		converterFactory.MCR.assertNumberOfCallsToMethod("factorUsingConvertible",
				listOfData.size());
	}

	private void assertCorrectFactoryAndConvertAndAddingToDataForDataNumberFromList(
			int listNumber) {
		BasicClientJsonArrayBuilderSpy dataArrayBuilder = getDataArrayBuilderFromSpy();
		BasicClientDataToJsonConverterSpy recordConverterSpy1 = (BasicClientDataToJsonConverterSpy) converterFactory.MCR
				.getReturnValue("factorUsingConvertible", listNumber);
		recordConverterSpy1.MCR.assertMethodWasCalled("toJsonObjectBuilder");
		var recordBuilder1 = recordConverterSpy1.MCR.getReturnValue("toJsonObjectBuilder", 0);
		dataArrayBuilder.MCR.assertParameters("addJsonObjectBuilder", listNumber, recordBuilder1);
	}

	@Test
	public void testToJson() throws Exception {

		DataListToJsonConverterForTest recordListConverterForTest = new DataListToJsonConverterForTest(
				converterFactory, builderFactory, dataList);

		String json = recordListConverterForTest.toJson();

		recordListConverterForTest.MCR.methodWasCalled("toJsonObjectBuilder");
		BasicClientJsonObjectBuilderSpy jsonBuilder = (BasicClientJsonObjectBuilderSpy) recordListConverterForTest.MCR
				.getReturnValue("toJsonObjectBuilder", 0);
		jsonBuilder.MCR.assertMethodWasCalled("toJsonFormattedPrettyString");
		jsonBuilder.MCR.assertReturn("toJsonFormattedPrettyString", 0, json);
	}

	@Test
	public void testToJsonCompactFormat() throws Exception {

		DataListToJsonConverterForTest recordListConverterForTest = new DataListToJsonConverterForTest(
				converterFactory, builderFactory, dataList);

		String json = recordListConverterForTest.toJsonCompactFormat();

		recordListConverterForTest.MCR.methodWasCalled("toJsonObjectBuilder");
		BasicClientJsonObjectBuilderSpy jsonBuilder = (BasicClientJsonObjectBuilderSpy) recordListConverterForTest.MCR
				.getReturnValue("toJsonObjectBuilder", 0);
		jsonBuilder.MCR.assertMethodWasCalled("toJsonFormattedString");
		jsonBuilder.MCR.assertReturn("toJsonFormattedString", 0, json);
	}

	class DataListToJsonConverterForTest extends BasicClientDataListToJsonConverter {
		MethodCallRecorder MCR = new MethodCallRecorder();

		DataListToJsonConverterForTest(BasicClientDataToJsonConverterFactorySpy converterFactory,
				BasicClientJsonBuilderFactorySpy builderFactory, BasicClientDataList dataList) {
			super(converterFactory, builderFactory, dataList);
		}

		@Override
		public JsonObjectBuilder toJsonObjectBuilder() {
			MCR.addCall();
			BasicClientJsonObjectBuilderSpy objectBuilder = new BasicClientJsonObjectBuilderSpy();
			MCR.addReturned(objectBuilder);
			return objectBuilder;
		}
	}

	private BasicClientDataList createDataList() {
		BasicClientDataList dataList = BasicClientDataList.withContainDataOfType("place");
		ClientDataRecord dataRecord = new ClientDataRecordSpy();
		dataList.addData(dataRecord);
		ClientDataRecord dataRecord2 = new ClientDataRecordSpy();
		dataList.addData(dataRecord2);
		dataList.setTotalNo("111");
		dataList.setFromNo("1");
		dataList.setToNo("100");
		return dataList;
	}

}
