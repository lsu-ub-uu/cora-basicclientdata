/*
 * Copyright 2015 Uppsala University Library
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

package se.uu.ub.cora.clientbasicdata.converter.datatojson;

import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientDataAttribute;
import se.uu.ub.cora.clientdata.ClientDataChild;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.converter.DataToJsonConverter;
import se.uu.ub.cora.clientdata.converter.DataToJsonConverterFactory;
import se.uu.ub.cora.json.builder.JsonArrayBuilder;
import se.uu.ub.cora.json.builder.JsonBuilderFactory;
import se.uu.ub.cora.json.builder.JsonObjectBuilder;

public class DataGroupToJsonConverter implements DataToJsonConverter {

	private ClientDataGroup dataGroup;
	JsonObjectBuilder dataGroupJsonObjectBuilder;
	JsonBuilderFactory jsonBuilderFactory;
	DataToJsonConverterFactory converterFactory;

	public static DataToJsonConverter usingConverterFactoryAndBuilderFactoryAndDataGroup(
			DataToJsonConverterFactory converterFactory, JsonBuilderFactory builderFactory,
			ClientDataGroup dataGroup) {
		return new DataGroupToJsonConverter(converterFactory, builderFactory, dataGroup);
	}

	DataGroupToJsonConverter(DataToJsonConverterFactory converterFactory,
			JsonBuilderFactory builderFactory, ClientDataGroup dataGroup) {
		this.converterFactory = converterFactory;
		this.jsonBuilderFactory = builderFactory;
		this.dataGroup = dataGroup;
		dataGroupJsonObjectBuilder = builderFactory.createObjectBuilder();
	}

	@Override
	public JsonObjectBuilder toJsonObjectBuilder() {
		possiblyAddRepeatId();
		if (dataGroup.hasAttributes()) {
			addAttributesToGroup();
		}
		if (dataGroup.hasChildren()) {
			addChildrenToGroup();
		}
		hookForSubclassesToImplementExtraConversion();
		dataGroupJsonObjectBuilder.addKeyString("name", dataGroup.getNameInData());
		return dataGroupJsonObjectBuilder;
	}

	/**
	 * hookForSubclassesToImplementExtraConversion enables subclasses (converters for
	 * ClientDataRecordLink and ClientDataResourceLink) to add extra conversion needed to completely
	 * convert their classes to json
	 */
	void hookForSubclassesToImplementExtraConversion() {
		// No default implementation in this class
	}

	private void possiblyAddRepeatId() {
		if (hasNonEmptyRepeatId()) {
			dataGroupJsonObjectBuilder.addKeyString("repeatId", dataGroup.getRepeatId());
		}
	}

	private boolean hasNonEmptyRepeatId() {
		return dataGroup.getRepeatId() != null && !"".equals(dataGroup.getRepeatId());
	}

	private void addAttributesToGroup() {
		JsonObjectBuilder attributes = jsonBuilderFactory.createObjectBuilder();
		for (ClientDataAttribute attribute : dataGroup.getAttributes()) {
			attributes.addKeyString(attribute.getNameInData(), attribute.getValue());
		}
		dataGroupJsonObjectBuilder.addKeyJsonObjectBuilder("attributes", attributes);
	}

	void addChildrenToGroup() {
		JsonArrayBuilder childrenArray = jsonBuilderFactory.createArrayBuilder();
		for (ClientDataChild dataElement : dataGroup.getChildren()) {
			ClientConvertible convertible = (ClientConvertible) dataElement;
			childrenArray.addJsonObjectBuilder(
					converterFactory.factorUsingConvertible(convertible).toJsonObjectBuilder());
		}
		dataGroupJsonObjectBuilder.addKeyJsonArrayBuilder("children", childrenArray);
	}

	@Override
	public String toJson() {
		JsonObjectBuilder jsonObjectBuilder = toJsonObjectBuilder();
		return jsonObjectBuilder.toJsonFormattedPrettyString();
	}

	@Override
	public String toJsonCompactFormat() {
		JsonObjectBuilder jsonObjectBuilder = toJsonObjectBuilder();
		return jsonObjectBuilder.toJsonFormattedString();
	}
}
