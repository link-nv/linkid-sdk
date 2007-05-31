/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.spike.net.link.safeonline;

import junit.framework.TestCase;

public class ArrayTest extends TestCase {

	public void testArrayType() throws Exception {
		assertFalse(Object[].class.equals(String[].class));

		assertTrue(Object[].class.isAssignableFrom(String[].class));

		assertFalse(String[].class.isAssignableFrom(Object[].class));
	}
}
