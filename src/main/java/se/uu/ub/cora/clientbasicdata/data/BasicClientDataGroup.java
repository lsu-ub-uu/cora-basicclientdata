/*
 * Copyright 2015, 2019, 2020, 2022 Uppsala University Library
 * Copyright 2016 Olov McKie
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import se.uu.ub.cora.clientdata.ClientDataAtomic;
import se.uu.ub.cora.clientdata.ClientDataAttribute;
import se.uu.ub.cora.clientdata.ClientDataChild;
import se.uu.ub.cora.clientdata.ClientDataChildFilter;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataMissingException;

public class BasicClientDataGroup implements ClientDataGroup {

	private String nameInData;
	private Set<ClientDataAttribute> attributes = new HashSet<>();
	private List<ClientDataChild> children = new ArrayList<>();
	private String repeatId;
	private Predicate<ClientDataChild> isDataAtomic = BasicClientDataAtomic.class::isInstance;
	private Predicate<ClientDataChild> isDataGroup = BasicClientDataGroup.class::isInstance;

	public static BasicClientDataGroup withNameInData(String nameInData) {
		return new BasicClientDataGroup(nameInData);
	}

	protected BasicClientDataGroup(String nameInData) {
		this.nameInData = nameInData;
	}

	@Override
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	@Override
	public boolean containsChildWithNameInData(String nameInData) {
		return children.stream().anyMatch(filterByNameInData(nameInData));
	}

	private Predicate<ClientDataChild> filterByNameInData(String childNameInData) {
		return dataElement -> dataElementsNameInDataIs(dataElement, childNameInData);
	}

	private boolean dataElementsNameInDataIs(ClientDataChild dataElement, String childNameInData) {
		return dataElement.getNameInData().equals(childNameInData);
	}

	@Override
	public String getFirstAtomicValueWithNameInData(String childNameInData) {
		Optional<BasicClientDataAtomic> optionalFirst = getAtomicChildrenWithNameInData(
				childNameInData).findFirst();
		return possiblyReturnAtomicChildWithNameInData(childNameInData, optionalFirst);
	}

	private String possiblyReturnAtomicChildWithNameInData(String childNameInData,
			Optional<BasicClientDataAtomic> optionalFirst) {
		if (optionalFirst.isPresent()) {
			return optionalFirst.get().getValue();
		}
		throw new ClientDataMissingException(
				"Atomic value not found for childNameInData:" + childNameInData);
	}

	private Stream<BasicClientDataAtomic> getAtomicChildrenWithNameInData(String childNameInData) {
		return getAtomicChildrenStream().filter(filterByNameInData(childNameInData))
				.map(BasicClientDataAtomic.class::cast);
	}

	private Stream<ClientDataChild> getAtomicChildrenStream() {
		return children.stream().filter(isDataAtomic);
	}

	@Override
	public List<ClientDataAtomic> getAllDataAtomicsWithNameInData(String childNameInData) {
		return getDataAtomicChildrenWithNameInData(childNameInData).toList();
	}

	private Stream<ClientDataAtomic> getDataAtomicChildrenWithNameInData(String childNameInData) {
		return getAtomicChildrenStream().filter(filterByNameInData(childNameInData))
				.map(BasicClientDataAtomic.class::cast);
	}

	@Override
	public ClientDataGroup getFirstGroupWithNameInData(String childNameInData) {
		Optional<ClientDataGroup> findFirst = getGroupChildrenWithNameInDataStream(childNameInData)
				.findFirst();
		if (findFirst.isPresent()) {
			return findFirst.get();
		}
		throw new ClientDataMissingException(
				"Group not found for childNameInData:" + childNameInData);
	}

	private Stream<ClientDataGroup> getGroupChildrenWithNameInDataStream(String childNameInData) {
		return getGroupChildrenStream().filter(filterByNameInData(childNameInData))
				.map(BasicClientDataGroup.class::cast);
	}

	private Stream<ClientDataChild> getGroupChildrenStream() {
		return children.stream().filter(isDataGroup);
	}

	@Override
	public ClientDataAtomic getFirstDataAtomicWithNameInData(String childNameInData) {
		Optional<ClientDataAtomic> findFirst = getDataAtomicChildrenWithNameInData(childNameInData)
				.findFirst();

		if (findFirst.isPresent()) {
			return findFirst.get();
		}
		throw new ClientDataMissingException(
				"ClientDataAtomic not found for childNameInData:" + childNameInData);
	}

	@Override
	public ClientDataChild getFirstChildWithNameInData(String childNameInData) {
		Optional<ClientDataChild> optionalFirst = possiblyFindFirstChildWithNameInData(
				childNameInData);
		if (optionalFirst.isPresent()) {
			return optionalFirst.get();
		}
		throw new ClientDataMissingException(
				"Element not found for childNameInData:" + childNameInData);
	}

	private Optional<ClientDataChild> possiblyFindFirstChildWithNameInData(String childNameInData) {
		return children.stream().filter(filterByNameInData(childNameInData)).findFirst();
	}

	@Override
	public List<ClientDataGroup> getAllGroupsWithNameInData(String childNameInData) {
		return getGroupChildrenWithNameInDataStream(childNameInData).toList();
	}

	@Override
	public boolean hasAttributes() {
		return !attributes.isEmpty();
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
	public boolean removeFirstChildWithNameInData(String childNameInData) {
		return tryToRemoveChild(childNameInData);
	}

	private boolean tryToRemoveChild(String childNameInData) {
		for (ClientDataChild dataElement : children) {
			if (dataElementsNameInDataIs(dataElement, childNameInData)) {
				children.remove(dataElement);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean removeAllChildrenWithNameInData(String childNameInData) {
		return getChildren().removeIf(filterByNameInData(childNameInData));
	}

	@Override
	public String getNameInData() {
		return nameInData;
	}

	@Override
	public Collection<ClientDataAttribute> getAttributes() {
		return attributes;
	}

	@Override
	public List<ClientDataChild> getChildren() {
		return children;
	}

	@Override
	public void addChild(ClientDataChild dataElement) {
		children.add(dataElement);
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
	public Collection<ClientDataGroup> getAllGroupsWithNameInDataAndAttributes(
			String childNameInData, ClientDataAttribute... childAttributes) {
		return getGroupChildrenWithNameInDataAndAttributes(childNameInData, childAttributes)
				.toList();

	}

	private Stream<ClientDataGroup> getGroupChildrenWithNameInDataAndAttributes(
			String childNameInData, ClientDataAttribute... childAttributes) {
		return getGroupChildrenWithNameInDataStream(childNameInData)
				.filter(filterByAttributes(childAttributes));
	}

	private Predicate<ClientDataChild> filterByAttributes(ClientDataAttribute... childAttributes) {
		return dataElement -> dataElementsHasAttributes(dataElement, childAttributes);
	}

	private boolean dataElementsHasAttributes(ClientDataChild dataElement,
			ClientDataAttribute[] childAttributes) {
		Collection<ClientDataAttribute> attributesFromElement = dataElement.getAttributes();
		if (differentNumberOfAttributesInRequestedAndExisting(childAttributes,
				attributesFromElement)) {
			return false;
		}
		return allRequestedAttributesMatchExistingAttributes(childAttributes,
				attributesFromElement);
	}

	private boolean differentNumberOfAttributesInRequestedAndExisting(
			ClientDataAttribute[] childAttributes,
			Collection<ClientDataAttribute> attributesFromElement) {
		return childAttributes.length != attributesFromElement.size();
	}

	private boolean allRequestedAttributesMatchExistingAttributes(
			ClientDataAttribute[] childAttributes,
			Collection<ClientDataAttribute> attributesFromElement) {
		for (ClientDataAttribute dataAttribute : childAttributes) {
			if (attributesDoesNotMatch(attributesFromElement, dataAttribute)) {
				return false;
			}
		}
		return true;
	}

	private boolean attributesDoesNotMatch(Collection<ClientDataAttribute> attributesFromElement,
			ClientDataAttribute dataAttribute) {
		return requestedAttributeDoesNotExists(attributesFromElement, dataAttribute);
	}

	private boolean requestedAttributeDoesNotExists(
			Collection<ClientDataAttribute> attributesFromElement,
			ClientDataAttribute requestedDataAttribute) {
		for (ClientDataAttribute dataAttribute : attributesFromElement) {
			if (sameAttributeNameInData(requestedDataAttribute, dataAttribute)
					&& sameAttributeValue(requestedDataAttribute, dataAttribute)) {
				return false;
			}
		}
		return true;
	}

	private boolean sameAttributeValue(ClientDataAttribute requestedDataAttribute,
			ClientDataAttribute dataAttribute) {
		return dataAttribute.getValue().equals(requestedDataAttribute.getValue());
	}

	private boolean sameAttributeNameInData(ClientDataAttribute requestedDataAttribute,
			ClientDataAttribute dataAttribute) {
		return dataAttribute.getNameInData().equals(requestedDataAttribute.getNameInData());
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
	public void addChildren(Collection<ClientDataChild> dataElements) {
		children.addAll(dataElements);
	}

	@Override
	public List<ClientDataChild> getAllChildrenWithNameInData(String childNameInData) {
		return getChildrenWithNameInData(childNameInData).toList();

	}

	private Stream<ClientDataChild> getChildrenWithNameInData(String childNameInData) {
		return children.stream().filter(filterByNameInData(childNameInData))
				.map(ClientDataChild.class::cast);
	}

	@Override
	public boolean removeAllChildrenWithNameInDataAndAttributes(String childNameInData,
			ClientDataAttribute... childAttributes) {

		Predicate<? super ClientDataChild> childNameInDataMatches = element -> dataElementsNameInDataAndAttributesMatch(
				element, childNameInData, childAttributes);
		return removeMatchingChildren(childNameInDataMatches);

	}

	private boolean dataElementsNameInDataAndAttributesMatch(ClientDataChild element,
			String childNameInData, ClientDataAttribute... childAttributes) {
		return dataElementsNameInDataIs(element, childNameInData)
				&& dataElementsHasAttributes(element, childAttributes);
	}

	@Override
	public List<ClientDataChild> getAllChildrenWithNameInDataAndAttributes(String childNameInData,
			ClientDataAttribute... childAttributes) {
		Predicate<? super ClientDataChild> childNameInDataMatches = element -> dataElementsNameInDataAndAttributesMatch(
				element, childNameInData, childAttributes);
		return filterChildren(childNameInDataMatches);
	}

	@Override
	public Collection<ClientDataAtomic> getAllDataAtomicsWithNameInDataAndAttributes(
			String childNameInData, ClientDataAttribute... childAttributes) {
		return getAtomicChildrenWithNameInDataAndAttributes(childNameInData, childAttributes)
				.toList();
	}

	private Stream<ClientDataAtomic> getAtomicChildrenWithNameInDataAndAttributes(
			String childNameInData, ClientDataAttribute... childAttributes) {
		return getAtomicChildrenWithNameInDataStream(childNameInData)
				.filter(filterByAttributes(childAttributes));
	}

	private Stream<ClientDataAtomic> getAtomicChildrenWithNameInDataStream(String childNameInData) {
		return getAtomicChildrenStream().filter(filterByNameInData(childNameInData))
				.map(BasicClientDataAtomic.class::cast);
	}

	@Override
	public List<ClientDataChild> getAllChildrenMatchingFilter(ClientDataChildFilter childFilter) {
		return filterChildren(childFilter::childMatches);
	}

	private List<ClientDataChild> filterChildren(Predicate<? super ClientDataChild> predicate) {
		return children.stream().filter(predicate).toList();
	}

	@Override
	public boolean removeAllChildrenMatchingFilter(ClientDataChildFilter childFilter) {
		return removeMatchingChildren(childFilter::childMatches);
	}

	private boolean removeMatchingChildren(Predicate<? super ClientDataChild> filter) {
		return children.removeIf(filter);
	}

	@Override
	public <T> boolean containsChildOfTypeAndName(Class<T> type, String name) {
		return children.stream().filter(filterByNameInData(name)).anyMatch(type::isInstance);
	}

	@Override
	public <T extends ClientDataChild> T getFirstChildOfTypeAndName(Class<T> type, String name) {
		Optional<T> optionalFirst = getOptionalFirstChildOfTypeAndName(type, name);
		if (optionalFirst.isPresent()) {
			return optionalFirst.get();
		}
		throw new ClientDataMissingException("Child of type: " + type.getSimpleName()
				+ " and name: " + name + " not found as child.");
	}

	private <T extends ClientDataChild> Optional<T> getOptionalFirstChildOfTypeAndName(
			Class<T> type, String name) {
		return children.stream().filter(filterByNameInData(name)).map(type::cast).findFirst();
	}

	@Override
	public <T extends ClientDataChild> List<T> getChildrenOfTypeAndName(Class<T> type,
			String name) {
		return children.stream().filter(filterByNameInData(name)).map(type::cast).toList();
	}

	@Override
	public <T extends ClientDataChild> boolean removeFirstChildWithTypeAndName(Class<T> type,
			String name) {
		Optional<T> optionalFirst = getOptionalFirstChildOfTypeAndName(type, name);
		if (optionalFirst.isPresent()) {
			return children.remove(optionalFirst.get());
		}
		return false;
	}

	@Override
	public <T extends ClientDataChild> boolean removeChildrenWithTypeAndName(Class<T> type,
			String name) {
		return children.removeAll(getChildrenOfTypeAndName(type, name));
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
