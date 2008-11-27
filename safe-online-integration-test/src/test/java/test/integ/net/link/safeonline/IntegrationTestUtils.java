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

import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.ApplicationServiceRemote;
import net.link.safeonline.authentication.service.AttributeProviderManagerService;
import net.link.safeonline.authentication.service.AttributeProviderManagerServiceRemote;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationServiceRemote;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.IdentityServiceRemote;
import net.link.safeonline.authentication.service.ProxyAttributeService;
import net.link.safeonline.authentication.service.ProxyAttributeServiceRemote;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.SubscriptionServiceRemote;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.UserRegistrationServiceRemote;
import net.link.safeonline.model.password.PasswordDeviceService;
import net.link.safeonline.model.password.PasswordDeviceServiceRemote;
import net.link.safeonline.pkix.service.PkiService;
import net.link.safeonline.pkix.service.PkiServiceRemote;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.AttributeTypeServiceRemote;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.service.SubjectServiceRemote;
import net.link.safeonline.util.ee.EjbUtils;

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


    public static final String NAME_ATTRIBUTE = "urn:test:integration:attribute:name";


    /**
     * Retrieves the JNDI initial context. This assumes that we have a locally running JBoss Application Server on port 1099.
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static InitialContext getInitialContext()
            throws Exception {

        Hashtable environment = new Hashtable();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        environment.put(Context.PROVIDER_URL, "localhost:1099");
        InitialContext initialContext = new InitialContext(environment);
        return initialContext;
    }

    /**
     * Performs a client-side JAAS login.
     * 
     * @param username
     *            the username.
     * @param password
     *            the password.
     * @return the client-side subject.
     * @throws Exception
     */
    public static Subject login(String username, String password)
            throws Exception {

        LoginContext loginContext = new LoginContext("client-login", new UsernamePasswordHandler(username, password));
        loginContext.login();
        Subject subject = loginContext.getSubject();
        return subject;
    }

    /**
     * Setup the client-side JAAS login configuration. The JBoss RMI will use the credentials from the JBoss ClientLoginModule to
     * authenticate at the server-side.
     * 
     * @throws Exception
     */
    public static void setupLoginConfig()
            throws Exception {

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

    public static AttributeProviderManagerService getAttributeProviderManagerService(InitialContext initialContext) {

        final AttributeProviderManagerService attributeProviderManagerService = EjbUtils.getEJB(initialContext,
                AttributeProviderManagerServiceRemote.JNDI_BINDING, AttributeProviderManagerService.class);
        return attributeProviderManagerService;
    }

    public static UserRegistrationService getUserRegistrationService(InitialContext initialContext) {

        final UserRegistrationService userRegistrationService = EjbUtils.getEJB(initialContext, UserRegistrationServiceRemote.JNDI_BINDING,
                UserRegistrationService.class);
        return userRegistrationService;
    }

    public static AttributeTypeService getAttributeTypeService(InitialContext initialContext) {

        final AttributeTypeService attributeTypeService = EjbUtils.getEJB(initialContext, AttributeTypeServiceRemote.JNDI_BINDING,
                AttributeTypeService.class);
        return attributeTypeService;
    }

    public static ApplicationService getApplicationService(InitialContext initialContext) {

        final ApplicationService applicationService = EjbUtils.getEJB(initialContext, ApplicationServiceRemote.JNDI_BINDING,
                ApplicationService.class);
        return applicationService;
    }

    public static IdentityService getIdentityService(InitialContext initialContext) {

        IdentityService identityService = EjbUtils.getEJB(initialContext, IdentityServiceRemote.JNDI_BINDING, IdentityService.class);
        return identityService;
    }

    public static ProxyAttributeService getProxyAttributeService(InitialContext initialContext) {

        ProxyAttributeService proxyAttributeService = EjbUtils.getEJB(initialContext, ProxyAttributeServiceRemote.JNDI_BINDING,
                ProxyAttributeService.class);
        return proxyAttributeService;
    }

    public static SubjectService getSubjectService(InitialContext initialContext) {

        SubjectService subjectService = EjbUtils.getEJB(initialContext, SubjectServiceRemote.JNDI_BINDING, SubjectService.class);
        return subjectService;
    }

    public static SubscriptionService getSubscriptionService(InitialContext initialContext) {

        final SubscriptionService subscriptionService = EjbUtils.getEJB(initialContext, SubscriptionServiceRemote.JNDI_BINDING,
                SubscriptionService.class);
        return subscriptionService;
    }

    public static PkiService getPkiService(InitialContext initialContext) {

        final PkiService pkiService = EjbUtils.getEJB(initialContext, PkiServiceRemote.JNDI_BINDING, PkiService.class);
        return pkiService;
    }

    public static AuthenticationService getAuthenticationService(InitialContext initialContext) {

        AuthenticationService authenticationService = EjbUtils.getEJB(initialContext, AuthenticationServiceRemote.JNDI_BINDING,
                AuthenticationServiceRemote.class);
        return authenticationService;
    }

    public static PasswordDeviceService getPasswordDeviceService(InitialContext initialContext) {

        PasswordDeviceService passwordDeviceService = EjbUtils.getEJB(initialContext, PasswordDeviceServiceRemote.JNDI_BINDING,
                PasswordDeviceServiceRemote.class);
        return passwordDeviceService;
    }
}
