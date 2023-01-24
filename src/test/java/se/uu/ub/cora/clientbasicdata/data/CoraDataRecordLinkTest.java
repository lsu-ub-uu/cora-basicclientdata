/*
 * Copyright 2015, 2016, 2019 Uppsala University Library
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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientDataAttribute;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataLink;
import se.uu.ub.cora.clientdata.ClientDataMissingException;
import se.uu.ub.cora.clientdata.ClientDataRecordLink;

public class CoraDataRecordLinkTest {

	BasicClientDataRecordLink recordLink;

	@BeforeMethod
	public void setUp() {
		recordLink = BasicClientDataRecordLink.withNameInData("nameInData");

		BasicClientDataAtomic linkedRecordType = BasicClientDataAtomic
				.withNameInDataAndValue("linkedRecordType", "myLinkedRecordType");
		recordLink.addChild(linkedRecordType);

		BasicClientDataAtomic linkedRecordId = BasicClientDataAtomic
				.withNameInDataAndValue("linkedRecordId", "myLinkedRecordId");
		recordLink.addChild(linkedRecordId);

	}

	@Test
	public void testCorrectType() {
		assertTrue(recordLink instanceof ClientDataLink);
		assertTrue(recordLink instanceof ClientDataRecordLink);
	}

	@Test
	public void testInit() {
		assertEquals(recordLink.getNameInData(), "nameInData");
		assertNotNull(recordLink.getAttributes());
		assertNotNull(recordLink.getChildren());
		assertEquals(recordLink.getFirstAtomicValueWithNameInData("linkedRecordType"),
				"myLinkedRecordType");
		assertEquals(recordLink.getFirstAtomicValueWithNameInData("linkedRecordId"),
				"myLinkedRecordId");
	}

	@Test
	public void testInitWithRepeatId() {
		recordLink.setRepeatId("hugh");
		assertEquals(recordLink.getRepeatId(), "hugh");
	}

	@Test
	public void testAddAttribute() {
		recordLink = BasicClientDataRecordLink.withNameInData("nameInData");
		recordLink.addAttributeByIdWithValue("someId", "someValue");

		Collection<ClientDataAttribute> attributes = recordLink.getAttributes();
		ClientDataAttribute attribute = attributes.iterator().next();
		assertEquals(attribute.getNameInData(), "someId");
		assertEquals(attribute.getValue(), "someValue");
	}

	@Test
	public void testInitWithLinkedPath() {
		ClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("linkedPath");
		recordLink.addChild(dataGroup);
		assertNotNull(recordLink.getFirstChildWithNameInData("linkedPath"));
	}

	@Test
	public void testFromDataGroup() {
		ClientDataGroup dataGroupRecordLink = createRecordLinkAsDataGroup();

		BasicClientDataRecordLink dataRecordLink = BasicClientDataRecordLink
				.fromDataGroup(dataGroupRecordLink);

		assertCorrectFromDataRecordLink(dataRecordLink);
		assertNull(dataRecordLink.getRepeatId());
	}

	private ClientDataGroup createRecordLinkAsDataGroup() {
		ClientDataGroup dataGroupRecordLink = BasicClientDataGroup.withNameInData("nameInData");

		BasicClientDataAtomic linkedRecordType = BasicClientDataAtomic
				.withNameInDataAndValue("linkedRecordType", "someLinkedRecordType");
		dataGroupRecordLink.addChild(linkedRecordType);

		BasicClientDataAtomic linkedRecordId = BasicClientDataAtomic
				.withNameInDataAndValue("linkedRecordId", "someLinkedRecordId");
		dataGroupRecordLink.addChild(linkedRecordId);
		return dataGroupRecordLink;
	}

	private void assertCorrectFromDataRecordLink(BasicClientDataRecordLink recordLink) {
		assertEquals(recordLink.getNameInData(), "nameInData");

		BasicClientDataAtomic convertedRecordType = (BasicClientDataAtomic) recordLink
				.getFirstChildWithNameInData("linkedRecordType");
		assertEquals(convertedRecordType.getValue(), "someLinkedRecordType");

		BasicClientDataAtomic convertedRecordId = (BasicClientDataAtomic) recordLink
				.getFirstChildWithNameInData("linkedRecordId");
		assertEquals(convertedRecordId.getValue(), "someLinkedRecordId");
	}

	@Test
	public void testFromDataGroupWithRepeatId() {
		ClientDataGroup dataGroupRecordLink = createRecordLinkAsDataGroup();
		dataGroupRecordLink.setRepeatId("1");

		BasicClientDataRecordLink dataRecordLink = BasicClientDataRecordLink
				.fromDataGroup(dataGroupRecordLink);

		assertCorrectFromDataRecordLink(dataRecordLink);
		assertEquals(dataRecordLink.getRepeatId(), "1");
	}

	@Test
	public void testHasReadDatasNoReadData() throws Exception {
		assertFalse(recordLink.hasReadAction());

	}

	@Test
	public void testHasReadDatasReadData() throws Exception {
		recordLink.addAction(ClientAction.READ);

		assertTrue(recordLink.hasReadAction());

	}

	@Test
	public void testGetLinkedRecordType() throws Exception {
		assertEquals(recordLink.getLinkedRecordType(), "myLinkedRecordType");
	}

	@Test(expectedExceptions = ClientDataMissingException.class)
	public void testGetLinkedRecordTypeMissing() throws Exception {
		BasicClientDataRecordLink withNameInData = BasicClientDataRecordLink
				.withNameInData("nameInData");
		withNameInData.getLinkedRecordType();
	}

	@Test
	public void testGetLinkedRecordId() throws Exception {
		assertEquals(recordLink.getLinkedRecordId(), "myLinkedRecordId");
	}

	@Test(expectedExceptions = ClientDataMissingException.class)
	public void testGetLinkedRecordIdMissing() throws Exception {
		BasicClientDataRecordLink withNameInData = BasicClientDataRecordLink
				.withNameInData("nameInData");
		withNameInData.getLinkedRecordId();
	}

}
