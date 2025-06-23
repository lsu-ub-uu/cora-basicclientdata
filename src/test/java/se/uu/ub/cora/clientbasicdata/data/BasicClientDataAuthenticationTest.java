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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientData;
import se.uu.ub.cora.clientdata.ClientDataAuthentication;
import se.uu.ub.cora.clientdata.ClientDataRecordLink;
import se.uu.ub.cora.clientdata.spies.ClientActionLinkSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataGroupSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordLinkSpy;

public class BasicClientDataAuthenticationTest {

	private ClientDataAuthentication dataAuthentication;
	private ClientDataGroupSpy dataGroup;

	@BeforeMethod
	public void beforeMethod() {
		dataGroup = new ClientDataGroupSpy();
		dataAuthentication = BasicClientDataAuthentication.withDataGroup(dataGroup);
	}

	@Test
	public void testRecordIsData() {
		assertTrue(dataAuthentication instanceof ClientData);
		assertTrue(dataAuthentication instanceof ClientConvertible);
		assertTrue(dataAuthentication instanceof ClientDataAuthentication);
	}

	@Test
	public void testAddActionLink() {
		ClientActionLink expectedActionLink = new ClientActionLinkSpy();
		((BasicClientDataAuthentication) dataAuthentication).addActionLink(expectedActionLink);

		Optional<ClientActionLink> actionLink = dataAuthentication
				.getActionLink(expectedActionLink.getAction());

		assertSame(actionLink.get(), expectedActionLink);

		// small hack to get 100% coverage on enum
		ClientAction.valueOf(ClientAction.READ.toString());
	}

	@Test
	public void testGetAction_NonSetup() {
		ClientActionLink expectedActionLink = new ClientActionLinkSpy();
		assertTrue(dataAuthentication.getActionLink(expectedActionLink.getAction()).isEmpty());
	}

	@Test
	public void testGetToken() {
		dataGroup.MRV.setSpecificReturnValuesSupplier("getFirstAtomicValueWithNameInData",
				() -> "someToken", "token");

		String token = dataAuthentication.getToken();

		assertEquals(token, "someToken");
	}

	@Test
	public void testGetLoginId() {
		dataGroup.MRV.setSpecificReturnValuesSupplier("getFirstAtomicValueWithNameInData",
				() -> "someLoginId", "loginId");

		String token = dataAuthentication.getLoginId();

		assertEquals(token, "someLoginId");
	}

	@Test
	public void testGetUserId() {
		dataGroup.MRV.setSpecificReturnValuesSupplier("getFirstAtomicValueWithNameInData",
				() -> "someUserId", "userId");

		String token = dataAuthentication.getUserId();

		assertEquals(token, "someUserId");
	}

	@Test
	public void testGetValidUntil() {
		dataGroup.MRV.setSpecificReturnValuesSupplier("getFirstAtomicValueWithNameInData",
				() -> "2131321", "validUntil");

		String token = dataAuthentication.getValidUntil();

		assertEquals(token, "2131321");
	}

	@Test
	public void testGetRenewUntil() {
		dataGroup.MRV.setSpecificReturnValuesSupplier("getFirstAtomicValueWithNameInData",
				() -> "2131321", "renewUntil");

		String token = dataAuthentication.getRenewUntil();

		assertEquals(token, "2131321");
	}

	@Test
	public void testGetFirstName() {
		dataGroup.MRV.setSpecificReturnValuesSupplier("getFirstAtomicValueWithNameInData",
				() -> "someFirstName", "firstName");

		String token = dataAuthentication.getFirstName();

		assertEquals(token, "someFirstName");
	}

	@Test
	public void testGetLastName() {
		dataGroup.MRV.setSpecificReturnValuesSupplier("getFirstAtomicValueWithNameInData",
				() -> "someLastName", "lastName");

		String token = dataAuthentication.getLastName();

		assertEquals(token, "someLastName");
	}

	@Test
	public void testGetPermissionUnitIds_noPermissionUnits() {
		dataGroup.MRV.setSpecificReturnValuesSupplier("containsChildWithNameInData", () -> false,
				"permissionUnit");

		List<String> permissionUnitIds = dataAuthentication.getPermissionUnitIds();

		assertTrue(permissionUnitIds.isEmpty());
	}

	@Test
	public void testGetPermissionUnitIds_twoPermissionUnits() {
		dataGroup.MRV.setSpecificReturnValuesSupplier("containsChildWithNameInData", () -> true,
				"permissionUnit");
		ClientDataRecordLinkSpy permissionUnit1 = new ClientDataRecordLinkSpy();
		permissionUnit1.MRV.setSpecificReturnValuesSupplier("getLinkedRecordId", () -> "id1");
		ClientDataRecordLinkSpy permissionUnit2 = new ClientDataRecordLinkSpy();
		permissionUnit2.MRV.setSpecificReturnValuesSupplier("getLinkedRecordId", () -> "id2");
		dataGroup.MRV.setSpecificReturnValuesSupplier("getChildrenOfTypeAndName",
				() -> List.of(permissionUnit1, permissionUnit2), ClientDataRecordLink.class,
				"permissionUnit");

		List<String> permissionUnitIds = dataAuthentication.getPermissionUnitIds();

		assertEquals(permissionUnitIds.size(), 2);
	}
}