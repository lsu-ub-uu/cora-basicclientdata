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
import se.uu.ub.cora.clientdata.ClientDataChildFilter;
import se.uu.ub.cora.clientdata.ClientDataFactory;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataList;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.clientdata.ClientDataRecordLink;
import se.uu.ub.cora.clientdata.ClientDataResourceLink;

public class BasicClientDataFactory implements ClientDataFactory {

	@Override
	public ClientDataList factorListUsingNameOfDataType(String nameOfDataType) {
		return BasicClientDataList.withContainDataOfType(nameOfDataType);
	}

	@Override
	public ClientDataRecord factorRecordUsingDataRecordGroup(ClientDataRecordGroup dataGroup) {
		return BasicClientDataRecord.withDataRecordGroup(dataGroup);
	}

	@Override
	public ClientDataRecordGroup factorRecordGroupUsingNameInData(String nameInData) {
		return BasicClientDataRecordGroup.withNameInData(nameInData);
	}

	@Override
	public ClientDataRecordGroup factorRecordGroupFromDataGroup(ClientDataGroup dataGroup) {
		BasicClientDataRecordGroup recordGroup = BasicClientDataRecordGroup
				.withNameInData(dataGroup.getNameInData());
		recordGroup.addChildren(dataGroup.getChildren());
		for (ClientDataAttribute attribute : dataGroup.getAttributes()) {
			recordGroup.addAttributeByIdWithValue(attribute.getNameInData(), attribute.getValue());
		}
		return recordGroup;
	}

	@Override
	public ClientDataGroup factorGroupFromDataRecordGroup(ClientDataRecordGroup dataRecordGroup) {
		BasicClientDataGroup group = BasicClientDataGroup
				.withNameInData(dataRecordGroup.getNameInData());
		group.addChildren(dataRecordGroup.getChildren());
		for (ClientDataAttribute attribute : dataRecordGroup.getAttributes()) {
			group.addAttributeByIdWithValue(attribute.getNameInData(), attribute.getValue());
		}
		return group;
	}

	@Override
	public ClientDataGroup factorGroupUsingNameInData(String nameInData) {
		return BasicClientDataGroup.withNameInData(nameInData);
	}

	@Override
	public ClientDataRecordLink factorRecordLinkUsingNameInData(String nameInData) {
		return BasicClientDataRecordLink.withNameInData(nameInData);
	}

	@Override
	public ClientDataRecordLink factorRecordLinkUsingNameInDataAndTypeAndId(String nameInData,
			String recordType, String recordId) {
		return BasicClientDataRecordLink.usingNameInDataAndTypeAndId(nameInData, recordType,
				recordId);
	}

	@Override
	public ClientDataResourceLink factorResourceLinkUsingNameInData(String nameInData) {
		return BasicClientDataResourceLink.withNameInData(nameInData);
	}

	@Override
	public ClientDataAtomic factorAtomicUsingNameInDataAndValue(String nameInData, String value) {
		return BasicClientDataAtomic.withNameInDataAndValue(nameInData, value);
	}

	@Override
	public ClientDataAtomic factorAtomicUsingNameInDataAndValueAndRepeatId(String nameInData,
			String value, String repeatId) {
		return BasicClientDataAtomic.withNameInDataAndValueAndRepeatId(nameInData, value, repeatId);
	}

	@Override
	public ClientDataAttribute factorAttributeUsingNameInDataAndValue(String nameInData,
			String value) {
		return BasicClientDataAttribute.withNameInDataAndValue(nameInData, value);
	}

	@Override
	public ClientDataChildFilter factorDataChildFilterUsingNameInData(String childNameInData) {
		return BasicClientDataChildFilter.usingNameInData(childNameInData);
	}

}
