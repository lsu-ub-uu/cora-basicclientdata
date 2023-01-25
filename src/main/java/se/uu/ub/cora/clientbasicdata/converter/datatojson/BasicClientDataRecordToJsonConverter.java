/*
 * Copyright 2015, 2019, 2021, 2022 Uppsala University Library
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

import java.util.Set;

import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.converter.DataToJsonConverter;
import se.uu.ub.cora.clientdata.converter.DataToJsonConverterFactory;
import se.uu.ub.cora.json.builder.JsonArrayBuilder;
import se.uu.ub.cora.json.builder.JsonBuilderFactory;
import se.uu.ub.cora.json.builder.JsonObjectBuilder;

public class BasicClientDataRecordToJsonConverter implements DataToJsonConverter {

	DataToJsonConverterFactory converterFactory;
	JsonBuilderFactory builderFactory;
	BasicClientRecordActionsToJsonConverter actionsConverter;
	String baseUrl;
	ClientDataRecord dataRecord;
	private JsonObjectBuilder recordJsonObjectBuilder;

	public static BasicClientDataRecordToJsonConverter usingConverterFactoryAndActionsConverterAndBuilderFactoryAndBaseUrlAndDataRecord(
			DataToJsonConverterFactory converterFactory,
			BasicClientRecordActionsToJsonConverter actionsConverter, JsonBuilderFactory builderFactory,
			String baseUrl, ClientDataRecord dataRecord) {
		return new BasicClientDataRecordToJsonConverter(converterFactory, actionsConverter, builderFactory,
				baseUrl, dataRecord);
	}

	BasicClientDataRecordToJsonConverter(DataToJsonConverterFactory converterFactory,
			BasicClientRecordActionsToJsonConverter actionsConverter, JsonBuilderFactory builderFactory,
			String baseUrl, ClientDataRecord dataRecord) {
		this.converterFactory = converterFactory;
		this.actionsConverter = actionsConverter;
		this.builderFactory = builderFactory;
		this.baseUrl = baseUrl;
		this.dataRecord = dataRecord;
		recordJsonObjectBuilder = builderFactory.createObjectBuilder();
	}

	@Override
	public String toJson() {
		return toJsonObjectBuilder().toJsonFormattedPrettyString();
	}

	@Override
	public JsonObjectBuilder toJsonObjectBuilder() {
		convertMainDataGroup();
		possiblyConvertPermissions();
		possiblyConvertActions();
		return createTopLevelJsonObjectWithRecordAsChild();
	}

	private void possiblyConvertActions() {
		if (dataRecord.hasActions()) {
			BasicClientActionsConverterData actionsConverterData = collectDataForActions();
			possiblySetSearchIdFromRecordType(actionsConverterData);
			JsonObjectBuilder jsonObjectBuilder = actionsConverter
					.toJsonObjectBuilder(actionsConverterData);
			recordJsonObjectBuilder.addKeyJsonObjectBuilder("actionLinks", jsonObjectBuilder);
		}
	}

	private BasicClientActionsConverterData collectDataForActions() {
		BasicClientActionsConverterData actionsConverterData = new BasicClientActionsConverterData();
		actionsConverterData.recordType = dataRecord.getType();
		actionsConverterData.recordId = dataRecord.getId();
		actionsConverterData.actions.addAll(dataRecord.getActions());
		return actionsConverterData;
	}

	private void possiblySetSearchIdFromRecordType(BasicClientActionsConverterData actionsConverterData) {
		if (thisRecordIsRecordType()) {
			ClientDataGroup dataGroup = dataRecord.getDataGroup();
			possiblySetSearchRecordIdIfDefinedInDataGroup(actionsConverterData, dataGroup);
		}
	}

	private void possiblySetSearchRecordIdIfDefinedInDataGroup(
			BasicClientActionsConverterData actionsConverterData, ClientDataGroup dataGroup) {
		if (dataGroup.containsChildWithNameInData("search")) {
			ClientDataGroup searchGroup = dataGroup.getFirstGroupWithNameInData("search");
			actionsConverterData.searchRecordId = searchGroup
					.getFirstAtomicValueWithNameInData("linkedRecordId");
		}
	}

	private boolean thisRecordIsRecordType() {
		return "recordType".equals(dataRecord.getType());
	}

	private void convertMainDataGroup() {
		DataToJsonConverter dataToJsonConverter;
		dataToJsonConverter = createConverterForMainDataGroup();

		JsonObjectBuilder jsonDataGroupObjectBuilder = dataToJsonConverter.toJsonObjectBuilder();
		recordJsonObjectBuilder.addKeyJsonObjectBuilder("data", jsonDataGroupObjectBuilder);
	}

	private DataToJsonConverter createConverterForMainDataGroup() {
		if (actionLinksShouldBeCreated()) {
			String recordUrl = baseUrl + dataRecord.getType() + "/" + dataRecord.getId();
			return converterFactory.factorUsingBaseUrlAndRecordUrlAndConvertible(baseUrl, recordUrl,
					dataRecord.getDataGroup());
		}
		return converterFactory.factorUsingConvertible(dataRecord.getDataGroup());

	}

	private boolean actionLinksShouldBeCreated() {
		return baseUrl != null;
	}

	private void possiblyConvertPermissions() {
		if (recordHasPermissions()) {
			convertPermissions();
		}
	}

	private boolean recordHasPermissions() {
		return dataRecord.hasReadPermissions() || dataRecord.hasWritePermissions();
	}

	private void convertPermissions() {
		JsonObjectBuilder permissionsJsonObjectBuilder = builderFactory.createObjectBuilder();
		possiblyAddReadPermissions(permissionsJsonObjectBuilder);
		possiblyAddWritePermissions(permissionsJsonObjectBuilder);
		recordJsonObjectBuilder.addKeyJsonObjectBuilder("permissions",
				permissionsJsonObjectBuilder);
	}

	private void possiblyAddReadPermissions(JsonObjectBuilder permissionsJsonObjectBuilder) {
		if (dataRecord.hasReadPermissions()) {
			addReadPermissions(permissionsJsonObjectBuilder);
		}
	}

	private void addReadPermissions(JsonObjectBuilder permissionsJsonObjectBuilder) {
		JsonArrayBuilder readPermissionsArray = createJsonForPermissions(
				dataRecord.getReadPermissions());
		permissionsJsonObjectBuilder.addKeyJsonArrayBuilder("read", readPermissionsArray);
	}

	private JsonArrayBuilder createJsonForPermissions(Set<String> permissions) {
		JsonArrayBuilder permissionsBuilder = builderFactory.createArrayBuilder();
		for (String permission : permissions) {
			permissionsBuilder.addString(permission);
		}
		return permissionsBuilder;
	}

	private void possiblyAddWritePermissions(JsonObjectBuilder permissionsJsonObjectBuilder) {
		if (dataRecord.hasWritePermissions()) {
			addWritePermissions(permissionsJsonObjectBuilder);
		}
	}

	private void addWritePermissions(JsonObjectBuilder permissionsJsonObjectBuilder) {
		JsonArrayBuilder writePermissionsArray = createJsonForPermissions(
				dataRecord.getWritePermissions());
		permissionsJsonObjectBuilder.addKeyJsonArrayBuilder("write", writePermissionsArray);
	}

	private JsonObjectBuilder createTopLevelJsonObjectWithRecordAsChild() {
		JsonObjectBuilder rootWrappingJsonObjectBuilder = builderFactory.createObjectBuilder();
		rootWrappingJsonObjectBuilder.addKeyJsonObjectBuilder("record", recordJsonObjectBuilder);
		return rootWrappingJsonObjectBuilder;
	}

	@Override
	public String toJsonCompactFormat() {
		return toJsonObjectBuilder().toJsonFormattedString();
	}

}
