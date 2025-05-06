/*
 * Copyright 2021 Uppsala University Library
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

import java.util.List;

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataAtomic;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataGroup;
import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverter;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterFactory;
import se.uu.ub.cora.json.builder.JsonBuilderFactory;
import se.uu.ub.cora.json.builder.JsonObjectBuilder;

public class BasicClientRecordActionsToJsonConverterImp implements BasicClientRecordActionsToJsonConverter {

	private static final String RECORD_TYPE = "recordType";
	private static final String ACCEPT = "accept";
	private static final String CONTENT_TYPE = "contentType";
	private static final String APPLICATION_VND_UUB_RECORD_LIST_JSON = "application/vnd.cora.recordList+json";
	private static final String APPLICATION_VND_UUB_RECORD_JSON = "application/vnd.cora.record+json";

	ClientDataToJsonConverterFactory converterFactory;
	JsonBuilderFactory builderFactory;
	String baseUrl;
	private JsonObjectBuilder mainBuilder;
	private List<ClientAction> actions;
	private String recordType;
	private String recordId;
	private String currentLowerCaseAction;
	private JsonObjectBuilder currentLinkBuilder;
	private String currentRequestMethod;
	private String currentUrl;
	private String currentAccept;
	private BasicClientActionsConverterData actionsConverterData;

	public static BasicClientRecordActionsToJsonConverterImp usingConverterFactoryAndBuilderFactoryAndBaseUrl(
			ClientDataToJsonConverterFactory converterFactory, JsonBuilderFactory builderFactory,
			String baseUrl) {
		return new BasicClientRecordActionsToJsonConverterImp(converterFactory, builderFactory, baseUrl);
	}

	private BasicClientRecordActionsToJsonConverterImp(ClientDataToJsonConverterFactory converterFactory,
			JsonBuilderFactory builderFactory, String baseUrl) {
		this.converterFactory = converterFactory;
		this.builderFactory = builderFactory;
		this.baseUrl = baseUrl;
		mainBuilder = builderFactory.createObjectBuilder();
	}

	@Override
	public JsonObjectBuilder toJsonObjectBuilder(BasicClientActionsConverterData actionsConverterData) {
		this.actionsConverterData = actionsConverterData;
		actions = actionsConverterData.actions;
		recordType = actionsConverterData.recordType;
		recordId = actionsConverterData.recordId;

		createJsonForActions();
		return mainBuilder;
	}

	private void createJsonForActions() {
		for (ClientAction action : actions) {
			setStandardForAction(action);

			possiblyCreateActionsForAll(action);
			possiblyCreateUploadActionForBinaryAndItsChildren(action);
			possiblyCreateSearchActionForSearchOrRecordType(action);
			if (RECORD_TYPE.equals(recordType)) {
				possiblyCreateActionsForRecordType(action);
			}
		}
	}

	private void possiblyCreateActionsForAll(ClientAction action) {
		if (action == ClientAction.READ) {
			addStandardParametersToCurrentLinkBuilder();
			currentLinkBuilder.addKeyString(ACCEPT, currentAccept);
		} else if (action == ClientAction.UPDATE) {
			currentRequestMethod = "POST";
			addStandardParametersToCurrentLinkBuilder();
			currentLinkBuilder.addKeyString(ACCEPT, currentAccept);
			currentLinkBuilder.addKeyString(CONTENT_TYPE, APPLICATION_VND_UUB_RECORD_JSON);
		} else if (action == ClientAction.READ_INCOMING_LINKS) {
			currentUrl = currentUrl + "/incomingLinks";
			addStandardParametersToCurrentLinkBuilder();
			currentLinkBuilder.addKeyString(ACCEPT, APPLICATION_VND_UUB_RECORD_LIST_JSON);
		} else if (action == ClientAction.DELETE) {
			currentRequestMethod = "DELETE";
			addStandardParametersToCurrentLinkBuilder();
		} else if (action == ClientAction.INDEX) {
			currentRequestMethod = "POST";
			currentUrl = baseUrl + "workOrder/";
			addStandardParametersToCurrentLinkBuilder();
			currentLinkBuilder.addKeyString(ACCEPT, currentAccept);
			currentLinkBuilder.addKeyString(CONTENT_TYPE, APPLICATION_VND_UUB_RECORD_JSON);
			createBody();
		}
	}

	private void possiblyCreateUploadActionForBinaryAndItsChildren(ClientAction action) {
		if (action == ClientAction.UPLOAD) {
			currentRequestMethod = "POST";
			currentUrl = baseUrl + recordType + "/" + recordId + "/master";
			addStandardParametersToCurrentLinkBuilder();
			currentLinkBuilder.addKeyString(CONTENT_TYPE, "multipart/form-data");
		}
	}

	private void possiblyCreateSearchActionForSearchOrRecordType(ClientAction action) {
		if (action == ClientAction.SEARCH) {
			String searchIdOrRecordId = setSearchRecordId();
			currentUrl = baseUrl + "searchResult/" + searchIdOrRecordId;
			addStandardParametersToCurrentLinkBuilder();
			currentLinkBuilder.addKeyString(ACCEPT, APPLICATION_VND_UUB_RECORD_LIST_JSON);
		}
	}

	private String setSearchRecordId() {
		if (searchIdIsSpecifiedOnThisRecord()) {
			return actionsConverterData.searchRecordId;
		}
		return recordId;
	}

	private boolean searchIdIsSpecifiedOnThisRecord() {
		return actionsConverterData.searchRecordId != null;
	}

	private void possiblyCreateActionsForRecordType(ClientAction action) {
		if (action == ClientAction.CREATE) {
			currentRequestMethod = "POST";
			String urlForRecordTypeActions = baseUrl + recordId + "/";
			currentUrl = urlForRecordTypeActions;
			addStandardParametersToCurrentLinkBuilder();
			currentLinkBuilder.addKeyString(ACCEPT, currentAccept);
			currentLinkBuilder.addKeyString(CONTENT_TYPE, APPLICATION_VND_UUB_RECORD_JSON);
		} else if (action == ClientAction.LIST) {
			currentRequestMethod = "GET";
			String urlForRecordTypeActions = baseUrl + recordId + "/";
			currentUrl = urlForRecordTypeActions;
			currentAccept = APPLICATION_VND_UUB_RECORD_LIST_JSON;
			addStandardParametersToCurrentLinkBuilder();
			currentLinkBuilder.addKeyString(ACCEPT, currentAccept);
		} else if (action == ClientAction.BATCH_INDEX) {
			currentRequestMethod = "POST";
			currentUrl = baseUrl + "index/" + recordId + "/";
			addStandardParametersToCurrentLinkBuilder();
			currentLinkBuilder.addKeyString(ACCEPT, currentAccept);
			currentLinkBuilder.addKeyString(CONTENT_TYPE, APPLICATION_VND_UUB_RECORD_JSON);
		} else if (action == ClientAction.VALIDATE) {
			createActionLinkForValidate();
		}
	}

	private void createActionLinkForValidate() {
		currentRequestMethod = "POST";
		currentUrl = baseUrl + "workOrder/";
		addStandardParametersToCurrentLinkBuilder();
		currentLinkBuilder.addKeyString(ACCEPT, currentAccept);
		currentLinkBuilder.addKeyString(CONTENT_TYPE, "application/vnd.cora.workorder+json");
	}

	private void setStandardForAction(ClientAction action) {
		currentLowerCaseAction = action.name().toLowerCase();
		currentLinkBuilder = builderFactory.createObjectBuilder();
		currentRequestMethod = "GET";
		String urlForActionsOnThisRecord = baseUrl + recordType + "/" + recordId;
		currentUrl = urlForActionsOnThisRecord;
		currentAccept = APPLICATION_VND_UUB_RECORD_JSON;
		mainBuilder.addKeyJsonObjectBuilder(currentLowerCaseAction, currentLinkBuilder);
	}

	private void createBody() {
		JsonObjectBuilder workOrderBuilder = convertBody();
		currentLinkBuilder.addKeyJsonObjectBuilder("body", workOrderBuilder);
	}

	private JsonObjectBuilder convertBody() {
		BasicClientDataGroup workOrder = createWorkOrderDataGroup();
		ClientDataToJsonConverter workOrderConverter = converterFactory.factorUsingConvertible(workOrder);
		return workOrderConverter.toJsonObjectBuilder();
	}

	private BasicClientDataGroup createWorkOrderDataGroup() {
		BasicClientDataGroup workOrder = BasicClientDataGroup.withNameInData("workOrder");
		BasicClientDataGroup recordTypeGroup = BasicClientDataGroup.withNameInData(RECORD_TYPE);
		recordTypeGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("linkedRecordType", RECORD_TYPE));
		recordTypeGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("linkedRecordId", recordType));
		workOrder.addChild(recordTypeGroup);
		workOrder.addChild(BasicClientDataAtomic.withNameInDataAndValue("recordId", recordId));
		workOrder.addChild(BasicClientDataAtomic.withNameInDataAndValue("type", "index"));
		return workOrder;
	}

	private void addStandardParametersToCurrentLinkBuilder() {
		currentLinkBuilder.addKeyString("rel", currentLowerCaseAction);
		currentLinkBuilder.addKeyString("url", currentUrl);
		currentLinkBuilder.addKeyString("requestMethod", currentRequestMethod);
	}
}
