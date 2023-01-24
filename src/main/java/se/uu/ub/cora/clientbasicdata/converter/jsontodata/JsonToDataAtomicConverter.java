/*
 * Copyright 2015, 2019, 2022 Uppsala University Library
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

import java.util.Map.Entry;

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataAtomic;
import se.uu.ub.cora.data.Convertible;
import se.uu.ub.cora.data.DataChild;
import se.uu.ub.cora.data.converter.JsonToDataConverter;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonString;
import se.uu.ub.cora.json.parser.JsonValue;

public final class JsonToDataAtomicConverter implements JsonToDataConverter {
	private static final String ATTRIBUTES = "attributes";
	private static final String REPEAT_ID = "repeatId";
	private static final int ALLOWED_MAX_NO_OF_ELEMENTS_AT_TOP_LEVEL = 4;
	private static final int ONE_OPTIONAL_KEY_PRESENT = 3;
	private static final String NAME = "name";
	private static final String VALUE = "value";
	private JsonObject jsonObject;

	static JsonToDataAtomicConverter forJsonObject(JsonObject jsonObject) {
		return new JsonToDataAtomicConverter(jsonObject);
	}

	private JsonToDataAtomicConverter(JsonObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	@Override
	public Convertible toInstance() {
		try {
			return tryToInstantiate();
		} catch (Exception e) {
			throw new JsonParseException("Error parsing jsonObject: " + e.getMessage(), e);
		}
	}

	private Convertible tryToInstantiate() {
		validateJsonData();
		return convertJsonToDataAtomic();
	}

	private void validateJsonData() {
		validateNameInData();
		validateValue();
		validateOptionalKeys();
		validateNoExtraElements();
	}

	private void validateNameInData() {
		if (keyMissingOrNotStringValueInJsonObject(NAME)) {
			throw new JsonParseException("Value of atomic data name must contain a String");
		}
	}

	private boolean keyMissingOrNotStringValueInJsonObject(String key) {
		return !jsonObject.containsKey(key) || !(jsonObject.getValue(key) instanceof JsonString);
	}

	private void validateValue() {
		if (keyMissingOrNotStringValueInJsonObject(VALUE)) {
			throw new JsonParseException("Value of atomic data value must contain a String");
		}
	}

	private void validateOptionalKeys() {
		if (oneOptionalKeyButRepeatIdAndAttributesMissing()
				|| maxOptionalKeysButRepeatIdOrAttributesMissing()) {
			throw new JsonParseException(
					"Atomic data can only contain string value for name, value, repeatId and attributes");
		}
	}

	private boolean oneOptionalKeyButRepeatIdAndAttributesMissing() {
		return jsonObject.keySet().size() == ONE_OPTIONAL_KEY_PRESENT
				&& keyMissingOrNotStringValueInJsonObject(REPEAT_ID) && !hasAttributes();
	}

	private boolean maxOptionalKeysButRepeatIdOrAttributesMissing() {
		return jsonObject.keySet().size() == ALLOWED_MAX_NO_OF_ELEMENTS_AT_TOP_LEVEL
				&& (keyMissingOrNotStringValueInJsonObject(REPEAT_ID) || !hasAttributes());
	}

	private boolean hasAttributes() {
		return jsonObject.containsKey(ATTRIBUTES);
	}

	private void validateNoExtraElements() {
		if (jsonObject.keySet().size() > ALLOWED_MAX_NO_OF_ELEMENTS_AT_TOP_LEVEL) {
			throw new JsonParseException("Atomic data can only contain name, value and repeatId");
		}
	}

	private BasicClientDataAtomic convertJsonToDataAtomic() {
		BasicClientDataAtomic dataAtomic = createFromJsonWithNameInDataAndValue();
		addRepeatIdFromJson(dataAtomic);
		possiblyAddAttributes(dataAtomic);
		return dataAtomic;
	}

	private BasicClientDataAtomic createFromJsonWithNameInDataAndValue() {
		String nameInData = getStringFromJson(NAME);
		String value = getStringFromJson(VALUE);
		return BasicClientDataAtomic.withNameInDataAndValue(nameInData, value);
	}

	private String getStringFromJson(String key) {
		return jsonObject.getValueAsJsonString(key).getStringValue();
	}

	private void addRepeatIdFromJson(BasicClientDataAtomic dataAtomic) {
		if (jsonObject.containsKey(REPEAT_ID)) {
			dataAtomic.setRepeatId(jsonObject.getValueAsJsonString(REPEAT_ID).getStringValue());
		}
	}

	private void possiblyAddAttributes(DataChild dataAtomic) {
		if (hasAttributes()) {
			JsonObject attributes = jsonObject.getValueAsJsonObject(ATTRIBUTES);
			for (Entry<String, JsonValue> attributeEntry : attributes.entrySet()) {
				addAttributeToGroup(dataAtomic, attributeEntry);
			}
		}
	}

	private void addAttributeToGroup(DataChild dataAtomic,
			Entry<String, JsonValue> attributeEntry) {
		String value = ((JsonString) attributeEntry.getValue()).getStringValue();
		dataAtomic.addAttributeByIdWithValue(attributeEntry.getKey(), value);
	}
}
