/*
 * Copyright 2021 Uppsala University Library
 * Copyright 2023 Olov McKie
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

import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.json.builder.org.OrgJsonBuilderFactoryAdapter;

public class BasicClientDataToJsonConverterFactoryCreatorTest {

	private BasicClientDataToJsonConverterFactoryCreator factoryCreator;

	@BeforeMethod
	public void beforeMethod() {
		factoryCreator = new BasicClientDataToJsonConverterFactoryCreator();
	}

	@Test
	public void testInit() throws Exception {
		BasicClientDataToJsonConverterFactory createdFactory = (BasicClientDataToJsonConverterFactory) factoryCreator
				.createFactory();

		assertTrue(createdFactory instanceof BasicClientDataToJsonConverterFactory);
		assertTrue(createdFactory.builderFactory instanceof OrgJsonBuilderFactoryAdapter);
	}

	@Test
	public void testOneInstanceOfJsonBuilder() throws Exception {
		BasicClientDataToJsonConverterFactory createdFactoryOne = (BasicClientDataToJsonConverterFactory) factoryCreator
				.createFactory();

		BasicClientDataToJsonConverterFactory createdFactoryTwo = (BasicClientDataToJsonConverterFactory) factoryCreator
				.createFactory();

		assertSame(createdFactoryOne.builderFactory, createdFactoryTwo.builderFactory);
	}

}
