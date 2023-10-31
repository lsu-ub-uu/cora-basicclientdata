package se.uu.ub.cora.clientbasicdata.data;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientActionLink;
import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientDataLink;
import se.uu.ub.cora.clientdata.ClientDataResourceLink;
import se.uu.ub.cora.clientdata.spies.ClientActionLinkSpy;

public class BasicClientDataResourceLinkTest {

	BasicClientDataResourceLink resourceLink;
	private static final String NOT_YET_IMPLEMENTED = "Not yet implemented.";
	private static final String SOME_NAME_IN_DATA = "someNameInData";
	private static final String SOME_MIME_TYPE = "someMimeType";

	@BeforeMethod
	public void setUp() {
		resourceLink = BasicClientDataResourceLink.withNameInDataAndMimeType(SOME_NAME_IN_DATA,
				SOME_MIME_TYPE);
	}

	@Test
	public void testCorrectType() {
		assertTrue(resourceLink instanceof ClientDataLink);
		assertTrue(resourceLink instanceof ClientConvertible);
		assertTrue(resourceLink instanceof ClientDataResourceLink);
	}

	@Test
	public void testInit() {
		assertEquals(resourceLink.getNameInData(), SOME_NAME_IN_DATA);
		assertEquals(resourceLink.getMimeType(), SOME_MIME_TYPE);
	}

	@Test
	public void testInitWithRepeatId() {
		resourceLink.setRepeatId("hugh");
		assertEquals(resourceLink.getRepeatId(), "hugh");
	}

	@Test
	public void testHasRepeatIdNotSet() throws Exception {
		assertFalse(resourceLink.hasRepeatId());
	}

	@Test
	public void testHasRepeatIdSetToEmpty() throws Exception {
		resourceLink.setRepeatId("");
		assertFalse(resourceLink.hasRepeatId());
	}

	@Test
	public void testHasRepeatIdSet() throws Exception {
		resourceLink.setRepeatId("3");
		assertTrue(resourceLink.hasRepeatId());
	}

	@Test
	public void testHasReadActionsNoReadAction() throws Exception {
		assertFalse(resourceLink.hasReadAction());
	}

	@Test
	public void testHasReadDatasReadData() throws Exception {
		ClientActionLink actionLinkSpy = new ClientActionLinkSpy();

		resourceLink.addActionLink(actionLinkSpy);

		assertTrue(resourceLink.hasReadAction());
	}

	@Test
	public void testGetActionLinkNoActionAdded() throws Exception {
		Optional<ClientActionLink> actionLink = resourceLink.getActionLink(ClientAction.READ);

		assertTrue(actionLink.isEmpty());
	}

	@Test
	public void testGetActionLink() throws Exception {
		ClientActionLink actionLinkSpy = new ClientActionLinkSpy();

		resourceLink.addActionLink(actionLinkSpy);

		Optional<ClientActionLink> actionLink = resourceLink.getActionLink(ClientAction.READ);

		assertSame(actionLink.get(), actionLinkSpy);
	}

	@Test
	public void testMimeType() throws Exception {
		resourceLink.setMimeType("type");
		assertEquals(resourceLink.getMimeType(), "type");
	}

	@Test
	public void testHasAttributes() throws Exception {
		assertFalse(resourceLink.hasAttributes());
	}

	@Test(expectedExceptions = UnsupportedOperationException.class, expectedExceptionsMessageRegExp = NOT_YET_IMPLEMENTED)
	public void testGetAttribute() {
		resourceLink.getAttribute("someAttribute");
	}

	@Test(expectedExceptions = UnsupportedOperationException.class, expectedExceptionsMessageRegExp = NOT_YET_IMPLEMENTED)
	public void testAddAttributeByIdWithValue() {
		resourceLink.addAttributeByIdWithValue("someNameInData", "someValue");
	}

	@Test(expectedExceptions = UnsupportedOperationException.class, expectedExceptionsMessageRegExp = NOT_YET_IMPLEMENTED)
	public void testAttributes() throws Exception {
		resourceLink.getAttributes();
	}

	@Test(expectedExceptions = UnsupportedOperationException.class, expectedExceptionsMessageRegExp = NOT_YET_IMPLEMENTED)
	public void testAttributeValue() throws Exception {
		resourceLink.getAttributeValue("someValue");
	}
}
