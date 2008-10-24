/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.listener;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Set;

import javax.ejb.EJBException;
import javax.persistence.PreUpdate;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.SubjectEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;


/**
 * Implementation of application ownership security constraint.
 * 
 * @author fcorneli
 * 
 */
public class SecurityApplicationEntityListener {

    private static final Log    LOG                 = LogFactory.getLog(SecurityApplicationEntityListener.class);

    private static final String SUBJECT_CONTEXT_KEY = "javax.security.auth.Subject.container";


    @PreUpdate
    public void preUpdateCallback(ApplicationEntity application) {

        LOG.debug("pre update callback on application: " + application.getName());

        Subject subject;
        try {
            /*
             * JSR-115: Java Authorization Contract for Containers (JACC) 4.6.1.1. Container Subject Policy Context Handler
             */
            String contextId = PolicyContext.getContextID();
            LOG.debug("context Id: " + contextId);
            subject = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
        } catch (PolicyContextException e) {
            throw new EJBException("Policy Context error: " + e.getMessage(), e);
        }

        if (null == subject) {
            /*
             * We allow non-authenticated code to proceed. This is required for the bootstrap code.
             */
            LOG.debug("no subject active");
            return;
        }

        boolean isOperator = isCallerInRole(subject, SafeOnlineRoles.OPERATOR_ROLE);
        if (isOperator) {
            LOG.debug("operator ok");
            return;
        }
        boolean isGlobalOperator = isCallerInRole(subject, SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);
        if (isGlobalOperator) {
            LOG.debug("global operator ok");
            return;
        }

        boolean isOwner = isCallerInRole(subject, SafeOnlineRoles.OWNER_ROLE);
        if (!isOwner) {
            String msg = "caller has no owner role";
            LOG.debug(msg);
            return;
        }

        String login = getLogin(subject);
        LOG.debug("login: " + login);
        ApplicationOwnerEntity applicationOwner = application.getApplicationOwner();
        SubjectEntity adminSubject = applicationOwner.getAdmin();
        String ownerLogin = adminSubject.getUserId();
        LOG.debug("admin login: " + ownerLogin);

        Principal saPrincipal = SecurityAssociation.getPrincipal();
        LOG.debug("security association principal: " + saPrincipal.getName());
        Subject saSubject = SecurityAssociation.getSubject();
        LOG.debug("security association subject == JACC subject: " + saSubject.equals(subject));

        Set<Principal> principals = subject.getPrincipals();
        for (Principal principal : principals) {
            LOG.debug("principal: " + principal + "; " + principal.getClass().getName());
        }

        if (ownerLogin.equals(saPrincipal.getName())) {
            LOG.debug("ownership check ok");
            return;
        }
        String msg = "only the application owner admin can change the application";
        LOG.error(msg);
        throw new EJBException(msg);
    }

    private String getLogin(Subject subject) {

        Set<Principal> principals = subject.getPrincipals();
        for (Principal principal : principals) {
            if (principal instanceof Group) {
                continue;
            }
            String login = principal.getName();
            return login;
        }
        throw new EJBException("no simple principal found on subject");
    }

    private boolean isCallerInRole(Subject subject, String role) {

        Set<Group> groups = subject.getPrincipals(Group.class);
        if (null == groups)
            return false;
        SimplePrincipal rolePrincipal = new SimplePrincipal(role);
        for (Group group : groups) {
            if (!"Roles".equals(group.getName())) {
                continue;
            }
            if (group.isMember(rolePrincipal))
                return true;
        }
        return false;
    }
}
