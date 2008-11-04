/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.taglib;

import java.lang.reflect.Method;

import junit.framework.TestCase;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.taglib.AttributeOutputComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class AttributeOutputComponentTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(AttributeOutputComponentTest.class);


    public void testGetAttributeValueEncoder()
            throws Exception {

        // setup
        Class<AttributeOutputComponent> clazz = AttributeOutputComponent.class;
        Method method = clazz.getDeclaredMethod("getAttributeValueEncoder", new Class[] { DatatypeType.class });
        method.setAccessible(true);

        // operate
        Object result1 = method.invoke(null, new Object[] { DatatypeType.STRING });
        Object result2 = method.invoke(null, new Object[] { DatatypeType.STRING });

        // verify
        assertTrue(result1 == result2);
        LOG.debug("result class: " + result1.getClass().getName());
    }
}
