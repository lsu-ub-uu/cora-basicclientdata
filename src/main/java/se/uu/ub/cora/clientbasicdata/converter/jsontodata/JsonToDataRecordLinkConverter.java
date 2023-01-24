/*
 * Copyright 2019 Uppsala University Library
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

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataRecordLink;
import se.uu.ub.cora.data.Convertible;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.converter.JsonToDataConverter;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;

public class JsonToDataRecordLinkConverter extends JsonToDataGroupConverter
		implements JsonToDataConverter {

	private static final int OPTIONAL_NUM_OF_CHILDREN = 3;
	private static final int MIN_NUM_OF_CHILDREN = 2;
	private static final int MAX_NUM_OF_CHILDREN = 4;

	public static JsonToDataRecordLinkConverter forJsonObject(JsonObject jsonObject) {
		return new JsonToDataRecordLinkConverter(jsonObject);
	}

	private JsonToDataRecordLinkConverter(JsonObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public Convertible toInstance() {
		BasicClientDataRecordLink recordLink = (BasicClientDataRecordLink) super.toInstance();
		throwErrorIfLinkChildrenAreIncorrect(recordLink);
		return recordLink;
	}

	@Override
	protected void createInstanceOfDataElement(String nameInData) {
		dataGroup = BasicClientDataRecordLink.withNameInData(nameInData);
	}

	private void throwErrorIfLinkChildrenAreIncorrect(DataGroup recordLink) {
		if (incorrectNumberOfChildren(recordLink) || missingMandatoryChildren(recordLink)
				|| maxNumOfChildrenButOneOptionalChildIsMissing(recordLink)
				|| okNumOfChildrenButOpitionChildrenMissing(recordLink)) {
			throw new JsonParseException(
					"RecordLinkData must contain children with name linkedRecordType and linkedRecordId "
							+ "and might contain child with name linkedRepeatId and linkedPath");
		}
	}

	private boolean incorrectNumberOfChildren(DataGroup recordLink) {
		int numberOfChildren = recordLink.getChildren().size();
		return numberOfChildren < MIN_NUM_OF_CHILDREN || numberOfChildren > MAX_NUM_OF_CHILDREN;
	}

	private boolean missingMandatoryChildren(DataGroup recordLink) {
		return childIsMissing(recordLink, "linkedRecordType")
				|| childIsMissing(recordLink, "linkedRecordId");
	}

	private boolean childIsMissing(DataGroup recordLink, String nameInData) {
		return !recordLink.containsChildWithNameInData(nameInData);
	}

	private boolean maxNumOfChildrenButOneOptionalChildIsMissing(DataGroup recordLink) {
		return recordLink.getChildren().size() == MAX_NUM_OF_CHILDREN
				&& (childIsMissing(recordLink, "linkedRepeatId")
						|| childIsMissing(recordLink, "linkedPath"));
	}

	private boolean okNumOfChildrenButOpitionChildrenMissing(DataGroup recordLink) {
		return recordLink.getChildren().size() == OPTIONAL_NUM_OF_CHILDREN
				&& childIsMissing(recordLink, "linkedRepeatId")
				&& childIsMissing(recordLink, "linkedPath");
	}

}
