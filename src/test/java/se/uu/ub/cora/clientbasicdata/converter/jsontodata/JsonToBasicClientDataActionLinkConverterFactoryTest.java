/*
 * Copyright 2023 Uppsala University Library
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
package se.uu.ub.cora.clientbasicdata.converter.jsontodata;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterFactory;
import se.uu.ub.cora.clientdata.spies.JsonToClientDataConverterFactorySpy;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParser;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;

public class JsonToBasicClientDataActionLinkConverterFactoryTest {

	private static final String JSONSTRING = "{}";
	JsonToBasicClientDataActionLinkConverterFactory factory;
	JsonParser jsonParser = new OrgJsonParser();
	JsonObject json = jsonParser.parseStringAsObject(JSONSTRING);
	JsonToClientDataConverterFactory jsonToDataConverterFactory = new JsonToClientDataConverterFactorySpy();

	@Test
	public void testinit() throws Exception {
		factory = JsonToBasicClientDataActionLinkConverterFactoryImp
				.usingJsonToClientDataConverterFactory(jsonToDataConverterFactory);

		JsonToBasicClientDataActionLinkConverterImp converter = (JsonToBasicClientDataActionLinkConverterImp) factory
				.factor(json);
		assertNotNull(converter);

		JsonObject jsonObject = converter.onlyForTestGetJsonObject();
		assertSame(jsonObject, json);

		JsonToClientDataConverterFactory jsonToDataConverterFactoryReturned = converter
				.onlyForTestGetJsonToClientDataConverterFactory();
		assertSame(jsonToDataConverterFactoryReturned, jsonToDataConverterFactory);
	}

}
