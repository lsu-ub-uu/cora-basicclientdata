/*
 * Copyright 2015, 2019, 2021 Uppsala University Library
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

import se.uu.ub.cora.clientdata.ClientData;
import se.uu.ub.cora.clientdata.ClientDataList;
import se.uu.ub.cora.clientdata.converter.DataToJsonConverter;
import se.uu.ub.cora.clientdata.converter.DataToJsonConverterFactory;
import se.uu.ub.cora.json.builder.JsonArrayBuilder;
import se.uu.ub.cora.json.builder.JsonBuilderFactory;
import se.uu.ub.cora.json.builder.JsonObjectBuilder;

public class DataListToJsonConverter implements DataToJsonConverter {
	DataToJsonConverterFactory converterFactory;
	JsonBuilderFactory builderFactory;
	ClientDataList dataList;
	private JsonObjectBuilder dataListBuilder;
	private JsonArrayBuilder dataBuilder;

	public static DataListToJsonConverter usingJsonFactoryForDataList(
			DataToJsonConverterFactory converterFactory, JsonBuilderFactory builderFactory,
			ClientDataList restRecordList) {
		return new DataListToJsonConverter(converterFactory, builderFactory, restRecordList);
	}

	DataListToJsonConverter(DataToJsonConverterFactory converterFactory,
			JsonBuilderFactory builderFactory, ClientDataList dataList) {
		this.converterFactory = converterFactory;
		this.builderFactory = builderFactory;
		this.dataList = dataList;
		dataListBuilder = builderFactory.createObjectBuilder();
	}

	@Override
	public JsonObjectBuilder toJsonObjectBuilder() {
		addBasicListInfoToDataListBuilder();
		createDataBuilderAndAddAsDataToDataListBuilder();
		addAllRecordsOrGroupsFromListToDataBuilder();
		return createRootBuilderAndAddDataListBuilder();
	}

	private void addBasicListInfoToDataListBuilder() {
		dataListBuilder.addKeyString("totalNo", dataList.getTotalNumberOfTypeInStorage());
		dataListBuilder.addKeyString("fromNo", dataList.getFromNo());
		dataListBuilder.addKeyString("toNo", dataList.getToNo());
		dataListBuilder.addKeyString("containDataOfType", dataList.getContainDataOfType());
	}

	private void createDataBuilderAndAddAsDataToDataListBuilder() {
		dataBuilder = builderFactory.createArrayBuilder();
		dataListBuilder.addKeyJsonArrayBuilder("data", dataBuilder);
	}

	private void addAllRecordsOrGroupsFromListToDataBuilder() {
		for (ClientData data : dataList.getDataList()) {
			DataToJsonConverter dataConverter = converterFactory.factorUsingConvertible(data);
			JsonObjectBuilder jsonObjectBuilder = dataConverter.toJsonObjectBuilder();
			dataBuilder.addJsonObjectBuilder(jsonObjectBuilder);
		}
	}

	private JsonObjectBuilder createRootBuilderAndAddDataListBuilder() {
		JsonObjectBuilder rootWrappingJsonObjectBuilder = builderFactory.createObjectBuilder();
		rootWrappingJsonObjectBuilder.addKeyJsonObjectBuilder("dataList", dataListBuilder);
		return rootWrappingJsonObjectBuilder;
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
