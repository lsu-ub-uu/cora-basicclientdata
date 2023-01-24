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

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientDataChild;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecordLink;

public final class BasicClientDataRecordLink extends BasicClientDataGroup
		implements ClientDataRecordLink {

	private static final String LINKED_RECORD_ID = "linkedRecordId";
	private static final String LINKED_RECORD_TYPE = "linkedRecordType";
	private List<ClientAction> actions = new ArrayList<>();

	private BasicClientDataRecordLink(String nameInData) {
		super(nameInData);
	}

	public static BasicClientDataRecordLink fromDataGroup(ClientDataGroup dataGroup) {
		return new BasicClientDataRecordLink(dataGroup);
	}

	private BasicClientDataRecordLink(ClientDataGroup dataGroup) {
		super(dataGroup.getNameInData());
		addLinkedRecordTypeAndId(dataGroup);
		setRepeatId(dataGroup.getRepeatId());
	}

	private void addLinkedRecordTypeAndId(ClientDataGroup dataGroup) {
		ClientDataChild linkedRecordType = dataGroup
				.getFirstChildWithNameInData(LINKED_RECORD_TYPE);
		addChild(linkedRecordType);
		ClientDataChild linkedRecordId = dataGroup.getFirstChildWithNameInData(LINKED_RECORD_ID);
		addChild(linkedRecordId);
	}

	public static BasicClientDataRecordLink withNameInData(String nameInData) {
		return new BasicClientDataRecordLink(nameInData);
	}

	public static BasicClientDataRecordLink usingNameInDataAndTypeAndId(String nameInData,
			String type, String id) {
		BasicClientDataRecordLink dataRecordLink = new BasicClientDataRecordLink(nameInData);
		dataRecordLink
				.addChild(BasicClientDataAtomic.withNameInDataAndValue(LINKED_RECORD_TYPE, type));
		dataRecordLink.addChild(BasicClientDataAtomic.withNameInDataAndValue(LINKED_RECORD_ID, id));
		return dataRecordLink;
	}

	@Override
	public void addAction(ClientAction action) {
		actions.add(action);
	}

	@Override
	public boolean hasReadAction() {
		return actions.contains(ClientAction.READ);
	}

	@Override
	public String getLinkedRecordType() {
		return super.getFirstAtomicValueWithNameInData(LINKED_RECORD_TYPE);
	}

	@Override
	public String getLinkedRecordId() {
		return super.getFirstAtomicValueWithNameInData(LINKED_RECORD_ID);
	}

}
