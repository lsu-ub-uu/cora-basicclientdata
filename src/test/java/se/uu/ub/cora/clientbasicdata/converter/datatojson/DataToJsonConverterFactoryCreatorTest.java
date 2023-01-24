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

import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientbasicdata.converter.datatojson.BasicDataToJsonConverterFactory;
import se.uu.ub.cora.clientbasicdata.converter.datatojson.BasicDataToJsonConverterFactoryCreator;
import se.uu.ub.cora.json.builder.org.OrgJsonBuilderFactoryAdapter;

public class DataToJsonConverterFactoryCreatorTest {

	private BasicDataToJsonConverterFactoryCreator factoryCreator;

	@BeforeMethod
	public void beforeMethod() {
		factoryCreator = new BasicDataToJsonConverterFactoryCreator();
	}

	@Test
	public void testInit() throws Exception {
		BasicDataToJsonConverterFactory createdFactory = (BasicDataToJsonConverterFactory) factoryCreator
				.createFactory();

		assertTrue(createdFactory instanceof BasicDataToJsonConverterFactory);
		assertTrue(createdFactory.builderFactory instanceof OrgJsonBuilderFactoryAdapter);
	}

	@Test
	public void testOneInstanceOfJsonBuilder() throws Exception {
		BasicDataToJsonConverterFactory createdFactoryOne = (BasicDataToJsonConverterFactory) factoryCreator
				.createFactory();

		BasicDataToJsonConverterFactory createdFactoryTwo = (BasicDataToJsonConverterFactory) factoryCreator
				.createFactory();

		assertSame(createdFactoryOne.builderFactory, createdFactoryTwo.builderFactory);
	}

}
