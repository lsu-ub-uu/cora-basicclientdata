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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.spies.ClientDataGroupSpy;

public class BasicClientActionLinkTest {
	private ClientActionLink actionLink;

	@BeforeMethod
	private void beforeMethod() {
		actionLink = BasicClientActionLink.withAction(ClientAction.READ);
	}

	@Test
	public void testInit() {
		assertEquals(actionLink.getAction(), ClientAction.READ);
	}

	@Test
	public void testURL() {
		actionLink.setURL("http://test.org/test/test:001");
		assertEquals(actionLink.getURL(), "http://test.org/test/test:001");
	}

	@Test
	public void testRequestMethod() {
		actionLink.setRequestMethod("GET");
		assertEquals(actionLink.getRequestMethod(), "GET");
	}

	@Test
	public void testAccept() {
		actionLink.setAccept("application/metadata_record+json");
		assertEquals(actionLink.getAccept(), "application/metadata_record+json");
	}

	@Test
	public void testContentType() {
		actionLink.setContentType("application/metadata_record+json");
		assertEquals(actionLink.getContentType(), "application/metadata_record+json");
	}

	@Test
	public void testBody() {
		ClientDataGroup workOrder = new ClientDataGroupSpy();
		actionLink.setBody(workOrder);
		assertSame(actionLink.getBody(), workOrder);
	}

}
