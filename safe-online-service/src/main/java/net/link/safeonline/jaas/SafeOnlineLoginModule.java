/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.jaas;

import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import net.link.safeonline.service.AuthorizationService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;


/**
 * JAAS login module that performs authentication and authorization used by the SafeOnline core security domain.
 * 
 * @author fcorneli
 */
public class SafeOnlineLoginModule implements LoginModule {

    private static final Log   LOG                                     = LogFactory.getLog(SafeOnlineLoginModule.class);

    private Subject            subject;

    private CallbackHandler    callbackHandler;

    public static final String OPTION_AUTHORIZATION_SERVICE_JNDI_NAME  = "authorizationServiceJndiName";

    public static final String DEFAULT_AUTHORIZATION_SERVICE_JNDI_NAME = AuthorizationService.JNDI_BINDING;

    private String             authorizationServiceJndiName;

    private Principal          authenticatedPrincipal;

    private Set<String>        roles;


    public boolean abort() {

        LOG.debug("abort");

        authenticatedPrincipal = null;
        roles = null;

        return true;
    }

    public boolean commit()
            throws LoginException {

        LOG.debug("commit");

        Set<Principal> principals = subject.getPrincipals();
        if (null == authenticatedPrincipal)
            throw new LoginException("authenticated principal should be not null");
        // authenticate
        principals.add(authenticatedPrincipal);

        // make JBoss happy
        Group callerPrincipalGroup = getGroup("CallerPrincipal", principals);
        callerPrincipalGroup.addMember(authenticatedPrincipal);

        // authorize
        Group rolesGroup = getGroup("Roles", principals);

        if (null == roles)
            return true;

        for (String role : roles) {
            rolesGroup.addMember(new SimplePrincipal(role));
        }

        LOG.debug("committed");

        return true;
    }

    private String getOptionValue(Map<?, ?> options, String optionName, String defaultOptionValue) {

        String optionValue = (String) options.get(optionName);
        if (null == optionValue) {
            optionValue = defaultOptionValue;
            LOG.debug("using default option value for " + optionName + " = " + defaultOptionValue);
        }
        return optionValue;
    }

    public void initialize(Subject newSubject, CallbackHandler newCallbackHandler, @SuppressWarnings("unchecked") Map sharedState,
                           @SuppressWarnings("unchecked") Map options) {

        LOG.debug("initialize");

        authorizationServiceJndiName = getOptionValue(options, OPTION_AUTHORIZATION_SERVICE_JNDI_NAME,
                DEFAULT_AUTHORIZATION_SERVICE_JNDI_NAME);

        subject = newSubject;
        callbackHandler = newCallbackHandler;
        LOG.debug("subject class: " + subject.getClass().getName());
        LOG.debug("callback handler class: " + callbackHandler.getClass().getName());
    }

    private Group getGroup(String groupName, Set<Principal> principals) {

        Iterator<?> iter = principals.iterator();
        while (iter.hasNext()) {
            Object next = iter.next();
            if (next instanceof Group == false) {
                continue;
            }
            Group group = (Group) next;
            if (group.getName().equals(groupName))
                return group;
        }
        // If we did not find a group create one
        Group group = new SimpleGroup(groupName);
        principals.add(group);
        return group;
    }

    public boolean login()
            throws LoginException {

        LOG.debug("login");
        // retrieve credentials
        NameCallback nameCallback = new NameCallback("username");
        // TODO: we should verify a SAML assertion here
        Callback[] callbacks = new Callback[] { nameCallback };

        try {
            callbackHandler.handle(callbacks);
        } catch (IOException e) {
            String msg = "IO error: " + e.getMessage();
            LOG.error(msg);
            throw new LoginException(msg);
        } catch (UnsupportedCallbackException e) {
            String msg = "unsupported callback: " + e.getMessage();
            LOG.error(msg);
            throw new LoginException(msg);
        }

        String userId = nameCallback.getName();
        LOG.debug("userId: " + userId);
        if (null == userId)
            throw new LoginException("userId is null");

        // authenticate
        // TODO: authenticate here again via SAML assertion
        LOG.debug("authenticated");

        authenticatedPrincipal = new SimplePrincipal(userId);

        // authorize
        AuthorizationService authorizationService = getAuthorizationService();

        roles = authorizationService.getRoles(userId);

        return true;
    }

    private AuthorizationService getAuthorizationService()
            throws LoginException {

        try {
            AuthorizationService authorizationService = EjbUtils.getEJB(authorizationServiceJndiName, AuthorizationService.class);
            return authorizationService;
        } catch (RuntimeException e) {
            throw new LoginException("JNDI lookup error: " + e.getMessage());
        }
    }

    public boolean logout()
            throws LoginException {

        LOG.debug("logout");
        Set<Principal> principals = subject.getPrincipals();
        if (null == authenticatedPrincipal)
            throw new LoginException("authenticated principal should not be null");
        boolean result = principals.remove(authenticatedPrincipal);
        if (!result)
            throw new LoginException("could not remove authenticated principal");
        /*
         * Despite the fact that JBoss AbstractServerLoginModule is not removing the roles on the subject, we clear here all data on the
         * subject.
         */
        subject.getPrincipals().clear();
        subject.getPublicCredentials().clear();
        subject.getPrivateCredentials().clear();
        return true;
    }
}
