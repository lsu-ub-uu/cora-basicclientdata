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

import java.util.Collection;
import java.util.Optional;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientDataAttribute;
import se.uu.ub.cora.clientdata.ClientDataResourceLink;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class BasicClientDataResourceLinkSpy implements ClientDataResourceLink {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public BasicClientDataResourceLinkSpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("getNameInData", () -> "fakeDataResourceNameInData");
		MRV.setDefaultReturnValuesSupplier("hasReadAction", () -> false);
		MRV.setDefaultReturnValuesSupplier("hasRepeatId", () -> false);
		MRV.setDefaultReturnValuesSupplier("getMimeType", () -> "spyMimeType");
		MRV.setDefaultReturnValuesSupplier("getActionLink", () -> Optional.empty());
		MRV.setDefaultReturnValuesSupplier("getRepeatId", () -> "spyRepeatId");
	}
	// public MethodCallRecorder MCR = new MethodCallRecorder();
	//
	// public String nameInData;
	// public List<ClientAction> actions = new ArrayList<>();
	// public boolean hasReadAction = false;
	//
	// public BasicClientDataResourceLinkSpy(String nameInData) {
	// super(nameInData);
	// }

	@Override
	public String getNameInData() {
		return (String) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public boolean hasReadAction() {
		return (boolean) MCR.addCallAndReturnFromMRV();

		// MCR.addCall();
		// MCR.addReturned(hasReadAction);
		// return hasReadAction;
	}

	@Override
	public String getMimeType() {
		return (String) MCR.addCallAndReturnFromMRV();
		// MCR.addCall();
		// MCR.addReturned(mimeType);
		// return mimeType;
	}

	@Override
	public void setMimeType(String mimeType) {
		MCR.addCall(mimeType);
	}

	@Override
	public void addActionLink(ClientActionLink actionLink) {
		MCR.addCall(actionLink);

	}

	@Override
	public Optional<ClientActionLink> getActionLink(ClientAction action) {
		return (Optional<ClientActionLink>) MCR.addCallAndReturnFromMRV();
		// return Optional.empty();
	}

	@Override
	public boolean hasRepeatId() {
		return (boolean) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public void setRepeatId(String repeatId) {
		MCR.addCall(repeatId);
	}

	@Override
	public String getRepeatId() {
		return (String) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public void addAttributeByIdWithValue(String nameInData, String value) {
	}

	@Override
	public boolean hasAttributes() {
		return false;
	}

	@Override
	public ClientDataAttribute getAttribute(String nameInData) {
		return null;
	}

	@Override
	public Collection<ClientDataAttribute> getAttributes() {
		return null;
	}

	@Override
	public Optional<String> getAttributeValue(String nameInData) {
		return Optional.empty();
	}
}
