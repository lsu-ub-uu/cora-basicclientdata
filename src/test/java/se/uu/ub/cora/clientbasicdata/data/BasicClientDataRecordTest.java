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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientData;
import se.uu.ub.cora.clientdata.ClientDataMissingException;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.ClientDataRecordLink;
import se.uu.ub.cora.clientdata.spies.ClientActionLinkSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordGroupSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordLinkSpy;

public class BasicClientDataRecordTest {
	private ClientDataRecord dataRecord;
	private ClientDataRecordGroupSpy dataRecordGroup;
	// private ClientDataGroupSpy recordInfoGroup;
	// private ClientDataRecordLinkSpy typeLink;
	private ClientDataRecordLinkSpy searchLink;

	@BeforeMethod
	public void beforeMethod() {
		searchLink = new ClientDataRecordLinkSpy();
		dataRecordGroup = new ClientDataRecordGroupSpy();
		dataRecordGroup.MRV.setDefaultReturnValuesSupplier("getType", () -> "someType");
		dataRecordGroup.MRV.setDefaultReturnValuesSupplier("getId", () -> "someId");

		dataRecordGroup.MRV.setSpecificReturnValuesSupplier("getFirstChildWithNameInData",
				(Supplier<ClientDataRecordLink>) () -> searchLink, "search");
		dataRecordGroup.MRV.setSpecificReturnValuesSupplier("containsChildWithNameInData",
				(Supplier<Boolean>) () -> true, "search");

		dataRecord = BasicClientDataRecord.withDataRecordGroup(dataRecordGroup);

	}

	@Test
	public void testRecordIsData() {
		assertTrue(dataRecord instanceof ClientData);
	}

	@Test
	public void testAddData() {
		ClientActionLink actionLink = new ClientActionLinkSpy();
		dataRecord.addActionLink(actionLink);

		Optional<ClientActionLink> actionLink2 = dataRecord.getActionLink(actionLink.getAction());

		assertSame(actionLink2.get(), actionLink);

		// small hack to get 100% coverage on enum
		ClientAction.valueOf(ClientAction.READ.toString());
	}

	@Test
	public void testGetDataGroup() {
		assertEquals(dataRecord.getDataRecordGroup(), dataRecordGroup);
	}

	@Test
	public void testSetDataGroup() {
		// ClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("nameInData");

		ClientDataRecordGroupSpy dataRecordGroup = new ClientDataRecordGroupSpy();

		dataRecord.setDataRecordGroup(dataRecordGroup);

		assertEquals(dataRecord.getDataRecordGroup(), dataRecordGroup);
	}

	@Test
	public void testGetReadPermissions() {
		dataRecord.addReadPermission("rating");
		dataRecord.addReadPermission("value");
		Set<String> readPermissions = dataRecord.getReadPermissions();
		assertTrue(readPermissions.contains("rating"));
		assertTrue(readPermissions.contains("value"));
	}

	@Test
	public void testAddReadPermissions() {
		Set<String> readPermissionsToSet = createSetWithValues("rating", "value");
		dataRecord.addReadPermissions(readPermissionsToSet);

		Set<String> readPermissions = dataRecord.getReadPermissions();

		assertEquals(readPermissions.size(), 2);
		assertSetContains(readPermissions, "rating", "value");

		Set<String> readPermissionsToSet2 = createSetWithValues("rating2", "value2");
		dataRecord.addReadPermissions(readPermissionsToSet2);

		readPermissions = dataRecord.getReadPermissions();

		assertEquals(readPermissions.size(), 4);
		assertSetContains(readPermissions, "rating", "value", "rating2", "value2");

	}

	private Set<String> createSetWithValues(String... values) {
		Set<String> permissions = new HashSet<>();
		for (String value : values) {
			permissions.add(value);
		}

		return permissions;
	}

	private void assertSetContains(Set<String> permissions, String... values) {
		for (String value : values) {
			assertTrue(permissions.contains(value));

		}
	}

	@Test
	public void testGetWritePermissions() {
		dataRecord.addWritePermission("title");
		dataRecord.addWritePermission("author");
		Set<String> writePermissions = dataRecord.getWritePermissions();
		assertTrue(writePermissions.contains("title"));
		assertTrue(writePermissions.contains("author"));
	}

	@Test
	public void testAddWritePermissions() {
		Set<String> writePermissionsToSet = createSetWithValues("rating", "value");
		dataRecord.addWritePermissions(writePermissionsToSet);

		Set<String> writePermissions = dataRecord.getWritePermissions();

		assertEquals(writePermissions.size(), 2);
		assertSetContains(writePermissions, "rating", "value");

		Set<String> writePermissionsToSet2 = createSetWithValues("rating2", "value2");
		dataRecord.addWritePermissions(writePermissionsToSet2);

		writePermissions = dataRecord.getWritePermissions();

		assertEquals(writePermissions.size(), 4);
		assertSetContains(writePermissions, "rating", "value", "rating2", "value2");
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Record id not known")
	public void testGetIdNoDataRecordGroupInRecord() throws Exception {
		dataRecord.setDataRecordGroup(null);
		dataRecord.getId();
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Record id not known")
	public void testGetId_DataRecordGroupReturnsError() throws Exception {
		dataRecordGroup.MRV.setThrowException("getId",
				new ClientDataMissingException("DME from Spy"));

		dataRecord.getId();

		dataRecordGroup.MCR.assertMethodWasCalled("getId");
	}

	@Test
	public void testGetId() throws Exception {
		String recordId = dataRecord.getId();

		dataRecordGroup.MCR.assertReturn("getId", 0, recordId);
	}

	@Test
	public void testGetType() throws Exception {

		String type = dataRecord.getType();

		dataRecordGroup.MCR.assertReturn("getType", 0, type);
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Record type not known")
	public void testGetTypeNoDataGroup() throws Exception {
		dataRecord.setDataRecordGroup(null);
		dataRecord.getType();
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Record type not known")
	public void testGetTypeNoLinkedTypeDataGroup() throws Exception {
		dataRecordGroup.MRV.setThrowException("getType",
				new ClientDataMissingException("DME from Spy"));

		dataRecord.getType();
	}

	@Test
	public void testRequestedActionLinkNotPresent() throws Exception {
		Optional<ClientActionLink> actionLink = dataRecord.getActionLink(ClientAction.DELETE);

		assertTrue(actionLink.isEmpty());
	}

	@Test
	public void testRequestedActionLinkIsPresent() throws Exception {
		ClientActionLinkSpy link = new ClientActionLinkSpy();
		dataRecord.addActionLink(link);

		Optional<ClientActionLink> oActionLink = dataRecord.getActionLink(ClientAction.READ);

		assertTrue(oActionLink.isPresent());
	}

	@Test
	public void testHasReadPremissionsNoReadPermissions() throws Exception {
		assertFalse(dataRecord.hasReadPermissions());
	}

	@Test
	public void testHasReadPremissionsHasReadPermissions() throws Exception {
		dataRecord.addReadPermission("ReadPermission");
		assertTrue(dataRecord.hasReadPermissions());
	}

	@Test
	public void testHasWritePremissionsNoWritePermissions() throws Exception {
		assertFalse(dataRecord.hasWritePermissions());
	}

	@Test
	public void testHasWritedPremissionsHasWritePermissions() throws Exception {
		dataRecord.addWritePermission("WritePermission");
		assertTrue(dataRecord.hasWritePermissions());
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "No searchId exists")
	public void testGetSearchId_TypeIsNotSearchOrRecordType() {
		dataRecord.getSearchId();
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Record id not known")
	public void testGetSearchId_ForTypeSearchButNoId() {
		dataRecordGroup.MRV.setDefaultReturnValuesSupplier("getType", () -> "search");
		dataRecordGroup.MRV.setThrowException("getId",
				new ClientDataMissingException("DME from Spy"));

		dataRecord.getSearchId();
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "No searchId exists")
	public void testGetSearchId_ForRecordTypeButNoSearchId() {
		dataRecordGroup.MRV.setDefaultReturnValuesSupplier("getType", () -> "recordType");
		dataRecordGroup.MRV.setSpecificReturnValuesSupplier("containsChildWithNameInData",
				() -> false, "search");

		dataRecord.getSearchId();
	}

	@Test
	public void testGetSearchIdForSearch() {
		dataRecordGroup.MRV.setDefaultReturnValuesSupplier("getType", () -> "search");

		String searchId = dataRecord.getSearchId();

		dataRecordGroup.MCR.assertReturn("getId", 0, searchId);
	}

	@Test
	public void testGetSearchIdForRecordType() {
		dataRecordGroup.MRV.setDefaultReturnValuesSupplier("getType", () -> "recordType");

		String searchId = dataRecord.getSearchId();

		assertSearchIdFetchedFromIdInSearchGroup(searchId);
	}

	private void assertSearchIdFetchedFromIdInSearchGroup(String searchId) {
		dataRecordGroup.MCR.assertParameters("containsChildWithNameInData", 0, "search");
		dataRecordGroup.MCR.assertParameters("getFirstChildWithNameInData", 0, "search");
		searchLink.MCR.assertParameters("getLinkedRecordId", 0);
		searchLink.MCR.assertReturn("getLinkedRecordId", 0, searchId);
	}

	@Test
	public void testGetOtherProtocols_hasProtocol() throws Exception {
		String protocol = "iiif";
		assertFalse(dataRecord.hasProtocol(protocol));

		Map<String, String> iiifProperties = createIiifProperties();
		dataRecord.putProtocol(protocol, iiifProperties);

		assertTrue(dataRecord.hasProtocol(protocol));
	}

	private Map<String, String> createIiifProperties() {
		Map<String, String> iiifProperties = new HashMap<>();
		iiifProperties.put("server", "someServer");
		return iiifProperties;
	}

	@Test
	public void testGetOtherProtocols_getProtocol() throws Exception {
		String protocol = "iiif";
		Map<String, String> iiifProperties = createIiifProperties();

		dataRecord.putProtocol(protocol, iiifProperties);
		Map<String, String> returnedIiifProperties = dataRecord.getProtocol(protocol);

		assertEquals(returnedIiifProperties, iiifProperties);
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Requested protocol: nonExistingProtocol, does not exist in ClientDataRecord")
	public void testGetProtocolDoesNotExist() throws Exception {
		dataRecord.getProtocol("nonExistingProtocol");
	}
}
