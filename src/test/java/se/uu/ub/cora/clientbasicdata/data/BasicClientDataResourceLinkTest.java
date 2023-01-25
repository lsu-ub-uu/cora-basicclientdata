package se.uu.ub.cora.clientbasicdata.data;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataLink;
import se.uu.ub.cora.clientdata.ClientDataMissingException;
import se.uu.ub.cora.clientdata.ClientDataResourceLink;

public class BasicClientDataResourceLinkTest {

	BasicClientDataResourceLink resourceLink;

	@BeforeMethod
	public void setUp() {
		resourceLink = BasicClientDataResourceLink.withNameInData("nameInData");

	}

	@Test
	public void testCorrectType() {
		assertTrue(resourceLink instanceof ClientDataLink);
		assertTrue(resourceLink instanceof ClientDataResourceLink);
	}

	@Test
	public void testInit() {
		BasicClientDataAtomic streamId = BasicClientDataAtomic.withNameInDataAndValue("streamId",
				"myStreamId");
		resourceLink.addChild(streamId);

		assertEquals(resourceLink.getNameInData(), "nameInData");
		assertNotNull(resourceLink.getAttributes());
		assertNotNull(resourceLink.getChildren());
		assertEquals(resourceLink.getFirstAtomicValueWithNameInData("streamId"), "myStreamId");
	}

	@Test
	public void testInitWithRepeatId() {
		resourceLink.setRepeatId("hugh");
		assertEquals(resourceLink.getRepeatId(), "hugh");
	}

	@Test
	public void testFromDataGroup() {
		ClientDataGroup dataGroupResourceLink = createResourceLinkAsDataGroup();

		BasicClientDataResourceLink dataResourceLink = BasicClientDataResourceLink
				.fromDataGroup(dataGroupResourceLink);

		assertCorrectFromDataResourceLink(dataResourceLink);
		assertNull(dataResourceLink.getRepeatId());
	}

	private ClientDataGroup createResourceLinkAsDataGroup() {
		ClientDataGroup dataGroupRecordLink = BasicClientDataGroup.withNameInData("nameInData");

		BasicClientDataAtomic fileName = BasicClientDataAtomic.withNameInDataAndValue("filename",
				"someFileName");
		dataGroupRecordLink.addChild(fileName);

		BasicClientDataAtomic streamId = BasicClientDataAtomic.withNameInDataAndValue("streamId",
				"someStreamId");
		dataGroupRecordLink.addChild(streamId);
		BasicClientDataAtomic filesize = BasicClientDataAtomic.withNameInDataAndValue("filesize",
				"567");
		dataGroupRecordLink.addChild(filesize);
		BasicClientDataAtomic mimeType = BasicClientDataAtomic.withNameInDataAndValue("mimeType",
				"someMimeType");
		dataGroupRecordLink.addChild(mimeType);
		return dataGroupRecordLink;
	}

	private void assertCorrectFromDataResourceLink(BasicClientDataResourceLink resourceLink) {
		assertEquals(resourceLink.getNameInData(), "nameInData");

		BasicClientDataAtomic convertedFileName = (BasicClientDataAtomic) resourceLink
				.getFirstChildWithNameInData("filename");
		assertEquals(convertedFileName.getValue(), "someFileName");

		BasicClientDataAtomic convertedStreamId = (BasicClientDataAtomic) resourceLink
				.getFirstChildWithNameInData("streamId");
		assertEquals(convertedStreamId.getValue(), "someStreamId");

		BasicClientDataAtomic convertedFilesize = (BasicClientDataAtomic) resourceLink
				.getFirstChildWithNameInData("filesize");
		assertEquals(convertedFilesize.getValue(), "567");

		BasicClientDataAtomic convertedMimeType = (BasicClientDataAtomic) resourceLink
				.getFirstChildWithNameInData("mimeType");
		assertEquals(convertedMimeType.getValue(), "someMimeType");
	}

	@Test
	public void testFromDataGroupWithRepeatId() {
		ClientDataGroup dataGroupResourceLink = createResourceLinkAsDataGroup();
		dataGroupResourceLink.setRepeatId("2");

		BasicClientDataResourceLink dataResourceLink = BasicClientDataResourceLink
				.fromDataGroup(dataGroupResourceLink);

		assertCorrectFromDataResourceLink(dataResourceLink);
		assertEquals(dataResourceLink.getRepeatId(), "2");
	}

	@Test
	public void testHasReadActionsNoReadAction() throws Exception {
		assertFalse(resourceLink.hasReadAction());

	}

	@Test
	public void testHasReadActionsReadAction() throws Exception {
		resourceLink.addAction(ClientAction.READ);

		assertTrue(resourceLink.hasReadAction());
	}

	@Test
	public void testGetMimeType() throws Exception {
		ClientDataGroup dataGroupResourceLink = createResourceLinkAsDataGroup();

		BasicClientDataResourceLink dataResourceLink = BasicClientDataResourceLink
				.fromDataGroup(dataGroupResourceLink);

		assertEquals(dataResourceLink.getMimeType(), "someMimeType");
	}

	@Test
	public void testStreamId() throws Exception {
		resourceLink.setStreamId("id");
		assertEquals(resourceLink.getStreamId(), "id");
		assertEquals(resourceLink.getFirstAtomicValueWithNameInData("streamId"), "id");
	}

	@Test(expectedExceptions = ClientDataMissingException.class)
	public void testGetStreamIdDataMissing() throws Exception {
		assertEquals(resourceLink.getStreamId(), "");
	}

	@Test
	public void testFileName() throws Exception {
		resourceLink.setFileName("file");
		assertEquals(resourceLink.getFileName(), "file");
		assertEquals(resourceLink.getFirstAtomicValueWithNameInData("filename"), "file");
	}

	@Test(expectedExceptions = ClientDataMissingException.class)
	public void testGetFileNameDataMissing() throws Exception {
		assertEquals(resourceLink.getFileName(), "");
	}

	@Test
	public void testFileSize() throws Exception {
		resourceLink.setFileSize("987654");
		assertEquals(resourceLink.getFileSize(), "987654");
		assertEquals(resourceLink.getFirstAtomicValueWithNameInData("filesize"), "987654");
	}

	@Test(expectedExceptions = ClientDataMissingException.class)
	public void testGetFileSizeDataMissing() throws Exception {
		assertEquals(resourceLink.getFileSize(), "");
	}

	@Test
	public void testMimeType() throws Exception {
		resourceLink.setMimeType("type");
		assertEquals(resourceLink.getMimeType(), "type");
		assertEquals(resourceLink.getFirstAtomicValueWithNameInData("mimeType"), "type");
	}

	@Test(expectedExceptions = ClientDataMissingException.class)
	public void testGetMimeTypeDataMissing() throws Exception {
		assertEquals(resourceLink.getMimeType(), "");
	}
}
