/*
 * Copyright 2015, 2018, 2023 Uppsala University Library
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

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientData;
import se.uu.ub.cora.clientdata.ClientDataGroup;

public final class BasicClientActionLink implements ClientData, ClientActionLink {

	private ClientDataGroup body;
	private ClientAction action;
	private String url;
	private String requestMethod;
	private String accept;
	private String contentType;

	private BasicClientActionLink(ClientAction action) {
		this.action = action;
	}

	public static BasicClientActionLink withAction(ClientAction action) {
		return new BasicClientActionLink(action);
	}

	@Override
	public ClientAction getAction() {
		return action;
	}

	@Override
	public void setURL(String url) {
		this.url = url;
	}

	@Override
	public String getURL() {
		return url;
	}

	@Override
	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	@Override
	public String getRequestMethod() {
		return requestMethod;
	}

	@Override
	public void setAccept(String accept) {
		this.accept = accept;
	}

	@Override
	public String getAccept() {
		return accept;
	}

	@Override
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public void setBody(ClientDataGroup body) {
		this.body = body;
	}

	@Override
	public ClientDataGroup getBody() {
		return body;
	}
}
