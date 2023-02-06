/*
 * Copyright 2015, 2016, 2019, 2020, 2022 Uppsala University Library
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

import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientDataMissingException;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.clientdata.ClientDataRecordLink;

public final class BasicClientDataRecord implements ClientDataRecord {
	private static final String SEARCH = "search";
	private ClientDataRecordGroup dataRecordGroup;
	private Map<ClientAction, ClientActionLink> actions = new EnumMap<>(ClientAction.class);
	private Set<String> readPermissions = new LinkedHashSet<>();
	private Set<String> writePermissions = new LinkedHashSet<>();

	public static BasicClientDataRecord withDataRecordGroup(ClientDataRecordGroup dataRecordGroup) {
		return new BasicClientDataRecord(dataRecordGroup);
	}

	private BasicClientDataRecord(ClientDataRecordGroup dataRecordGroup) {
		this.dataRecordGroup = dataRecordGroup;
	}

	@Override
	public void setDataRecordGroup(ClientDataRecordGroup dataRecordGroup) {
		this.dataRecordGroup = dataRecordGroup;

	}

	@Override
	public ClientDataRecordGroup getDataRecordGroup() {
		return dataRecordGroup;
	}

	@Override
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
	public Set<String> getReadPermissions() {
		return readPermissions;
	}

	@Override
	public Set<String> getWritePermissions() {
		return writePermissions;
	}

	@Override
	public void addReadPermission(String readPermission) {
		readPermissions.add(readPermission);

	}

	@Override
	public void addWritePermission(String writePermission) {
		writePermissions.add(writePermission);
	}

	@Override
	public void addReadPermissions(Collection<String> readPermissions) {
		this.readPermissions.addAll(readPermissions);
	}

	@Override
	public boolean hasReadPermissions() {
		return !this.readPermissions.isEmpty();
	}

	@Override
	public void addWritePermissions(Collection<String> writePermissions) {
		this.writePermissions.addAll(writePermissions);

	}

	@Override
	public boolean hasWritePermissions() {
		return !this.writePermissions.isEmpty();
	}

	@Override
	public String getType() {
		try {
			return getTypeFromGroup();
		} catch (Exception dmException) {
			throw new ClientDataMissingException("Record type not known");
		}
	}

	private String getTypeFromGroup() {
		return dataRecordGroup.getType();
	}

	@Override
	public String getId() {
		try {
			return getIdFromGroup();
		} catch (Exception dmException) {
			throw new ClientDataMissingException("Record id not known");
		}
	}

	private String getIdFromGroup() {
		return dataRecordGroup.getId();
	}

	@Override
	public String getSearchId() {
		String type = getType();
		if (SEARCH.equals(type)) {
			return getId();
		} else if (isRecordTypeAndHasSearch(type)) {
			return extractSearchId();
		}
		throw new ClientDataMissingException("No searchId exists");
	}

	private boolean isRecordTypeAndHasSearch(String type) {
		return "recordType".equals(type) && dataRecordGroup.containsChildWithNameInData(SEARCH);
	}

	private String extractSearchId() {
		ClientDataRecordLink search = (ClientDataRecordLink) dataRecordGroup
				.getFirstChildWithNameInData(SEARCH);
		return search.getLinkedRecordId();

	}

}
