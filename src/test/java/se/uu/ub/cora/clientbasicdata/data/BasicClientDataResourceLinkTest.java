/*
 * Copyright 2025 Uppsala University Library
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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientDataLink;
import se.uu.ub.cora.clientdata.ClientDataResourceLink;
import se.uu.ub.cora.clientdata.spies.ClientActionLinkSpy;

public class BasicClientDataResourceLinkTest {

	BasicClientDataResourceLink resourceLink;
	private static final String NOT_YET_IMPLEMENTED = "Not yet implemented.";
	private static final String SOME_NAME_IN_DATA = "someNameInData";
	private static final String SOME_MIME_TYPE = "someMimeType";

	@BeforeMethod
	public void setUp() {
		resourceLink = BasicClientDataResourceLink.withNameInDataAndTypeAndIdAndMimeType(SOME_NAME_IN_DATA,
				"someType", "someId", SOME_MIME_TYPE);
	}

	@Test
	public void testCorrectType() {
		assertTrue(resourceLink instanceof ClientDataLink);
		assertTrue(resourceLink instanceof ClientConvertible);
		assertTrue(resourceLink instanceof ClientDataResourceLink);
	}

	@Test
	public void testInit() {
		assertEquals(resourceLink.getNameInData(), SOME_NAME_IN_DATA);
		assertEquals(resourceLink.getType(), "someType");
		assertEquals(resourceLink.getId(), "someId");
		assertEquals(resourceLink.getMimeType(), SOME_MIME_TYPE);
	}

	@Test
	public void testInitWithRepeatId() {
		resourceLink.setRepeatId("hugh");
		assertEquals(resourceLink.getRepeatId(), "hugh");
	}

	@Test
	public void testHasRepeatIdNotSet() {
		assertFalse(resourceLink.hasRepeatId());
	}

	@Test
	public void testHasRepeatIdSetToEmpty() {
		resourceLink.setRepeatId("");
		assertFalse(resourceLink.hasRepeatId());
	}

	@Test
	public void testHasRepeatIdSet() {
		resourceLink.setRepeatId("3");
		assertTrue(resourceLink.hasRepeatId());
	}

	@Test
	public void testHasReadActionsNoReadAction() {
		assertFalse(resourceLink.hasReadAction());
	}

	@Test
	public void testHasReadDatasReadData() {
		ClientActionLink actionLinkSpy = new ClientActionLinkSpy();

		resourceLink.addActionLink(actionLinkSpy);

		assertTrue(resourceLink.hasReadAction());
	}

	@Test
	public void testGetActionLinkNoActionAdded() {
		Optional<ClientActionLink> actionLink = resourceLink.getActionLink(ClientAction.READ);

		assertTrue(actionLink.isEmpty());
	}

	@Test
	public void testGetActionLink() {
		ClientActionLink actionLinkSpy = new ClientActionLinkSpy();

		resourceLink.addActionLink(actionLinkSpy);

		Optional<ClientActionLink> actionLink = resourceLink.getActionLink(ClientAction.READ);

		assertSame(actionLink.get(), actionLinkSpy);
	}

	@Test
	public void testMimeType() {
		resourceLink.setMimeType("type");
		assertEquals(resourceLink.getMimeType(), "type");
	}

	@Test
	public void testHasAttributes() {
		assertFalse(resourceLink.hasAttributes());
	}

	@Test(expectedExceptions = UnsupportedOperationException.class, expectedExceptionsMessageRegExp = NOT_YET_IMPLEMENTED)
	public void testGetAttribute() {
		resourceLink.getAttribute("someAttribute");
	}

	@Test(expectedExceptions = UnsupportedOperationException.class, expectedExceptionsMessageRegExp = NOT_YET_IMPLEMENTED)
	public void testAddAttributeByIdWithValue() {
		resourceLink.addAttributeByIdWithValue("someNameInData", "someValue");
	}

	@Test(expectedExceptions = UnsupportedOperationException.class, expectedExceptionsMessageRegExp = NOT_YET_IMPLEMENTED)
	public void testAttributes() {
		resourceLink.getAttributes();
	}

	@Test(expectedExceptions = UnsupportedOperationException.class, expectedExceptionsMessageRegExp = NOT_YET_IMPLEMENTED)
	public void testAttributeValue() {
		resourceLink.getAttributeValue("someValue");
	}
}
