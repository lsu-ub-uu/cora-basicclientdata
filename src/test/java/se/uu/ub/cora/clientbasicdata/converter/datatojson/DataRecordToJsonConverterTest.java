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

import se.uu.ub.cora.clientbasicdata.converter.datatojson.ClientDataRecordToJsonConverter;
import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientDataGroup;
//import se.uu.ub.cora.basicdata.data.spy.ClientDataRecordSpy;
import se.uu.ub.cora.data.converter.ClientDataToJsonConverter;
import se.uu.ub.cora.json.builder.JsonObjectBuilder;
import se.uu.ub.cora.testspies.data.ClientDataGroupSpy;
import se.uu.ub.cora.testspies.data.ClientDataRecordSpy;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class ClientDataRecordToJsonConverterTest {
	private ClientDataRecordSpy dataRecord;
	private ClientDataRecordToJsonConverter dataRecordToJsonConverter;
	private JsonBuilderFactorySpy builderFactory;

	private ClientDataToJsonConverterFactorySpy converterFactory;
	private String baseUrl = "some/base/url/";
	private ClientDataGroupSpy dataGroup;
	private RecordActionsToJsonConverterSpy actionsConverterSpy;

	@BeforeMethod
	public void setUp() {
		converterFactory = new ClientDataToJsonConverterFactorySpy();
		actionsConverterSpy = new RecordActionsToJsonConverterSpy();
		builderFactory = new JsonBuilderFactorySpy();
		dataGroup = new ClientDataGroupSpy();
		dataRecord = new ClientDataRecordSpy();
		dataRecord.MRV.setDefaultReturnValuesSupplier("getDataGroup",
				(Supplier<ClientDataGroup>) () -> dataGroup);
	}

	private void createDataRecordToJsonConverter() {
		dataRecordToJsonConverter = ClientDataRecordToJsonConverter
				.usingConverterFactoryAndActionsConverterAndBuilderFactoryAndBaseUrlAndDataRecord(
						converterFactory, actionsConverterSpy, builderFactory, baseUrl, dataRecord);
	}

	@Test
	public void testConverterImplementsDataToJsonConverter() throws Exception {
		createDataRecordToJsonConverter();
		assertTrue(dataRecordToJsonConverter instanceof ClientDataToJsonConverter);
	}

	@Test
	public void testConverterFactoryUsedToCreateConverterForMainDataGroupNoBaseUrl()
			throws Exception {
		dataRecordToJsonConverter = ClientDataRecordToJsonConverter
				.usingConverterFactoryAndActionsConverterAndBuilderFactoryAndBaseUrlAndDataRecord(
						converterFactory, actionsConverterSpy, builderFactory, null, dataRecord);

		JsonObjectBuilder returnedJsonObjectBuilder = dataRecordToJsonConverter
				.toJsonObjectBuilder();

		converterFactory.MCR.assertMethodNotCalled("factorUsingBaseUrlAndRecordUrlAndConvertible");
		converterFactory.MCR.assertParameters("factorUsingConvertible", 0,
				dataRecord.getDataGroup());

		ClientDataToJsonConverterSpy dataGroupConverter = (ClientDataToJsonConverterSpy) converterFactory.MCR
				.getReturnValue("factorUsingConvertible", 0);
		assertKeyDataAddedToRecordBuilderIsBuilderFromDataGroupConverter(dataGroupConverter);

		JsonObjectBuilderSpy recordBuilder = getRecordBuilderFromSpy();
		JsonObjectBuilderSpy rootWrappingBuilder = getRootWrappingBuilder();
		rootWrappingBuilder.MCR.assertParameters("addKeyJsonObjectBuilder", 0, "record",
				recordBuilder);
		assertSame(returnedJsonObjectBuilder, rootWrappingBuilder);
	}

	private void assertKeyDataAddedToRecordBuilderIsBuilderFromDataGroupConverter(
			ClientDataToJsonConverterSpy dataGroupConverter) {
		JsonObjectBuilderSpy dataGroupBuilder = (JsonObjectBuilderSpy) dataGroupConverter.MCR
				.getReturnValue("toJsonObjectBuilder", 0);

		JsonObjectBuilderSpy recordBuilder = getRecordBuilderFromSpy();
		recordBuilder.MCR.assertParameters("addKeyJsonObjectBuilder", 0, "data", dataGroupBuilder);
	}

	private JsonObjectBuilderSpy getRootWrappingBuilder() {
		return (JsonObjectBuilderSpy) builderFactory.MCR.getReturnValue("createObjectBuilder", 1);
	}

	private JsonObjectBuilderSpy getRecordBuilderFromSpy() {
		return (JsonObjectBuilderSpy) builderFactory.MCR.getReturnValue("createObjectBuilder", 0);
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

		ClientDataToJsonConverterSpy dataGroupConverter = (ClientDataToJsonConverterSpy) converterFactory.MCR
				.getReturnValue("factorUsingBaseUrlAndRecordUrlAndConvertible", 0);
		assertKeyDataAddedToRecordBuilderIsBuilderFromDataGroupConverter(dataGroupConverter);

		JsonObjectBuilderSpy recordBuilder = getRecordBuilderFromSpy();
		JsonObjectBuilderSpy rootWrappingBuilder = getRootWrappingBuilder();
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
		JsonObjectBuilderSpy permissionBuilder = getPermissionBuilderFromSpy();
		JsonObjectBuilderSpy recordBuilder = getRecordBuilderFromSpy();

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

	private JsonObjectBuilderSpy getPermissionBuilderFromSpy() {
		return (JsonObjectBuilderSpy) builderFactory.MCR.getReturnValue("createObjectBuilder", 1);
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
		ClientDataRecordToJsonConverterForTest forTest = new ClientDataRecordToJsonConverterForTest(
				builderFactory);

		String jsonString = forTest.toJson();

		forTest.MCR.assertMethodWasCalled("toJsonObjectBuilder");
		JsonObjectBuilderSpy builderSpy = (JsonObjectBuilderSpy) forTest.MCR
				.getReturnValue("toJsonObjectBuilder", 0);
		builderSpy.MCR.assertReturn("toJsonFormattedPrettyString", 0, jsonString);
		builderSpy.MCR.assertMethodWasCalled("toJsonFormattedPrettyString");
	}

	private class ClientDataRecordToJsonConverterForTest extends ClientDataRecordToJsonConverter {
		MethodCallRecorder MCR = new MethodCallRecorder();

		ClientDataRecordToJsonConverterForTest(JsonBuilderFactorySpy builderFactory) {
			super(null, null, builderFactory, null, null);
		}

		@Override
		public JsonObjectBuilder toJsonObjectBuilder() {
			MCR.addCall();
			JsonObjectBuilderSpy jsonObjectBuilderSpy = new JsonObjectBuilderSpy();
			MCR.addReturned(jsonObjectBuilderSpy);
			return jsonObjectBuilderSpy;
		}

	}

	@Test
	public void testToJsonCompactFormat() {
		ClientDataRecordToJsonConverterForTest forTest = new ClientDataRecordToJsonConverterForTest(
				builderFactory);

		String jsonString = forTest.toJsonCompactFormat();

		forTest.MCR.assertMethodWasCalled("toJsonObjectBuilder");
		JsonObjectBuilderSpy builderSpy = (JsonObjectBuilderSpy) forTest.MCR
				.getReturnValue("toJsonObjectBuilder", 0);
		builderSpy.MCR.assertReturn("toJsonFormattedString", 0, jsonString);
	}

	@Test
	public void testConvertActionsNoActions() throws Exception {
		ClientDataRecordSpy dataRecordSpy = createDataRecordToJsonConverterUsingDataRecordSpy();

		dataRecordToJsonConverter.toJsonObjectBuilder();

		dataRecordSpy.MCR.assertMethodNotCalled("getActions");
	}

	private ClientDataRecordSpy createDataRecordToJsonConverterUsingDataRecordSpy() {
		ClientDataRecordSpy dataRecordSpy = new ClientDataRecordSpy();
		dataRecordToJsonConverter = ClientDataRecordToJsonConverter
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

		JsonObjectBuilderSpy actionLinksBuilder = (JsonObjectBuilderSpy) actionsConverterSpy.MCR
				.getReturnValue("toJsonObjectBuilder", 0);
		JsonObjectBuilderSpy recordBuilder = (JsonObjectBuilderSpy) builderFactory.MCR
				.getReturnValue("createObjectBuilder", 0);

		recordBuilder.MCR.assertNumberOfCallsToMethod("addKeyJsonObjectBuilder", 2);
		recordBuilder.MCR.assertParameters("addKeyJsonObjectBuilder", 1, "actionLinks",
				actionLinksBuilder);

	}

	private void assertActionConverterData(ClientDataRecordSpy dataRecordSpy) {
		ClientActionsConverterData actionConverter = (ClientActionsConverterData) actionsConverterSpy.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("toJsonObjectBuilder", 0,
						"actionsConverterData");
		assertEquals(actionConverter.recordType, dataRecordSpy.getType());
		assertEquals(actionConverter.recordId, dataRecordSpy.getId());
		assertEquals(actionConverter.actions, dataRecordSpy.getActions());
		assertNull(actionConverter.searchRecordId);
	}

	private void addActionsToDataRecordSpy(ClientDataRecordSpy dataRecordSpy) {
		List<ClientAction> actionList = List.of(ClientAction.READ, ClientAction.UPDATE);

		dataRecordSpy.MRV.setDefaultReturnValuesSupplier("getActions",
				(Supplier<List<ClientAction>>) () -> actionList);
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
		ClientDataGroupSpy searchGroup = new ClientDataGroupSpy();
		searchGroup.MRV.setSpecificReturnValuesSupplier("getFirstAtomicValueWithNameInData",
				(Supplier<String>) () -> "someSearchId", "linkedRecordId");
		dataGroup.MRV.setSpecificReturnValuesSupplier("getFirstGroupWithNameInData",
				(Supplier<ClientDataGroup>) () -> searchGroup, "search");

		dataRecordToJsonConverter.toJsonObjectBuilder();

		assertSearchRecordIdIsFromDataGroupRecord(dataRecordSpy);

	}

	private void assertSearchRecordIdIsFromDataGroupRecord(ClientDataRecordSpy dataRecordSpy) {
		dataRecordSpy.MCR.assertNumberOfCallsToMethod("getDataGroup", 2);
		ClientDataGroupSpy dataGroup = (ClientDataGroupSpy) dataRecordSpy.MCR
				.getReturnValue("getDataGroup", 1);
		dataGroup.MCR.assertParameters("containsChildWithNameInData", 0, "search");
		ClientDataGroupSpy searchGroup = (ClientDataGroupSpy) dataGroup.MCR
				.getReturnValue("getFirstGroupWithNameInData", 0);
		searchGroup.MCR.assertParameters("getFirstAtomicValueWithNameInData", 0, "linkedRecordId");

		String searchId = (String) searchGroup.MCR
				.getReturnValue("getFirstAtomicValueWithNameInData", 0);

		ClientActionsConverterData actionConverter = (ClientActionsConverterData) actionsConverterSpy.MCR
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
		ClientActionsConverterData actionConverter = (ClientActionsConverterData) actionsConverterSpy.MCR
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
