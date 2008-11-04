/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.spike.net.link.safeonline;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.integ.net.link.safeonline.IntegrationTestUtils;


public class JndiTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(JndiTest.class);


    public void testJndiIteration()
            throws Exception {

        InitialContext initialContext = IntegrationTestUtils.getInitialContext();
        Context context = (Context) initialContext.lookup("SafeOnline");

        List<String> classes = contextIteration(context, Object.class);

        for (String className : classes) {
            if (className.equals("SamlAuthorityServiceBean")) {
                LOG.debug("found bean: " + className);
            }
        }

    }

    private List<String> contextIteration(Context context, Class<?> type)
            throws Exception {

        LOG.debug("Entering context: " + context.getNameInNamespace());

        List<String> result = new ArrayList<String>();

        NamingEnumeration<NameClassPair> items = context.list("");

        while (items.hasMore()) {
            NameClassPair nameClassPair = items.next();
            String objectName = nameClassPair.getName();
            Object object;
            try {
                object = context.lookup(objectName);
            } catch (Exception e) {
                return result;
            }
            if (type.isInstance(object)) {
                result.add(objectName);
                LOG.debug("Added: " + objectName);
            }
            if (Context.class.isInstance(object)) {
                LOG.debug("Seen: " + object.getClass().getName());
                result.addAll(contextIteration((Context) object, type));
            }
        }

        return result;
    }
}
