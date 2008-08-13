/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.util.ee;

import java.util.List;
import java.util.Map;

import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.util.ee.EjbUtils;
import junit.framework.TestCase;


public class EjbUtilsTest extends TestCase {

    private JndiTestUtils jndiTestUtils;


    @Override
    protected void setUp() throws Exception {

        super.setUp();

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();
    }

    @Override
    protected void tearDown() throws Exception {

        this.jndiTestUtils.tearDown();
        super.tearDown();
    }

    public void testGetComponentNamesReturnsEmptyMap() throws Exception {

        // setup
        String jndiPrefix = "test/prefix/" + getName();

        // operate
        Map<String, TestType> result = EjbUtils.getComponentNames(jndiPrefix, TestType.class);

        // verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    public void testGetComponentNamesReturnsObject() throws Exception {

        // setup
        String jndiPrefix = "test/prefix/" + getName();
        TestType testObject = new TestType();
        String objectName = "test-object-name";
        this.jndiTestUtils.bindComponent(jndiPrefix + "/" + objectName, testObject);

        // operate
        Map<String, TestType> result = EjbUtils.getComponentNames(jndiPrefix, TestType.class);

        // verify
        assertEquals(1, result.size());
        assertEquals(testObject, result.get(objectName));
    }

    public void testGetComponentsIsEmpty() throws Exception {

        // setup
        String jndiPrefix = "test/prefix/" + getName();

        // operate
        List<TestType> result = EjbUtils.getComponents(jndiPrefix, TestType.class);

        // verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    public void testGetComponentsReturnsObject() throws Exception {

        // setup
        String jndiPrefix = "test/prefix/" + getName();
        TestType testObject = new TestType();
        String objectName = "test-object-name";
        this.jndiTestUtils.bindComponent(jndiPrefix + "/" + objectName, testObject);

        // operate
        List<TestType> result = EjbUtils.getComponents(jndiPrefix, TestType.class);

        // verify
        assertEquals(1, result.size());
        assertEquals(testObject, result.get(0));
    }


    static class TestType {

    }
}
