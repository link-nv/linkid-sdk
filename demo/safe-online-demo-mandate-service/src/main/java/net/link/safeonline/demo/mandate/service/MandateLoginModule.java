/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.mandate.service;

import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import net.link.safeonline.demo.mandate.AuthorizationService;
import net.link.safeonline.demo.mandate.AuthorizationServiceFactory;
import net.link.safeonline.demo.mandate.MandateConstants;
import net.link.safeonline.sdk.auth.jaas.SimplePrincipal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.security.SimpleGroup;


/**
 * Mandate JAAS login module.
 * 
 * @author fcorneli
 * 
 */
public class MandateLoginModule implements LoginModule {

    private static final Log     LOG = LogFactory.getLog(MandateLoginModule.class);

    private Subject              subject;

    private CallbackHandler      callbackHandler;

    private Principal            authenticatedPrincipal;

    private boolean              admin;

    private AuthorizationService authorizationService;


    public void initialize(Subject inSubject, CallbackHandler inCallbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {

        LOG.debug("initialize");
        subject = inSubject;
        callbackHandler = inCallbackHandler;

        authorizationService = AuthorizationServiceFactory.newInstance();
    }

    public boolean login()
            throws LoginException {

        NameCallback nameCallback = new NameCallback("username");
        Callback[] callbacks = new Callback[] { nameCallback };

        try {
            callbackHandler.handle(callbacks);
        } catch (IOException e) {
            throw new LoginException("IO error: " + e.getMessage());
        } catch (UnsupportedCallbackException e) {
            throw new LoginException("Unsupported callback: " + e.getMessage());
        }

        String username = nameCallback.getName();
        LOG.debug("username: " + username);

        // authentication
        authenticatedPrincipal = new SimplePrincipal(username);

        // authorization
        admin = authorizationService.isAdmin(username);

        return true;
    }

    public boolean commit()
            throws LoginException {

        Set<Principal> principals = subject.getPrincipals();
        principals.add(authenticatedPrincipal);
        setRole(principals, MandateConstants.USER_ROLE);
        if (admin) {
            setRole(principals, MandateConstants.ADMIN_ROLE);
        }
        return true;
    }

    private void setRole(Set<Principal> principals, String role) {

        if (null == role)
            return;
        Group rolesGroup = getGroup("Roles", principals);
        Principal rolePrincipal = new SimplePrincipal(role);
        rolesGroup.addMember(rolePrincipal);
    }

    private Group getGroup(String groupName, Set<Principal> principals) {

        for (Principal principal : principals) {
            if (false == principal instanceof Group) {
                continue;
            }
            Group group = (Group) principal;
            if (groupName.equals(group.getName()))
                return group;
        }
        /*
         * If the group did not yet exist, create it and add it to the subject principals.
         */
        Group group = new SimpleGroup(groupName);
        principals.add(group);
        return group;
    }

    public boolean abort()
            throws LoginException {

        authenticatedPrincipal = null;
        admin = false;
        return true;
    }

    public boolean logout()
            throws LoginException {

        subject.getPrincipals().clear();
        subject.getPublicCredentials().clear();
        subject.getPrivateCredentials().clear();
        return true;
    }
}
