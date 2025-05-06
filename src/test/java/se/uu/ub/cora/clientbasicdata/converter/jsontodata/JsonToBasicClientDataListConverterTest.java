package se.uu.ub.cora.clientbasicdata.converter.jsontodata;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientData;
import se.uu.ub.cora.clientdata.ClientDataList;
import se.uu.ub.cora.clientdata.spies.JsonToClientDataConverterFactorySpy;
import se.uu.ub.cora.clientdata.spies.JsonToClientDataConverterSpy;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonValue;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;

public class JsonToBasicClientDataListConverterTest {

	private JsonToBasicClientDataListConverter toDataListConverter;
	private JsonToClientDataConverterFactorySpy factory;
	private OrgJsonParser jsonParser = new OrgJsonParser();

	@BeforeMethod
	private void beforeMethod() {
		factory = new JsonToClientDataConverterFactorySpy();
	}

	@Test
	public void testConstructorPassesVariables() throws Exception {

		JsonObject jsonObject = parseToJsonObject("{\"record\":\"record\"}");

		toDataListConverter = JsonToBasicClientDataListConverter
				.usingDataConverterAndJsonObject(factory, jsonObject);

		assertSame(toDataListConverter.onlyForTestGetConverterFactory(), factory);
		assertSame(toDataListConverter.onlyForTestGetJsonObject(), jsonObject);
	}

	private JsonObject parseToJsonObject(String json) {
		JsonValue jsonValue = jsonParser.parseString(json);
		return (JsonObject) jsonValue;
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing json: DataList must exist and must be an object.")
	public void testNotADataList() throws Exception {
		parseStringAndCreateConverter("{\"not\":\"dataList\"}");
	}

	private ClientConvertible parseStringAndCreateConverter(String json) {
		JsonObject jsonObject = parseToJsonObject(json);
		toDataListConverter = JsonToBasicClientDataListConverter
				.usingDataConverterAndJsonObject(factory, jsonObject);

		return toDataListConverter.toInstance();
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing json: DataList must exist and must be an object.")
	public void testDataListIsNotAnObject() throws Exception {
		parseStringAndCreateConverter("{\"dataList\":\"dataList\"}");
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing json: "
			+ "It must contains child with key: fromNo, data, totalNo, containDataOfType, toNo")
	public void testDataListMissesRequiredKeys() throws Exception {
		String json = """
				{"dataList":{
					"notData1":"notData",
					"notData2":"notData",
					"notData3":"notData",
					"notData4":"notData",
					"notData5":"notData"}}""";
		parseStringAndCreateConverter(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing json: DataList must have only one key: datalist")
	public void testOnlyDataListKeyOnFirstLevel() throws Exception {

		String json = """
				{"dataList":{
					"fromNo": "0",
					"data":"notData",
					"totalNo":"2",
					"containDataOfType":"demo",
					"toNo":"2"},
					"anotherKey": "anotherKeyValue"}""";
		parseStringAndCreateConverter(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing json: Datalist must have 5 key childs.")
	public void testOnlyDataListKeyOnSecondLevel() throws Exception {

		String json = """
				{"dataList":{
					"fromNo": "0",
					"data":"notData",
					"totalNo":"2",
					"containDataOfType":"demo",
					"toNo":"2",
					"anotherKey": "anotherKeyValue"}}""";
		parseStringAndCreateConverter(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing json: Data in Datalist is not an Array.")
	public void testOnlyDataListKeyDataIsAnArray() throws Exception {

		String json = """
				{"dataList":{
					"fromNo": "0",
					"data":"notAnArray",
					"totalNo":"2",
					"containDataOfType":"demo",
					"toNo":"2"}}""";
		parseStringAndCreateConverter(json);
	}

	@Test
	public void testToDataListWithOutRecords() throws Exception {
		String json = """
				{"dataList":{
					"fromNo": "0",
					"data":[],
					"totalNo":"0",
					"containDataOfType":"demo",
					"toNo":"0"}}""";
		ClientDataList clientDataList = (ClientDataList) parseStringAndCreateConverter(json);

		assertEquals(clientDataList.getFromNo(), "0");
		assertEquals(clientDataList.getTotalNumberOfTypeInStorage(), "0");
		assertEquals(clientDataList.getContainDataOfType(), "demo");
		assertEquals(clientDataList.getToNo(), "0");
		assertEquals(clientDataList.getDataList().size(), 0);
	}

	@Test
	public void testToDataListWithSRecords() throws Exception {
		setRecordConvereterToreturnClientDataRecord();

		String record = """
				{"record":{"data":{"children":[],"name":"%s"},\
				"actionLinks":{"read":{"requestMethod":"GET","rel":"read",\
				"url":"https://cora.example.org/somesystem/rest/record/somerecordtype/somerecordid"\
				,"accept":"application/vnd.cora.record+json"}}}}""";
		String record1 = record.formatted("record1");
		String record2 = record.formatted("record2");
		String json = """
				{"dataList": {
				    "fromNo": "0",
				    "data": [%s, %s],
				    "totalNo": "2",
				    "containDataOfType": "demo",
				    "toNo": "2"
				  }}""".formatted(record1, record2);

		ClientDataList clientDataList = (ClientDataList) parseStringAndCreateConverter(json);

		assertDataListKeys(clientDataList);
		assertRecordConverterCalled(record1, record2, clientDataList);
	}

	private void assertRecordConverterCalled(String record1, String record2,
			ClientDataList clientDataList) {
		factory.MCR.assertNumberOfCallsToMethod("factorUsingJsonObject", 2);

		assertCallToRecordConverterFactoryAndConversionToDataRecord(clientDataList, record1, 0);
		assertCallToRecordConverterFactoryAndConversionToDataRecord(clientDataList, record2, 1);
	}

	private void assertCallToRecordConverterFactoryAndConversionToDataRecord(
			ClientDataList clientDataList, String expectedJsonRecord, int callNumber) {
		assertCallToRecordConverterFactory(expectedJsonRecord, callNumber);
		assertDataRecordConverter(clientDataList, callNumber);
	}

	private void assertCallToRecordConverterFactory(String expectedJsonRecord, int callNumber) {
		JsonObject jsonObjectPassed = (JsonObject) factory.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("factorUsingJsonObject",
						callNumber, "jsonObject");
		assertEquals(jsonObjectPassed.toJsonFormattedString(), expectedJsonRecord);
	}

	private void assertDataRecordConverter(ClientDataList clientDataList, int callNumber) {
		JsonToClientDataConverterSpy recordConverter = (JsonToClientDataConverterSpy) factory.MCR
				.getReturnValue("factorUsingJsonObject", callNumber);
		recordConverter.MCR.assertMethodWasCalled("toInstance");
		ClientData dataRecord = (ClientData) recordConverter.MCR.getReturnValue("toInstance", 0);
		assertSame(clientDataList.getDataList().get(callNumber), dataRecord);
	}

	private void assertDataListKeys(ClientDataList clientDataList) {
		assertEquals(clientDataList.getFromNo(), "0");
		assertEquals(clientDataList.getTotalNumberOfTypeInStorage(), "2");
		assertEquals(clientDataList.getContainDataOfType(), "demo");
		assertEquals(clientDataList.getToNo(), "2");
		assertEquals(clientDataList.getDataList().size(), 2);
	}

	private void setRecordConvereterToreturnClientDataRecord() {
		/**
		 * Cheating a little bit. Returning same dataRecords object because jsonObject maps
		 * incorrectly as a parameter on MRV.setReturnValues
		 * 
		 * It is not a big problem for the test, but it would have been more realistic if we could
		 * have returned different dataRecords objects
		 */

		ClientData clientDataSpy = new ClientDataSpy();

		JsonToClientDataConverterSpy jsonToDataConverter = new JsonToClientDataConverterSpy();
		jsonToDataConverter.MRV.setDefaultReturnValuesSupplier("toInstance", () -> clientDataSpy);

		factory.MRV.setDefaultReturnValuesSupplier("factorUsingJsonObject",
				() -> jsonToDataConverter);

	}
}
