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

package se.uu.ub.cora.clientbasicdata.converter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.converter.ClientDataConversionException;

public class BasicClientConverterExceptionTest {

	@Test
	public void testName() throws Exception {
		BasicClientConverterException converterException = new BasicClientConverterException("message");
		assertTrue(converterException instanceof ClientDataConversionException);
	}

	@Test
	public void testInit() {
		Exception exception = new RuntimeException("e");
		BasicClientConverterException converterException = new BasicClientConverterException("message", exception);
		assertEquals(converterException.getMessage(), "message");
		assertEquals(converterException.getCause(), exception);
	}

	@Test
	public void testInitMessage() {
		BasicClientConverterException converterException = new BasicClientConverterException("message");
		assertEquals(converterException.getMessage(), "message");
	}

}
