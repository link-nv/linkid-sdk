/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service.bean;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.service.AuthorizationService;
import net.link.safeonline.service.AuthorizationServiceRemote;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementation of the authorization service interface. This component cannot live within the SafeOnline core security domain since it
 * will be used by a JAAS login module to perform authorization of a caller principal.
 * 
 * @author fcorneli
 * 
 */
@Stateless
@LocalBinding(jndiBinding = AuthorizationService.JNDI_BINDING)
public class AuthorizationServiceBean implements AuthorizationService, AuthorizationServiceRemote {

    private static final Log LOG              = LogFactory.getLog(AuthorizationServiceBean.class);

    @EJB
    private SubjectService   subjectService;

    @EJB
    private SubscriptionDAO  subscriptionDAO;

    @EJB
    private ApplicationDAO   applicationDAO;

    @Resource(name = "isGlobalOperator")
    private boolean          isGlobalOperator = false;


    public Set<String> getRoles(String userId) {

        Set<String> roles = new HashSet<String>();

        LOG.debug("get roles for userId: " + userId);
        try {
            SubjectEntity subject = this.subjectService.getSubject(userId);

            /*
             * For now we base the authorization on made subscriptions. Of course, later on we could let this decision depend on explicit
             * ACL, i.e., have a trust layer to make the decision.
             */
            addRoleIfSubscribed(SafeOnlineRoles.USER_ROLE, subject, SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME, roles);

            addRoleIfSubscribed(SafeOnlineRoles.OWNER_ROLE, subject, SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME, roles);

            if (true == this.isGlobalOperator) {
                LOG.debug("assigning global operator role");
                addRoleIfSubscribed(SafeOnlineRoles.GLOBAL_OPERATOR_ROLE, subject,
                        SafeOnlineConstants.SAFE_ONLINE_OPERATOR_APPLICATION_NAME, roles);
            }
            addRoleIfSubscribed(SafeOnlineRoles.OPERATOR_ROLE, subject, SafeOnlineConstants.SAFE_ONLINE_OPERATOR_APPLICATION_NAME, roles);

            addRoleIfSubscribed(SafeOnlineRoles.HELPDESK_ROLE, subject, SafeOnlineConstants.SAFE_ONLINE_HELPDESK_APPLICATION_NAME, roles);
        }

        catch (SubjectNotFoundException e) {
            LOG.error("entity not found: " + userId);
        }

        return roles;
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
        }
    }
}
