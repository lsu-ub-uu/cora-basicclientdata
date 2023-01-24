/*
 * Copyright 2015 Uppsala University Library
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
import java.util.List;

import se.uu.ub.cora.clientdata.ClientData;
import se.uu.ub.cora.clientdata.ClientDataList;

public final class BasicClientDataList implements ClientDataList {

	private String containDataOfType;
	private List<ClientData> listOfData = new ArrayList<>();
	private String totalNo;
	private String fromNo;
	private String toNo;

	private BasicClientDataList(String containDataOfType) {
		this.containDataOfType = containDataOfType;
	}

	public static BasicClientDataList withContainDataOfType(String containDataOfType) {
		return new BasicClientDataList(containDataOfType);
	}

	@Override
	public String getContainDataOfType() {
		return containDataOfType;
	}

	@Override
	public void addData(ClientData data) {
		listOfData.add(data);

	}

	@Override
	public List<ClientData> getDataList() {
		return listOfData;
	}

	@Override
	public void setTotalNo(String totalNo) {
		this.totalNo = totalNo;
	}

	@Override
	public String getTotalNumberOfTypeInStorage() {
		return totalNo;
	}

	@Override
	public void setFromNo(String fromNo) {
		this.fromNo = fromNo;
	}

	@Override
	public String getFromNo() {
		return fromNo;
	}

	@Override
	public void setToNo(String toNo) {
		this.toNo = toNo;
	}

	@Override
	public String getToNo() {
		return toNo;
	}

}
