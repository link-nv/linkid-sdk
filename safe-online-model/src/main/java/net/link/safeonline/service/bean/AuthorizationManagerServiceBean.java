/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service.bean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.RoleNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.service.AuthorizationManagerService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.util.ee.SecurityManagerUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;


/**
 * Implementation of the authorization manager service. For the moment the authorization is granted via application subscriptions. I.e. when
 * assigning a role to a subject, the subject will actually be subscribed onto the corresponding SafeOnline application.
 * 
 * @author fcorneli
 * 
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@LocalBinding(jndiBinding = AuthorizationManagerService.JNDI_BINDING)
public class AuthorizationManagerServiceBean implements AuthorizationManagerService {

    private static final Log                 LOG                    = LogFactory.getLog(AuthorizationManagerServiceBean.class);

    @EJB
    private SubjectService                   subjectService;

    @EJB
    private SubscriptionDAO                  subscriptionDAO;

    @EJB
    private ApplicationDAO                   applicationDAO;

    private static final Map<String, String> roleApplicationNameMap = new HashMap<String, String>();

    static {
        roleApplicationNameMap.put(SafeOnlineRoles.OWNER_ROLE, SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME);
        roleApplicationNameMap.put(SafeOnlineRoles.OPERATOR_ROLE, SafeOnlineConstants.SAFE_ONLINE_OPERATOR_APPLICATION_NAME);
        roleApplicationNameMap.put(SafeOnlineRoles.HELPDESK_ROLE, SafeOnlineConstants.SAFE_ONLINE_HELPDESK_APPLICATION_NAME);
    }


    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public Set<String> getAvailableRoles() {

        LOG.debug("getAvailableRoles");
        Set<String> availableRoles = new HashSet<String>();

        /*
         * We limit ourself to the SafeOnline application on which a user cannot subscribe himself.
         */
        availableRoles.add(SafeOnlineRoles.HELPDESK_ROLE);
        availableRoles.add(SafeOnlineRoles.OPERATOR_ROLE);
        availableRoles.add(SafeOnlineRoles.OWNER_ROLE);

        return availableRoles;
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public Set<String> getRoles(String login) throws SubjectNotFoundException {

        LOG.debug("getRoles for subject: " + login);
        SubjectEntity subject = this.subjectService.getSubjectFromUserName(login);
        Set<String> roles = new HashSet<String>();

        addRoleIfSubscribed(SafeOnlineRoles.OWNER_ROLE, subject, SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME, roles);
        addRoleIfSubscribed(SafeOnlineRoles.OPERATOR_ROLE, subject, SafeOnlineConstants.SAFE_ONLINE_OPERATOR_APPLICATION_NAME, roles);
        addRoleIfSubscribed(SafeOnlineRoles.HELPDESK_ROLE, subject, SafeOnlineConstants.SAFE_ONLINE_HELPDESK_APPLICATION_NAME, roles);

        return roles;
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void setRoles(String login, Set<String> roles) throws SubjectNotFoundException, RoleNotFoundException {

        LOG.debug("set roles for subject: " + login);
        SubjectEntity subject = this.subjectService.getSubjectFromUserName(login);
        for (String role : roles) {
            setRole(subject, role);
        }

        Set<String> removeRoles = getAvailableRoles();
        removeRoles.removeAll(roles);
        for (String removeRole : removeRoles) {
            unsetRole(subject, removeRole);
        }

        SecurityManagerUtils.flushCredentialCache(login, SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN);
    }

    private void setRole(SubjectEntity subject, String role) throws RoleNotFoundException {

        ApplicationEntity application = getApplication(role);
        SubscriptionEntity subscription = this.subscriptionDAO.findSubscription(subject, application);
        if (null != subscription)
            return;

        this.subscriptionDAO.addSubscription(SubscriptionOwnerType.APPLICATION, subject, application);
    }

    private ApplicationEntity getApplication(String role) throws RoleNotFoundException {

        String applicationName = roleApplicationNameMap.get(role);
        if (null == applicationName)
            throw new RoleNotFoundException();

        ApplicationEntity application = this.applicationDAO.findApplication(applicationName);
        if (null == application)
            throw new EJBException("application not found: " + applicationName);

        return application;
    }

    private void unsetRole(SubjectEntity subject, String role) throws RoleNotFoundException {

        ApplicationEntity application = getApplication(role);
        SubscriptionEntity subscription = this.subscriptionDAO.findSubscription(subject, application);
        if (null == subscription)
            return;

        this.subscriptionDAO.removeSubscription(subscription);
    }

    private void addRoleIfSubscribed(String roleToAdd, SubjectEntity subject, String applicationName, Set<String> roles) {

        try {
            ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
            SubscriptionEntity subscription = this.subscriptionDAO.findSubscription(subject, application);
            if (null == subscription)
                return;

            roles.add(roleToAdd);
        }

        catch (ApplicationNotFoundException e) {
            LOG.error("application not found: " + applicationName);
            return;
        }
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<String> getUsers(String prefix) throws AttributeTypeNotFoundException {

        return this.subjectService.listUsers(prefix);
    }
}
