/*
 * Copyright 2015, 2019, 2021, 2022 Uppsala University Library
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
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordSpy;
import se.uu.ub.cora.clientdata.spy.DataRecordSpy;
import se.uu.ub.cora.clientdata.starter.DataToJsonConverterSpy;
import se.uu.ub.cora.json.builder.JsonObjectBuilder;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class BasicClientDataRecordToJsonConverterTest {
	private BasicClientDataRecordToJsonConverter dataRecordToJsonConverter;

	private ClientDataRecordSpy dataRecord;
	private BasicClientJsonBuilderFactorySpy builderFactory;

	private BasicClientDataToJsonConverterFactorySpy converterFactory;
	private String baseUrl = "some/base/url/";
	private ClientClientDataRecordSpy dataGroup;
	private BasicClientRecordActionsToJsonConverterSpy actionsConverterSpy;

	@BeforeMethod
	public void setUp() {
		converterFactory = new BasicClientDataToJsonConverterFactorySpy();
		actionsConverterSpy = new BasicClientRecordActionsToJsonConverterSpy();
		builderFactory = new BasicClientJsonBuilderFactorySpy();
		dataGroup = new ClientClientDataRecordSpy();
		dataRecord = new ClientDataRecordSpy();
		dataRecord.MRV.setDefaultReturnValuesSupplier("getDataGroup",
				(Supplier<ClientDataGroup>) () -> dataGroup);
	}

	private void createDataRecordToJsonConverter() {
		dataRecordToJsonConverter = BasicClientDataRecordToJsonConverter
				.usingConverterFactoryAndActionsConverterAndBuilderFactoryAndBaseUrlAndDataRecord(
						converterFactory, null, builderFactory, baseUrl, dataRecord);
	}

	@Test
	public void testConverterImplementsDataToJsonConverter() throws Exception {
		createDataRecordToJsonConverter();
		assertTrue(dataRecordToJsonConverter instanceof DataToJsonConverter);
	}

	@Test
	public void testConverterFactoryUsedToCreateConverterForMainDataGroupNoBaseUrl()
			throws Exception {
		dataRecordToJsonConverter = BasicClientDataRecordToJsonConverter
				.usingConverterFactoryAndActionsConverterAndBuilderFactoryAndBaseUrlAndDataRecord(
						converterFactory, actionsConverterSpy, builderFactory, null, dataRecord);

		JsonObjectBuilder returnedJsonObjectBuilder = dataRecordToJsonConverter
				.toJsonObjectBuilder();

		converterFactory.MCR.assertMethodNotCalled("factorUsingBaseUrlAndRecordUrlAndConvertible");
		converterFactory.MCR.assertParameters("factorUsingConvertible", 0,
				dataRecord.getDataGroup());

		BasicClientDataToJsonConverterSpy dataGroupConverter = (BasicClientDataToJsonConverterSpy) converterFactory.MCR
				.getReturnValue("factorUsingConvertible", 0);
		assertKeyDataAddedToRecordBuilderIsBuilderFromDataGroupConverter(dataGroupConverter);

		BasicClientBasicClientJsonObjectBuilderSpy recordBuilder = getRecordBuilderFromSpy();
		BasicClientBasicClientJsonObjectBuilderSpy rootWrappingBuilder = getRootWrappingBuilder();
		rootWrappingBuilder.MCR.assertParameters("addKeyJsonObjectBuilder", 0, "record",
				recordBuilder);
		assertSame(returnedJsonObjectBuilder, rootWrappingBuilder);
	}

	private void assertKeyDataAddedToRecordBuilderIsBuilderFromDataGroupConverter(
			BasicClientDataToJsonConverterSpy dataGroupConverter) {
		BasicClientBasicClientJsonObjectBuilderSpy dataGroupBuilder = (BasicClientBasicClientJsonObjectBuilderSpy) dataGroupConverter.MCR
				.getReturnValue("toJsonObjectBuilder", 0);

		BasicClientBasicClientJsonObjectBuilderSpy recordBuilder = getRecordBuilderFromSpy();
		recordBuilder.MCR.assertParameters("addKeyJsonObjectBuilder", 0, "data", dataGroupBuilder);
	}

	private BasicClientBasicClientJsonObjectBuilderSpy getRootWrappingBuilder() {
		return (BasicClientBasicClientJsonObjectBuilderSpy) builderFactory.MCR
				.getReturnValue("createObjectBuilder", 1);
	}

	private BasicClientBasicClientJsonObjectBuilderSpy getRecordBuilderFromSpy() {
		return (BasicClientBasicClientJsonObjectBuilderSpy) builderFactory.MCR
				.getReturnValue("createObjectBuilder", 0);
	}

	@Test
	public void testConverterFactoryUsedToCreateConverterForMainDataGroupWithBaseUrl()
			throws Exception {
		createDataRecordToJsonConverter();

		dataRecordToJsonConverter.toJsonObjectBuilder();

		converterFactory.MCR.assertMethodNotCalled("factorUsingConvertible");

		String recordUrl = baseUrl + dataRecord.getType() + "/" + dataRecord.getId();

		converterFactory.MCR.assertParameters("factorUsingBaseUrlAndRecordUrlAndConvertible", 0,
				baseUrl, recordUrl, dataRecord.getDataGroup());

		DataToJsonConverterSpy dataGroupConverter = (DataToJsonConverterSpy) converterFactory.MCR
				.getReturnValue("factorUsingBaseUrlAndRecordUrlAndConvertible", 0);
		assertKeyDataAddedToRecordBuilderIsBuilderFromDataGroupConverter(dataGroupConverter);

		BasicClientBasicClientJsonObjectBuilderSpy recordBuilder = getRecordBuilderFromSpy();
		BasicClientBasicClientJsonObjectBuilderSpy rootWrappingBuilder = getRootWrappingBuilder();
		rootWrappingBuilder.MCR.assertParameters("addKeyJsonObjectBuilder", 0, "record",
				recordBuilder);
	}

	@Test
	public void testToJsonWithListOfReadPermissions() {
		dataRecord.MRV.setDefaultReturnValuesSupplier("hasReadPermissions",
				(Supplier<Boolean>) () -> true);

		Set<String> readPermissions = Set.of("readPermissionOne", "readPermissionTwo");
		dataRecord.MRV.setDefaultReturnValuesSupplier("getReadPermissions",
				(Supplier<Set<String>>) () -> readPermissions);

		createDataRecordToJsonConverter();

		dataRecordToJsonConverter.toJsonObjectBuilder();

		assertTwoPermissionsAddedCorrectlyForType("read", 0);
	}

	private void assertTwoPermissionsAddedCorrectlyForType(String type, int postitionOfTypes) {
		BasicClientBasicClientJsonObjectBuilderSpy permissionBuilder = getPermissionBuilderFromSpy();
		BasicClientBasicClientJsonObjectBuilderSpy recordBuilder = getRecordBuilderFromSpy();

		JsonArrayBuilderSpy typePermissionBuilder = getTypePermissionArrayBuilderFromSpy(
				postitionOfTypes);

		assertPermissionsCalledAddString(type, typePermissionBuilder);

		typePermissionBuilder.MCR.assertNumberOfCallsToMethod("addString", 2);

		permissionBuilder.MCR.assertParameters("addKeyJsonArrayBuilder", postitionOfTypes, type,
				typePermissionBuilder);

		recordBuilder.MCR.assertParameters("addKeyJsonObjectBuilder", 1, "permissions",
				permissionBuilder);
	}

	private void assertPermissionsCalledAddString(String type,
			JsonArrayBuilderSpy typePermissionBuilder) {

		List<Object> values = getAllParameterValuesOnCallAddString(typePermissionBuilder);

		assertTrue(
				values.contains(type + "PermissionOne") && values.contains(type + "PermissionTwo"));
	}

	private List<Object> getAllParameterValuesOnCallAddString(
			JsonArrayBuilderSpy typePermissionBuilder) {
		List<Object> values = new ArrayList<>();
		for (int i = 0; i <= 1; i++) {
			Object value = getValueFromAParameter(typePermissionBuilder, i);
			values.add(value);
		}
		return values;
	}

	private Object getValueFromAParameter(JsonArrayBuilderSpy typePermissionBuilder, int i) {
		Map<String, Object> parameters = typePermissionBuilder.MCR
				.getParametersForMethodAndCallNumber("addString", i);
		Object value = parameters.get("value");
		return value;
	}

	private JsonArrayBuilderSpy getTypePermissionArrayBuilderFromSpy(int postitionOfTypes) {
		return (JsonArrayBuilderSpy) builderFactory.MCR.getReturnValue("createArrayBuilder",
				postitionOfTypes);
	}

	private BasicClientBasicClientJsonObjectBuilderSpy getPermissionBuilderFromSpy() {
		return (BasicClientBasicClientJsonObjectBuilderSpy) builderFactory.MCR
				.getReturnValue("createObjectBuilder", 1);
	}

	@Test
	public void testToJsonWithWritePermissions() {
		dataRecord.MRV.setDefaultReturnValuesSupplier("hasWritePermissions",
				(Supplier<Boolean>) () -> true);

		Set<String> writePermissions = Set.of("writePermissionOne", "writePermissionTwo");
		dataRecord.MRV.setDefaultReturnValuesSupplier("getWritePermissions",
				(Supplier<Set<String>>) () -> writePermissions);

		createDataRecordToJsonConverter();

		dataRecordToJsonConverter.toJsonObjectBuilder();
		assertTwoPermissionsAddedCorrectlyForType("write", 0);
	}

	@Test
	public void testToJsonWithReadAndWritePermissions() {
		dataRecord.MRV.setDefaultReturnValuesSupplier("hasReadPermissions",
				(Supplier<Boolean>) () -> true);
		dataRecord.MRV.setDefaultReturnValuesSupplier("hasWritePermissions",
				(Supplier<Boolean>) () -> true);

		Set<String> readPermissions = Set.of("readPermissionOne", "readPermissionTwo");
		dataRecord.MRV.setDefaultReturnValuesSupplier("getReadPermissions",
				(Supplier<Set<String>>) () -> readPermissions);
		Set<String> writePermissions = Set.of("writePermissionOne", "writePermissionTwo");
		dataRecord.MRV.setDefaultReturnValuesSupplier("getWritePermissions",
				(Supplier<Set<String>>) () -> writePermissions);

		createDataRecordToJsonConverter();

		dataRecordToJsonConverter.toJsonObjectBuilder();
		assertTwoPermissionsAddedCorrectlyForType("read", 0);
		assertTwoPermissionsAddedCorrectlyForType("write", 1);
	}

	@Test
	public void testToJson() {
		DataRecordToJsonConverterForTest forTest = new DataRecordToJsonConverterForTest(
				builderFactory);

		String jsonString = forTest.toJson();

		forTest.MCR.assertMethodWasCalled("toJsonObjectBuilder");
		BasicClientBasicClientJsonObjectBuilderSpy builderSpy = (BasicClientBasicClientJsonObjectBuilderSpy) forTest.MCR
				.getReturnValue("toJsonObjectBuilder", 0);
		builderSpy.MCR.assertReturn("toJsonFormattedPrettyString", 0, jsonString);
		builderSpy.MCR.assertMethodWasCalled("toJsonFormattedPrettyString");
	}

	private class DataRecordToJsonConverterForTest extends DataRecordToJsonConverter {
		MethodCallRecorder MCR = new MethodCallRecorder();

		DataRecordToJsonConverterForTest(JsonBuilderFactorySpy builderFactory) {
			super(null, null, builderFactory, null, null);
		}

		@Override
		public JsonObjectBuilder toJsonObjectBuilder() {
			MCR.addCall();
			BasicClientJsonObjectBuilderSpy BasicClientJsonObjectBuilderSpy = new BasicClientJsonObjectBuilderSpy();
			MCR.addReturned(BasicClientJsonObjectBuilderSpy);
			return BasicClientJsonObjectBuilderSpy;
		}

	}

	@Test
	public void testToJsonCompactFormat() {
		DataRecordToJsonConverterForTest forTest = new DataRecordToJsonConverterForTest(
				builderFactory);

		String jsonString = forTest.toJsonCompactFormat();

		forTest.MCR.assertMethodWasCalled("toJsonObjectBuilder");
		BasicClientJsonObjectBuilderSpy builderSpy = (BasicClientJsonObjectBuilderSpy) forTest.MCR
				.getReturnValue("toJsonObjectBuilder", 0);
		builderSpy.MCR.assertReturn("toJsonFormattedString", 0, jsonString);
	}

	@Test
	public void testConvertActionsNoActions() throws Exception {
		DataRecordSpy dataRecordSpy = createDataRecordToJsonConverterUsingDataRecordSpy();

		dataRecordToJsonConverter.toJsonObjectBuilder();

		dataRecordSpy.MCR.assertMethodNotCalled("getActions");
	}

	private ClientDataRecordSpy createDataRecordToJsonConverterUsingDataRecordSpy() {
		ClientDataRecordSpy dataRecordSpy = new ClientDataRecordSpy();
		dataRecordToJsonConverter = DataRecordToJsonConverter
				.usingConverterFactoryAndActionsConverterAndBuilderFactoryAndBaseUrlAndDataRecord(
						converterFactory, actionsConverterSpy, builderFactory, baseUrl,
						dataRecordSpy);
		dataRecordSpy.MRV.setDefaultReturnValuesSupplier("getDataGroup",
				(Supplier<ClientDataGroup>) () -> dataGroup);
		return dataRecordSpy;
	}

	@Test
	public void testConvertActionsAllTypes() throws Exception {
		ClientDataRecordSpy dataRecordSpy = createDataRecordToJsonConverterUsingDataRecordSpy();
		dataRecordSpy.MRV.setDefaultReturnValuesSupplier("hasActions",
				(Supplier<Boolean>) () -> true);

		addActionsToDataRecordSpy(dataRecordSpy);

		dataRecordToJsonConverter.toJsonObjectBuilder();

		actionsConverterSpy.MCR.assertParameters("toJsonObjectBuilder", 0);
		assertActionConverterData(dataRecordSpy);

		BasicClientJsonObjectBuilderSpy actionLinksBuilder = (BasicClientJsonObjectBuilderSpy) actionsConverterSpy.MCR
				.getReturnValue("toJsonObjectBuilder", 0);
		BasicClientJsonObjectBuilderSpy recordBuilder = (BasicClientJsonObjectBuilderSpy) builderFactory.MCR
				.getReturnValue("createObjectBuilder", 0);

		recordBuilder.MCR.assertNumberOfCallsToMethod("addKeyJsonObjectBuilder", 2);
		recordBuilder.MCR.assertParameters("addKeyJsonObjectBuilder", 1, "actionLinks",
				actionLinksBuilder);

	}

	private void assertActionConverterData(ClientDataRecordSpy dataRecordSpy) {
		ActionsConverterData actionConverter = (ActionsConverterData) actionsConverterSpy.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("toJsonObjectBuilder", 0,
						"actionsConverterData");
		assertEquals(actionConverter.recordType, dataRecordSpy.getType());
		assertEquals(actionConverter.recordId, dataRecordSpy.getId());
		assertEquals(actionConverter.actions, dataRecordSpy.getActions());
		assertNull(actionConverter.searchRecordId);
	}

	private void addActionsToDataRecordSpy(DataRecordSpy dataRecordSpy) {
		List<Action> actionList = List.of(Action.READ, Action.UPDATE);

		dataRecordSpy.MRV.setDefaultReturnValuesSupplier("getActions",
				(Supplier<List<Action>>) () -> actionList);
	}

	@Test
	public void testConvertSearchActionForRecordTypeAndSearchRecordId() throws Exception {
		ClientDataRecordSpy dataRecordSpy = createDataRecordToJsonConverterUsingDataRecordSpy();
		dataRecordSpy.MRV.setDefaultReturnValuesSupplier("hasActions",
				(Supplier<Boolean>) () -> true);
		dataRecordSpy.MRV.setDefaultReturnValuesSupplier("getType",
				(Supplier<String>) () -> "recordType");
		dataGroup.MRV.setSpecificReturnValuesSupplier("containsChildWithNameInData",
				(Supplier<Boolean>) () -> true, "search");
		ClientDataRecordSpy searchGroup = new ClientDataRecordSpy();
		searchGroup.MRV.setSpecificReturnValuesSupplier("getFirstAtomicValueWithNameInData",
				(Supplier<String>) () -> "someSearchId", "linkedRecordId");
		dataGroup.MRV.setSpecificReturnValuesSupplier("getFirstGroupWithNameInData",
				(Supplier<ClientDataGroup>) () -> searchGroup, "search");

		dataRecordToJsonConverter.toJsonObjectBuilder();

		assertSearchRecordIdIsFromDataGroupRecord(dataRecordSpy);

	}

	private void assertSearchRecordIdIsFromDataGroupRecord(DataRecordSpy dataRecordSpy) {
		dataRecordSpy.MCR.assertNumberOfCallsToMethod("getDataGroup", 2);
		ClientDataRecordSpy dataGroup = (ClientDataRecordSpy) dataRecordSpy.MCR
				.getReturnValue("getDataGroup", 1);
		dataGroup.MCR.assertParameters("containsChildWithNameInData", 0, "search");
		ClientDataRecordSpy searchGroup = (ClientDataRecordSpy) dataGroup.MCR
				.getReturnValue("getFirstGroupWithNameInData", 0);
		searchGroup.MCR.assertParameters("getFirstAtomicValueWithNameInData", 0, "linkedRecordId");

		String searchId = (String) searchGroup.MCR
				.getReturnValue("getFirstAtomicValueWithNameInData", 0);

		ActionsConverterData actionConverter = (ActionsConverterData) actionsConverterSpy.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("toJsonObjectBuilder", 0,
						"actionsConverterData");
		assertSame(actionConverter.searchRecordId, searchId);
	}

	@Test
	public void testConvertSearchActionForRecordTypeAndSearchRecordIdOnlyForRecordType()
			throws Exception {
		ClientDataRecordSpy dataRecordSpy = createDataRecordToJsonConverterUsingDataRecordSpy();
		dataRecordSpy.MRV.setDefaultReturnValuesSupplier("hasActions",
				(Supplier<Boolean>) () -> true);
		dataRecordSpy.MRV.setDefaultReturnValuesSupplier("getType",
				(Supplier<String>) () -> "otherThanRecordType");

		dataRecordToJsonConverter.toJsonObjectBuilder();

		assertSearchRecordIdNotSet();
	}

	private void assertSearchRecordIdNotSet() {
		ActionsConverterData actionConverter = (ActionsConverterData) actionsConverterSpy.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("toJsonObjectBuilder", 0,
						"actionsConverterData");
		assertSame(actionConverter.searchRecordId, null);
	}

	@Test
	public void testConvertSearchActionForRecordTypeAndSearchRecordIdButNoSearchDefinedInDataGroup()
			throws Exception {
		ClientDataRecordSpy dataRecordSpy = createDataRecordToJsonConverterUsingDataRecordSpy();
		dataRecordSpy.MRV.setDefaultReturnValuesSupplier("hasActions",
				(Supplier<Boolean>) () -> true);
		dataRecordSpy.MRV.setDefaultReturnValuesSupplier("getType",
				(Supplier<String>) () -> "recordType");

		dataRecordToJsonConverter.toJsonObjectBuilder();

		assertSearchRecordIdNotSet();
	}
}
