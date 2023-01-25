/*
 * Copyright 2015, 2019 Uppsala University Library
 * Copyright 2022 Olov McKie
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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientData;
import se.uu.ub.cora.clientdata.ClientDataAtomic;
import se.uu.ub.cora.clientdata.ClientDataAttribute;
import se.uu.ub.cora.clientdata.ClientDataChild;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataMissingException;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.clientdata.ClientDataRecordLink;

public class BasicClientDataRecordGroupTest {

	private ClientDataRecordGroup defaultRecordGroup;

	@BeforeMethod
	public void setUp() {
		defaultRecordGroup = BasicClientDataRecordGroup.withNameInData("someDataGroup");
	}

	@Test
	public void testInit() {
		assertEquals(defaultRecordGroup.getNameInData(), "someDataGroup");
		assertNotNull(defaultRecordGroup.getAttributes());
		assertNotNull(defaultRecordGroup.getChildren());
	}

	@Test
	public void testGroupIsData() {
		assertTrue(defaultRecordGroup instanceof ClientData);
	}

	@Test
	public void testInitWithRepeatId() {
		assertEquals(defaultRecordGroup.getNameInData(), "someDataGroup");
		assertNotNull(defaultRecordGroup.getAttributes());
		assertNotNull(defaultRecordGroup.getChildren());
	}

	@Test
	public void testAddAttribute() {
		defaultRecordGroup.addAttributeByIdWithValue("someAttributeName", "value");
		Collection<ClientDataAttribute> attributes = defaultRecordGroup.getAttributes();
		ClientDataAttribute next = attributes.iterator().next();
		assertEquals(next.getNameInData(), "someAttributeName");
		assertEquals(next.getValue(), "value");
	}

	@Test
	public void testAddAttributeWithSameNameInDataOverwrites() {
		defaultRecordGroup.addAttributeByIdWithValue("someAttributeName", "value");
		defaultRecordGroup.addAttributeByIdWithValue("someAttributeName", "someOtherValue");

		Collection<ClientDataAttribute> attributes = defaultRecordGroup.getAttributes();
		assertEquals(attributes.size(), 1);
		ClientDataAttribute next = attributes.iterator().next();
		assertEquals(next.getValue(), "someOtherValue");
	}

	@Test
	public void testHasAttributes() {
		assertFalse(defaultRecordGroup.hasAttributes());
		defaultRecordGroup.addAttributeByIdWithValue("attributeId", "attributeValue");
		assertTrue(defaultRecordGroup.hasAttributes());
	}

	@Test
	public void testGetAttribute() {
		defaultRecordGroup.addAttributeByIdWithValue("attributeId", "attributeValue");
		assertEquals(defaultRecordGroup.getAttribute("attributeId").getValue(), "attributeValue");
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Attribute with id someAttributeId not found.")
	public void testGetAttributeDoesNotExist() {
		defaultRecordGroup.getAttribute("someAttributeId");
	}

	@Test
	public void testAddChild() {
		ClientDataChild dataElement = BasicClientDataAtomic
				.withNameInDataAndValue("childNameInData", "childValue");
		defaultRecordGroup.addChild(dataElement);
		List<ClientDataChild> children = defaultRecordGroup.getChildren();
		ClientDataChild childElementOut = children.get(0);
		assertEquals(childElementOut.getNameInData(), "childNameInData");
	}

	@Test
	public void testHasChildren() throws Exception {
		assertFalse(defaultRecordGroup.hasChildren());
		defaultRecordGroup.addChild(BasicClientDataGroup.withNameInData("child"));
		assertTrue(defaultRecordGroup.hasChildren());
	}

	@Test
	public void addChildrenEmptyList() {
		defaultRecordGroup.addChildren(Collections.emptyList());
		assertTrue(defaultRecordGroup.getChildren().isEmpty());
	}

	@Test
	public void testAddChildrenAddOneChildNoChildrenBefore() {
		List<ClientDataChild> dataElements = createListWithOneChild();

		defaultRecordGroup.addChildren(dataElements);

		List<ClientDataChild> children = defaultRecordGroup.getChildren();
		assertEquals(children.size(), 1);
		assertSame(children.get(0), dataElements.get(0));
	}

	@Test
	public void testAddChildrenAddOneChildOneChildBefore() {
		defaultRecordGroup
				.addChild(BasicClientDataAtomic.withNameInDataAndValue("someChild", "someValue"));
		List<ClientDataChild> dataElements = createListWithOneChild();

		defaultRecordGroup.addChildren(dataElements);

		List<ClientDataChild> children = defaultRecordGroup.getChildren();
		assertEquals(children.size(), 2);
		assertSame(children.get(1), dataElements.get(0));
	}

	@Test
	public void testAddChildrenAddMultipleChildOneChildBefore() {
		defaultRecordGroup
				.addChild(BasicClientDataAtomic.withNameInDataAndValue("someChild", "someValue"));
		List<ClientDataChild> dataElements = createListWithOneChild();
		dataElements.add(BasicClientDataRecordGroup.withNameInData("someGroupChild"));
		dataElements
				.add(BasicClientDataAtomic.withNameInDataAndValue("someOtherAtomicChild", "42"));

		defaultRecordGroup.addChildren(dataElements);

		List<ClientDataChild> children = defaultRecordGroup.getChildren();
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
		defaultRecordGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("otherChildId", "otherChildValue"));
		ClientDataChild child = BasicClientDataAtomic.withNameInDataAndValue("childId",
				"child value");
		defaultRecordGroup.addChild(child);
		assertTrue(defaultRecordGroup.containsChildWithNameInData("childId"));
	}

	@Test
	public void testContainsChildWithIdNotFound() {
		ClientDataChild child = BasicClientDataAtomic.withNameInDataAndValue("childId",
				"child value");
		defaultRecordGroup.addChild(child);
		assertFalse(defaultRecordGroup.containsChildWithNameInData("childId_NOT_FOUND"));
	}

	@Test
	public void testGetAtomicValue() {
		defaultRecordGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("atomicNameInData", "atomicValue"));
		assertEquals(defaultRecordGroup.getFirstAtomicValueWithNameInData("atomicNameInData"),
				"atomicValue");
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Atomic value not found for childNameInData:" + "atomicNameInData_NOT_FOUND")
	public void testExtractAtomicValueNotFound() {
		defaultRecordGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("atomicNameInData", "atomicValue"));
		defaultRecordGroup.getFirstAtomicValueWithNameInData("atomicNameInData_NOT_FOUND");
	}

	@Test
	public void testGetAllDataAtomicsWithNameInData() {
		ClientDataRecordGroup book = createDataGroupWithTwoAtomicChildrenAndOneGroupChild();

		assertEquals(book.getAllDataAtomicsWithNameInData("someChild").size(), 2);
	}

	@Test
	public void testGetAllDataAtomicsWithNameInDataNoResult() throws Exception {
		ClientDataRecordGroup dataGroup = BasicClientDataRecordGroup
				.withNameInData("someNameInData");
		List<ClientDataAtomic> aList = dataGroup.getAllDataAtomicsWithNameInData("someNameInData");
		assertEquals(aList.size(), 0);
	}

	private ClientDataRecordGroup createDataGroupWithTwoAtomicChildrenAndOneGroupChild() {
		ClientDataRecordGroup book = BasicClientDataRecordGroup.withNameInData("book");
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
		ClientDataRecordGroup book = createDataGroupWithTwoAtomicChildrenAndOneGroupChild();
		assertEquals(book.getFirstDataAtomicWithNameInData("someChild"), book.getChildren().get(0));
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "ClientDataAtomic not found for childNameInData:childNameInData_NOT_FOUND")
	public void testGetFirstDataAtomicWithNameInDataNotFound() {
		defaultRecordGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("someChildNameInData", "atomicValue"));
		defaultRecordGroup.getFirstDataAtomicWithNameInData("childNameInData_NOT_FOUND");
	}

	@Test
	public void testGetAllDataAtomicsdWithNameInDataAndAttributesOneMatch() {
		ClientDataRecordGroup dataGroup = createDataGroupWithDataAtomicChildren();

		List<ClientDataAtomic> atomicsFound = (List<ClientDataAtomic>) dataGroup
				.getAllDataAtomicsWithNameInDataAndAttributes("childOne", BasicClientDataAttribute
						.withNameInDataAndValue("otherAttribute", "alternative"));

		assertEquals(atomicsFound.size(), 1);
		ClientDataChild expectedMatchingChild = dataGroup.getChildren().get(2);
		assertSame(atomicsFound.get(0), expectedMatchingChild);
	}

	private ClientDataRecordGroup createDataGroupWithDataAtomicChildren() {
		ClientDataRecordGroup dataGroup = BasicClientDataRecordGroup
				.withNameInData("someDataGroup");

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
		ClientDataRecordGroup dataGroup = createDataGroupWithDataAtomicChildren();

		List<ClientDataAtomic> atomicsFound = (List<ClientDataAtomic>) dataGroup
				.getAllDataAtomicsWithNameInDataAndAttributes("childOne", BasicClientDataAttribute
						.withNameInDataAndValue("nonMatchingAttribute", "alternative"));

		assertEquals(atomicsFound.size(), 0);
	}

	@Test
	public void testGetGroup() {
		defaultRecordGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("atomicNameInData", "atomicValue"));
		ClientDataGroup dataGroup2 = BasicClientDataGroup.withNameInData("childNameInData");
		dataGroup2.addChild(BasicClientDataGroup.withNameInData("grandChildNameInData"));
		defaultRecordGroup.addChild(dataGroup2);
		assertEquals(defaultRecordGroup.getFirstGroupWithNameInData("childNameInData"), dataGroup2);
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Group not found for childNameInData:childNameInData_NOT_FOUND")
	public void testGetFirstGroupWithNameInDataNotFound() {
		defaultRecordGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("atomicNameInData", "atomicValue"));
		ClientDataGroup dataGroup2 = BasicClientDataGroup.withNameInData("childNameInData");
		dataGroup2.addChild(BasicClientDataGroup.withNameInData("grandChildNameInData"));
		defaultRecordGroup.addChild(dataGroup2);
		defaultRecordGroup.getFirstGroupWithNameInData("childNameInData_NOT_FOUND");
	}

	@Test
	public void testGetFirstChildWithNameInData() {
		defaultRecordGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("atomicNameInData", "atomicValue"));
		ClientDataGroup dataGroup2 = BasicClientDataGroup.withNameInData("childNameInData");
		dataGroup2.addChild(BasicClientDataGroup.withNameInData("grandChildNameInData"));
		defaultRecordGroup.addChild(dataGroup2);
		assertEquals(defaultRecordGroup.getFirstChildWithNameInData("childNameInData"), dataGroup2);
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Element not found for childNameInData:childNameInData_NOT_FOUND")
	public void testGetFirstChildWithNameInDataNotFound() {
		defaultRecordGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("atomicNameInData", "atomicValue"));
		ClientDataGroup dataGroup2 = BasicClientDataGroup.withNameInData("childNameInData");
		dataGroup2.addChild(BasicClientDataGroup.withNameInData("grandChildNameInData"));
		defaultRecordGroup.addChild(dataGroup2);
		defaultRecordGroup.getFirstChildWithNameInData("childNameInData_NOT_FOUND");
	}

	@Test
	public void testGetAllGroupsWithNameInData() {
		defaultRecordGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("atomicNameInData", "atomicValue"));
		addTwoGroupChildrenWithSameNameInData(defaultRecordGroup);

		List<ClientDataGroup> groupsFound = defaultRecordGroup
				.getAllGroupsWithNameInData("childNameInData");
		assertEquals(groupsFound.size(), 2);
	}

	private void addTwoGroupChildrenWithSameNameInData(ClientDataRecordGroup parentDataGroup) {
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
		defaultRecordGroup.addChild(
				BasicClientDataAtomic.withNameInDataAndValue("atomicNameInData", "atomicValue"));

		List<ClientDataGroup> groupsFound = defaultRecordGroup
				.getAllGroupsWithNameInData("childNameInData");
		assertEquals(groupsFound.size(), 0);
	}

	@Test
	public void testGetAllGroupsWithNameInDataAndAttributesOneMatch() {
		ClientDataRecordGroup dataGroup = BasicClientDataRecordGroup
				.withNameInData("someDataGroup");
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
			ClientDataRecordGroup dataGroup) {
		addAndReturnDataGroupChildWithNameInData(dataGroup, "groupId2");
		addAndReturnDataGroupChildWithNameInData(dataGroup, "groupId3");
		addAndReturnDataGroupChildWithNameInData(dataGroup, "groupId2");
		ClientDataGroup child3 = addAndReturnDataGroupChildWithNameInDataAndAttributes(dataGroup,
				"groupId2",
				BasicClientDataAttribute.withNameInDataAndValue("nameInData", "value1"));
		return child3;
	}

	private ClientDataGroup addAndReturnDataGroupChildWithNameInData(
			ClientDataRecordGroup dataGroup, String nameInData) {
		ClientDataGroup child = BasicClientDataGroup.withNameInData(nameInData);
		dataGroup.addChild(child);
		return child;
	}

	private ClientDataGroup addAndReturnDataGroupChildWithNameInDataAndAttributes(
			ClientDataRecordGroup dataGroup, String nameInData,
			BasicClientDataAttribute... attributes) {
		ClientDataGroup child = BasicClientDataGroup.withNameInData(nameInData);
		dataGroup.addChild(child);
		for (BasicClientDataAttribute attribute : attributes) {
			child.addAttributeByIdWithValue(attribute.getNameInData(), attribute.getValue());
		}
		return child;
	}

	@Test
	public void testGetAllGroupsWithNameInDataAndAttributesTwoMatches() {
		ClientDataRecordGroup dataGroup = BasicClientDataRecordGroup
				.withNameInData("someDataGroup");
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
		ClientDataRecordGroup dataGroup = BasicClientDataRecordGroup
				.withNameInData("someDataGroup");
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
		ClientDataRecordGroup dataGroup = BasicClientDataRecordGroup
				.withNameInData("someDataGroup");
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
		ClientDataRecordGroup dataGroup = BasicClientDataRecordGroup
				.withNameInData("someDataGroup");
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
		ClientDataRecordGroup dataGroup = BasicClientDataRecordGroup
				.withNameInData("someDataGroup");
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
		ClientDataRecordGroup dataGroup = BasicClientDataRecordGroup
				.withNameInData("someDataGroup");
		createAndAddAnAtomicChildToDataGroup(dataGroup);
		boolean childWasRemoved = dataGroup.removeFirstChildWithNameInData("childId");
		assertTrue(childWasRemoved);
		assertFalse(dataGroup.containsChildWithNameInData("childId"));
	}

	@Test
	public void testRemoveChildMoreThanOneChildExist() {
		ClientDataRecordGroup dataGroup = BasicClientDataRecordGroup
				.withNameInData("someDataGroup");
		createAndAddAnAtomicChildToDataGroup(dataGroup);
		createAndAddAnAtomicChildToDataGroup(dataGroup);
		boolean childWasRemoved = dataGroup.removeFirstChildWithNameInData("childId");
		assertTrue(childWasRemoved);
		assertTrue(dataGroup.containsChildWithNameInData("childId"));
	}

	@Test
	public void testRemoveChildNotFound() {
		ClientDataRecordGroup dataGroup = BasicClientDataRecordGroup
				.withNameInData("someDataGroup");
		createAndAddAnAtomicChildToDataGroup(dataGroup);
		boolean childWasRemoved = dataGroup.removeFirstChildWithNameInData("childId_NOTFOUND");
		assertFalse(childWasRemoved);
	}

	private ClientDataChild createAndAddAnAtomicChildToDataGroup(ClientDataRecordGroup dataGroup) {
		return createAndAddAnAtomicChildToDataGroupUsingNameInData(dataGroup, "childId");
	}

	private ClientDataChild createAndAddAnAtomicChildToDataGroupUsingNameInData(
			ClientDataRecordGroup dataGroup, String nameInData) {
		ClientDataChild child = BasicClientDataAtomic.withNameInDataAndValue(nameInData,
				"child value");
		dataGroup.addChild(child);
		return child;
	}

	@Test
	public void testRemoveAllChildrenWithNameInData() {
		ClientDataRecordGroup dataGroup = BasicClientDataRecordGroup
				.withNameInData("someDataGroup");
		createAndAddAnAtomicChildWithRepeatIdToDataGroup(dataGroup, "0");
		createAndAddAnAtomicChildWithRepeatIdToDataGroup(dataGroup, "1");
		boolean childWasRemoved = dataGroup.removeAllChildrenWithNameInData("childId");
		assertTrue(childWasRemoved);
		assertFalse(dataGroup.containsChildWithNameInData("childId"));
	}

	private ClientDataChild createAndAddAnAtomicChildWithRepeatIdToDataGroup(
			ClientDataRecordGroup dataGroup, String repeatId) {
		ClientDataChild child = BasicClientDataAtomic.withNameInDataAndValueAndRepeatId("childId",
				"child value", repeatId);
		dataGroup.addChild(child);
		return child;
	}

	@Test
	public void testRemoveAllChildrenWithNameInDataWhenOtherChildrenExist() {
		ClientDataRecordGroup dataGroup = BasicClientDataRecordGroup
				.withNameInData("someDataGroup");
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
		ClientDataRecordGroup dataGroup = BasicClientDataRecordGroup
				.withNameInData("someDataGroup");
		createAndAddAnAtomicChildToDataGroup(dataGroup);
		assertFalse(dataGroup.removeAllChildrenWithNameInData("childId_NOTFOUND"));
	}

	@Test
	public void testGetAllChildrenWithNameInDataNoChildren() {
		ClientDataRecordGroup dataGroup = BasicClientDataRecordGroup
				.withNameInData("someDataGroup");
		List<ClientDataChild> allChildrenWithNameInData = dataGroup
				.getAllChildrenWithNameInData("someChildNameInData");
		assertTrue(allChildrenWithNameInData.isEmpty());

	}

	@Test
	public void testGetAllChildrenWithNameInDataNoMatchingChildren() {
		ClientDataRecordGroup dataGroup = BasicClientDataRecordGroup
				.withNameInData("someDataGroup");
		createAndAddAtomicChild(dataGroup, "someChildNameInData", "0");
		List<ClientDataChild> allChildrenWithNameInData = dataGroup
				.getAllChildrenWithNameInData("someOtherChildNameInData");
		assertTrue(allChildrenWithNameInData.isEmpty());

	}

	@Test
	public void testGetAllChildrenWithNameInDataOneMatchingAtomicChild() {
		ClientDataRecordGroup dataGroup = BasicClientDataRecordGroup
				.withNameInData("someDataGroup");
		BasicClientDataAtomic atomicChild = createAndAddAtomicChild(dataGroup,
				"someChildNameInData", "0");

		List<ClientDataChild> allChildrenWithNameInData = dataGroup
				.getAllChildrenWithNameInData("someChildNameInData");
		assertEquals(allChildrenWithNameInData.size(), 1);
		assertSame(allChildrenWithNameInData.get(0), atomicChild);
	}

	private BasicClientDataAtomic createAndAddAtomicChild(ClientDataRecordGroup dataGroup,
			String nameInData, String repeatId) {
		BasicClientDataAtomic atomicChild = BasicClientDataAtomic.withNameInDataAndValue(nameInData,
				"someValue");
		atomicChild.setRepeatId(repeatId);
		dataGroup.addChild(atomicChild);
		return atomicChild;
	}

	@Test
	public void testGetAllChildrenWithNameInDataMultipleMatchesDifferentTypes() {
		BasicClientDataAtomic atomicChild = createAndAddAtomicChild(defaultRecordGroup,
				"someChildNameInData", "0");
		BasicClientDataAtomic atomicChild2 = createAndAddAtomicChild(defaultRecordGroup,
				"someChildNameInData", "1");
		BasicClientDataAtomic atomicChild3 = createAndAddAtomicChild(defaultRecordGroup,
				"someNOTChildNameInData", "2");

		ClientDataGroup dataGroupChild = BasicClientDataGroup.withNameInData("someChildNameInData");
		defaultRecordGroup.addChild(dataGroupChild);

		List<ClientDataChild> allChildrenWithNameInData = defaultRecordGroup
				.getAllChildrenWithNameInData("someChildNameInData");
		assertEquals(allChildrenWithNameInData.size(), 3);
		assertSame(allChildrenWithNameInData.get(0), atomicChild);
		assertSame(allChildrenWithNameInData.get(1), atomicChild2);
		assertSame(allChildrenWithNameInData.get(2), dataGroupChild);
		assertFalse(allChildrenWithNameInData.contains(atomicChild3));
	}

	@Test
	public void testRemoveChildrenWithAttributesNoMatchWrongChildNoAttributes() {
		createAndAddAnAtomicChildWithRepeatIdToDataGroup(defaultRecordGroup, "0");
		boolean childWasRemoved = defaultRecordGroup
				.removeAllChildrenWithNameInDataAndAttributes("NOTchildId");
		assertFalse(childWasRemoved);
		assertTrue(defaultRecordGroup.containsChildWithNameInData("childId"));
	}

	@Test
	public void testRemoveChildrenWithAttributesNoMatchNoAttributes() {
		createAndAddAnAtomicChildWithRepeatIdToDataGroup(defaultRecordGroup, "0");
		boolean childWasRemoved = defaultRecordGroup.removeAllChildrenWithNameInDataAndAttributes(
				"childId",
				BasicClientDataAttribute.withNameInDataAndValue("someName", "someValue"));
		assertFalse(childWasRemoved);
		assertTrue(defaultRecordGroup.containsChildWithNameInData("childId"));
	}

	@Test
	public void testRemoveChildrenWithAttributesNoMatchWithWrongAttributes() {
		ClientDataGroup childDataGroup = BasicClientDataGroup.withNameInData("childId");
		childDataGroup.addAttributeByIdWithValue("someName", "someValue");
		defaultRecordGroup.addChild(childDataGroup);

		boolean childWasRemoved = defaultRecordGroup.removeAllChildrenWithNameInDataAndAttributes(
				"childId",
				BasicClientDataAttribute.withNameInDataAndValue("someName", "someOtherValue"));
		assertFalse(childWasRemoved);
		assertTrue(defaultRecordGroup.containsChildWithNameInData("childId"));
	}

	@Test
	public void testRemoveChildrenWithAttributesOneMatchNoAttributes() {
		createAndAddAnAtomicChildWithRepeatIdToDataGroup(defaultRecordGroup, "0");
		boolean childWasRemoved = defaultRecordGroup
				.removeAllChildrenWithNameInDataAndAttributes("childId");
		assertTrue(childWasRemoved);
	}

	@Test
	public void testRemoveChildrenWithAttributesOneMatchWithAttributes() {
		ClientDataGroup childDataGroup = BasicClientDataGroup.withNameInData("childId");
		childDataGroup.addAttributeByIdWithValue("someName", "someValue");
		defaultRecordGroup.addChild(childDataGroup);
		boolean childWasRemoved = defaultRecordGroup.removeAllChildrenWithNameInDataAndAttributes(
				"childId",
				BasicClientDataAttribute.withNameInDataAndValue("someName", "someValue"));
		assertTrue(childWasRemoved);
		assertFalse(defaultRecordGroup.containsChildWithNameInData("childId"));
	}

	@Test
	public void testRemoveChildrenWithAttributesTwoChildrenOneMatchWithAttributes() {
		setUpDataGroupWithTwoChildrenOneWithAttributes();

		assertEquals(defaultRecordGroup.getAllChildrenWithNameInData("childId").size(), 2);

		boolean childWasRemoved = defaultRecordGroup.removeAllChildrenWithNameInDataAndAttributes(
				"childId",
				BasicClientDataAttribute.withNameInDataAndValue("someName", "someValue"));

		assertTrue(childWasRemoved);
		assertTrue(defaultRecordGroup.containsChildWithNameInData("childId"));

		List<ClientDataChild> allChildrenWithNameInData = defaultRecordGroup
				.getAllChildrenWithNameInData("childId");
		assertEquals(allChildrenWithNameInData.size(), 1);
		assertTrue(allChildrenWithNameInData.get(0) instanceof BasicClientDataAtomic);

	}

	@Test
	public void testRemoveChildrenWithAttributesTwoChildrenOneMatchWithoutAttributes() {
		setUpDataGroupWithTwoChildrenOneWithAttributes();

		boolean childWasRemoved = defaultRecordGroup
				.removeAllChildrenWithNameInDataAndAttributes("childId");
		assertTrue(childWasRemoved);
		assertTrue(defaultRecordGroup.containsChildWithNameInData("childId"));

		List<ClientDataChild> allChildrenWithNameInData = defaultRecordGroup
				.getAllChildrenWithNameInData("childId");
		assertEquals(allChildrenWithNameInData.size(), 1);
		assertTrue(allChildrenWithNameInData.get(0) instanceof BasicClientDataGroup);
	}

	private void setUpDataGroupWithTwoChildrenOneWithAttributes() {
		ClientDataGroup childDataGroup = BasicClientDataGroup.withNameInData("childId");
		childDataGroup.addAttributeByIdWithValue("someName", "someValue");
		defaultRecordGroup.addChild(childDataGroup);
		createAndAddAnAtomicChildWithRepeatIdToDataGroup(defaultRecordGroup, "0");
	}

	@Test
	public void testRemoveChildrenWithAttributesTwoChildrenNoMatchWithAttributes() {
		setUpDataGroupWithTwoChildrenOneWithAttributes();

		boolean childWasRemoved = defaultRecordGroup.removeAllChildrenWithNameInDataAndAttributes(
				"childId",
				BasicClientDataAttribute.withNameInDataAndValue("someNOTName", "someValue"));
		assertFalse(childWasRemoved);
		assertTrue(defaultRecordGroup.containsChildWithNameInData("childId"));

		List<ClientDataChild> allChildrenWithNameInData = defaultRecordGroup
				.getAllChildrenWithNameInData("childId");
		assertEquals(allChildrenWithNameInData.size(), 2);
		assertTrue(allChildrenWithNameInData.get(0) instanceof BasicClientDataGroup);
		assertTrue(allChildrenWithNameInData.get(1) instanceof BasicClientDataAtomic);
	}

	@Test
	public void testRemoveChildrenWithAttributesMultipleChildrenTwoMatchesWithAttributes() {
		setUpDataGroupWithMultipleChildrenWithAttributesAndWithoutAttributes();

		boolean childWasRemoved = defaultRecordGroup.removeAllChildrenWithNameInDataAndAttributes(
				"childId",
				BasicClientDataAttribute.withNameInDataAndValue("someName", "someValue"));
		assertTrue(childWasRemoved);
		assertTrue(defaultRecordGroup.containsChildWithNameInData("childId"));

		List<ClientDataChild> allChildrenWithNameInData = defaultRecordGroup
				.getAllChildrenWithNameInData("childId");
		assertEquals(allChildrenWithNameInData.size(), 3);
		assertTrue(allChildrenWithNameInData.get(0) instanceof BasicClientDataAtomic);
		assertTrue(allChildrenWithNameInData.get(1) instanceof BasicClientDataAtomic);
		assertTrue(allChildrenWithNameInData.get(2) instanceof BasicClientDataGroup);

		assertEquals(defaultRecordGroup.getAllChildrenWithNameInData("childOtherId").size(), 1);
	}

	private void setUpDataGroupWithMultipleChildrenWithAttributesAndWithoutAttributes() {
		ClientDataGroup childDataGroupWithAttribute = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"childId", "0");
		defaultRecordGroup.addChild(childDataGroupWithAttribute);
		ClientDataGroup childDataGroupWithAttribute2 = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"childId", "1");
		defaultRecordGroup.addChild(childDataGroupWithAttribute2);

		createAndAddAnAtomicChildWithRepeatIdToDataGroup(defaultRecordGroup, "0");
		createAndAddAnAtomicChildWithRepeatIdToDataGroup(defaultRecordGroup, "1");

		ClientDataGroup childDataGroupWithAtttributeOtherName = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"childOtherId", "0");
		defaultRecordGroup.addChild(childDataGroupWithAtttributeOtherName);

		ClientDataGroup childDataGroupWithExtraAttribute = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"childId", "0");
		childDataGroupWithExtraAttribute.addAttributeByIdWithValue("someOtherName", "someValue");
		defaultRecordGroup.addChild(childDataGroupWithExtraAttribute);
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
		defaultRecordGroup.addChild(childGroup);

		List<ClientDataChild> children = defaultRecordGroup
				.getAllChildrenWithNameInDataAndAttributes("someChildNameInData");

		assertTrue(children.isEmpty());
	}

	@Test
	public void testGetAllChildrenWithNameInDataAndAttributesNoMatchNotMatchingNameInData() {
		ClientDataGroup childGroup = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"someChildNameInData", "0");
		defaultRecordGroup.addChild(childGroup);

		BasicClientDataAttribute attribute = BasicClientDataAttribute
				.withNameInDataAndValue("someName", "someValue");
		List<ClientDataChild> children = defaultRecordGroup
				.getAllChildrenWithNameInDataAndAttributes("someOtherChildNameInData", attribute);

		assertTrue(children.isEmpty());
	}

	@Test
	public void testGetAllChildrenWithNameInDataAndAttributesMatch() {
		ClientDataGroup childGroup = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"someChildNameInData", "0");
		defaultRecordGroup.addChild(childGroup);

		BasicClientDataAttribute attribute = BasicClientDataAttribute
				.withNameInDataAndValue("someName", "someValue");

		List<ClientDataChild> children = defaultRecordGroup
				.getAllChildrenWithNameInDataAndAttributes("someChildNameInData", attribute);

		assertEquals(children.size(), 1);

		assertSame(children.get(0), childGroup);
	}

	@Test
	public void testGetAllChildrenWithNameInDataAndAttributesDataAtomicChild() {
		ClientDataGroup childGroup = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"someChildNameInData", "0");
		defaultRecordGroup.addChild(childGroup);

		BasicClientDataAtomic coraDataAtomic = BasicClientDataAtomic
				.withNameInDataAndValue("someChildNameInData", "someValue");

		defaultRecordGroup.addChild(coraDataAtomic);

		List<ClientDataChild> children = defaultRecordGroup
				.getAllChildrenWithNameInDataAndAttributes("someChildNameInData");

		assertEquals(children.size(), 1);
		assertSame(children.get(0), coraDataAtomic);

	}

	@Test
	public void testGetAllChildrenWithNameInDataAndAttributesMatchRepeatingGroup() {
		ClientDataGroup childGroup = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"someChildNameInData", "0");
		defaultRecordGroup.addChild(childGroup);

		ClientDataGroup childGroup2 = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"someChildNameInData", "1");
		defaultRecordGroup.addChild(childGroup2);

		BasicClientDataAttribute attribute = BasicClientDataAttribute
				.withNameInDataAndValue("someName", "someValue");

		List<ClientDataChild> children = defaultRecordGroup
				.getAllChildrenWithNameInDataAndAttributes("someChildNameInData", attribute);

		assertEquals(children.size(), 2);

		assertSame(children.get(0), childGroup);
		assertSame(children.get(1), childGroup2);
	}

	@Test
	public void testGetAllChildrenWithNameInDataAndAttributesMultipleChildrenMatchOneGroup() {
		ClientDataGroup childGroup = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"someChildNameInData", "0");
		defaultRecordGroup.addChild(childGroup);

		ClientDataGroup childGroupOtherNameInData = createChildGroupWithNameInDataAndRepatIdAndAttributes(
				"someOtherChildNameInData", "1");
		defaultRecordGroup.addChild(childGroupOtherNameInData);

		ClientDataGroup childGroup2 = BasicClientDataGroup.withNameInData("someChildNameInData");
		defaultRecordGroup.addChild(childGroup2);

		BasicClientDataAttribute attribute = BasicClientDataAttribute
				.withNameInDataAndValue("someName", "someValue");

		List<ClientDataChild> childrenWithAttributes = defaultRecordGroup
				.getAllChildrenWithNameInDataAndAttributes("someChildNameInData", attribute);

		assertEquals(childrenWithAttributes.size(), 1);
		assertSame(childrenWithAttributes.get(0), childGroup);

		List<ClientDataChild> childrenWithoutAttributes = defaultRecordGroup
				.getAllChildrenWithNameInDataAndAttributes("someChildNameInData");

		assertEquals(childrenWithoutAttributes.size(), 1);
		assertSame(childrenWithoutAttributes.get(0), childGroup2);
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Group not found for childNameInData:recordInfo")
	public void testGetType_NoRecordInfo() throws Exception {
		defaultRecordGroup.getType();
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Element not found for childNameInData:type")
	public void testGetType_NoTypeLink() throws Exception {
		defaultRecordGroup.addChild(BasicClientDataGroup.withNameInData("recordInfo"));

		defaultRecordGroup.getType();
	}

	@Test
	public void testGetType() throws Exception {
		BasicClientDataGroup recordInfo = BasicClientDataGroup.withNameInData("recordInfo");
		defaultRecordGroup.addChild(recordInfo);
		recordInfo.addChild(
				BasicClientDataRecordLink.usingNameInDataAndTypeAndId("type", "", "someTypeId"));

		assertEquals(defaultRecordGroup.getType(), "someTypeId");
	}

	@Test
	public void testSetType() throws Exception {
		BasicClientDataGroup recordInfo = BasicClientDataGroup.withNameInData("recordInfo");
		defaultRecordGroup.addChild(recordInfo);
		recordInfo.addChild(
				BasicClientDataRecordLink.usingNameInDataAndTypeAndId("type", "", "someTypeId"));

		defaultRecordGroup.setType("someOtherTypeId");

		assertEquals(defaultRecordGroup.getType(), "someOtherTypeId");
	}

	@Test
	public void testSetType_NoType() throws Exception {
		BasicClientDataGroup recordInfo = BasicClientDataGroup.withNameInData("recordInfo");
		defaultRecordGroup.addChild(recordInfo);

		defaultRecordGroup.setType("someOtherTypeId");

		assertEquals(defaultRecordGroup.getType(), "someOtherTypeId");
		ClientDataRecordLink dataDividerLink = (ClientDataRecordLink) recordInfo
				.getFirstChildWithNameInData("type");
		assertEquals(dataDividerLink.getLinkedRecordType(), "recordType");
	}

	@Test
	public void testSetType_NoRecordInfo() throws Exception {
		defaultRecordGroup.setType("someOtherTypeId");

		assertEquals(defaultRecordGroup.getType(), "someOtherTypeId");
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Group not found for childNameInData:recordInfo")
	public void testGetId_NoRecordInfo() throws Exception {
		defaultRecordGroup.getId();
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Atomic value not found for childNameInData:id")
	public void testGetId_NoIdLink() throws Exception {
		defaultRecordGroup.addChild(BasicClientDataGroup.withNameInData("recordInfo"));

		defaultRecordGroup.getId();
	}

	@Test
	public void testGetId() throws Exception {
		BasicClientDataGroup recordInfo = BasicClientDataGroup.withNameInData("recordInfo");
		defaultRecordGroup.addChild(recordInfo);
		recordInfo.addChild(BasicClientDataAtomic.withNameInDataAndValue("id", "someId"));

		assertEquals(defaultRecordGroup.getId(), "someId");
	}

	@Test
	public void testSetId() throws Exception {
		BasicClientDataGroup recordInfo = BasicClientDataGroup.withNameInData("recordInfo");
		defaultRecordGroup.addChild(recordInfo);
		recordInfo.addChild(
				BasicClientDataRecordLink.usingNameInDataAndTypeAndId("id", "", "someIdId"));

		defaultRecordGroup.setId("someOtherId");

		assertEquals(defaultRecordGroup.getId(), "someOtherId");
	}

	@Test
	public void testSetId_NoId() throws Exception {
		BasicClientDataGroup recordInfo = BasicClientDataGroup.withNameInData("recordInfo");
		defaultRecordGroup.addChild(recordInfo);

		defaultRecordGroup.setId("someOtherId");

		assertEquals(defaultRecordGroup.getId(), "someOtherId");
		ClientDataAtomic id = (ClientDataAtomic) recordInfo.getFirstChildWithNameInData("id");
		assertEquals(id.getValue(), "someOtherId");
	}

	@Test
	public void testSetId_NoRecordInfo() throws Exception {
		defaultRecordGroup.setId("someOtherId");

		assertEquals(defaultRecordGroup.getId(), "someOtherId");
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Group not found for childNameInData:recordInfo")
	public void testGetDataDivider_NoRecordInfo() throws Exception {
		defaultRecordGroup.getDataDivider();
	}

	@Test(expectedExceptions = ClientDataMissingException.class, expectedExceptionsMessageRegExp = ""
			+ "Element not found for childNameInData:dataDivider")
	public void testGetDataDivider_NoDataDividerLink() throws Exception {
		defaultRecordGroup.addChild(BasicClientDataGroup.withNameInData("recordInfo"));

		defaultRecordGroup.getDataDivider();
	}

	@Test
	public void testGetDataDivider() throws Exception {
		BasicClientDataGroup recordInfo = BasicClientDataGroup.withNameInData("recordInfo");
		defaultRecordGroup.addChild(recordInfo);
		recordInfo.addChild(BasicClientDataRecordLink.usingNameInDataAndTypeAndId("dataDivider", "",
				"someDataDividerId"));

		assertEquals(defaultRecordGroup.getDataDivider(), "someDataDividerId");
	}

	@Test
	public void testSetDataDivider() throws Exception {
		BasicClientDataGroup recordInfo = BasicClientDataGroup.withNameInData("recordInfo");
		defaultRecordGroup.addChild(recordInfo);
		recordInfo.addChild(BasicClientDataRecordLink.usingNameInDataAndTypeAndId("dataDivider", "",
				"someDataDividerId"));

		defaultRecordGroup.setDataDivider("someOtherDataDividerId");

		assertEquals(defaultRecordGroup.getDataDivider(), "someOtherDataDividerId");
	}

	@Test
	public void testSetDataDivider_NoDataDivider() throws Exception {
		BasicClientDataGroup recordInfo = BasicClientDataGroup.withNameInData("recordInfo");
		defaultRecordGroup.addChild(recordInfo);

		defaultRecordGroup.setDataDivider("someOtherDataDividerId");

		assertEquals(defaultRecordGroup.getDataDivider(), "someOtherDataDividerId");
		ClientDataRecordLink dataDividerLink = (ClientDataRecordLink) recordInfo
				.getFirstChildWithNameInData("dataDivider");
		assertEquals(dataDividerLink.getLinkedRecordType(), "system");
	}

	@Test
	public void testSetDataDivider_NoRecordInfo() throws Exception {
		defaultRecordGroup.setDataDivider("someOtherDataDividerId");

		assertEquals(defaultRecordGroup.getDataDivider(), "someOtherDataDividerId");
	}
}