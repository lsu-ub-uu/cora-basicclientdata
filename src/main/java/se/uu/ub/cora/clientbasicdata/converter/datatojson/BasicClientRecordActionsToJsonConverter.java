/*
 * Copyright 2021 Uppsala University Library
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
package se.uu.ub.cora.clientbasicdata.converter.datatojson;

import se.uu.ub.cora.json.builder.JsonObjectBuilder;

/**
 * RecordActionsToJsonConverter is an interface to decouple the implementation from the API to
 * enable testing, this interface is currently not meant to be exported or used outside of the
 * package it is in. The Java module system is used to keep it as an internal interface to this
 * module.
 * <p>
 * Implementations do not have to be threadsafe.
 */
public interface BasicClientRecordActionsToJsonConverter {
	/**
	 * toJsonObjectBuilder return a {@link JsonObjectBuilder} representation of the Actions that are
	 * to be converted.
	 * 
	 * @param actionsConverterData
	 *            An {@link BasicClientActionsConverterData} with info needed to convert the actions
	 * 
	 * @return A JsonObectBuilder set up to build the Actions.
	 */
	JsonObjectBuilder toJsonObjectBuilder(BasicClientActionsConverterData actionsConverterData);

}
