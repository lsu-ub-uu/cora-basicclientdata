/*
 * Copyright 2019, 2022 Uppsala University Library
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

package se.uu.ub.cora.clientbasicdata;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientbasicdata.data.BasicClientDataAtomic;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataAttribute;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataChildFilter;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataGroup;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataList;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataRecord;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataRecordGroup;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataRecordLink;
import se.uu.ub.cora.clientbasicdata.data.BasicClientDataResourceLink;
import se.uu.ub.cora.clientdata.ClientDataAtomic;
import se.uu.ub.cora.clientdata.ClientDataAttribute;
import se.uu.ub.cora.clientdata.ClientDataFactory;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataList;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.clientdata.ClientDataRecordLink;
import se.uu.ub.cora.clientdata.ClientDataResourceLink;

public class BasicClientCoraDataFactoryTest {
	private ClientDataFactory dataFactory;
	private String containDataOfType = "someType";
	private String nameInData = "nameInData";
	private String recordType = "someRecordType";
	private String recordId = "someRecordId";
	private String value = "someValue";

	@BeforeMethod
	public void setUp() {
		dataFactory = new BasicClientDataFactory();
	}

	@Test
	public void testFactorListUsingNameOfDataType() {
		ClientDataList factoredDataList = dataFactory
				.factorListUsingNameOfDataType(containDataOfType);

		assertTrue(factoredDataList instanceof BasicClientDataList);
		assertEquals(factoredDataList.getContainDataOfType(), containDataOfType);
	}

	@Test
	public void testFactorRecordUsingDataGroup() {
		ClientDataRecordGroup dataRecordGroup = BasicClientDataRecordGroup
				.withNameInData("someNameInData");
		ClientDataRecord factoredDataRecord = dataFactory
				.factorRecordUsingDataRecordGroup(dataRecordGroup);
		assertTrue(factoredDataRecord instanceof BasicClientDataRecord);
		assertSame(factoredDataRecord.getDataRecordGroup(), dataRecordGroup);
	}

	@Test
	public void testFactorRecordGroupUsingNameInData() {
		ClientDataRecordGroup factoredDataRecordGroup = dataFactory
				.factorRecordGroupUsingNameInData(nameInData);
		assertTrue(factoredDataRecordGroup instanceof BasicClientDataRecordGroup);
		assertEquals(factoredDataRecordGroup.getNameInData(), nameInData);
	}

	@Test
	public void testFactorRecordGroupFromDataGroup() {
		ClientDataGroup dataGroup = BasicClientDataGroup.withNameInData(nameInData);
		dataGroup.addChild(BasicClientDataAtomic.withNameInDataAndValue("atomic", "aValue"));
		dataGroup.addAttributeByIdWithValue("attribute", "atValue");

		ClientDataRecordGroup factoredDataRecordGroup = dataFactory
				.factorRecordGroupFromDataGroup(dataGroup);

		assertTrue(factoredDataRecordGroup instanceof BasicClientDataRecordGroup);
		assertEquals(factoredDataRecordGroup.getNameInData(), nameInData);
		assertSame(factoredDataRecordGroup.getChildren().size(), dataGroup.getChildren().size());
		assertSame(factoredDataRecordGroup.getFirstChildWithNameInData("atomic"),
				dataGroup.getFirstChildWithNameInData("atomic"));
		Collection<ClientDataAttribute> attributes = factoredDataRecordGroup.getAttributes();
		assertEquals(attributes.size(), 1);
		ClientDataAttribute attribute = factoredDataRecordGroup.getAttribute("attribute");
		assertEquals(attribute.getValue(), "atValue");
	}

	@Test
	public void testFactorGroupFromDataRecordGroup() {
		ClientDataRecordGroup dataRecordGroup = BasicClientDataRecordGroup
				.withNameInData(nameInData);
		dataRecordGroup.addChild(BasicClientDataAtomic.withNameInDataAndValue("atomic", "aValue"));
		dataRecordGroup.addAttributeByIdWithValue("attribute", "atValue");

		ClientDataGroup factoredDataGroup = dataFactory
				.factorGroupFromDataRecordGroup(dataRecordGroup);

		assertTrue(factoredDataGroup instanceof BasicClientDataGroup);
		assertEquals(factoredDataGroup.getNameInData(), nameInData);
		assertSame(factoredDataGroup.getChildren().size(), dataRecordGroup.getChildren().size());
		assertSame(factoredDataGroup.getFirstChildWithNameInData("atomic"),
				dataRecordGroup.getFirstChildWithNameInData("atomic"));
		Collection<ClientDataAttribute> attributes = factoredDataGroup.getAttributes();
		assertEquals(attributes.size(), 1);
		ClientDataAttribute attribute = factoredDataGroup.getAttribute("attribute");
		assertEquals(attribute.getValue(), "atValue");
	}

	@Test
	public void testFactorGroupUsingNameInData() {
		ClientDataGroup factoredDataGroup = dataFactory.factorGroupUsingNameInData(nameInData);
		assertTrue(factoredDataGroup instanceof BasicClientDataGroup);
		assertEquals(factoredDataGroup.getNameInData(), nameInData);
	}

	@Test
	public void testFactorRecordLinkUsingNameInData() {
		ClientDataRecordLink factoredDataRecordLink = dataFactory
				.factorRecordLinkUsingNameInData(nameInData);

		assertTrue(factoredDataRecordLink instanceof BasicClientDataRecordLink);
		assertEquals(factoredDataRecordLink.getNameInData(), nameInData);
	}

	@Test
	public void testFactorRecordLinkUsingNameInDataAndTypeAndId() {
		ClientDataRecordLink factoredDataRecordLink = dataFactory
				.factorRecordLinkUsingNameInDataAndTypeAndId(nameInData, recordType, recordId);
		assertEquals(factoredDataRecordLink.getNameInData(), nameInData);
		assertEquals(factoredDataRecordLink.getLinkedRecordType(), recordType);
		assertEquals(factoredDataRecordLink.getLinkedRecordId(), recordId);

		// assertEquals(factoredDataRecordLink.getChildren().size(), 2);
		// assertEquals(factoredDataRecordLink.getFirstAtomicValueWithNameInData("linkedRecordType"),
		// recordType);
		// assertEquals(factoredDataRecordLink.getFirstAtomicValueWithNameInData("linkedRecordId"),
		// recordId);
	}

	@Test
	public void testFactorResourceLinkUsingNameInData() {
		ClientDataResourceLink factoredDataResourceLink = dataFactory
				.factorResourceLinkUsingNameInData(nameInData);
		assertTrue(factoredDataResourceLink instanceof BasicClientDataResourceLink);
		assertEquals(factoredDataResourceLink.getNameInData(), nameInData);
	}

	@Test
	public void testFactorAtomicUsingNameInDataAndValue() {
		ClientDataAtomic factoredDataAtomic = dataFactory
				.factorAtomicUsingNameInDataAndValue(nameInData, value);
		assertCorrectBasicDataAtomic(factoredDataAtomic);
	}

	private void assertCorrectBasicDataAtomic(ClientDataAtomic factoredDataAtomic) {
		assertTrue(factoredDataAtomic instanceof BasicClientDataAtomic);
		assertEquals(factoredDataAtomic.getNameInData(), nameInData);
		assertEquals(factoredDataAtomic.getValue(), value);
	}

	@Test
	public void testFactorAtomciUsingNameInDataAndValueAndRepeatId() {
		String repeatId = "r1";
		ClientDataAtomic factoredDataAtomic = dataFactory
				.factorAtomicUsingNameInDataAndValueAndRepeatId(nameInData, value, repeatId);
		assertCorrectBasicDataAtomic(factoredDataAtomic);
		assertEquals(factoredDataAtomic.getRepeatId(), repeatId);
	}

	@Test
	public void testFactorAttributeUsingNameInDataAndValue() {
		ClientDataAttribute factoredDataAttribute = dataFactory
				.factorAttributeUsingNameInDataAndValue(nameInData, value);

		assertTrue(factoredDataAttribute instanceof BasicClientDataAttribute);
		assertEquals(factoredDataAttribute.getNameInData(), nameInData);
		assertEquals(factoredDataAttribute.getValue(), value);
	}

	@Test
	public void testFactorDataChildFilterUsingNameInData() {
		BasicClientDataChildFilter childFilter = (BasicClientDataChildFilter) dataFactory
				.factorDataChildFilterUsingNameInData(nameInData);

		assertEquals(childFilter.onlyForTestGetChildNameInData(), nameInData);

	}

}
