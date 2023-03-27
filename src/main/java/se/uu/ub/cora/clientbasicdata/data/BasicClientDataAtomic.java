/*
 * Copyright 2015, 2022 Uppsala University Library
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import se.uu.ub.cora.clientdata.ClientDataAtomic;
import se.uu.ub.cora.clientdata.ClientDataAttribute;
import se.uu.ub.cora.clientdata.ClientDataMissingException;

public final class BasicClientDataAtomic implements ClientDataAtomic {

	private String nameInData;
	private String value;
	private String repeatId;
	private Set<ClientDataAttribute> attributes = new HashSet<>();

	public static BasicClientDataAtomic withNameInDataAndValue(String nameInData, String value) {
		return new BasicClientDataAtomic(nameInData, value);
	}

	public static BasicClientDataAtomic withNameInDataAndValueAndRepeatId(String nameInData,
			String value, String repeatId) {
		return new BasicClientDataAtomic(nameInData, value, repeatId);
	}

	private BasicClientDataAtomic(String nameInData, String value) {
		this.nameInData = nameInData;
		this.value = value;
	}

	public BasicClientDataAtomic(String nameInData, String value, String repeatId) {
		this.nameInData = nameInData;
		this.value = value;
		this.repeatId = repeatId;
	}

	@Override
	public String getNameInData() {
		return nameInData;
	}

	@Override
	public void addAttributeByIdWithValue(String nameInData, String value) {
		possiblyRemovePreviouslyStoredAttribute(nameInData);
		attributes.add(BasicClientDataAttribute.withNameInDataAndValue(nameInData, value));
	}

	private void possiblyRemovePreviouslyStoredAttribute(String nameInData) {
		Iterator<ClientDataAttribute> iterator = attributes.iterator();
		while (iterator.hasNext()) {
			possiblyRemoveAttribute(iterator, nameInData);
		}
	}

	private void possiblyRemoveAttribute(Iterator<ClientDataAttribute> iterator,
			String nameInData) {
		ClientDataAttribute next = iterator.next();
		if (next.getNameInData().equals(nameInData)) {
			iterator.remove();
		}
	}

	@Override
	public boolean hasAttributes() {
		return !attributes.isEmpty();
	}

	@Override
	public Collection<ClientDataAttribute> getAttributes() {
		return attributes;
	}

	@Override
	public ClientDataAttribute getAttribute(String attributeId) {
		for (ClientDataAttribute dataAttribute : attributes) {
			if (dataAttribute.getNameInData().equals(attributeId)) {
				return dataAttribute;
			}
		}
		throw new ClientDataMissingException("Attribute with id " + attributeId + " not found.");
	}

	@Override
	public String getValue() {
		return value;
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
	public Optional<String> getAttributeValue(String nameInData) {
		for (ClientDataAttribute dataAttribute : attributes) {
			if (dataAttribute.getNameInData().equals(nameInData)) {
				return Optional.of(dataAttribute.getValue());
			}
		}
		return Optional.empty();
	}

}
