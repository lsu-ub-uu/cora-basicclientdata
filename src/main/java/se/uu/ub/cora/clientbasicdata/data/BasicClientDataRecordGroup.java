/*
 * Copyright 2022 Olov McKie
 * Copyright 2022 Uppsala University Library
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

import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.clientdata.ClientDataRecordLink;

public class BasicClientDataRecordGroup extends BasicClientDataGroup
		implements ClientDataRecordGroup {

	private static final String ID = "id";
	private static final String DATA_DIVIDER = "dataDivider";
	private static final String TYPE = "type";
	private static final String RECORD_INFO = "recordInfo";

	public static BasicClientDataRecordGroup withNameInData(String nameInData) {
		return new BasicClientDataRecordGroup(nameInData);
	}

	protected BasicClientDataRecordGroup(String nameInData) {
		super(nameInData);
	}

	@Override
	public String getType() {
		ClientDataGroup recordInfo = this.getFirstGroupWithNameInData(RECORD_INFO);
		ClientDataRecordLink typeLink = (ClientDataRecordLink) recordInfo
				.getFirstChildWithNameInData(TYPE);
		return typeLink.getLinkedRecordId();
	}

	@Override
	public void setType(String type) {
		ensureRecordInfoExists();
		ClientDataGroup recordInfo = this.getFirstGroupWithNameInData(RECORD_INFO);
		recordInfo
				.removeAllChildrenMatchingFilter(BasicClientDataChildFilter.usingNameInData(TYPE));
		recordInfo.addChild(
				BasicClientDataRecordLink.usingNameInDataAndTypeAndId(TYPE, "recordType", type));
	}

	private void ensureRecordInfoExists() {
		if (!this.containsChildWithNameInData(RECORD_INFO)) {
			this.addChild(BasicClientDataGroup.withNameInData(RECORD_INFO));
		}
	}

	@Override
	public String getId() {
		ClientDataGroup recordInfo = this.getFirstGroupWithNameInData(RECORD_INFO);
		return recordInfo.getFirstAtomicValueWithNameInData(ID);
	}

	@Override
	public void setId(String id) {
		ensureRecordInfoExists();
		ClientDataGroup recordInfo = this.getFirstGroupWithNameInData(RECORD_INFO);
		recordInfo.removeAllChildrenMatchingFilter(BasicClientDataChildFilter.usingNameInData(ID));
		recordInfo.addChild(BasicClientDataAtomic.withNameInDataAndValue(ID, id));
	}

	@Override
	public String getDataDivider() {
		ClientDataGroup recordInfo = this.getFirstGroupWithNameInData(RECORD_INFO);
		ClientDataRecordLink typeLink = (ClientDataRecordLink) recordInfo
				.getFirstChildWithNameInData(DATA_DIVIDER);
		return typeLink.getLinkedRecordId();
	}

	@Override
	public void setDataDivider(String dataDivider) {
		ensureRecordInfoExists();
		ClientDataGroup recordInfo = this.getFirstGroupWithNameInData(RECORD_INFO);
		recordInfo.removeAllChildrenMatchingFilter(
				BasicClientDataChildFilter.usingNameInData(DATA_DIVIDER));
		recordInfo.addChild(BasicClientDataRecordLink.usingNameInDataAndTypeAndId(DATA_DIVIDER,
				"system", dataDivider));
	}

}
