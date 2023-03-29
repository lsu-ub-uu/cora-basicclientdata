package se.uu.ub.cora.clientbasicdata.data.spy;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import se.uu.ub.cora.clientdata.ClientDataAtomic;
import se.uu.ub.cora.clientdata.ClientDataAttribute;
import se.uu.ub.cora.clientdata.ClientDataChild;
import se.uu.ub.cora.clientdata.ClientDataChildFilter;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataMissingException;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class BasicClientDataGroupOldSpy implements ClientDataGroup {

	public MethodCallRecorder MCR = new MethodCallRecorder();
	public boolean throwException = false;
	BasicClientDataGroupOldSpy childDataGroupToReturn = null;
	public boolean searchGroupDefined = false;

	public BasicClientDataGroupOldSpy(String nameInData) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setRepeatId(String repeatId) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getRepeatId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNameInData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsChildWithNameInData(String nameInData) {
		MCR.addCall("nameInData", nameInData);
		if ("search".equals(nameInData)) {
			MCR.addReturned(searchGroupDefined);
			return searchGroupDefined;
		}
		MCR.addReturned(true);
		return true;
	}

	@Override
	public void addChild(ClientDataChild dataElement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addChildren(Collection<ClientDataChild> dataElements) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ClientDataChild> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ClientDataChild> getAllChildrenWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ClientDataChild> getAllChildrenWithNameInDataAndAttributes(String nameInData,
			ClientDataAttribute... childAttributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientDataChild getFirstChildWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFirstAtomicValueWithNameInData(String nameInData) {
		MCR.addCall("nameInData", nameInData);
		if (throwException) {
			throw new ClientDataMissingException("DME from Spy");
		}
		String atomicValue = "fakeFromSpy_" + nameInData;
		MCR.addReturned(atomicValue);
		return atomicValue;
	}

	@Override
	public List<ClientDataAtomic> getAllDataAtomicsWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientDataGroup getFirstGroupWithNameInData(String nameInData) {
		MCR.addCall("nameInData", nameInData);
		if (throwException) {
			throw new ClientDataMissingException("DME from Spy");
		}
		if (null == childDataGroupToReturn) {
			childDataGroupToReturn = new BasicClientDataGroupOldSpy("child to return");
		}
		MCR.addReturned(childDataGroupToReturn);
		return childDataGroupToReturn;
	}

	@Override
	public List<ClientDataGroup> getAllGroupsWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ClientDataGroup> getAllGroupsWithNameInDataAndAttributes(String nameInData,
			ClientDataAttribute... childAttributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeFirstChildWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAllChildrenWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAllChildrenWithNameInDataAndAttributes(String nameInData,
			ClientDataAttribute... childAttributes) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ClientDataAtomic getFirstDataAtomicWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setChildDataGroupToReturn(BasicClientDataGroupOldSpy childDataGroupToReturn) {
		this.childDataGroupToReturn = childDataGroupToReturn;
	}

	public void setChildToThrowException() {
		childDataGroupToReturn = new BasicClientDataGroupOldSpy("child to return");
		childDataGroupToReturn.throwException = true;
	}

	@Override
	public void addAttributeByIdWithValue(String nameInData, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasAttributes() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ClientDataAttribute getAttribute(String nameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ClientDataAttribute> getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ClientDataAtomic> getAllDataAtomicsWithNameInDataAndAttributes(
			String childNameInData, ClientDataAttribute... childAttributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ClientDataChild> getAllChildrenMatchingFilter(ClientDataChildFilter childFilter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeAllChildrenMatchingFilter(ClientDataChildFilter childFilter) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> boolean containsChildOfTypeAndName(Class<T> type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T extends ClientDataChild> T getFirstChildOfTypeAndName(Class<T> type, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends ClientDataChild> List<T> getChildrenOfTypeAndName(Class<T> type,
			String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends ClientDataChild> boolean removeFirstChildWithTypeAndName(Class<T> type,
			String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T extends ClientDataChild> boolean removeChildrenWithTypeAndName(Class<T> type,
			String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Optional<String> getAttributeValue(String nameInData) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

}
