/*
 * Copyright 2015, 2019, 2022 Uppsala University Library
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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientData;
import se.uu.ub.cora.clientdata.ClientDataAtomic;
import se.uu.ub.cora.clientdata.ClientDataAttribute;
import se.uu.ub.cora.clientdata.ClientDataChild;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataMissingException;
import se.uu.ub.cora.clientdata.spies.ClientDataAtomicSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataChildFilterSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataGroupSpy;

public class BasicClientDataGroupTest {

	private ClientDataGroup defaultDataGroup;

	@BeforeMethod
	public void setUp() {
		defaultDataGroup = BasicClientDataGroup.withNameInData("someDataGroup");

	}

	@Test
	public void testInit() {
		assertEquals(defaultDataGroup.getNameInData(), "someDataGroup");
		assertNotNull(defaultDataGroup.getAttributes());
		assertNotNull(defaultDataGroup.getChildren());
	}

	@Test
	public void testGroupIsData() {
		ClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("nameInData");
		assertTrue(dataGroup instanceof ClientData);
	}

	@Test
	public void testInitWithRepeatId() {
		defaultDataGroup.setRepeatId("hrumph");
		assertEquals(defaultDataGroup.getNameInData(), "someDataGroup");
		assertNotNull(defaultDataGroup.getAttributes());
		assertNotNull(defaultDataGroup.getChildren());
		assertEquals(defaultDataGroup.getRepeatId(), "hrumph");
	}

	@Test
	public void testHasRepeatIdNotSet() throws Exception {
		assertFalse(defaultDataGroup.hasRepeatId());
	}

	@Test
	public void testHasRepeatIdSetToEmpty() throws Exception {
		defaultDataGroup.setRepeatId("");
		assertFalse(defaultDataGroup.hasRepeatId());
	}

	@Test
	public void testHasRepeatIdSet() throws Exception {
		defaultDataGroup.setRepeatId("3");
		assertTrue(defaultDataGroup.hasRepeatId());
	}

	@Test
	public void testAddAttribute() {
		defaultDataGroup.addAttributeByIdWithValue("someAttributeName", "value");
		Collection<ClientDataAttribute> attributes = defaultDataGroup.getAttributes();
		ClientDataAttribute next = attributes.iterator().next();
		assertEquals(next.getNameInData(), "someAttributeName");
		assertEquals(next.getValue(), "value");
	}

	@Test
	public void testAddAttributeWithSameNameInDataOverwrites() {
		defaultDataGroup.addAttributeByIdWithValue("someAttributeName", "value");
		defaultDataGroup.addAttributeByIdWithValue("someAttributeName", "someOtherValue");

		Collection<ClientDataAttribute> attributes = defaultDataGroup.getAttributes();
		assertEquals(attributes.size(), 1);
		ClientDataAttribute next = attributes.iterator().next();
		assertEquals(next.getValue(), "someOtherValue");
	}

	@Test
	public void testHasAttributes() {
		assertFalse(defaultDataGroup.hasAttributes());
		defaultDataGroup.addAttributeByIdWithValue("attributeId", "attributeValue");
		assertTrue(defaultDataGroup.hasAttributes());
	}

	@Test
	public void testGetAttribute() {
		defaultDataGroup.addAttributeByIdWithValue("attributeId", "attributeValue");
		assertEquals(defaultDataGroup.getAttribute("attributeId").getValue(), "attributeValue");
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Attribute with id someAttributeId not found.")
	public void testGetAttributeDoesNotExist() {
		defaultDataGroup.getAttribute("someAttributeId");
	}

	@Test
	public void testAddChild() {
		ClientDataChild dataElement = BasicClientDataAtomic
				.withNameInDataAndValue("childNameInData", "childValue");
		defaultDataGroup.addChild(dataElement);
		List<ClientDataChild> children = defaultDataGroup.getChildren();
		ClientDataChild childElementOut = children.get(0);
		assertEquals(childElementOut.getNameInData(), "childNameInData");
	}

	@Test
	public void testHasChildren() throws Exception {
		assertFalse(defaultDataGroup.hasChildren());
		defaultDataGroup.addChild(BasicClientDataGroup.withNameInData("child"));
		assertTrue(defaultDataGroup.hasChildren());
	}

	@Test
	public void addChildrenEmptyList() {
		defaultDataGroup.addChildren(Collections.emptyList());
		assertTrue(defaultDataGroup.getChildren().isEmpty());
	}

	@Test
	public void testAddChildrenAddOneChildNoChildrenBefore() {
		List<ClientDataChild> dataElements = createListWithOneChild();

		defaultDataGroup.addChildren(dataElements);

		List<ClientDataChild> children = defaultDataGroup.getChildren();
		assertEquals(children.size(), 1);
		assertSame(children.get(0), dataElements.get(0));
	}

	@Test
	public void testAddChildrenAddOneChildOneChildBefore() {
		defaultDataGroup
				.addChild(BasicClientDataAtomic.withNameInDataAndValue("someChild", "someValue"));
		List<ClientDataChild> dataElements = createListWithOneChild();

		defaultDataGroup.addChildren(dataElements);

		List<ClientDataChild> children = defaultDataGroup.getChildren();
		assertEquals(children.size(), 2);
		assertSame(children.get(1), dataElements.get(0));
	}

	@Test
	public void testAddChildrenAddMultipleChildOneChildBefore() {
		defaultDataGroup
				.addChild(BasicClientDataAtomic.withNameInDataAndValue("someChild", "someValue"));
		List<ClientDataChild> dataElements = createListWithOneChild();
		dataElements.add(BasicClientDataGroup.withNameInData("someGroupChild"));
		dataElements
				.add(BasicClientDataAtomic.withNameInDataAndValue("someOtherAtomicChild", "42"));

		defaultDataGroup.addChildren(dataElements);

		List<ClientDataChild> children = defaultDataGroup.getChildren();
		assertEquals(children.size(), 4);
		assertSame(children.get(1), dataElements.get(0));
		assertSame(children.get(2), dataElements.get(1));
		assertSame(children.get(3), dataElements.get(2));
	}

	private List<ClientDataChild> createListWithOneChild() {
		ClientDataChild dataElement = BasicClientDataAtomic
				.withNameInDataAndValue("childNameInData", "childValue");
		List<ClientDataChild> dataElements = new ArrayList<>();
		dataElements.add(dataElement);
		return dataElements;
	}

	@Test
	public void testContainsChildWithId() {
		defaultDataGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("otherChildId", "otherChildValue"));
		ClientDataChild child = BasicClientDataAtomic.withNameInDataAndValue("childId",
				"child value");
		defaultDataGroup.addChild(child);
		assertTrue(defaultDataGroup.containsChildWithNameInData("childId"));
	}

	@Test
	public void testContainsChildWithIdNotFound() {
		ClientDataChild child = BasicClientDataAtomic.withNameInDataAndValue("childId",
				"child value");
		defaultDataGroup.addChild(child);
		assertFalse(defaultDataGroup.containsChildWithNameInData("childId_NOT_FOUND"));
	}

	@Test
	public void testGetAtomicValue() {
		defaultDataGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("atomicNameInData", "atomicValue"));
		assertEquals(defaultDataGroup.getFirstAtomicValueWithNameInData("atomicNameInData"),
				"atomicValue");
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Atomic value not found for childNameInData:" + "atomicNameInData_NOT_FOUND")
	public void testExtractAtomicValueNotFound() {
		defaultDataGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("atomicNameInData", "atomicValue"));
		defaultDataGroup.getFirstAtomicValueWithNameInData("atomicNameInData_NOT_FOUND");
	}

	@Test
	public void testGetAllDataAtomicsWithNameInData() {
		ClientDataGroup book = createDataGroupWithTwoAtomicChildrenAndOneGroupChild();

		assertEquals(book.getAllDataAtomicsWithNameInData("someChild").size(), 2);
	}

	@Test
	public void testGetAllDataAtomicsWithNameInDataNoResult() throws Exception {
		BasicClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("someNameInData");
		List<ClientDataAtomic> aList = dataGroup.getAllDataAtomicsWithNameInData("someNameInData");
		assertEquals(aList.size(), 0);
	}

	private ClientDataGroup createDataGroupWithTwoAtomicChildrenAndOneGroupChild() {
		ClientDataGroup book = BasicClientDataGroup.withNameInData("book");
		BasicClientDataAtomic child1 = BasicClientDataAtomic.withNameInDataAndValue("someChild",
				"child1");
		child1.setRepeatId("0");
		book.addChild(child1);

		BasicClientDataAtomic child2 = BasicClientDataAtomic.withNameInDataAndValue("someChild",
				"child2");
		child2.setRepeatId("1");
		book.addChild(child2);

		ClientDataGroup child3 = BasicClientDataGroup.withNameInData("someChild");
		book.addChild(child3);
		return book;
	}

	@Test
	public void testGetFirstDataAtomicWithNameInData() {
		ClientDataGroup book = createDataGroupWithTwoAtomicChildrenAndOneGroupChild();
		assertEquals(book.getFirstDataAtomicWithNameInData("someChild"), book.getChildren().get(0));
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "ClientDataAtomic not found for childNameInData:childNameInData_NOT_FOUND")
	public void testGetFirstDataAtomicWithNameInDataNotFound() {
		defaultDataGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("someChildNameInData", "atomicValue"));
		defaultDataGroup.getFirstDataAtomicWithNameInData("childNameInData_NOT_FOUND");
	}

	@Test
	public void testGetAllDataAtomicsdWithNameInDataAndAttributesOneMatch() {
		BasicClientDataGroup dataGroup = createDataGroupWithDataAtomicChildren();

		List<ClientDataAtomic> atomicsFound = (List<ClientDataAtomic>) dataGroup
				.getAllDataAtomicsWithNameInDataAndAttributes("childOne", BasicClientDataAttribute
						.withNameInDataAndValue("otherAttribute", "alternative"));

		assertEquals(atomicsFound.size(), 1);
		ClientDataChild expectedMatchingChild = dataGroup.getChildren().get(2);
		assertSame(atomicsFound.get(0), expectedMatchingChild);
	}

	private BasicClientDataGroup createDataGroupWithDataAtomicChildren() {
		BasicClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("someDataGroup");

		BasicClientDataAtomic childAtomic1 = BasicClientDataAtomic
				.withNameInDataAndValueAndRepeatId("childOne", "value1", "1");
		childAtomic1.addAttributeByIdWithValue("oneAttribute", "deafult");
		dataGroup.addChild(childAtomic1);

		BasicClientDataAtomic childAtomic2 = BasicClientDataAtomic
				.withNameInDataAndValueAndRepeatId("childOne", "value1", "2");
		dataGroup.addChild(childAtomic2);
		BasicClientDataAtomic childAtomic3 = BasicClientDataAtomic
				.withNameInDataAndValueAndRepeatId("childOne", "value1", "3");
		childAtomic3.addAttributeByIdWithValue("otherAttribute", "alternative");
		dataGroup.addChild(childAtomic3);
		return dataGroup;
	}

	@Test
	public void testGetAllDataAtomicsdWithNameInDataAndAttributesNoMatch() {
		BasicClientDataGroup dataGroup = createDataGroupWithDataAtomicChildren();

		List<ClientDataAtomic> atomicsFound = (List<ClientDataAtomic>) dataGroup
				.getAllDataAtomicsWithNameInDataAndAttributes("childOne", BasicClientDataAttribute
						.withNameInDataAndValue("nonMatchingAttribute", "alternative"));

		assertEquals(atomicsFound.size(), 0);
	}

	@Test
	public void testGetGroup() {
		defaultDataGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("atomicNameInData", "atomicValue"));
		ClientDataGroup dataGroup2 = BasicClientDataGroup.withNameInData("childNameInData");
		dataGroup2.addChild(BasicClientDataGroup.withNameInData("grandChildNameInData"));
		defaultDataGroup.addChild(dataGroup2);
		assertEquals(defaultDataGroup.getFirstGroupWithNameInData("childNameInData"), dataGroup2);
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Group not found for childNameInData:childNameInData_NOT_FOUND")
	public void testGetFirstGroupWithNameInDataNotFound() {
		defaultDataGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("atomicNameInData", "atomicValue"));
		ClientDataGroup dataGroup2 = BasicClientDataGroup.withNameInData("childNameInData");
		dataGroup2.addChild(BasicClientDataGroup.withNameInData("grandChildNameInData"));
		defaultDataGroup.addChild(dataGroup2);
		defaultDataGroup.getFirstGroupWithNameInData("childNameInData_NOT_FOUND");
	}

	@Test
	public void testGetFirstChildWithNameInData() {
		defaultDataGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("atomicNameInData", "atomicValue"));
		ClientDataGroup dataGroup2 = BasicClientDataGroup.withNameInData("childNameInData");
		dataGroup2.addChild(BasicClientDataGroup.withNameInData("grandChildNameInData"));
		defaultDataGroup.addChild(dataGroup2);
		assertEquals(defaultDataGroup.getFirstChildWithNameInData("childNameInData"), dataGroup2);
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Element not found for childNameInData:childNameInData_NOT_FOUND")
	public void testGetFirstChildWithNameInDataNotFound() {
		defaultDataGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("atomicNameInData", "atomicValue"));
		ClientDataGroup dataGroup2 = BasicClientDataGroup.withNameInData("childNameInData");
		dataGroup2.addChild(BasicClientDataGroup.withNameInData("grandChildNameInData"));
		defaultDataGroup.addChild(dataGroup2);
		defaultDataGroup.getFirstChildWithNameInData("childNameInData_NOT_FOUND");
	}

	@Test
	public void testGetAllGroupsWithNameInData() {
		defaultDataGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("atomicNameInData", "atomicValue"));
		addTwoGroupChildrenWithSameNameInData(defaultDataGroup);

		List<ClientDataGroup> groupsFound = defaultDataGroup
				.getAllGroupsWithNameInData("childNameInData");
		assertEquals(groupsFound.size(), 2);
	}

	private void addTwoGroupChildrenWithSameNameInData(ClientDataGroup parentDataGroup) {
		ClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("childNameInData");
		dataGroup.addChild(BasicClientDataAtomic.withNameInDataAndValue("firstName", "someName"));
		dataGroup.setRepeatId("0");
		parentDataGroup.addChild(dataGroup);
		ClientDataGroup dataGroup2 = BasicClientDataGroup.withNameInData("childNameInData");
		dataGroup2.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("firstName", "someOtherName"));
		dataGroup2.setRepeatId("1");
		parentDataGroup.addChild(dataGroup2);
	}

	@Test
	public void testGetAllGroupsWithNameInDataNoMatches() {
		defaultDataGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("atomicNameInData", "atomicValue"));

		List<ClientDataGroup> groupsFound = defaultDataGroup
				.getAllGroupsWithNameInData("childNameInData");
		assertEquals(groupsFound.size(), 0);
	}

	@Test
	public void testGetAllGroupsWithNameInDataAndAttributesOneMatch() {
		BasicClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("someDataGroup");
		ClientDataGroup child3 = createTestGroupForAttributesReturnChildGroupWithAttribute(
				dataGroup);

		Collection<ClientDataGroup> groupsFound = dataGroup.getAllGroupsWithNameInDataAndAttributes(
				"groupId2",
				BasicClientDataAttribute.withNameInDataAndValue("nameInData", "value1"));

		assertEquals(groupsFound.size(), 1);
		assertGroupsFoundAre(groupsFound, child3);
	}

	private void assertGroupsFoundAre(Collection<ClientDataGroup> groupsFound,
			ClientDataGroup... assertedGroups) {
		int i = 0;
		for (ClientDataGroup groupFound : groupsFound) {
			assertEquals(groupFound, assertedGroups[i]);
			i++;
		}
	}

	private ClientDataGroup createTestGroupForAttributesReturnChildGroupWithAttribute(
			ClientDataGroup dataGroup) {
		addAndReturnDataGroupChildWithNameInData(dataGroup, "groupId2");
		addAndReturnDataGroupChildWithNameInData(dataGroup, "groupId3");
		addAndReturnDataGroupChildWithNameInData(dataGroup, "groupId2");
		ClientDataGroup child3 = addAndReturnDataGroupChildWithNameInDataAndAttributes(dataGroup,
				"groupId2",
				BasicClientDataAttribute.withNameInDataAndValue("nameInData", "value1"));
		return child3;
	}

	private ClientDataGroup addAndReturnDataGroupChildWithNameInData(ClientDataGroup dataGroup,
			String nameInData) {
		ClientDataGroup child = BasicClientDataGroup.withNameInData(nameInData);
		dataGroup.addChild(child);
		return child;
	}

	private ClientDataGroup addAndReturnDataGroupChildWithNameInDataAndAttributes(
			ClientDataGroup dataGroup, String nameInData, BasicClientDataAttribute... attributes) {
		ClientDataGroup child = BasicClientDataGroup.withNameInData(nameInData);
		dataGroup.addChild(child);
		for (BasicClientDataAttribute attribute : attributes) {
			child.addAttributeByIdWithValue(attribute.getNameInData(), attribute.getValue());
		}
		return child;
	}

	@Test
	public void testGetAllGroupsWithNameInDataAndAttributesTwoMatches() {
		BasicClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("someDataGroup");
		ClientDataGroup child3 = createTestGroupForAttributesReturnChildGroupWithAttribute(
				dataGroup);
		ClientDataGroup child4 = addAndReturnDataGroupChildWithNameInDataAndAttributes(dataGroup,
				"groupId2",
				BasicClientDataAttribute.withNameInDataAndValue("nameInData", "value1"));

		Collection<ClientDataGroup> groupsFound = dataGroup.getAllGroupsWithNameInDataAndAttributes(
				"groupId2",
				BasicClientDataAttribute.withNameInDataAndValue("nameInData", "value1"));

		assertEquals(groupsFound.size(), 2);
		assertGroupsFoundAre(groupsFound, child3, child4);
	}

	@Test
	public void testGetAllGroupsWithNameInDataAndAttributesOneWrongAttributeValueTwoMatches() {
		BasicClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("someDataGroup");
		ClientDataGroup child3 = createTestGroupForAttributesReturnChildGroupWithAttribute(
				dataGroup);
		ClientDataGroup child4 = addAndReturnDataGroupChildWithNameInDataAndAttributes(dataGroup,
				"groupId2",
				BasicClientDataAttribute.withNameInDataAndValue("nameInData", "value1"));
		addAndReturnDataGroupChildWithNameInDataAndAttributes(dataGroup, "groupId2",
				BasicClientDataAttribute.withNameInDataAndValue("nameInData", "value2"));

		Collection<ClientDataGroup> groupsFound = dataGroup.getAllGroupsWithNameInDataAndAttributes(
				"groupId2",
				BasicClientDataAttribute.withNameInDataAndValue("nameInData", "value1"));

		assertEquals(groupsFound.size(), 2);
		assertGroupsFoundAre(groupsFound, child3, child4);
	}

	@Test
	public void testGetAllGroupsWithNameInDataAndAttributesOneWrongAttributeNameTwoMatches() {
		BasicClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("someDataGroup");
		ClientDataGroup child3 = createTestGroupForAttributesReturnChildGroupWithAttribute(
				dataGroup);
		ClientDataGroup child4 = addAndReturnDataGroupChildWithNameInDataAndAttributes(dataGroup,
				"groupId2",
				BasicClientDataAttribute.withNameInDataAndValue("nameInData", "value1"));
		addAndReturnDataGroupChildWithNameInDataAndAttributes(dataGroup, "groupId2",
				BasicClientDataAttribute.withNameInDataAndValue("nameInData2", "value1"));

		Collection<ClientDataGroup> groupsFound = dataGroup.getAllGroupsWithNameInDataAndAttributes(
				"groupId2",
				BasicClientDataAttribute.withNameInDataAndValue("nameInData", "value1"));

		assertEquals(groupsFound.size(), 2);
		assertGroupsFoundAre(groupsFound, child3, child4);
	}

	@Test
	public void testGetAllGroupsWithNameInDataAndTwoAttributesNoMatches() {
		BasicClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("someDataGroup");
		createTestGroupForAttributesReturnChildGroupWithAttribute(dataGroup);
		addAndReturnDataGroupChildWithNameInDataAndAttributes(dataGroup, "groupId2",
				BasicClientDataAttribute.withNameInDataAndValue("nameInData", "value1"),
				BasicClientDataAttribute.withNameInDataAndValue("nameInData2", "value2"));

		Collection<ClientDataGroup> groupsFound = dataGroup.getAllGroupsWithNameInDataAndAttributes(
				"groupId2", BasicClientDataAttribute.withNameInDataAndValue("nameInData", "value1"),
				BasicClientDataAttribute.withNameInDataAndValue("nameInData2", "value1"));

		assertEquals(groupsFound.size(), 0);
	}

	@Test
	public void testGetAllGroupsWithNameInDataAndTwoAttributesOneMatches() {
		BasicClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("someDataGroup");
		createTestGroupForAttributesReturnChildGroupWithAttribute(dataGroup);
		ClientDataGroup child4 = addAndReturnDataGroupChildWithNameInDataAndAttributes(dataGroup,
				"groupId2", BasicClientDataAttribute.withNameInDataAndValue("nameInData", "value1"),
				BasicClientDataAttribute.withNameInDataAndValue("nameInData2", "value2"));
		addAndReturnDataGroupChildWithNameInDataAndAttributes(dataGroup, "groupId2",
				BasicClientDataAttribute.withNameInDataAndValue("nameInData", "value1"),
				BasicClientDataAttribute.withNameInDataAndValue("nameInData3", "value2"));

		Collection<ClientDataGroup> groupsFound = dataGroup.getAllGroupsWithNameInDataAndAttributes(
				"groupId2", BasicClientDataAttribute.withNameInDataAndValue("nameInData", "value1"),
				BasicClientDataAttribute.withNameInDataAndValue("nameInData2", "value2"));

		assertEquals(groupsFound.size(), 1);
		assertGroupsFoundAre(groupsFound, child4);
	}

	@Test
	public void testRemoveChild() {
		ClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("someDataGroup");
		createAndAddAnAtomicChildToDataGroup(dataGroup);
		boolean childWasRemoved = dataGroup.removeFirstChildWithNameInData("childId");
		assertTrue(childWasRemoved);
		assertFalse(dataGroup.containsChildWithNameInData("childId"));
	}

	@Test
	public void testRemoveChildMoreThanOneChildExist() {
		ClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("someDataGroup");
		createAndAddAnAtomicChildToDataGroup(dataGroup);
		createAndAddAnAtomicChildToDataGroup(dataGroup);
		boolean childWasRemoved = dataGroup.removeFirstChildWithNameInData("childId");
		assertTrue(childWasRemoved);
		assertTrue(dataGroup.containsChildWithNameInData("childId"));
	}

	@Test
	public void testRemoveChildNotFound() {
		ClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("someDataGroup");
		createAndAddAnAtomicChildToDataGroup(dataGroup);
		boolean childWasRemoved = dataGroup.removeFirstChildWithNameInData("childId_NOTFOUND");
		assertFalse(childWasRemoved);
	}

	private ClientDataChild createAndAddAnAtomicChildToDataGroup(ClientDataGroup dataGroup) {
		return createAndAddAnAtomicChildToDataGroupUsingNameInData(dataGroup, "childId");
	}

	private ClientDataChild createAndAddAnAtomicChildToDataGroupUsingNameInData(
			ClientDataGroup dataGroup, String nameInData) {
		ClientDataChild child = BasicClientDataAtomic.withNameInDataAndValue(nameInData,
				"child value");
		dataGroup.addChild(child);
		return child;
	}

	@Test
	public void testRemoveAllChildrenWithNameInData() {
		ClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("someDataGroup");
		createAndAddAnAtomicChildWithRepeatIdToDataGroup(dataGroup, "0");
		createAndAddAnAtomicChildWithRepeatIdToDataGroup(dataGroup, "1");
		boolean childWasRemoved = dataGroup.removeAllChildrenWithNameInData("childId");
		assertTrue(childWasRemoved);
		assertFalse(dataGroup.containsChildWithNameInData("childId"));
	}

	private ClientDataChild createAndAddAnAtomicChildWithRepeatIdToDataGroup(
			ClientDataGroup dataGroup, String repeatId) {
		ClientDataChild child = BasicClientDataAtomic.withNameInDataAndValueAndRepeatId("childId",
				"child value", repeatId);
		dataGroup.addChild(child);
		return child;
	}

	@Test
	public void testRemoveAllChildrenWithNameInDataWhenOtherChildrenExist() {
		ClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("someDataGroup");
		createAndAddAnAtomicChildWithRepeatIdToDataGroup(dataGroup, "0");
		createAndAddAnAtomicChildWithRepeatIdToDataGroup(dataGroup, "1");
		createAndAddAnAtomicChildToDataGroupUsingNameInData(dataGroup, "someOtherChildId");

		boolean childWasRemoved = dataGroup.removeAllChildrenWithNameInData("childId");
		assertTrue(childWasRemoved);
		assertFalse(dataGroup.containsChildWithNameInData("childId"));
		assertTrue(dataGroup.containsChildWithNameInData("someOtherChildId"));
	}

	@Test
	public void testRemoveAllChildNotFound() {
		ClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("someDataGroup");
		createAndAddAnAtomicChildToDataGroup(dataGroup);
		assertFalse(dataGroup.removeAllChildrenWithNameInData("childId_NOTFOUND"));
	}

	@Test
	public void testGetAllChildrenWithNameInDataNoChildren() {
		ClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("someDataGroup");
		List<ClientDataChild> allChildrenWithNameInData = dataGroup
				.getAllChildrenWithNameInData("someChildNameInData");
		assertTrue(allChildrenWithNameInData.isEmpty());

	}

	@Test
	public void testGetAllChildrenWithNameInDataNoMatchingChildren() {
		ClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("someDataGroup");
		createAndAddAtomicChild(dataGroup, "someChildNameInData", "0");
		List<ClientDataChild> allChildrenWithNameInData = dataGroup
				.getAllChildrenWithNameInData("someOtherChildNameInData");
		assertTrue(allChildrenWithNameInData.isEmpty());

	}

	@Test
	public void testGetAllChildrenWithNameInDataOneMatchingAtomicChild() {
		ClientDataGroup dataGroup = BasicClientDataGroup.withNameInData("someDataGroup");
		BasicClientDataAtomic atomicChild = createAndAddAtomicChild(dataGroup,
				"someChildNameInData", "0");

		List<ClientDataChild> allChildrenWithNameInData = dataGroup
				.getAllChildrenWithNameInData("someChildNameInData");
		assertEquals(allChildrenWithNameInData.size(), 1);
		assertSame(allChildrenWithNameInData.get(0), atomicChild);
	}

	private BasicClientDataAtomic createAndAddAtomicChild(ClientDataGroup dataGroup,
			String nameInData, String repeatId) {
		BasicClientDataAtomic atomicChild = BasicClientDataAtomic.withNameInDataAndValue(nameInData,
				"someValue");
		atomicChild.setRepeatId(repeatId);
		dataGroup.addChild(atomicChild);
		return atomicChild;
	}

	@Test
	public void testGetAllChildrenWithNameInDataMultipleMatchesDifferentTypes() {
		BasicClientDataAtomic atomicChild = createAndAddAtomicChild(defaultDataGroup,
				"someChildNameInData", "0");
		BasicClientDataAtomic atomicChild2 = createAndAddAtomicChild(defaultDataGroup,
				"someChildNameInData", "1");
		BasicClientDataAtomic atomicChild3 = createAndAddAtomicChild(defaultDataGroup,
				"someNOTChildNameInData", "2");

		ClientDataGroup dataGroupChild = BasicClientDataGroup.withNameInData("someChildNameInData");
		defaultDataGroup.addChild(dataGroupChild);

		List<ClientDataChild> allChildrenWithNameInData = defaultDataGroup
				.getAllChildrenWithNameInData("someChildNameInData");
		assertEquals(allChildrenWithNameInData.size(), 3);
		assertSame(allChildrenWithNameInData.get(0), atomicChild);
		assertSame(allChildrenWithNameInData.get(1), atomicChild2);
		assertSame(allChildrenWithNameInData.get(2), dataGroupChild);
		assertFalse(allChildrenWithNameInData.contains(atomicChild3));
	}

	@Test
	public void testRemoveChildrenWithAttributesNoMatchWrongChildNoAttributes() {
		createAndAddAnAtomicChildWithRepeatIdToDataGroup(defaultDataGroup, "0");
		boolean childWasRemoved = defaultDataGroup
				.removeAllChildrenWithNameInDataAndAttributes("NOTchildId");
		assertFalse(childWasRemoved);
		assertTrue(defaultDataGroup.containsChildWithNameInData("childId"));
	}

	@Test
	public void testRemoveChildrenWithAttributesNoMatchNoAttributes() {
		createAndAddAnAtomicChildWithRepeatIdToDataGroup(defaultDataGroup, "0");
		boolean childWasRemoved = defaultDataGroup.removeAllChildrenWithNameInDataAndAttributes(
				"childId",
				BasicClientDataAttribute.withNameInDataAndValue("someName", "someValue"));
		assertFalse(childWasRemoved);
		assertTrue(defaultDataGroup.containsChildWithNameInData("childId"));
	}

	@Test
	public void testRemoveChildrenWithAttributesNoMatchWithWrongAttributes() {
		ClientDataGroup childDataGroup = BasicClientDataGroup.withNameInData("childId");
		childDataGroup.addAttributeByIdWithValue("someName", "someValue");
		defaultDataGroup.addChild(childDataGroup);

		boolean childWasRemoved = defaultDataGroup.removeAllChildrenWithNameInDataAndAttributes(
				"childId",
				BasicClientDataAttribute.withNameInDataAndValue("someName", "someOtherValue"));
		assertFalse(childWasRemoved);
		assertTrue(defaultDataGroup.containsChildWithNameInData("childId"));
	}

	@Test
	public void testRemoveChildrenWithAttributesOneMatchNoAttributes() {
		createAndAddAnAtomicChildWithRepeatIdToDataGroup(defaultDataGroup, "0");
		boolean childWasRemoved = defaultDataGroup
				.removeAllChildrenWithNameInDataAndAttributes("childId");
		assertTrue(childWasRemoved);
	}

	@Test
	public void testRemoveChildrenWithAttributesOneMatchWithAttributes() {
		ClientDataGroup childDataGroup = BasicClientDataGroup.withNameInData("childId");
		childDataGroup.addAttributeByIdWithValue("someName", "someValue");
		defaultDataGroup.addChild(childDataGroup);
		boolean childWasRemoved = defaultDataGroup.removeAllChildrenWithNameInDataAndAttributes(
				"childId",
				BasicClientDataAttribute.withNameInDataAndValue("someName", "someValue"));
		assertTrue(childWasRemoved);
		assertFalse(defaultDataGroup.containsChildWithNameInData("childId"));
	}

	@Test
	public void testRemoveChildrenWithAttributesTwoChildrenOneMatchWithAttributes() {
		setUpDataGroupWithTwoChildrenOneWithAttributes();

		assertEquals(defaultDataGroup.getAllChildrenWithNameInData("childId").size(), 2);

		boolean childWasRemoved = defaultDataGroup.removeAllChildrenWithNameInDataAndAttributes(
				"childId",
				BasicClientDataAttribute.withNameInDataAndValue("someName", "someValue"));

		assertTrue(childWasRemoved);
		assertTrue(defaultDataGroup.containsChildWithNameInData("childId"));

		List<ClientDataChild> allChildrenWithNameInData = defaultDataGroup
				.getAllChildrenWithNameInData("childId");
		assertEquals(allChildrenWithNameInData.size(), 1);
		assertTrue(allChildrenWithNameInData.get(0) instanceof BasicClientDataAtomic);

	}

	@Test
	public void testRemoveChildrenWithAttributesTwoChildrenOneMatchWithoutAttributes() {
		setUpDataGroupWithTwoChildrenOneWithAttributes();

		boolean childWasRemoved = defaultDataGroup
				.removeAllChildrenWithNameInDataAndAttributes("childId");
		assertTrue(childWasRemoved);
		assertTrue(defaultDataGroup.containsChildWithNameInData("childId"));

		List<ClientDataChild> allChildrenWithNameInData = defaultDataGroup
				.getAllChildrenWithNameInData("childId");
		assertEquals(allChildrenWithNameInData.size(), 1);
		assertTrue(allChildrenWithNameInData.get(0) instanceof BasicClientDataGroup);
	}

	private void setUpDataGroupWithTwoChildrenOneWithAttributes() {
		ClientDataGroup childDataGroup = BasicClientDataGroup.withNameInData("childId");
		childDataGroup.addAttributeByIdWithValue("someName", "someValue");
		defaultDataGroup.addChild(childDataGroup);
		createAndAddAnAtomicChildWithRepeatIdToDataGroup(defaultDataGroup, "0");
	}

	@Test
	public void testRemoveChildrenWithAttributesTwoChildrenNoMatchWithAttributes() {
		setUpDataGroupWithTwoChildrenOneWithAttributes();

		boolean childWasRemoved = defaultDataGroup.removeAllChildrenWithNameInDataAndAttributes(
				"childId",
				BasicClientDataAttribute.withNameInDataAndValue("someNOTName", "someValue"));
		assertFalse(childWasRemoved);
		assertTrue(defaultDataGroup.containsChildWithNameInData("childId"));

		List<ClientDataChild> allChildrenWithNameInData = defaultDataGroup
				.getAllChildrenWithNameInData("childId");
		assertEquals(allChildrenWithNameInData.size(), 2);
		assertTrue(allChildrenWithNameInData.get(0) instanceof BasicClientDataGroup);
		assertTrue(allChildrenWithNameInData.get(1) instanceof BasicClientDataAtomic);
	}

	@Test
	public void testRemoveChildrenWithAttributesMultipleChildrenTwoMatchesWithAttributes() {
		setUpDataGroupWithMultipleChildrenWithAttributesAndWithoutAttributes();

		boolean childWasRemoved = defaultDataGroup.removeAllChildrenWithNameInDataAndAttributes(
				"childId",
				BasicClientDataAttribute.withNameInDataAndValue("someName", "someValue"));
		assertTrue(childWasRemoved);
		assertTrue(defaultDataGroup.containsChildWithNameInData("childId"));

		List<ClientDataChild> allChildrenWithNameInData = defaultDataGroup
				.getAllChildrenWithNameInData("childId");
		assertEquals(allChildrenWithNameInData.size(), 3);
		assertTrue(allChildrenWithNameInData.get(0) instanceof BasicClientDataAtomic);
		assertTrue(allChildrenWithNameInData.get(1) instanceof BasicClientDataAtomic);
		assertTrue(allChildrenWithNameInData.get(2) instanceof BasicClientDataGroup);

		assertEquals(defaultDataGroup.getAllChildrenWithNameInData("childOtherId").size(), 1);
	}

	private void setUpDataGroupWithMultipleChildrenWithAttributesAndWithoutAttributes() {
		ClientDataGroup childDataGroupWithAttribute = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"childId", "0");
		defaultDataGroup.addChild(childDataGroupWithAttribute);
		ClientDataGroup childDataGroupWithAttribute2 = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"childId", "1");
		defaultDataGroup.addChild(childDataGroupWithAttribute2);

		createAndAddAnAtomicChildWithRepeatIdToDataGroup(defaultDataGroup, "0");
		createAndAddAnAtomicChildWithRepeatIdToDataGroup(defaultDataGroup, "1");

		ClientDataGroup childDataGroupWithAtttributeOtherName = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"childOtherId", "0");
		defaultDataGroup.addChild(childDataGroupWithAtttributeOtherName);

		ClientDataGroup childDataGroupWithExtraAttribute = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"childId", "0");
		childDataGroupWithExtraAttribute.addAttributeByIdWithValue("someOtherName", "someValue");
		defaultDataGroup.addChild(childDataGroupWithExtraAttribute);
	}

	private ClientDataGroup createChildGroupWithNameInDataAndRepatIdAndAttributes(String nameInData,
			String repeatId) {
		ClientDataGroup childDataGroup = BasicClientDataGroup.withNameInData(nameInData);
		childDataGroup.addAttributeByIdWithValue("someName", "someValue");
		childDataGroup.setRepeatId(repeatId);
		return childDataGroup;
	}

	@Test
	public void testGetAllChildrenWithNameInDataAndAttributesNoMatch() {
		ClientDataGroup childGroup = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"someChildNameInData", "0");
		defaultDataGroup.addChild(childGroup);

		List<ClientDataChild> children = defaultDataGroup
				.getAllChildrenWithNameInDataAndAttributes("someChildNameInData");

		assertTrue(children.isEmpty());
	}

	@Test
	public void testGetAllChildrenWithNameInDataAndAttributesNoMatchNotMatchingNameInData() {
		ClientDataGroup childGroup = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"someChildNameInData", "0");
		defaultDataGroup.addChild(childGroup);

		BasicClientDataAttribute attribute = BasicClientDataAttribute
				.withNameInDataAndValue("someName", "someValue");
		List<ClientDataChild> children = defaultDataGroup
				.getAllChildrenWithNameInDataAndAttributes("someOtherChildNameInData", attribute);

		assertTrue(children.isEmpty());
	}

	@Test
	public void testGetAllChildrenWithNameInDataAndAttributesMatch() {
		ClientDataGroup childGroup = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"someChildNameInData", "0");
		defaultDataGroup.addChild(childGroup);

		BasicClientDataAttribute attribute = BasicClientDataAttribute
				.withNameInDataAndValue("someName", "someValue");

		List<ClientDataChild> children = defaultDataGroup
				.getAllChildrenWithNameInDataAndAttributes("someChildNameInData", attribute);

		assertEquals(children.size(), 1);

		assertSame(children.get(0), childGroup);
	}

	@Test
	public void testGetAllChildrenWithNameInDataAndAttributesDataAtomicChild() {
		ClientDataGroup childGroup = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"someChildNameInData", "0");
		defaultDataGroup.addChild(childGroup);

		BasicClientDataAtomic coraDataAtomic = BasicClientDataAtomic
				.withNameInDataAndValue("someChildNameInData", "someValue");

		defaultDataGroup.addChild(coraDataAtomic);

		List<ClientDataChild> children = defaultDataGroup
				.getAllChildrenWithNameInDataAndAttributes("someChildNameInData");

		assertEquals(children.size(), 1);
		assertSame(children.get(0), coraDataAtomic);

	}

	@Test
	public void testGetAllChildrenWithNameInDataAndAttributesMatchRepeatingGroup() {
		ClientDataGroup childGroup = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"someChildNameInData", "0");
		defaultDataGroup.addChild(childGroup);

		ClientDataGroup childGroup2 = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"someChildNameInData", "1");
		defaultDataGroup.addChild(childGroup2);

		BasicClientDataAttribute attribute = BasicClientDataAttribute
				.withNameInDataAndValue("someName", "someValue");

		List<ClientDataChild> children = defaultDataGroup
				.getAllChildrenWithNameInDataAndAttributes("someChildNameInData", attribute);

		assertEquals(children.size(), 2);

		assertSame(children.get(0), childGroup);
		assertSame(children.get(1), childGroup2);
	}

	@Test
	public void testGetAllChildrenWithNameInDataAndAttributesMultipleChildrenMatchOneGroup() {
		ClientDataGroup childGroup = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"someChildNameInData", "0");
		defaultDataGroup.addChild(childGroup);

		ClientDataGroup childGroupOtherNameInData = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"someOtherChildNameInData", "1");
		defaultDataGroup.addChild(childGroupOtherNameInData);

		ClientDataGroup childGroup2 = BasicClientDataGroup.withNameInData("someChildNameInData");
		defaultDataGroup.addChild(childGroup2);

		BasicClientDataAttribute attribute = BasicClientDataAttribute
				.withNameInDataAndValue("someName", "someValue");

		List<ClientDataChild> childrenWithAttributes = defaultDataGroup
				.getAllChildrenWithNameInDataAndAttributes("someChildNameInData", attribute);

		assertEquals(childrenWithAttributes.size(), 1);
		assertSame(childrenWithAttributes.get(0), childGroup);

		List<ClientDataChild> childrenWithoutAttributes = defaultDataGroup
				.getAllChildrenWithNameInDataAndAttributes("someChildNameInData");

		assertEquals(childrenWithoutAttributes.size(), 1);
		assertSame(childrenWithoutAttributes.get(0), childGroup2);
	}

	@Test
	public void testGetAllChildrenMatchingFilter_noChildren() throws Exception {
		ClientDataChildFilterSpy childFilter = new ClientDataChildFilterSpy();

		List<ClientDataChild> matchingChildren = defaultDataGroup
				.getAllChildrenMatchingFilter(childFilter);

		childFilter.MCR.assertMethodNotCalled("childMatches");
		assertEquals(matchingChildren.size(), 0);
	}

	@Test
	public void testGetAllChildrenMatchingFilter_oneChild() throws Exception {
		ClientDataChildFilterSpy childFilter = new ClientDataChildFilterSpy();
		ClientDataAtomicSpy atomicChild = new ClientDataAtomicSpy();
		defaultDataGroup.addChild(atomicChild);

		List<ClientDataChild> matchingChildren = defaultDataGroup
				.getAllChildrenMatchingFilter(childFilter);

		childFilter.MCR.assertParameters("childMatches", 0, atomicChild);
		assertEquals(matchingChildren.size(), 1);
		assertSame(matchingChildren.get(0), atomicChild);
	}

	@Test
	public void testGetAllChildrenMatchingFilter_twoChildMatchesOneDoNot() throws Exception {
		ClientDataAtomicSpy atomicChild = new ClientDataAtomicSpy();
		defaultDataGroup.addChild(atomicChild);
		ClientDataAtomicSpy atomicChild2 = new ClientDataAtomicSpy();
		defaultDataGroup.addChild(atomicChild2);
		ClientDataGroupSpy groupChild = new ClientDataGroupSpy();
		defaultDataGroup.addChild(groupChild);
		ClientDataChildFilterSpy childFilter = new ClientDataChildFilterSpy();
		childFilter.MRV.setSpecificReturnValuesSupplier("childMatches",
				(Supplier<Boolean>) () -> false, atomicChild);

		List<ClientDataChild> matchingChildren = defaultDataGroup
				.getAllChildrenMatchingFilter(childFilter);

		childFilter.MCR.assertParameters("childMatches", 0, atomicChild);
		childFilter.MCR.assertParameters("childMatches", 1, atomicChild2);
		childFilter.MCR.assertParameters("childMatches", 2, groupChild);
		assertEquals(matchingChildren.size(), 2);
		assertSame(matchingChildren.get(0), atomicChild2);
		assertSame(matchingChildren.get(1), groupChild);
	}

	@Test
	public void testRemoveAllChildrenMatchingFilter_noChildren() throws Exception {
		ClientDataChildFilterSpy childFilter = new ClientDataChildFilterSpy();

		boolean childRemoved = defaultDataGroup.removeAllChildrenMatchingFilter(childFilter);

		childFilter.MCR.assertMethodNotCalled("childMatches");
		assertFalse(childRemoved);
	}

	@Test
	public void testRemoveAllChildrenMatchingFilter_oneChild() throws Exception {
		ClientDataChildFilterSpy childFilter = new ClientDataChildFilterSpy();
		ClientDataAtomicSpy atomicChild = new ClientDataAtomicSpy();
		defaultDataGroup.addChild(atomicChild);

		boolean childRemoved = defaultDataGroup.removeAllChildrenMatchingFilter(childFilter);

		childFilter.MCR.assertParameters("childMatches", 0, atomicChild);
		assertTrue(childRemoved);
		assertEquals(defaultDataGroup.getChildren().size(), 0);
	}

	@Test
	public void testRemoveAllChildrenMatchingFilter_twoChildMatchesOneDoNot() throws Exception {
		ClientDataAtomicSpy atomicChild = new ClientDataAtomicSpy();
		defaultDataGroup.addChild(atomicChild);
		ClientDataAtomicSpy atomicChild2 = new ClientDataAtomicSpy();
		defaultDataGroup.addChild(atomicChild2);
		ClientDataGroupSpy groupChild = new ClientDataGroupSpy();
		defaultDataGroup.addChild(groupChild);
		ClientDataChildFilterSpy childFilter = new ClientDataChildFilterSpy();
		childFilter.MRV.setSpecificReturnValuesSupplier("childMatches",
				(Supplier<Boolean>) () -> false, atomicChild);

		boolean childRemoved = defaultDataGroup.removeAllChildrenMatchingFilter(childFilter);

		assertTrue(childRemoved);
		childFilter.MCR.assertParameters("childMatches", 0, atomicChild);
		childFilter.MCR.assertParameters("childMatches", 1, atomicChild2);
		childFilter.MCR.assertParameters("childMatches", 2, groupChild);
		List<ClientDataChild> childrenLeft = defaultDataGroup.getChildren();
		assertEquals(childrenLeft.size(), 1);
		assertSame(childrenLeft.get(0), atomicChild);
	}

	@Test
	public void testContainsChildOfTypeAndName() throws Exception {
		addTestChildrenToDefaultGroup();

		assertFalse(defaultDataGroup.containsChildOfTypeAndName(ClientDataAtomic.class, ""));
		assertTrue(defaultDataGroup.containsChildOfTypeAndName(ClientDataAtomic.class, "atomic1"));
		assertFalse(defaultDataGroup.containsChildOfTypeAndName(ClientDataGroup.class, ""));
		assertTrue(defaultDataGroup.containsChildOfTypeAndName(ClientDataGroup.class, "group1"));
	}

	private void addTestChildrenToDefaultGroup() {
		ClientDataAtomicSpy atomicChild = new ClientDataAtomicSpy();
		defaultDataGroup.addChild(atomicChild);
		atomicChild.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> "atomic1");

		ClientDataAtomicSpy atomicChild2 = new ClientDataAtomicSpy();
		defaultDataGroup.addChild(atomicChild2);
		atomicChild2.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> "atomic2");

		ClientDataAtomicSpy atomicChild3 = new ClientDataAtomicSpy();
		defaultDataGroup.addChild(atomicChild3);
		atomicChild3.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> "atomic2");

		ClientDataGroupSpy groupChild = new ClientDataGroupSpy();
		defaultDataGroup.addChild(groupChild);
		groupChild.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> "group1");

		ClientDataGroupSpy groupChild2 = new ClientDataGroupSpy();
		defaultDataGroup.addChild(groupChild2);
		groupChild2.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> "group2");

		ClientDataGroupSpy groupChild3 = new ClientDataGroupSpy();
		defaultDataGroup.addChild(groupChild3);
		groupChild3.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> "group2");
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Child of type: ClientDataAtomic and name: someName not found as child.")
	public void testGetFirstChildOfTypeAndNameThrowsExceptionIfMissing() throws Exception {
		addTestChildrenToDefaultGroup();

		defaultDataGroup.getFirstChildOfTypeAndName(ClientDataAtomic.class, "someName");
	}

	@Test
	public void testGetFirstChildOfTypeAndName() throws Exception {
		addTestChildrenToDefaultGroup();

		ClientDataAtomic atomic = defaultDataGroup
				.getFirstChildOfTypeAndName(ClientDataAtomic.class, "atomic1");
		assertEquals(atomic.getNameInData(), "atomic1");

		ClientDataGroup group = defaultDataGroup.getFirstChildOfTypeAndName(ClientDataGroup.class,
				"group1");
		assertEquals(group.getNameInData(), "group1");
	}

	@Test
	public void testGetChildrenOfTypeAndNameNoMatchReturnsEmptyList() throws Exception {
		addTestChildrenToDefaultGroup();

		List<ClientDataAtomic> children = defaultDataGroup
				.getChildrenOfTypeAndName(ClientDataAtomic.class, "");
		assertTrue(children.isEmpty());
	}

	@Test
	public void testGetChildrenOfTypeAndName() throws Exception {
		addTestChildrenToDefaultGroup();

		List<ClientDataAtomic> children = defaultDataGroup
				.getChildrenOfTypeAndName(ClientDataAtomic.class, "atomic2");
		assertEquals(children.size(), 2);
		assertEquals(children.get(0).getNameInData(), "atomic2");
		assertEquals(children.get(1).getNameInData(), "atomic2");
	}

	@Test
	public void testRemoveFirstChildWithTypeAndNameNoRemovedChildReturnsFalse() throws Exception {
		addTestChildrenToDefaultGroup();

		boolean removed = defaultDataGroup.removeFirstChildWithTypeAndName(ClientDataAtomic.class,
				"");
		assertFalse(removed);
		assertEquals(defaultDataGroup.getChildren().size(), 6);
	}

	@Test
	public void testRemoveFirstChildWithTypeAndNameIsRemovedAndReturnsTrue() throws Exception {
		addTestChildrenToDefaultGroup();

		boolean removed = defaultDataGroup.removeFirstChildWithTypeAndName(ClientDataAtomic.class,
				"atomic1");
		assertTrue(removed);
		assertEquals(defaultDataGroup.getChildren().size(), 5);
	}

	@Test
	public void testRemoveChildrenWithTypeAndNameNoRemovedChildReturnsFalse() throws Exception {
		addTestChildrenToDefaultGroup();

		boolean removed = defaultDataGroup.removeChildrenWithTypeAndName(ClientDataAtomic.class,
				"");
		assertFalse(removed);
		assertEquals(defaultDataGroup.getChildren().size(), 6);
	}

	@Test
	public void testRemoveChildrenWithTypeAndNameIsRemovedAndReturnsTrue() throws Exception {
		addTestChildrenToDefaultGroup();

		boolean removed = defaultDataGroup.removeChildrenWithTypeAndName(ClientDataAtomic.class,
				"atomic1");
		assertTrue(removed);
		assertEquals(defaultDataGroup.getChildren().size(), 5);

		boolean removed2 = defaultDataGroup.removeChildrenWithTypeAndName(ClientDataAtomic.class,
				"atomic2");
		assertTrue(removed2);
		assertEquals(defaultDataGroup.getChildren().size(), 3);

	}

	@Test
	public void testGetAttributeValueNoAttribute() throws Exception {
		Optional<String> attributeValue = defaultDataGroup.getAttributeValue("attributeNameInData");

		assertTrue(attributeValue.isEmpty());
	}

	@Test
	public void testGetAttributeValueAttributeExists() throws Exception {
		defaultDataGroup.addAttributeByIdWithValue("someAttributeName2", "someValue");
		defaultDataGroup.addAttributeByIdWithValue("someAttributeName", "someValue");
		defaultDataGroup.addAttributeByIdWithValue("someAttributeName3", "someValue");

		Optional<String> attributeValue = defaultDataGroup.getAttributeValue("someAttributeName");

		assertTrue(attributeValue.isPresent());
		assertEquals(attributeValue.get(), "someValue");
	}
}