/*
 * Copyright 2021 Uppsala University Library
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
package se.uu.ub.cora.clientbasicdata.data.spy;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientDataRecordLink;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class DataRecordLinkOldSpy extends DataGroupOldSpy implements ClientDataRecordLink {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public boolean hasReadAction = false;

	public DataRecordLinkOldSpy(String nameInData) {
		super(nameInData);
	}

	@Override
	public void addAction(ClientAction action) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasReadAction() {
		MCR.addCall();
		MCR.addReturned(hasReadAction);
		return hasReadAction;
	}

	@Override
	public String getLinkedRecordId() {
		MCR.addCall();
		String linkedRecordId = "someRecordId";
		MCR.addReturned(linkedRecordId);
		return linkedRecordId;
	}

	@Override
	public String getLinkedRecordType() {
		MCR.addCall();
		String linkedRecordType = "someRecordType";
		MCR.addReturned(linkedRecordType);
		return linkedRecordType;
	}

}
