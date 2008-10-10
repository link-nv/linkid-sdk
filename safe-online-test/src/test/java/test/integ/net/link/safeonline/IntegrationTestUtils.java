/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.jboss.security.auth.callback.UsernamePasswordHandler;


/**
 * Utility methods to aid the integration testing via the remote RMI interface.
 *
 * @author fcorneli
 *
 */
public class IntegrationTestUtils {

    private IntegrationTestUtils() {

        // empty
    }

    /**
     * Retrieves the JNDI initial context. This assumes that we have a locally running JBoss Application Server on port
     * 1099.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static InitialContext getInitialContext() throws Exception {

        Hashtable environment = new Hashtable();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        environment.put(Context.PROVIDER_URL, "localhost:1099");
        InitialContext initialContext = new InitialContext(environment);
        return initialContext;
    }

    /**
     * Performs a client-side JAAS login.
     *
     * @param userId
     *            the userId.
     * @param password
     *            the password.
     * @return the client-side subject.
     * @throws Exception
     */
    public static Subject login(String userId, String password) throws Exception {

        LoginContext loginContext = new LoginContext("client-login", new UsernamePasswordHandler(userId, password));
        loginContext.login();
        Subject subject = loginContext.getSubject();
        return subject;
    }

    /**
     * Setup the client-side JAAS login configuration. The JBoss RMI will use the credentials from the JBoss
     * ClientLoginModule to authenticate at the server-side.
     *
     * @throws Exception
     */
    public static void setupLoginConfig() throws Exception {

        File tmpConfigFile = File.createTempFile("jaas-", ".conf");
        tmpConfigFile.deleteOnExit();
        PrintWriter configWriter = new PrintWriter(new FileOutputStream(tmpConfigFile), true);
        configWriter.println("client-login {");
        configWriter.println("org.jboss.security.ClientLoginModule required");
        configWriter.println(";");
        configWriter.println("};");
        configWriter.close();
        System.setProperty("java.security.auth.login.config", tmpConfigFile.getAbsolutePath());
    }
}
