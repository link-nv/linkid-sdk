/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Utility class for JNDI unit testing.
 * 
 * @author fcorneli
 * 
 */
public class JndiTestUtils {

    private static final Log    LOG = LogFactory.getLog(JndiTestUtils.class);

    private Map<String, Object> components;


    public JndiTestUtils() {

        this.components = new HashMap<String, Object>();
    }

    public void bindComponent(String jndiName, Object component) throws NamingException {

        LOG.debug("bind component: " + jndiName);
        this.components.put(jndiName, component);
        InitialContext initialContext = new InitialContext();
        String[] names = jndiName.split("/");
        Context context = initialContext;
        for (int idx = 0; idx < names.length - 1; idx++) {
            String name = names[idx];
            LOG.debug("name: " + name);
            NamingEnumeration<NameClassPair> listContent = context.list("");
            boolean subContextPresent = false;
            while (listContent.hasMore()) {
                NameClassPair nameClassPair = listContent.next();
                if (false == name.equals(nameClassPair.getName())) {
                    continue;
                }
                subContextPresent = true;
            }
            if (false == subContextPresent) {
                context = context.createSubcontext(name);
            } else {
                context = (Context) context.lookup(name);
            }
        }
        String name = names[names.length - 1];
        context.rebind(name, component);
    }

    public void setUp() {

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.shiftone.ooc.InitialContextFactoryImpl");
    }

    /**
     * Tear down the test JNDI tree. This will unbind all previously bound component.
     * 
     * @throws NamingException
     */
    public void tearDown() throws NamingException {

        InitialContext initialContext = new InitialContext();
        for (String name : this.components.keySet()) {
            LOG.debug("unbinding: " + name);
            initialContext.unbind(name);
        }
    }
}
