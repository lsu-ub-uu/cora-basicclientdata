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

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataResourceLink;
import se.uu.ub.cora.data.Convertible;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.converter.JsonToDataConverter;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParseException;

public class JsonToDataResourceLinkConverter extends JsonToDataGroupConverter
		implements JsonToDataConverter {

	private static final int NUM_OF_RESOURCELINK_CHILDREN = 4;

	private JsonToDataResourceLinkConverter(JsonObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public Convertible toInstance() {
		BasicClientDataResourceLink resourceLink = (BasicClientDataResourceLink) super.toInstance();
		throwErrorIfLinkChildrenAreIncorrect(resourceLink);
		return resourceLink;
	}

	private void throwErrorIfLinkChildrenAreIncorrect(DataGroup recordLink) {
		if (incorrectNumberOfChildren(recordLink) || incorrectChildren(recordLink)) {
			throw new JsonParseException(
					"ResourceLinkData must and can only contain children with name "
							+ "streamId and filename and filesize and mimeType");
		}
	}

	private boolean incorrectNumberOfChildren(DataGroup recordLink) {
		return recordLink.getChildren().size() != NUM_OF_RESOURCELINK_CHILDREN;
	}

	private boolean incorrectChildren(DataGroup recordLink) {
		return !recordLink.containsChildWithNameInData("streamId")
				|| !recordLink.containsChildWithNameInData("filename")
				|| !recordLink.containsChildWithNameInData("filesize")
				|| !recordLink.containsChildWithNameInData("mimeType");
	}

	public static JsonToDataResourceLinkConverter forJsonObject(JsonObject jsonObject) {
		return new JsonToDataResourceLinkConverter(jsonObject);
	}

	@Override
	protected void createInstanceOfDataElement(String nameInData) {
		dataGroup = BasicClientDataResourceLink.withNameInData(nameInData);
	}

}
