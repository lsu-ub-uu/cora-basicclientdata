/*
 * Copyright 2023 Uppsala University Library
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
package se.uu.ub.cora.clientbasicdata.converter.jsontodata;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordGroupSpy;
import se.uu.ub.cora.clientdata.spies.JsonToClientDataConverterFactorySpy;
import se.uu.ub.cora.clientdata.spies.JsonToClientDataConverterSpy;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonValue;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;

public class JsonToBasicClientDataRecordConverterTest {

	private JsonToClientDataConverterFactorySpy factory;
	private JsonToBasicClientDataRecordConverter jsonToDataConverter;
	private JsonToBasicClientDataActionLinkConverterFactorySpy actionLinkConverterFactory;
	private OrgJsonParser jsonParser = new OrgJsonParser();

	@BeforeMethod
	private void beforeMethod() {
		factory = new JsonToClientDataConverterFactorySpy();
		actionLinkConverterFactory = new JsonToBasicClientDataActionLinkConverterFactorySpy();
	}

	@Test
	public void testFactoryIsSentAlong() throws Exception {
		jsonToDataConverter = JsonToBasicClientDataRecordConverter
				.usingConverterFactoryAndJsonObject(factory, null, null);

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
				.usingConverterFactoryAndJsonObject(factory, actionLinkConverterFactory,
						(JsonObject) jsonValue);

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
	public void providedFactoryIsUsedForActionLinks() throws Exception {
		setActionLinkConvereterToreturnClientDataGroup();

		String json = """
				{"record":{
					"data":{
						"name":"groupNameInData"
						, "children":[]
					},
					"actionLinks":{
				 		"read":{
				 			"requestMethod":"GET",
				 			"rel":"read",
				 			"url":"https://cora.example.org/somesystem/rest/record/somerecordtype/somerecordid",
				 			"accept":"application/vnd.uub.record+json"
						}
					}
				}
				}""";

		ClientDataRecord clientDataRecord = (ClientDataRecord) parseStringAndCreateConverter(json);

		JsonObject actionLinkAsJsonObject = (JsonObject) actionLinkConverterFactory.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("factor", 0, "jsonObject");
		String expectedJson = """
				{\
				"requestMethod":"GET",\
				"rel":"read",\
				"url":"https://cora.example.org/somesystem/rest/record/somerecordtype/somerecordid",\
				"accept":"application/vnd.uub.record+json"\
				}""";
		assertEquals(actionLinkAsJsonObject.toJsonFormattedString(), expectedJson);

		JsonToBasicClientDataActionLinkConverterSpy actionLinkConverter = (JsonToBasicClientDataActionLinkConverterSpy) actionLinkConverterFactory.MCR
				.getReturnValue("factor", 0);
		actionLinkConverter.MCR.assertParameters("toInstance", 0);

		actionLinkConverter.MCR.assertReturn("toInstance", 0,
				clientDataRecord.getActionLink(ClientAction.READ).get());
	}

	private void setActionLinkConvereterToreturnClientDataGroup() {
		ClientDataRecordGroupSpy clientDataRecordGroup = new ClientDataRecordGroupSpy();

		JsonToClientDataConverterSpy jsonToDataConverter = new JsonToClientDataConverterSpy();
		jsonToDataConverter.MRV.setDefaultReturnValuesSupplier("toInstance",
				() -> clientDataRecordGroup);
		factory.MRV.setDefaultReturnValuesSupplier("factorUsingJsonObject",
				() -> jsonToDataConverter);
	}

	@Test
	public void testCheckReadPermissions() {
		setActionLinkConvereterToreturnClientDataGroup();

		String json = """
				{"record":{"data":{"name":"groupNameInData","children":[]},
				"actionLinks":{"read":{"requestMethod":"GET","rel":"read"}},
				"permissions":{"read":["librisId", "topLevel"]}}}""";
		ClientDataRecord clientDataRecord = (ClientDataRecord) parseStringAndCreateConverter(json);

		Set<String> readPermissions = clientDataRecord.getReadPermissions();
		assertEquals(readPermissions.size(), 2);
		assertTrue(readPermissions.contains("librisId"));
		assertTrue(readPermissions.contains("topLevel"));

	}

	@Test
	public void testCheckWritePermissions() {
		setActionLinkConvereterToreturnClientDataGroup();

		String json = """
				{"record":{"data":{"name":"groupNameInData","children":[]},
				"actionLinks":{"read":{"requestMethod":"GET","rel":"read"}},
				"permissions":{"write":["rating","parentId"]}}}""";
		ClientDataRecord clientDataRecord = (ClientDataRecord) parseStringAndCreateConverter(json);

		Set<String> writePermissions = clientDataRecord.getWritePermissions();
		assertEquals(writePermissions.size(), 2);
		assertTrue(writePermissions.contains("rating"));
		assertTrue(writePermissions.contains("parentId"));

	}

}
