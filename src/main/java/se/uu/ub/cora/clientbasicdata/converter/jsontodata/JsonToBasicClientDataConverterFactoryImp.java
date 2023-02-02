/*
 * Copyright 2015, 2022 Uppsala University Library
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

	private static final int NUM_OF_RECORDLINK_CHILDREN = 2;
	private static final int NUM_OF_RECORDLINK_CHILDREN_ONE_OPTIONAL = 3;
	private static final int MAX_NUM_OF_RECORDLINK_CHILDREN = 4;
	private static final int NUM_OF_RESOURCELINK_CHILDREN = 4;

	@Override
	public JsonToClientDataConverter factorUsingString(String jsonString) {
		JsonParser jsonParser = new OrgJsonParser();
		JsonObject json = jsonParser.parseStringAsObject(jsonString);

		return factorUsingJsonObject(json);
	}

	@Override
	public JsonToClientDataConverter factorUsingJsonObject(JsonObject json) {
		if (!(json instanceof JsonObject)) {
			throw new JsonParseException("Json value is not an object, can not convert");
		}
		JsonObject jsonObject = json;

		if (isGroup(jsonObject)) {
			return createConverterForGroupOrLink(jsonObject);
		}
		if (isAtomicData(jsonObject)) {
			return JsonToBasicClientDataAtomicConverter.forJsonObject(jsonObject);
		}
		return JsonToBasicClientDataAttributeConverter.forJsonObject(jsonObject);
	}

	// JsonToClientDataConverter createConvertedForJsonValue(JsonValue json) {
	// if (!(json instanceof JsonObject)) {
	// throw new JsonParseException("Json value is not an object, can not convert");
	// }
	// JsonObject jsonObject = (JsonObject) json;
	//
	// if (isGroup(jsonObject)) {
	// return createConverterForGroupOrLink(jsonObject);
	// }
	// if (isAtomicData(jsonObject)) {
	// return JsonToBasicClientDataAtomicConverter.forJsonObject(jsonObject);
	// }
	// return JsonToBasicClientDataAttributeConverter.forJsonObject(jsonObject);
	// }

	private JsonToClientDataConverter createConverterForGroupOrLink(JsonObject jsonObject) {
		List<String> foundNames = extractChildNames(jsonObject);
		if (isRecordLink(foundNames)) {
			return JsonToBasicClientDataRecordLinkConverter.forJsonObject(jsonObject);
		}
		if (isResourceLink(foundNames)) {
			return JsonToBasicClientDataResourceLinkConverter.forJsonObject(jsonObject);
		}

		return JsonToBasicClientDataGroupConverter.forJsonObject(jsonObject);
	}

	private boolean isResourceLink(List<String> foundNames) {
		return foundNames.size() == NUM_OF_RESOURCELINK_CHILDREN && foundNames.contains("streamId")
				&& foundNames.contains("filename") && foundNames.contains("filesize")
				&& foundNames.contains("mimeType");
	}

	private boolean isRecordLink(List<String> foundNames) {
		return correctChildrenForLink(foundNames)
				|| correctChildrenForLinkWithPathAndRepeatId(foundNames)
				|| correctChildrenForLinkWithPath(foundNames);
	}

	private boolean correctChildrenForLink(List<String> foundNames) {
		return foundNames.size() == NUM_OF_RECORDLINK_CHILDREN
				&& foundNames.contains("linkedRecordType") && foundNames.contains("linkedRecordId");
	}

	private boolean correctChildrenForLinkWithPathAndRepeatId(List<String> foundNames) {
		return foundNames.size() == MAX_NUM_OF_RECORDLINK_CHILDREN
				&& (foundNames.contains("linkedPath") && foundNames.contains("linkedRepeatId"));
	}

	private boolean correctChildrenForLinkWithPath(List<String> foundNames) {
		return foundNames.size() == NUM_OF_RECORDLINK_CHILDREN_ONE_OPTIONAL
				&& foundNames.contains("linkedRecordType") && foundNames.contains("linkedRecordId")
				&& (foundNames.contains("linkedPath") || foundNames.contains("linkedRepeatId"));
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

	private String getNameInDataFromChild(JsonObject child) {
		return child.getValueAsJsonString("name").getStringValue();
	}

	private boolean isAtomicData(JsonObject jsonObject) {
		return jsonObject.containsKey("value");
	}

	private boolean isGroup(JsonObject jsonObject) {
		return jsonObject.containsKey("children");
	}
}
