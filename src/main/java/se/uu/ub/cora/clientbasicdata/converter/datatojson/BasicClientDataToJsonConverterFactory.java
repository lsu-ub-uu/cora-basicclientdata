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

import java.util.Optional;

import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientDataAtomic;
import se.uu.ub.cora.clientdata.ClientDataAttribute;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataList;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.ClientDataRecordLink;
import se.uu.ub.cora.clientdata.ClientDataResourceLink;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverter;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterFactory;
import se.uu.ub.cora.json.builder.JsonBuilderFactory;

public class BasicClientDataToJsonConverterFactory implements ClientDataToJsonConverterFactory {
	JsonBuilderFactory builderFactory;
	String baseUrl;
	String recordUrl;

	/**
	 * withoutActionLinksUsingBuilderFactory will factor {@link ClientDataToJsonConverter}s that
	 * does not generates actionLinks for linked data
	 * 
	 * @param factory
	 *            A {@link JsonBuilderFactory} to pass on to factored converters
	 * 
	 * @return A ClientDataToJsonConverterFactoryImp that does not generates actionLinks for linked
	 *         data
	 */
	public static BasicClientDataToJsonConverterFactory usingBuilderFactory(
			JsonBuilderFactory factory) {
		return new BasicClientDataToJsonConverterFactory(factory);
	}

	BasicClientDataToJsonConverterFactory(JsonBuilderFactory factory) {
		this.builderFactory = factory;
	}

	@Override
	public ClientDataToJsonConverter factorUsingConvertible(ClientConvertible convertible) {
		if (convertible instanceof ClientDataList) {
			return BasicClientDataListToJsonConverter.usingJsonFactoryForDataList(this,
					builderFactory, (ClientDataList) convertible);
		}
		if (convertible instanceof ClientDataRecord) {
			// RecordActionsToJsonConverter actionsConverter = RecordActionsToJsonConverterImp
			// .usingConverterFactoryAndBuilderFactoryAndBaseUrl(this, builderFactory,
			// baseUrl);
			// return BasicClientDataRecordToJsonConverter
			// .usingConverterFactoryAndActionsConverterAndBuilderFactoryAndBaseUrlAndDataRecord(
			// this, actionsConverter, builderFactory, baseUrl,
			// (ClientDataRecord) convertible);
			return BasicClientDataRecordToJsonConverter
					.usingConverterFactoryAndActionsConverterAndBuilderFactoryAndBaseUrlAndDataRecord(
							this, null, builderFactory, baseUrl, (ClientDataRecord) convertible);
		}

		if (isDataRecordLinkAndHasBaseUrl(convertible)) {
			return BasicClientDataRecordLinkToJsonConverter
					.usingConverterFactoryAndJsonBuilderFactoryAndDataRecordLinkAndBaseUrl(this,
							builderFactory, (ClientDataRecordLink) convertible, baseUrl);
		}
		if (isDataResourceLink(convertible)) {
			Optional<String> optionalRecordUrl = Optional.ofNullable(recordUrl);
			return BasicClientDataResourceLinkToJsonConverter
					.usingConverterFactoryJsonBuilderFactoryAndDataResourceLinkAndRecordUrl(this,
							builderFactory, (ClientDataResourceLink) convertible,
							optionalRecordUrl);

		}

		if (convertible instanceof ClientDataGroup) {
			return BasicClientDataGroupToJsonConverter
					.usingConverterFactoryAndBuilderFactoryAndDataGroup(this, builderFactory,
							(ClientDataGroup) convertible);
		}
		if (convertible instanceof ClientDataAtomic) {
			return BasicClientDataAtomicToJsonConverter.usingJsonBuilderFactoryAndDataAtomic(
					builderFactory, (ClientDataAtomic) convertible);
		}
		return BasicClientDataAttributeToJsonConverter.usingJsonBuilderFactoryAndDataAttribute(
				builderFactory, (ClientDataAttribute) convertible);
	}

	// private boolean isDataResourceLinkAndHasRecordUrl(ClientConvertible convertible) {
	// return (convertible instanceof ClientDataResourceLink) && (recordUrl != null);
	// }
	private boolean isDataResourceLink(ClientConvertible convertible) {
		return (convertible instanceof ClientDataResourceLink);
	}

	private boolean isDataRecordLinkAndHasBaseUrl(ClientConvertible convertible) {
		return baseUrl != null && (convertible instanceof ClientDataRecordLink);
	}

	@Override
	public ClientDataToJsonConverter factorUsingBaseUrlAndConvertible(String baseUrl,
			ClientConvertible convertible) {
		this.baseUrl = baseUrl;

		return factorUsingConvertible(convertible);
	}

	@Override
	public ClientDataToJsonConverter factorUsingBaseUrlAndRecordUrlAndConvertible(String baseUrl,
			String recordUrl, ClientConvertible convertible) {
		this.baseUrl = baseUrl;
		this.recordUrl = recordUrl;

		return factorUsingConvertible(convertible);
	}
}
