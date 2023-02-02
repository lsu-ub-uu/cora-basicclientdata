/*
 * Copyright 2015, 2018 Uppsala University Library
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

import static org.testng.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientbasicdata.data.BasicClientActionLink;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataAtomic;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataGroup;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataRecordLink;
import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecordLink;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterFactory;
import se.uu.ub.cora.json.builder.JsonBuilderFactory;
import se.uu.ub.cora.json.builder.org.OrgJsonBuilderFactoryAdapter;

public class BasicClientActionLinksToJsonConverterTest {
	private ClientActionLink actionLink;
	private Map<ClientAction, ClientActionLink> actionLinks;

	private ClientDataToJsonConverterFactory dataToJsonConverterFactory;
	private JsonBuilderFactory factory;

	@BeforeMethod
	public void beforeMethod() {
		factory = new OrgJsonBuilderFactoryAdapter();
		dataToJsonConverterFactory = BasicClientDataToJsonConverterFactory
				.usingBuilderFactory(factory);

		actionLink = createReadActionLink();
		actionLinks = new LinkedHashMap<>();
		actionLinks.put(ClientAction.READ, actionLink);
	}

	@Test
	public void testConvertToJsonCompact() {
		BasicClientActionLinksToJsonConverter converter = new BasicClientActionLinksToJsonConverter(
				factory, actionLinks, dataToJsonConverterFactory);

		String json = """
				{"read":{"requestMethod":"GET","rel":"read",\
				"contentType":"application/metadata_record+json",\
				"url":"http://localhost:8080/theclient/client/record/place/place:0001"\
				,"accept":"application/metadata_record+json"}}""";
		assertEquals(converter.toJsonCompactFormat(), json);
	}

	@Test
	public void testConvertToJson() {
		BasicClientActionLinksToJsonConverter converter = new BasicClientActionLinksToJsonConverter(
				factory, actionLinks, dataToJsonConverterFactory);

		String json = """
				{"read": {
				    "requestMethod": "GET",
				    "rel": "read",
				    "contentType": "application/metadata_record+json",
				    "url": "http://localhost:8080/theclient/client/record/place/place:0001",
				    "accept": "application/metadata_record+json"
				}}""";

		assertEquals(converter.toJson(), json);
	}

	private ClientActionLink createReadActionLink() {
		ClientActionLink actionLink = BasicClientActionLink.withAction(ClientAction.READ);
		actionLink.setAccept("application/metadata_record+json");
		actionLink.setContentType("application/metadata_record+json");
		actionLink.setRequestMethod("GET");
		actionLink.setURL("http://localhost:8080/theclient/client/record/place/place:0001");
		return actionLink;
	}

	@Test
	public void testConvertJsonWithBody() {
		ClientDataGroup workOrder = createWorkOrder();
		actionLink.setBody(workOrder);

		BasicClientActionLinksToJsonConverter converter = new BasicClientActionLinksToJsonConverter(
				factory, actionLinks, dataToJsonConverterFactory);

		String json = """
				{"read": {
				    "requestMethod": "GET",
				    "rel": "read",
				    "body": {
				        "children": [
				            {
				                "children": [
				                    {
				                        "name": "linkedRecordType",
				                        "value": "recordType"
				                    },
				                    {
				                        "name": "linkedRecordId",
				                        "value": "textSystemOne"
				                    }
				                ],
				                "name": "recordType"
				            },
				            {
				                "name": "recordId",
				                "value": "refItemText"
				            },
				            {
				                "name": "type",
				                "value": "index"
				            }
				        ],
				        "name": "workOrder"
				    },
				    "contentType": "application/metadata_record+json",
				    "url": "http://localhost:8080/theclient/client/record/place/place:0001",
				    "accept": "application/metadata_record+json"
				}}""";
		assertEquals(converter.toJson(), json);
	}

	private ClientDataGroup createWorkOrder() {
		ClientDataGroup workOrder = BasicClientDataGroup.withNameInData("workOrder");
		ClientDataRecordLink recordType = BasicClientDataRecordLink
				.usingNameInDataAndTypeAndId("recordType", "recordType", "textSystemOne");
		workOrder.addChild(recordType);
		workOrder.addChild(BasicClientDataAtomic.withNameInDataAndValue("recordId", "refItemText"));
		workOrder.addChild(BasicClientDataAtomic.withNameInDataAndValue("type", "index"));
		return workOrder;
	}
}
