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

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientDataChild;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataResourceLink;

public final class BasicClientDataResourceLink extends BasicClientDataGroup
		implements ClientDataResourceLink {

	private Map<ClientAction, ClientActionLink> actions = new EnumMap<>(ClientAction.class);

	public static BasicClientDataResourceLink withNameInData(String nameInData) {
		return new BasicClientDataResourceLink(nameInData);
	}

	private BasicClientDataResourceLink(String nameInData) {
		super(nameInData);
	}

	public static BasicClientDataResourceLink fromDataGroup(ClientDataGroup dataGroup) {
		return new BasicClientDataResourceLink(dataGroup);
	}

	private BasicClientDataResourceLink(ClientDataGroup dataGroup) {
		super(dataGroup.getNameInData());
		addResourceLinkChildren(dataGroup);
		setRepeatId(dataGroup.getRepeatId());
	}

	private void addResourceLinkChildren(ClientDataGroup dataGroup) {
		ClientDataChild streamId = dataGroup.getFirstChildWithNameInData("streamId");
		addChild(streamId);
		ClientDataChild fileName = dataGroup.getFirstChildWithNameInData("filename");
		addChild(fileName);
		ClientDataChild fileSize = dataGroup.getFirstChildWithNameInData("filesize");
		addChild(fileSize);
		ClientDataChild mimeType = dataGroup.getFirstChildWithNameInData("mimeType");
		addChild(mimeType);
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
	public void setStreamId(String streamId) {
		super.addChild(BasicClientDataAtomic.withNameInDataAndValue("streamId", streamId));
	}

	@Override
	public String getStreamId() {
		return super.getFirstAtomicValueWithNameInData("streamId");
	}

	@Override
	public void setFileName(String fileName) {
		super.addChild(BasicClientDataAtomic.withNameInDataAndValue("filename", fileName));
	}

	@Override
	public String getFileName() {
		return super.getFirstAtomicValueWithNameInData("filename");
	}

	@Override
	public void setFileSize(String fileSize) {
		super.addChild(BasicClientDataAtomic.withNameInDataAndValue("filesize", fileSize));
	}

	@Override
	public String getFileSize() {
		return super.getFirstAtomicValueWithNameInData("filesize");
	}

	@Override
	public void setMimeType(String mimeType) {
		super.addChild(BasicClientDataAtomic.withNameInDataAndValue("mimeType", mimeType));
	}

	@Override
	public String getMimeType() {
		return super.getFirstAtomicValueWithNameInData("mimeType");
	}
}
