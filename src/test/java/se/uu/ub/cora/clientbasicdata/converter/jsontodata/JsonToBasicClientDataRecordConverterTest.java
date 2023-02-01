package se.uu.ub.cora.clientbasicdata.converter.jsontodata;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.spies.ClientDataGroupSpy;
import se.uu.ub.cora.clientdata.spies.JsonToClientDataConverterFactorySpy;
import se.uu.ub.cora.clientdata.spies.JsonToClientDataConverterSpy;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonValue;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;

public class JsonToBasicClientDataRecordConverterTest {

	private JsonToClientDataConverterFactorySpy factory;
	private JsonToBasicClientDataRecordConverter jsonToDataConverter;
	private OrgJsonParser jsonParser = new OrgJsonParser();

	@BeforeMethod
	private void beforeMethod() {
		factory = new JsonToClientDataConverterFactorySpy();
	}

	@Test
	public void testFactoryIsSentAlong() throws Exception {
		jsonToDataConverter = JsonToBasicClientDataRecordConverter
				.usingConverterFactoryAndJsonObject(factory, null);

		assertSame(jsonToDataConverter.onlyForTestGetConverterFactory(), factory);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Record data must contain key: record")
	public void testNotARecordString() throws Exception {
		parseStringAndCreateConverter("{\"not\":\"record\"}");
	}

	private ClientConvertible parseStringAndCreateConverter(String json) {
		JsonValue jsonValue = jsonParser.parseString(json);
		jsonToDataConverter = JsonToBasicClientDataRecordConverter
				.usingConverterFactoryAndJsonObject(factory, (JsonObject) jsonValue);

		return jsonToDataConverter.toInstance();
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Not an object")
	public void testRecordIsNotAnObject() throws Exception {
		parseStringAndCreateConverter("{\"record\":\"record\"}");
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Record data must contain child with key: data")
	public void testRecordDoesNotContainData() throws Exception {
		String json = "{\"record\":{";
		json += "\"notData\":\"notData\"";
		json += "}}";
		parseStringAndCreateConverter(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Not an object")
	public void testRecordDataNotAnObject() throws Exception {
		String json = "{\"record\":{";
		json += "\"data\":\"notData\"";
		json += ",\"actionLinks\":\"noActionLink\"";
		json += "}}";
		parseStringAndCreateConverter(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Record data must contain only key: record")
	public void testRecordExtraKey() throws Exception {
		String json = "{\"record\":{";
		json += "\"data\":{";
		json += "\"name\":\"groupNameInData\", \"children\":[]";
		json += "}";
		json += ",\"actionLinks\":\"noActionLink\"";
		json += "}";
		json += ",\"someExtraKey\":\"someExtraData\"";
		json += "}";
		parseStringAndCreateConverter(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Record data must contain child with key: actionLinks")
	public void testRecordNoActionLinks() throws Exception {
		// String json = "{\"record\":{\"data\":{\"name\":\"groupNameInData\", \"children\":[]}}}";
		String json = "{\"record\":{\"data\":{\"name\":\"groupNameInData\",\"children\":[]},\"permissions\":{\"read\":[\"librisId\"],\"write\":[\"librisId\",\"rootOrganisation\"]}}}";

		parseStringAndCreateConverter(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Record data must contain only keys: data and actionLinks and permissions")
	public void testRecordExtraKeyOnSecondLevel() throws Exception {
		String json = "{\"record\":{\"data\":{\"name\":\"groupNameInData\",\"children\":[]},\"actionLinks\":{\"read\":{\"requestMethod\":\"GET\",\"rel\":\"read\"}},\"permissions\":{\"read\":[\"librisId\"]},\"extraKey\":{\"name\":\"groupNameInData\"}}}";
		parseStringAndCreateConverter(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Record data must contain only keys: data and actionLinks and permissions")
	public void testMaxNumberOfKeysOnSecondLevelNoPermissions() throws Exception {
		String json = "{\"record\":{\"data\":{\"name\":\"groupNameInData\",\"children\":[]},\"actionLinks\":{\"read\":{\"requestMethod\":\"GET\",\"rel\":\"read\"}},\"NOTpermissions\":{\"read\":[\"librisId\"]}}}";

		parseStringAndCreateConverter(json);

	}

	@Test
	public void providedFactoryIsUsedForDataGroup() throws Exception {
		ClientDataGroupSpy clientDataGroup = new ClientDataGroupSpy();

		JsonToClientDataConverterSpy jsonToDataConverter = new JsonToClientDataConverterSpy();
		jsonToDataConverter.MRV.setDefaultReturnValuesSupplier("toInstance", () -> clientDataGroup);
		factory.MRV.setDefaultReturnValuesSupplier("factorUsingJsonObject",
				() -> jsonToDataConverter);

		String json = "{\"record\":{\"data\":{";
		json += "\"name\":\"groupNameInData\", \"children\":[]";
		json += "}";
		json += ", \"actionLinks\":{";
		json += " \"read\":{";
		json += " \"requestMethod\":\"GET\",";
		json += " \"rel\":\"read\",";
		json += " \"url\":\"https://cora.example.org/somesystem/rest/record/somerecordtype/somerecordid\",";
		json += " \"accept\":\"application/vnd.uub.record+json\"";
		json += "}";
		json += "}";
		json += "}";
		json += "}";
		parseStringAndCreateConverter(json);
		// JsonToClientDataConverterFactoryForDataRecordSpy factorySpy =
		// (JsonToClientDataConverterFactoryForDataRecordSpy) factory;
		// assertEquals(factorySpy.numOfTimesFactoryCalled, 2);
		factory.MCR.assertNumberOfCallsToMethod("factorUsingJsonObject", 2);

		// JsonToClientDataConverterSpy groupConverterSpy = factorySpy.factoredConverters.get(0);
		// JsonObject jsonValueSentToConverter = groupConverterSpy.jsonValue;
		//
		// assertEquals(jsonValueSentToConverter.getValueAsJsonString("name").getStringValue(),
		// "groupNameInData");
	}

	@Test
	public void providedFactoryIsUsedForActionLinks() throws Exception {
		String json = "{\"record\":{\"data\":{";
		json += "\"name\":\"groupNameInData\", \"children\":[]";
		json += "}";
		json += ", \"actionLinks\":{";
		json += " \"read\":{";
		json += " \"requestMethod\":\"GET\",";
		json += " \"rel\":\"read\",";
		json += " \"url\":\"https://cora.example.org/somesystem/rest/record/somerecordtype/somerecordid\",";
		json += " \"accept\":\"application/vnd.uub.record+json\"";
		json += "}";
		json += "}";
		json += "}";
		json += "}";
		parseStringAndCreateConverter(json);
		JsonToClientDataConverterFactoryForDataRecordSpy factorySpy = (JsonToClientDataConverterFactoryForDataRecordSpy) factory;
		assertEquals(factorySpy.numOfTimesFactoryCalled, 2);

		JsonToClientDataActionLinkConverterSpy actionLinksConverterSpy = factorySpy.factoredActionLinksConverters
				.get(0);
		JsonObject readLink = actionLinksConverterSpy.jsonValue;

		assertEquals(readLink.getValueAsJsonString("requestMethod").getStringValue(), "GET");
		assertEquals(readLink.getValueAsJsonString("rel").getStringValue(), "read");
		assertEquals(readLink.getValueAsJsonString("url").getStringValue(),
				"https://cora.example.org/somesystem/rest/record/somerecordtype/somerecordid");
		assertEquals(readLink.getValueAsJsonString("accept").getStringValue(),
				"application/vnd.uub.record+json");
	}

	@Test
	public void testToClass() {
		String json = "{\"record\":{\"data\":{";
		json += "\"name\":\"groupNameInData\", \"children\":[]";
		json += "}";
		json += ", \"actionLinks\":{";
		json += " \"read\":{";
		json += " \"requestMethod\":\"GET\",";
		json += " \"rel\":\"read\",";
		json += "\"url\":\"https://cora.example.org/somesystem/rest/record/somerecordtype/somerecordid\",";
		json += " \"accept\":\"application/vnd.uub.record+json\"";
		json += "}";
		json += "}";
		json += ", \"permissions\":{}";
		json += "}";
		json += "}";
		ClientDataRecord clientDataRecord = parseStringAndCreateConverter(json);

		JsonToClientDataConverterFactoryForDataRecordSpy factorySpy = (JsonToClientDataConverterFactoryForDataRecordSpy) factory;
		JsonToClientDataConverterSpy groupConverterSpy = factorySpy.factoredConverters.get(0);
		JsonToClientDataActionLinkConverterSpy actionLinksConverterSpy = factorySpy.factoredActionLinksConverters
				.get(0);

		ClientDataGroup clientDataGroup = clientDataRecord.getClientDataGroup();
		assertEquals(groupConverterSpy.returnedElement, clientDataGroup);

		ActionLink actionLink = clientDataRecord.getActionLinks().get("read");
		assertEquals(actionLinksConverterSpy.returnedElement, actionLink);

	}

	@Test
	public void testCheckReadPermissions() {
		String json = "{\"record\":{\"data\":{\"name\":\"groupNameInData\",\"children\":[]},\"actionLinks\":{\"read\":{\"requestMethod\":\"GET\",\"rel\":\"read\"}},\"permissions\":{\"read\":[\"librisId\", \"topLevel\"]}}}";
		ClientDataRecord clientDataRecord = parseStringAndCreateConverter(json);

		Set<String> readPermissions = clientDataRecord.getReadPermissions();
		assertEquals(readPermissions.size(), 2);
		assertTrue(readPermissions.contains("librisId"));
		assertTrue(readPermissions.contains("topLevel"));

	}

	@Test
	public void testCheckWritePermissions() {
		String json = "{\"record\":{\"data\":{\"name\":\"groupNameInData\",\"children\":[]},\"actionLinks\":{\"read\":{\"requestMethod\":\"GET\",\"rel\":\"read\"}},\"permissions\":{\"write\":[\"rating\",\"parentId\"]}}}";
		ClientDataRecord clientDataRecord = parseStringAndCreateConverter(json);

		Set<String> writePermissions = clientDataRecord.getWritePermissions();
		assertEquals(writePermissions.size(), 2);
		assertTrue(writePermissions.contains("rating"));
		assertTrue(writePermissions.contains("parentId"));

	}

}
