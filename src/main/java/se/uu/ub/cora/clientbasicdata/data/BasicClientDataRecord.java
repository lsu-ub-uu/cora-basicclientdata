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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataMissingException;
import se.uu.ub.cora.clientdata.ClientDataRecord;

public final class BasicClientDataRecord implements ClientDataRecord {
	private static final String SEARCH = "search";
	private ClientDataGroup dataGroup;
	private List<ClientAction> actions = new ArrayList<>();
	private Set<String> readPermissions = new LinkedHashSet<>();
	private Set<String> writePermissions = new LinkedHashSet<>();

	public static BasicClientDataRecord withDataGroup(ClientDataGroup dataGroup) {
		return new BasicClientDataRecord(dataGroup);
	}

	private BasicClientDataRecord(ClientDataGroup dataGroup) {
		this.dataGroup = dataGroup;
	}

	@Override
	public void setDataGroup(ClientDataGroup dataGroup) {
		this.dataGroup = dataGroup;

	}

	@Override
	public ClientDataGroup getDataGroup() {
		return dataGroup;
	}

	@Override
	public void addAction(ClientAction action) {
		actions.add(action);
	}

	@Override
	public List<ClientAction> getActions() {
		return actions;
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
		ClientDataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		ClientDataGroup linkedTypeGroup = recordInfo.getFirstGroupWithNameInData("type");
		return linkedTypeGroup.getFirstAtomicValueWithNameInData("linkedRecordId");
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
		ClientDataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		return recordInfo.getFirstAtomicValueWithNameInData("id");
	}

	@Override
	public boolean hasActions() {
		return !actions.isEmpty();
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
		return "recordType".equals(type) && dataGroup.containsChildWithNameInData(SEARCH);
	}

	private String extractSearchId() {
		ClientDataGroup search = dataGroup.getFirstGroupWithNameInData(SEARCH);
		return search.getFirstAtomicValueWithNameInData("linkedRecordId");
	}

}
