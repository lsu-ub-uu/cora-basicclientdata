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
import se.uu.ub.cora.clientdata.ClientDataResourceLink;
import se.uu.ub.cora.clientdata.converter.DataToJsonConverter;
import se.uu.ub.cora.clientdata.converter.DataToJsonConverterFactory;
import se.uu.ub.cora.json.builder.JsonBuilderFactory;
import se.uu.ub.cora.json.builder.JsonObjectBuilder;

public class BasicClientDataResourceLinkToJsonConverter extends BasicClientDataGroupToJsonConverter
		implements DataToJsonConverter {

	private ClientDataResourceLink dataResourceLink;
	String recordURL;
	JsonBuilderFactory resourceLinkBuilderFactory;
	private static final String READ = "read";
	private static final String GET = "GET";

	private BasicClientDataResourceLinkToJsonConverter(DataToJsonConverterFactory converterFactory,
			ClientDataResourceLink dataResourceLink, String recordURL,
			JsonBuilderFactory jsonBuilderFactory) {

		super(converterFactory, jsonBuilderFactory, (ClientDataGroup) dataResourceLink);
		this.dataResourceLink = dataResourceLink;
		this.recordURL = recordURL;
		this.resourceLinkBuilderFactory = jsonBuilderFactory;
	}

	public static BasicClientDataResourceLinkToJsonConverter usingConverterFactoryJsonBuilderFactoryAndDataResourceLinkAndRecordUrl(
			DataToJsonConverterFactory converterFactory, JsonBuilderFactory factory,
			ClientDataResourceLink convertible, String recordUrl) {

		return new BasicClientDataResourceLinkToJsonConverter(converterFactory, convertible, recordUrl,
				factory);
	}

	@Override
	void hookForSubclassesToImplementExtraConversion() {
		possiblyAddActionLink();
	}

	private void possiblyAddActionLink() {
		if (dataResourceLink.hasReadAction()) {
			createReadActionLink();
		}
	}

	private void createReadActionLink() {
		JsonObjectBuilder actionLinksObject = resourceLinkBuilderFactory.createObjectBuilder();

		JsonObjectBuilder internalLinkBuilder = buildInternalLinkBuilder();
		actionLinksObject.addKeyJsonObjectBuilder(READ, internalLinkBuilder);

		dataGroupJsonObjectBuilder.addKeyJsonObjectBuilder("actionLinks", actionLinksObject);
	}

	private JsonObjectBuilder buildInternalLinkBuilder() {
		String url = recordURL + "/" + dataResourceLink.getNameInData();
		String mimeType = dataResourceLink.getMimeType();
		JsonObjectBuilder internalLinkBuilder = resourceLinkBuilderFactory.createObjectBuilder();
		internalLinkBuilder.addKeyString("rel", READ);
		internalLinkBuilder.addKeyString("url", url);
		internalLinkBuilder.addKeyString("requestMethod", GET);
		internalLinkBuilder.addKeyString("accept", mimeType);
		return internalLinkBuilder;
	}

}
