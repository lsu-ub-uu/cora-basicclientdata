/*
 * Copyright 2015, 2019 Uppsala University Library
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

import se.uu.ub.cora.clientdata.ClientDataAtomic;
import se.uu.ub.cora.clientdata.ClientDataAttribute;
import se.uu.ub.cora.clientdata.converter.DataToJsonConverter;
import se.uu.ub.cora.json.builder.JsonBuilderFactory;
import se.uu.ub.cora.json.builder.JsonObjectBuilder;

public final class DataAtomicToJsonConverter implements DataToJsonConverter {

	private ClientDataAtomic dataAtomic;
	JsonBuilderFactory factory;

	public static DataToJsonConverter usingJsonBuilderFactoryAndDataAtomic(
			JsonBuilderFactory factory, ClientDataAtomic dataAtomic) {
		return new DataAtomicToJsonConverter(factory, dataAtomic);
	}

	private DataAtomicToJsonConverter(JsonBuilderFactory factory, ClientDataAtomic dataAtomic) {
		this.factory = factory;
		this.dataAtomic = dataAtomic;
	}

	@Override
	public JsonObjectBuilder toJsonObjectBuilder() {
		JsonObjectBuilder jsonObjectBuilder = factory.createObjectBuilder();

		jsonObjectBuilder.addKeyString("name", dataAtomic.getNameInData());
		jsonObjectBuilder.addKeyString("value", dataAtomic.getValue());
		possiblyAddRepeatId(jsonObjectBuilder);
		possiblyAddAttributes(jsonObjectBuilder);
		return jsonObjectBuilder;
	}

	private void possiblyAddRepeatId(JsonObjectBuilder jsonObjectBuilder) {
		if (hasNonEmptyRepeatId()) {
			jsonObjectBuilder.addKeyString("repeatId", dataAtomic.getRepeatId());
		}
	}

	private boolean hasNonEmptyRepeatId() {
		return dataAtomic.getRepeatId() != null && !"".equals(dataAtomic.getRepeatId());
	}

	private void possiblyAddAttributes(JsonObjectBuilder jsonObjectBuilder) {
		if (dataAtomic.hasAttributes()) {
			addAttributes(jsonObjectBuilder);
		}
	}

	private void addAttributes(JsonObjectBuilder jsonObjectBuilder) {
		JsonObjectBuilder attributes = factory.createObjectBuilder();
		for (ClientDataAttribute attribute : dataAtomic.getAttributes()) {
			attributes.addKeyString(attribute.getNameInData(), attribute.getValue());
		}
		jsonObjectBuilder.addKeyJsonObjectBuilder("attributes", attributes);
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
