/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationScopeIdDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationScopeIdEntity;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This service serves as a mapping between the SafeOnline global user id and the required application's user id as specified in the
 * application's id scope.
 * 
 * This service does not live within a security domain as it is used by services in the application security domain as well as in the user
 * security domain.
 * 
 * @author wvdhaute
 * 
 */

@Stateless
@LocalBinding(jndiBinding = UserIdMappingService.JNDI_BINDING)
public class UserIdMappingServiceBean implements UserIdMappingService {

    private final static Log      LOG = LogFactory.getLog(UserIdMappingServiceBean.class);

    @EJB
    private ApplicationDAO        applicationDAO;

    @EJB
    private SubscriptionDAO       subscriptionDAO;

    @EJB
    private ApplicationScopeIdDAO applicationScopeIdDAO;

    @EJB
    private SubjectService        subjectService;


    public String getApplicationUserId(String applicationName, String userId) throws ApplicationNotFoundException,
                                                                             SubscriptionNotFoundException {

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        IdScopeType idScope = application.getIdScope();
        if (IdScopeType.USER == idScope)
            return userId;
        else if (IdScopeType.SUBSCRIPTION == idScope)
            return getSubscriptionScopeId(application, userId);
        else
            return getApplicationScopeId(application, userId);
    }

    private String getApplicationScopeId(ApplicationEntity application, String userId) {

        SubjectEntity subject = this.subjectService.findSubject(userId);
        ApplicationScopeIdEntity subjectApplicationScopeId = this.applicationScopeIdDAO.findApplicationScopeId(subject, application);
        if (null == subjectApplicationScopeId) {
            subjectApplicationScopeId = this.applicationScopeIdDAO.addApplicationScopeId(subject, application);
        }
        return subjectApplicationScopeId.getId();
    }

    private String getSubscriptionScopeId(ApplicationEntity application, String userId) throws SubscriptionNotFoundException {

        SubjectEntity subject = this.subjectService.findSubject(userId);
        SubscriptionEntity subscription = this.subscriptionDAO.findSubscription(subject, application);
        if (null == subscription) {
            this.subscriptionDAO.addSubscription(SubscriptionOwnerType.APPLICATION, subject, application);
            subscription = this.subscriptionDAO.getSubscription(subject, application);
        }
        return subscription.getSubscriptionUserId();
    }

    public String findUserId(String applicationName, String applicationUserId) throws ApplicationNotFoundException {

        LOG.debug("findUserId: " + applicationName + ", " + applicationUserId);
        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        IdScopeType idScope = application.getIdScope();
        if (IdScopeType.USER == idScope)
            return applicationUserId;
        else if (IdScopeType.SUBSCRIPTION == application.getIdScope())
            return findUserIdFromSubscriptionScope(applicationUserId);
        else
            return findUserIdFromApplicationScope(applicationUserId);
    }

    private String findUserIdFromSubscriptionScope(String applicationUserId) {

        LOG.debug("getUserIdFromSubscriptionScope: " + applicationUserId);
        SubscriptionEntity subscription = this.subscriptionDAO.findSubscription(applicationUserId);
        if (null == subscription)
            return null;
        return subscription.getSubject().getUserId();
    }

    private String findUserIdFromApplicationScope(String applicationUserId) {

        LOG.debug("getUserIdFromApplicationScope: " + applicationUserId);
        ApplicationScopeIdEntity applicationScopeId = this.applicationScopeIdDAO.findApplicationScopeId(applicationUserId);
        if (null == applicationScopeId)
            return null;
        return applicationScopeId.getSubject().getUserId();
    }
}
