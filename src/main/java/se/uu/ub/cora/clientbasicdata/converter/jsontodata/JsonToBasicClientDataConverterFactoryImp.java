/*
 * Copyright 2015, 2022, 2023, 2025 Uppsala University Library
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

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverter;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterFactory;
import se.uu.ub.cora.json.parser.JsonArray;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonParser;
import se.uu.ub.cora.json.parser.JsonValue;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;

public class JsonToBasicClientDataConverterFactoryImp implements JsonToClientDataConverterFactory {

	private static final int NUM_OF_RESOURCELINK_CHILDREN = 3;

	@Override
	public JsonToClientDataConverter factorUsingString(String jsonString) {

		JsonParser jsonParser = new OrgJsonParser();
		JsonObject json = jsonParser.parseStringAsObject(jsonString);

		return factorUsingJsonObject(json);
	}

	@Override
	public JsonToClientDataConverter factorUsingJsonObject(JsonObject json) {
		verifyJsonObject(json);
		return createJsonToClientDataConverter(json);
	}

	private JsonToClientDataConverter createJsonToClientDataConverter(JsonObject json) {
		if (isDataList(json)) {
			return JsonToBasicClientDataListConverter.usingDataConverterAndJsonObject(this, json);
		}
		if (isRecord(json)) {
			return createJsonToClientDataRecordConverter(json);
		}
		if (hasChildren(json)) {
			return determineElementWithChildrenAndReturnConverter(json);
		}
		if (isAtomicData(json)) {
			return JsonToBasicClientDataAtomicConverter.forJsonObject(json);
		}
		if (isAuthentication(json)) {
			return createJsonToClientDataAuthenticationConverter(json);
		}
		return JsonToBasicClientDataAttributeConverter.forJsonObject(json);
	}

	private JsonToClientDataConverter determineElementWithChildrenAndReturnConverter(
			JsonObject json) {
		if (isRecordGroup(json)) {
			return JsonToBasicClientDataRecordGroupConverter.forJsonObject(json);
		}
		if (isResourceLink(json)) {
			return JsonToBasicClientDataResourceLinkConverter
					.usingActionLinkConverterFactoryforJsonObject(
							createActionLinkConverterFactory(), json);
		}
		if (isRecordLink(json)) {
			return JsonToBasicClientDataRecordLinkConverter
					.forJsonObject(createActionLinkConverterFactory(), json);
		}
		return JsonToBasicClientDataGroupConverter.forJsonObject(json);
	}

	private boolean isDataList(JsonObject json) {
		return json.containsKey("dataList");
	}

	private boolean isRecord(JsonObject jsonObject) {
		return jsonObject.containsKey("record");
	}

	private boolean isAuthentication(JsonObject jsonObject) {
		return jsonObject.containsKey("authentication");
	}

	private JsonToClientDataConverter createJsonToClientDataRecordConverter(JsonObject json) {
		JsonToClientDataFactories factories = createConverterFactories();
		return JsonToBasicClientDataRecordConverter.usingConverterFactoriesAndJsonObject(factories,
				json);
	}

	private JsonToClientDataConverter createJsonToClientDataAuthenticationConverter(
			JsonObject json) {
		JsonToClientDataFactories factories = createConverterFactories();
		return JsonToBasicClientDataAuthenticationConverter
				.usingConverterFactoriesAndJsonObject(factories, json);
	}

	private JsonToClientDataFactories createConverterFactories() {
		JsonToBasicClientDataActionLinkConverterFactory converterFactory = createActionLinkConverterFactory();
		return new JsonToClientDataFactories(this, converterFactory);
	}

	private JsonToBasicClientDataActionLinkConverterFactory createActionLinkConverterFactory() {
		return JsonToBasicClientDataActionLinkConverterFactoryImp
				.usingJsonToClientDataConverterFactory(this);
	}

	private void verifyJsonObject(JsonObject json) {
		if (!(json instanceof JsonObject)) {
			throw new JsonParseException("Json value is not an object, can not convert");
		}
	}

	private boolean isRecordGroup(JsonObject jsonObject) {
		return recordInfoExists(jsonObject);
	}

	private boolean recordInfoExists(JsonObject jsonObject) {
		List<String> foundNames = extractChildNames(jsonObject);
		return foundNames.contains("recordInfo");
	}

	private boolean isResourceLink(JsonObject json) {
		List<String> foundNames = extractChildNames(json);
		return foundNames.size() == NUM_OF_RESOURCELINK_CHILDREN
				&& foundNames.contains("linkedRecordType") && foundNames.contains("linkedRecordId")
				&& foundNames.contains("mimeType");
	}

	private boolean isRecordLink(JsonObject json) {
		List<String> foundNames = extractChildNames(json);
		return correctChildrenForLink(foundNames);
	}

	private boolean correctChildrenForLink(List<String> foundNames) {
		return foundNames.contains("linkedRecordType") && foundNames.contains("linkedRecordId");
	}

	private List<String> extractChildNames(JsonObject jsonObject) {
		JsonArray childrenArray = jsonObject.getValueAsJsonArray("children");
		List<String> foundNames = new ArrayList<>();
		for (JsonValue child : childrenArray) {
			String name = getNameInDataFromChild((JsonObject) child);
			foundNames.add(name);
		}
		return foundNames;
	}

	private boolean isAtomicData(JsonObject jsonObject) {
		return jsonObject.containsKey("value");
	}

	private boolean hasChildren(JsonObject jsonObject) {
		return jsonObject.containsKey("children");
	}

	private String getNameInDataFromChild(JsonObject child) {
		return child.getValueAsJsonString("name").getStringValue();
	}
}
