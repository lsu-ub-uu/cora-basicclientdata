/*
 * Copyright 2015, 2022 Uppsala University Library
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

package se.uu.ub.cora.clientbasicdata.data;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientDataAttribute;
import se.uu.ub.cora.clientdata.ClientDataMissingException;

public class BasicClientDataAtomicTest {

	private BasicClientDataAtomic dataAtomic;

	@BeforeMethod
	public void setUp() {
		dataAtomic = BasicClientDataAtomic.withNameInDataAndValue("nameInData", "value");

	}

	@Test
	public void testInit() {
		assertEquals(dataAtomic.getNameInData(), "nameInData");
		assertEquals(dataAtomic.getValue(), "value");
	}

	@Test
	public void testInitWithRepeatId() {
		dataAtomic = BasicClientDataAtomic.withNameInDataAndValueAndRepeatId("nameInData", "value",
				"2");
		assertEquals(dataAtomic.getNameInData(), "nameInData");
		assertEquals(dataAtomic.getValue(), "value");
		assertEquals(dataAtomic.getRepeatId(), "2");
	}

	@Test
	public void testSetRepeatId() {
		dataAtomic.setRepeatId("3");
		assertEquals(dataAtomic.getNameInData(), "nameInData");
		assertEquals(dataAtomic.getValue(), "value");
		assertEquals(dataAtomic.getRepeatId(), "3");
	}

	@Test
	public void testHasRepeatIdNotSet() throws Exception {
		assertFalse(dataAtomic.hasRepeatId());
	}

	@Test
	public void testHasRepeatIdSetToEmpty() throws Exception {
		dataAtomic.setRepeatId("");
		assertFalse(dataAtomic.hasRepeatId());
	}

	@Test
	public void testHasRepeatIdSet() throws Exception {
		dataAtomic.setRepeatId("3");
		assertTrue(dataAtomic.hasRepeatId());
	}

	@Test
	public void testAddAttribute() {
		dataAtomic.addAttributeByIdWithValue("someAttributeName", "value");
		Collection<ClientDataAttribute> attributes = dataAtomic.getAttributes();
		ClientDataAttribute next = attributes.iterator().next();
		assertEquals(next.getNameInData(), "someAttributeName");
		assertEquals(next.getValue(), "value");
	}

	@Test
	public void testAddAttributeWithSameNameInDataOverwrites() {
		dataAtomic.addAttributeByIdWithValue("someAttributeName", "value");
		dataAtomic.addAttributeByIdWithValue("someAttributeName", "someOtherValue");

		Collection<ClientDataAttribute> attributes = dataAtomic.getAttributes();
		assertEquals(attributes.size(), 1);
		ClientDataAttribute next = attributes.iterator().next();
		assertEquals(next.getValue(), "someOtherValue");
	}

	@Test
	public void testHasAttributes() {
		assertFalse(dataAtomic.hasAttributes());
		dataAtomic.addAttributeByIdWithValue("attributeId", "attributeValue");
		assertTrue(dataAtomic.hasAttributes());
	}

	@Test
	public void testGetAttribute() {
		dataAtomic.addAttributeByIdWithValue("someOtherAttributeId", "attributeValue");
		dataAtomic.addAttributeByIdWithValue("attributeId", "attributeValue");
		assertEquals(dataAtomic.getAttribute("attributeId").getValue(), "attributeValue");
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Attribute with id someAttributeId not found.")
	public void testGetAttributeDoesNotExist() {
		dataAtomic.getAttribute("someAttributeId");
	}

	@Test
	public void testGetAttributeValueNoAttribute() throws Exception {
		Optional<String> attributeValue = dataAtomic.getAttributeValue("attributeNameInData");

		assertTrue(attributeValue.isEmpty());
	}

	@Test
	public void testGetAttributeValueAttributeExists() throws Exception {
		dataAtomic.addAttributeByIdWithValue("someAttributeName3", "someValue");
		dataAtomic.addAttributeByIdWithValue("someAttributeName2", "someValue");
		dataAtomic.addAttributeByIdWithValue("someAttributeName", "someValue");

		Optional<String> attributeValue = dataAtomic.getAttributeValue("someAttributeName");

		assertTrue(attributeValue.isPresent());
		assertEquals(attributeValue.get(), "someValue");
	}
}
