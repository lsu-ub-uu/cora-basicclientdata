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

import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecordLink;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverter;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterFactory;
import se.uu.ub.cora.json.builder.JsonBuilderFactory;
import se.uu.ub.cora.json.builder.JsonObjectBuilder;

public class BasicClientDataRecordLinkToJsonConverter extends BasicClientDataGroupToJsonConverter
		implements ClientDataToJsonConverter {
	private static final String READ = "read";
	private static final String GET = "GET";

	String baseURL;
	ClientDataRecordLink dataRecordLink;

	public static BasicClientDataRecordLinkToJsonConverter usingConverterFactoryAndJsonBuilderFactoryAndDataRecordLinkAndBaseUrl(
			ClientDataToJsonConverterFactory converterFactory, JsonBuilderFactory jsonBuilderFactory,
			ClientDataRecordLink dataRecordLink, String baseURL) {
		return new BasicClientDataRecordLinkToJsonConverter(converterFactory, dataRecordLink, baseURL,
				jsonBuilderFactory);
	}

	private BasicClientDataRecordLinkToJsonConverter(ClientDataToJsonConverterFactory converterFactory,
			ClientDataRecordLink dataRecordLink, String baseURL,
			JsonBuilderFactory jsonBuilderFactory) {
		super(converterFactory, jsonBuilderFactory, (ClientDataGroup) dataRecordLink);
		this.dataRecordLink = dataRecordLink;
		this.baseURL = baseURL;
	}

	@Override
	void hookForSubclassesToImplementExtraConversion() {
		possiblyAddActionLink();
	}

	private void possiblyAddActionLink() {
		if (dataRecordLink.hasReadAction()) {
			createReadActionLink();
		}
	}

	private void createReadActionLink() {
		JsonObjectBuilder actionLinksObject = jsonBuilderFactory.createObjectBuilder();
		dataGroupJsonObjectBuilder.addKeyJsonObjectBuilder("actionLinks", actionLinksObject);

		JsonObjectBuilder internalLinkBuilder = buildInternalLinkBuilder();
		actionLinksObject.addKeyJsonObjectBuilder(READ, internalLinkBuilder);
	}

	private JsonObjectBuilder buildInternalLinkBuilder() {
		String recordURL = baseURL + String.join("/", dataRecordLink.getLinkedRecordType(),
				dataRecordLink.getLinkedRecordId());

		JsonObjectBuilder internalLinkBuilder = jsonBuilderFactory.createObjectBuilder();
		internalLinkBuilder.addKeyString("rel", READ);
		internalLinkBuilder.addKeyString("url", recordURL);
		internalLinkBuilder.addKeyString("requestMethod", GET);
		internalLinkBuilder.addKeyString("accept", "application/vnd.cora.record+json");
		return internalLinkBuilder;
	}
}
