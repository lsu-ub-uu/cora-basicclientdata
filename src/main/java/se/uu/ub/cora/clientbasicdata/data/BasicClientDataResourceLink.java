/*
 * Copyright 2015, 2016 Uppsala University Library
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
import java.util.Map;
import java.util.Optional;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientDataAttribute;
import se.uu.ub.cora.clientdata.ClientDataResourceLink;

public final class BasicClientDataResourceLink implements ClientDataResourceLink {

	private static final String NOT_YET_IMPLEMENTED = "Not yet implemented.";

	private Map<ClientAction, ClientActionLink> actions = new EnumMap<>(ClientAction.class);
	private String mimeType;
	private String nameInData;
	private String repeatId;

	public static BasicClientDataResourceLink withNameInDataAndMimeType(String nameInData,
			String mimeType) {
		return new BasicClientDataResourceLink(nameInData, mimeType);
	}

	private BasicClientDataResourceLink(String nameInData, String mimeType) {
		this.nameInData = nameInData;
		this.mimeType = mimeType;
	}

	@Override
	public boolean hasReadAction() {
		return getActionLink(ClientAction.READ).isPresent();
	}

	@Override
	public void addActionLink(ClientActionLink actionLink) {
		actions.put(actionLink.getAction(), actionLink);
	}

	@Override
	public Optional<ClientActionLink> getActionLink(ClientAction action) {
		if (actions.containsKey(action)) {
			return Optional.of(actions.get(action));
		}
		return Optional.empty();
	}

	@Override
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;

	}

	@Override
	public String getMimeType() {
		return mimeType;
	}

	@Override
	public void setRepeatId(String repeatId) {
		this.repeatId = repeatId;

	}

	@Override
	public String getRepeatId() {
		return repeatId;
	}

	@Override
	public boolean hasRepeatId() {
		return repeatId != null && !"".equals(repeatId);
	}

	@Override
	public String getNameInData() {
		return nameInData;
	}

	@Override
	public void addAttributeByIdWithValue(String nameInData, String value) {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public boolean hasAttributes() {
		return false;
	}

	@Override
	public ClientDataAttribute getAttribute(String nameInData) {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public Collection<ClientDataAttribute> getAttributes() {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public Optional<String> getAttributeValue(String nameInData) {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

}
