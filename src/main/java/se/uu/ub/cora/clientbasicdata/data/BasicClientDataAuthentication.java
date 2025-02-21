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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientDataAuthentication;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecordLink;

public class BasicClientDataAuthentication implements ClientDataAuthentication {

	private Map<ClientAction, ClientActionLink> actions = new EnumMap<>(ClientAction.class);
	private ClientDataGroup dataGroup;

	public static BasicClientDataAuthentication withDataGroup(ClientDataGroup dataGroup) {
		return new BasicClientDataAuthentication(dataGroup);
	}

	public BasicClientDataAuthentication(ClientDataGroup dataGroup) {
		this.dataGroup = dataGroup;
	}

	@Override
	public String getToken() {
		return dataGroup.getFirstAtomicValueWithNameInData("token");
	}

	@Override
	public String getLoginId() {
		return dataGroup.getFirstAtomicValueWithNameInData("loginId");
	}

	@Override
	public String getUserId() {
		return dataGroup.getFirstAtomicValueWithNameInData("userId");
	}

	@Override
	public String getValidUntil() {
		return dataGroup.getFirstAtomicValueWithNameInData("validUntil");
	}

	@Override
	public String getRenewUntil() {
		return dataGroup.getFirstAtomicValueWithNameInData("renewUntil");
	}

	@Override
	public String getFirstName() {
		return dataGroup.getFirstAtomicValueWithNameInData("firstName");
	}

	@Override
	public String getLastName() {
		return dataGroup.getFirstAtomicValueWithNameInData("lastName");
	}

	public void addActionLink(ClientActionLink action) {
		actions.put(action.getAction(), action);
	}

	@Override
	public Optional<ClientActionLink> getActionLink(ClientAction action) {
		if (actions.containsKey(action)) {
			return Optional.of(actions.get(action));
		}
		return Optional.empty();
	}

	@Override
	public List<String> getPermissionUnitIds() {
		if (!dataGroup.containsChildWithNameInData("permissionUnit")) {
			return Collections.emptyList();
		}

		List<ClientDataRecordLink> permissionUnits = dataGroup
				.getChildrenOfTypeAndName(ClientDataRecordLink.class, "permissionUnit");
		List<String> linkIds = new ArrayList<>();
		for (ClientDataRecordLink permissionUnit : permissionUnits) {
			linkIds.add(permissionUnit.getLinkedRecordId());
		}
		return linkIds;
	}
}