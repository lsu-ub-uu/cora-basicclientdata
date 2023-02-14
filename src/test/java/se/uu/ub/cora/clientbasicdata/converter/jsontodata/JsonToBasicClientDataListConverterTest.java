package se.uu.ub.cora.clientbasicdata.converter.jsontodata;

import static org.testng.Assert.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.spies.JsonToClientDataConverterFactorySpy;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonValue;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;

public class JsonToBasicClientDataListConverterTest {

	private JsonToClientDataConverterFactorySpy factory;
	private OrgJsonParser jsonParser = new OrgJsonParser();
	private JsonToBasicClientDataListConverter toDataListConverter;

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
			+ "Error parsing json: It must contains the key: dataList")
	public void testNotADataList() throws Exception {
		parseStringAndCreateConverter("{\"not\":\"dataList\"}");
	}

	private ClientConvertible parseStringAndCreateConverter(String json) {
		JsonObject jsonObject = parseToJsonObject(json);
		toDataListConverter = JsonToBasicClientDataListConverter
				.usingDataConverterAndJsonObject(factory, (JsonObject) jsonObject);

		return toDataListConverter.toInstance();
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing json: It must exist a datalist object on the top.")
	public void testDataListIsNotAnObject() throws Exception {
		parseStringAndCreateConverter("{\"dataList\":\"dataList\"}");
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing json: It must contains child with key: data, fromNo, totalNo, containsDataOfType, toNo")
	public void testDataListDoesNotContainDataKey() throws Exception {
		String json = """
				{"dataList":{"notData":"notData"}}""";
		parseStringAndCreateConverter(json);
	}

	@Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing json: Datalist must have a child data that is an object.")
	public void testDataListDataKeyNotAnObject() throws Exception {

		String json = """
				{"dataList":{
					"fromNo": "0",
					"data":"notData",
					"totalNo":"2",
					"containDataOfType":"demo",
					"toNo":"2"}}""";
		parseStringAndCreateConverter(json);
	}

	// @Test(expectedExceptions = JsonParseException.class, expectedExceptionsMessageRegExp = ""
	// + "Error parsing json: Datalist must have a child data that is an object.")
	// public void testDataListDataKeyNotAnObject() throws Exception {
	//
	// String json = """
	// {"dataList":{
	// "fromNo": "0",
	// "data":"notData",
	// "totalNo":"2",
	// "containDataOfType":"demo",
	// "toNo":"2"}}""";
	// parseStringAndCreateConverter(json);
	// }

}
