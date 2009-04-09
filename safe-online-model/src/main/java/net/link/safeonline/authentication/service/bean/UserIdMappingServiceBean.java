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
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
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
import org.jboss.annotation.ejb.LocalBinding;


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

    @EJB(mappedName = ApplicationDAO.JNDI_BINDING)
    private ApplicationDAO        applicationDAO;

    @EJB(mappedName = SubscriptionDAO.JNDI_BINDING)
    private SubscriptionDAO       subscriptionDAO;

    @EJB(mappedName = ApplicationScopeIdDAO.JNDI_BINDING)
    private ApplicationScopeIdDAO applicationScopeIdDAO;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService        subjectService;


    public String getApplicationUserId(long applicationId, String userId)
            throws ApplicationNotFoundException, SubjectNotFoundException {

        ApplicationEntity application = applicationDAO.getApplication(applicationId);
        SubjectEntity subject = subjectService.getSubject(userId);
        return getApplicationUserId(application, subject);
    }

    public String getApplicationUserId(ApplicationEntity application, SubjectEntity subject) {

        IdScopeType idScope = application.getIdScope();
        if (IdScopeType.USER == idScope)
            return subject.getUserId();
        else if (IdScopeType.SUBSCRIPTION == idScope)
            return getSubscriptionScopeId(application, subject);
        else
            return getApplicationScopeId(application, subject);

    }

    private String getApplicationScopeId(ApplicationEntity application, SubjectEntity subject) {

        ApplicationScopeIdEntity subjectApplicationScopeId = applicationScopeIdDAO.findApplicationScopeId(subject, application);
        if (null == subjectApplicationScopeId) {
            subjectApplicationScopeId = applicationScopeIdDAO.addApplicationScopeId(subject, application);
        }
        return subjectApplicationScopeId.getId();
    }

    private String getSubscriptionScopeId(ApplicationEntity application, SubjectEntity subject) {

        SubscriptionEntity subscription = subscriptionDAO.findSubscription(subject, application);
        if (null == subscription) {
            subscription = subscriptionDAO.addSubscription(SubscriptionOwnerType.APPLICATION, subject, application);
        }
        return subscription.getSubscriptionUserId();
    }

    public String findUserId(long applicationId, String applicationUserId)
            throws ApplicationNotFoundException {

        LOG.debug("findUserId: " + applicationId + ", " + applicationUserId);
        ApplicationEntity application = applicationDAO.getApplication(applicationId);
        return findUserId(application, applicationUserId);
    }

    public String findUserId(ApplicationEntity application, String applicationUserId) {

        LOG.debug("findUserId: " + application.getName() + ", " + applicationUserId);
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
        SubscriptionEntity subscription = subscriptionDAO.findSubscription(applicationUserId);
        if (null == subscription)
            return null;
        return subscription.getSubject().getUserId();
    }

    private String findUserIdFromApplicationScope(String applicationUserId) {

        LOG.debug("getUserIdFromApplicationScope: " + applicationUserId);
        ApplicationScopeIdEntity applicationScopeId = applicationScopeIdDAO.findApplicationScopeId(applicationUserId);
        if (null == applicationScopeId)
            return null;
        return applicationScopeId.getSubject().getUserId();
    }
}
