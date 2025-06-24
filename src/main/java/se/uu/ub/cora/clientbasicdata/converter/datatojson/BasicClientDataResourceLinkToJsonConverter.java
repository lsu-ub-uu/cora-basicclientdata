/*
 * Copyright 2021, 2023, 2025 Uppsala University Library
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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import se.uu.ub.cora.clientdata.ClientDataResourceLink;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverter;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterFactory;
import se.uu.ub.cora.json.builder.JsonArrayBuilder;
import se.uu.ub.cora.json.builder.JsonBuilderFactory;
import se.uu.ub.cora.json.builder.JsonObjectBuilder;

public class BasicClientDataResourceLinkToJsonConverter implements ClientDataToJsonConverter {

	private ClientDataResourceLink dataResourceLink;
	private Optional<String> baseUrl;
	private JsonBuilderFactory jsonBuilderFactory;
	private JsonObjectBuilder jsonObjectBuilder;
	private static final String READ = "read";
	private static final String GET = "GET";
	private ClientDataToJsonConverterFactory converterFactory;

	public static BasicClientDataResourceLinkToJsonConverter usingConverterFactoryJsonBuilderFactoryAndDataResourceLinkAndRecordUrl(
			ClientDataToJsonConverterFactory converterFactory, JsonBuilderFactory factory,
			ClientDataResourceLink convertible, Optional<String> baseUrl) {

		return new BasicClientDataResourceLinkToJsonConverter(converterFactory, convertible,
				baseUrl, factory);
	}

	private BasicClientDataResourceLinkToJsonConverter(
			ClientDataToJsonConverterFactory converterFactory,
			ClientDataResourceLink dataResourceLink, Optional<String> baseUrl,
			JsonBuilderFactory jsonBuilderFactory) {

		this.converterFactory = converterFactory;
		this.dataResourceLink = dataResourceLink;
		this.baseUrl = baseUrl;
		this.jsonBuilderFactory = jsonBuilderFactory;
	}

	private void possiblyAddActionLink() {
		if (dataResourceLink.hasReadAction() && baseUrl.isPresent()) {
			createReadActionLink();
		}
	}

	private void createReadActionLink() {
		JsonObjectBuilder actionLinksObject = jsonBuilderFactory.createObjectBuilder();
		JsonObjectBuilder readAction = buildReadAction();
		actionLinksObject.addKeyJsonObjectBuilder(READ, readAction);
		jsonObjectBuilder.addKeyJsonObjectBuilder("actionLinks", actionLinksObject);
	}

	private JsonObjectBuilder buildReadAction() {

		String url = generateURL(dataResourceLink.getType(), dataResourceLink.getId(),
				dataResourceLink.getNameInData());
		String mimeType = dataResourceLink.getMimeType();
		JsonObjectBuilder readAction = jsonBuilderFactory.createObjectBuilder();
		readAction.addKeyString("rel", READ);
		readAction.addKeyString("url", url);
		readAction.addKeyString("requestMethod", GET);
		readAction.addKeyString("accept", mimeType);
		return readAction;
	}

	private String generateURL(String recordType, String recordId, String nameInData) {
		return MessageFormat.format("{0}{1}/{2}/{3}", baseUrl.get(), recordType, recordId,
				nameInData);
	}

	@Override
	public JsonObjectBuilder toJsonObjectBuilder() {
		jsonObjectBuilder = jsonBuilderFactory.createObjectBuilder();
		addNameInData();
		addChildren();
		possiblyAddRepeatId();
		possiblyAddActionLink();
		return jsonObjectBuilder;
	}

	private void addNameInData() {
		jsonObjectBuilder.addKeyString("name", dataResourceLink.getNameInData());
	}

	void addChildren() {
		Map<String, String> childrenToBe = collectResourceLinkFields();
		JsonArrayBuilder childrenArray = createJsonChildren(childrenToBe);
		jsonObjectBuilder.addKeyJsonArrayBuilder("children", childrenArray);
	}

	private Map<String, String> collectResourceLinkFields() {
		Map<String, String> childrenToBe = new HashMap<>();
		childrenToBe.put("linkedRecordType", dataResourceLink.getType());
		childrenToBe.put("linkedRecordId", dataResourceLink.getId());
		childrenToBe.put("mimeType", dataResourceLink.getMimeType());
		return childrenToBe;
	}

	private JsonArrayBuilder createJsonChildren(Map<String, String> childrenToBe) {
		JsonArrayBuilder childrenJsonArray = jsonBuilderFactory.createArrayBuilder();
		for (Entry<String, String> child : childrenToBe.entrySet()) {
			JsonObjectBuilder jsonChild = createChild(child.getKey(), child.getValue());
			childrenJsonArray.addJsonObjectBuilder(jsonChild);
		}
		return childrenJsonArray;
	}

	private JsonObjectBuilder createChild(String name, String value) {
		JsonObjectBuilder child = jsonBuilderFactory.createObjectBuilder();
		child.addKeyString("name", name);
		child.addKeyString("value", value);
		return child;
	}

	private void possiblyAddRepeatId() {
		if (dataResourceLink.hasRepeatId()) {
			jsonObjectBuilder.addKeyString("repeatId", dataResourceLink.getRepeatId());
		}
	}

	@Override
	public String toJsonCompactFormat() {
		return toJsonObjectBuilder().toJsonFormattedString();
	}

	@Override
	public String toJson() {
		return toJsonObjectBuilder().toJsonFormattedPrettyString();
	}

	ClientDataToJsonConverterFactory onlyForTestGetConverterFactory() {
		return converterFactory;
	}

	JsonBuilderFactory onlyForTestGetJsonBuilderFactory() {
		return jsonBuilderFactory;
	}

	Optional<String> onlyForTestGetBaseUrl() {
		return baseUrl;
	}
}
