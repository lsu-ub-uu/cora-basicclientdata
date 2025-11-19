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

import java.util.Map;
import java.util.Optional;
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
	private JsonToClientDataFactories factoriesRecord;

	@BeforeMethod
	private void beforeMethod() {
		factory = new JsonToClientDataConverterFactorySpy();
		actionLinkConverterFactory = new JsonToBasicClientDataActionLinkConverterFactorySpy();
		factoriesRecord = new JsonToClientDataFactories(factory, actionLinkConverterFactory);
	}

	@Test
	public void testFactoryIsSentAlong() {

		JsonObject jsonObject = (JsonObject) jsonParser.parseString("{\"record\":\"record\"}");

		jsonToDataConverter = JsonToBasicClientDataRecordConverter
				.usingConverterFactoriesAndJsonObject(factoriesRecord, jsonObject);

		assertSame(jsonToDataConverter.onlyForTestGetConverterFactory(), factory);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Record data must contain key: record")
	public void testNotARecordString() {
		parseStringAndCreateConverter("{\"not\":\"record\"}");
	}

	private ClientConvertible parseStringAndCreateConverter(String json) {
		JsonValue jsonValue = jsonParser.parseString(json);
		jsonToDataConverter = JsonToBasicClientDataRecordConverter
				.usingConverterFactoriesAndJsonObject(factoriesRecord, (JsonObject) jsonValue);

		return jsonToDataConverter.toInstance();
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Not an object")
	public void testRecordIsNotAnObject() {
		parseStringAndCreateConverter("{\"record\":\"record\"}");
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Record data must contain child with key: data")
	public void testRecordDoesNotContainData() {
		String json = "{\"record\":{";
		json += "\"notData\":\"notData\"";
		json += "}}";
		parseStringAndCreateConverter(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Not an object")
	public void testRecordDataNotAnObject() {
		String json = "{\"record\":{";
		json += "\"data\":\"notData\"";
		json += ",\"actionLinks\":\"noActionLink\"";
		json += "}}";
		parseStringAndCreateConverter(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Record data must contain only key: record")
	public void testRecordExtraKey() {
		String json = """
				{
					"record":{
						"data":{
							"name":"groupNameInData", "children":[]
						}
						,"actionLinks":"noActionLink"
					},
					"someExtraKey":"someExtraData"
				}
				""";
		parseStringAndCreateConverter(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Record data must contain keys: data and actionLinks "
			+ "and possibly permissions and otherProtocols")
	public void testRecordExtraKeyOnSecondLevel() {
		String json = """
				{
				  "record": {
				    "data": {
				      "name": "groupNameInData",
				      "children": []
				    },
				    "actionLinks": {
				      "read": {
				        "requestMethod": "GET",
				        "rel": "read"
				      }
				    },
				    "permissions": {
				      "read": [
				        "librisId"
				      ]
				    },
				    "extraKey": {
				      "name": "groupNameInData"
				    },
				    "otherProtocols": {
				      "iiif": {
				        "server": "someServer",
				        "identifier": "someIdentifier"
				      }
				    }
				  }
				}""";
		parseStringAndCreateConverter(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing jsonRecord: Record data must contain keys: data and actionLinks "
			+ "and possibly permissions and otherProtocols")
	public void testMaxNumberOfKeysOnSecondLevelNoPermissions() {
		String json = """
				  {"record": {
				    "data": {
				      "name": "groupNameInData",
				      "children": []
				    },
				    "actionLinks": {
				      "read": {
				        "requestMethod": "GET",
				        "rel": "read"
				      }
				    },
				    "NOTpermissions": {
				      "read": [
				        "librisId"
				      ]
				    },
					"otherProtocols":{"iiif":{"server":"someServer","identifier":"someIdentifier"}}
				  }
				}
				""";

		parseStringAndCreateConverter(json);

	}

	@Test
	public void providedFactoryIsUsedForActionLinks() {
		setActionLinkConvereterToReturnClientDataGroup();
		String json = """
				{
				"record":{
					"data":{
						"name":"groupNameInData"
						, "children":[]
					},
					"actionLinks":{
				 		"read":{
				 			"requestMethod":"GET",
				 			"rel":"read",
				 			"url":"https://cora.example.org/somesystem/rest/record/somerecordtype/somerecordid",
				 			"accept":"application/vnd.cora.record+json"
						}
					}
				}
				}""";

		ClientDataRecord clientDataRecord = (ClientDataRecord) parseStringAndCreateConverter(json);

		JsonObject actionLinkAsJsonObject = (JsonObject) actionLinkConverterFactory.MCR
				.getParameterForMethodAndCallNumberAndParameter("factor", 0, "jsonObject");
		String expectedJson = """
				{\
				"requestMethod":"GET",\
				"rel":"read",\
				"url":"https://cora.example.org/somesystem/rest/record/somerecordtype/somerecordid",\
				"accept":"application/vnd.cora.record+json"\
				}""";
		assertEquals(actionLinkAsJsonObject.toJsonFormattedString(), expectedJson);

		JsonToBasicClientDataActionLinkConverterSpy actionLinkConverter = (JsonToBasicClientDataActionLinkConverterSpy) actionLinkConverterFactory.MCR
				.getReturnValue("factor", 0);
		actionLinkConverter.MCR.assertParameters("toInstance", 0);

		actionLinkConverter.MCR.assertReturn("toInstance", 0,
				clientDataRecord.getActionLink(ClientAction.READ).get());
	}

	private void setActionLinkConvereterToReturnClientDataGroup() {
		ClientDataRecordGroupSpy clientDataRecordGroup = new ClientDataRecordGroupSpy();

		JsonToClientDataConverterSpy converter = new JsonToClientDataConverterSpy();
		converter.MRV.setDefaultReturnValuesSupplier("toInstance", () -> clientDataRecordGroup);
		factory.MRV.setDefaultReturnValuesSupplier("factorUsingJsonObject", () -> converter);
	}

	@Test
	public void testRecordNoActionLinks() {
		setActionLinkConvereterToReturnClientDataGroup();
		String json = """
				{
				"record":{
				"data":{
				"name":"groupNameInData"
				, "children":[]
				}
				}
				}""";

		ClientDataRecord clientDataRecord = (ClientDataRecord) parseStringAndCreateConverter(json);

		assertNoActionsAddedToTheRecord(clientDataRecord);
	}

	private void assertNoActionsAddedToTheRecord(ClientDataRecord clientDataRecord) {
		assertEquals(clientDataRecord.getActionLink(ClientAction.READ), Optional.empty());
		assertEquals(clientDataRecord.getActionLink(ClientAction.UPDATE), Optional.empty());
		assertEquals(clientDataRecord.getActionLink(ClientAction.DELETE), Optional.empty());
		assertEquals(clientDataRecord.getActionLink(ClientAction.READ_INCOMING_LINKS),
				Optional.empty());
		assertEquals(clientDataRecord.getActionLink(ClientAction.CREATE), Optional.empty());
		assertEquals(clientDataRecord.getActionLink(ClientAction.LIST), Optional.empty());
		assertEquals(clientDataRecord.getActionLink(ClientAction.SEARCH), Optional.empty());
		assertEquals(clientDataRecord.getActionLink(ClientAction.UPLOAD), Optional.empty());
		assertEquals(clientDataRecord.getActionLink(ClientAction.INDEX), Optional.empty());
		assertEquals(clientDataRecord.getActionLink(ClientAction.VALIDATE), Optional.empty());
		assertEquals(clientDataRecord.getActionLink(ClientAction.BATCH_INDEX), Optional.empty());
		assertEquals(clientDataRecord.getActionLink(ClientAction.RENEW), Optional.empty());
	}

	@Test
	public void testCheckReadPermissions() {
		setActionLinkConvereterToReturnClientDataGroup();

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
		setActionLinkConvereterToReturnClientDataGroup();

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

	@Test
	public void testOtherProtocols() {
		setActionLinkConvereterToReturnClientDataGroup();

		String json = """
				{"record":{"data":{"name":"groupNameInData","children":[]},
				"actionLinks":{"read":{"requestMethod":"GET","rel":"read"}},
				"permissions":{"write":["rating","parentId"]},
				"otherProtocols":{"iiif":{"server":"someServer","identifier":"someIdentifier"}}}}""";

		ClientDataRecord clientDataRecord = (ClientDataRecord) parseStringAndCreateConverter(json);

		assertTrue(clientDataRecord.hasProtocol("iiif"));
		Map<String, String> iiifProtocol = clientDataRecord.getProtocol("iiif");
		assertEquals(iiifProtocol.get("server"), "someServer");

	}

}
