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

import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterFactory;
import se.uu.ub.cora.json.builder.JsonBuilderFactory;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

/**
 * DataToJsonConverterForTest is an extension to DataGroupToJsonConverter to help with testing of
 * method hookForSubclassesToImplementExtraConversion.
 */
public class BasicClientDataToJsonConverterForTest extends BasicClientDataGroupToJsonConverter {
	MethodCallRecorder MCR = new MethodCallRecorder();

	BasicClientDataToJsonConverterForTest(ClientDataToJsonConverterFactory converterFactory,
			JsonBuilderFactory builderFactory, ClientDataGroup dataGroup) {
		super(converterFactory, builderFactory, dataGroup);
	}

	@Override
	void hookForSubclassesToImplementExtraConversion() {
		MCR.addCall();
	}
}
