/*
 * Copyright 2019, 2023 Uppsala University Library
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

import java.util.Map;

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataRecordLink;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverter;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;
import se.uu.ub.cora.json.parser.JsonValue;

public class JsonToBasicClientDataRecordLinkConverter extends JsonToBasicClientDataGroupConverter
		implements JsonToClientDataConverter {

	private static final String ERROR_MESSAGE_OPTIONAL_MISSING = "RecordLink must contain name and "
			+ "children. And it may contain actionLinks, attributes or repeatId";
	private static final int OPTIONAL_NUM_OF_CHILDREN = 3;
	private static final int NUM_OF_ALLOWED_KEYS_AT_TOP_LEVEL = 5;
	private static final int MIN_NUM_OF_CHILDREN = 2;
	private static final int MAX_NUM_OF_CHILDREN = 4;
	private static final String ACTION_LINKS = "actionLinks";
	private JsonToBasicClientDataActionLinkConverterFactory actionLinkConverterFactory;
	private BasicClientDataRecordLink recordLink;

	public static JsonToBasicClientDataRecordLinkConverter forJsonObject(
			JsonToBasicClientDataActionLinkConverterFactory actionLinkConverterFactory,
			JsonObject jsonObject) {
		return new JsonToBasicClientDataRecordLinkConverter(actionLinkConverterFactory, jsonObject);
	}

	private JsonToBasicClientDataRecordLinkConverter(
			JsonToBasicClientDataActionLinkConverterFactory actionLinkConverterFactory,
			JsonObject jsonObject) {
		super(jsonObject);
		this.actionLinkConverterFactory = actionLinkConverterFactory;
	}

	@Override
	public ClientConvertible toInstance() {
		recordLink = (BasicClientDataRecordLink) super.toInstance();
		throwErrorIfLinkChildrenAreIncorrect();
		possiblyAddActionLinks();
		return recordLink;
	}

	@Override
	protected void createInstanceOfDataElement(String nameInData) {
		dataGroup = BasicClientDataRecordLink.withNameInData(nameInData);
	}

	private void throwErrorIfLinkChildrenAreIncorrect() {
		if (incorrectNumberOfChildren(recordLink) || missingMandatoryChildren(recordLink)
				|| maxNumOfChildrenButOneOptionalChildIsMissing(recordLink)
				|| okNumOfChildrenButOpitionChildrenMissing(recordLink)) {
			throw new JsonParseException(
					"RecordLinkData must contain children with name linkedRecordType and linkedRecordId "
							+ "and might contain child with name linkedRepeatId and linkedPath");
		}
	}

	private boolean incorrectNumberOfChildren(ClientDataGroup recordLink) {
		int numberOfChildren = recordLink.getChildren().size();
		return numberOfChildren < MIN_NUM_OF_CHILDREN || numberOfChildren > MAX_NUM_OF_CHILDREN;
	}

	private boolean missingMandatoryChildren(ClientDataGroup recordLink) {
		return childIsMissing(recordLink, "linkedRecordType")
				|| childIsMissing(recordLink, "linkedRecordId");
	}

	private boolean childIsMissing(ClientDataGroup recordLink, String nameInData) {
		return !recordLink.containsChildWithNameInData(nameInData);
	}

	private boolean maxNumOfChildrenButOneOptionalChildIsMissing(ClientDataGroup recordLink) {
		return recordLink.getChildren().size() == MAX_NUM_OF_CHILDREN
				&& (childIsMissing(recordLink, "linkedRepeatId")
						|| childIsMissing(recordLink, "linkedPath"));
	}

	private boolean okNumOfChildrenButOpitionChildrenMissing(ClientDataGroup recordLink) {
		return recordLink.getChildren().size() == OPTIONAL_NUM_OF_CHILDREN
				&& childIsMissing(recordLink, "linkedRepeatId")
				&& childIsMissing(recordLink, "linkedPath");
	}

	private void possiblyAddActionLinks() {
		if (jsonObject.containsKey(ACTION_LINKS)) {
			JsonObject actionLinks = jsonObject.getValueAsJsonObject(ACTION_LINKS);
			for (Map.Entry<String, JsonValue> actionLinkEntry : actionLinks.entrySet()) {
				convertAndAddActionLink(actionLinkEntry);
			}
		}
	}

	private void convertAndAddActionLink(Map.Entry<String, JsonValue> actionLinkEntry) {
		JsonToBasicClientDataActionLinkConverter actionLinkConverter = actionLinkConverterFactory
				.factor((JsonObject) actionLinkEntry.getValue());
		ClientActionLink actionLink = actionLinkConverter.toInstance();
		recordLink.addActionLink(actionLink);
	}

	@Override
	protected void validateNoOfKeysAtTopLevel() {
		if (moreKeysAtTopLevelThanPossible()) {
			throw new JsonParseException(
					"RecordLinkData data can only contain keys: name, children, actionLinks, "
							+ "repeatId and attributes");
		}
		if (threeKeysAtTopLevelButButAllOptionalAreMissing()) {
			throw new JsonParseException(ERROR_MESSAGE_OPTIONAL_MISSING);
		}
		if (maxKeysAtTopLevelButAnyOfTheOptionalsIsMissing()) {
			throw new JsonParseException(ERROR_MESSAGE_OPTIONAL_MISSING);
		}
	}

	private boolean threeKeysAtTopLevelButButAllOptionalAreMissing() {
		return jsonObject.keySet().size() == ONE_OPTIONAL_KEY_IS_PRESENT && !hasAttributes()
				&& !hasRepeatId() && !hasActionLinks();
	}

	protected boolean hasActionLinks() {
		return jsonObject.containsKey(ACTION_LINKS);
	}

	private boolean maxKeysAtTopLevelButAnyOfTheOptionalsIsMissing() {
		return jsonObject.keySet().size() == NUM_OF_ALLOWED_KEYS_AT_TOP_LEVEL
				&& (!hasAttributes() || !hasRepeatId() || !hasActionLinks());
	}

	private boolean moreKeysAtTopLevelThanPossible() {
		return jsonObject.keySet().size() > NUM_OF_ALLOWED_KEYS_AT_TOP_LEVEL;
	}

	public JsonToBasicClientDataActionLinkConverterFactory onlyForTestGetActionLinkConverterFactory() {
		return actionLinkConverterFactory;
	}

}
