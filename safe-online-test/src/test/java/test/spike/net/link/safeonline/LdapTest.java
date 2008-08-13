/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.spike.net.link.safeonline;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class LdapTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(LdapTest.class);


    public void testLdapServerConnection() throws Exception {

        Hashtable<String, String> environment = new Hashtable<String, String>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.PROVIDER_URL, "ldap://localhost:389/");

        LdapContext ldapContext = new InitialLdapContext(environment, null);

        NamingEnumeration<NameClassPair> list = ldapContext.getSchema("").list("");
        while (list.hasMore()) {
            NameClassPair nameClassPair = list.next();
            LOG.debug("name: " + nameClassPair.getName());
        }

        ldapContext.search("", "(objectClass=*)", null);
    }
}
