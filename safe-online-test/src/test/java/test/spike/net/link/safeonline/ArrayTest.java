/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.spike.net.link.safeonline;

import java.lang.reflect.Array;

import junit.framework.TestCase;


public class ArrayTest extends TestCase {

    public void testArrayType()
            throws Exception {

        assertFalse(Object[].class.equals(String[].class));

        assertTrue(Object[].class.isAssignableFrom(String[].class));

        assertFalse(String[].class.isAssignableFrom(Object[].class));
    }

    public void testNewInstance()
            throws Exception {

        Class<String[]> clazz = String[].class;

        String[] result = getNewInstance(clazz);

        assertNotNull(result);
        assertEquals(2, result.length);
    }

    @SuppressWarnings("unchecked")
    private <Type> Type getNewInstance(Class<Type> clazz) {

        assertTrue(clazz.isArray());

        Class componentType = clazz.getComponentType();
        assertEquals(String.class, componentType);

        Type result = (Type) Array.newInstance(componentType, 2);

        return result;
    }
}
