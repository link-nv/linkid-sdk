/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.service.SubjectService;

/**
 * This service serves as a mapping between the SafeOnline global user id and
 * the required application's user id as specified in the application's id
 * scope.
 * 
 * @author wvdhaute
 * 
 */

@Stateless
public class UserIdMappingServiceBean implements UserIdMappingService {

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	@EJB
	private SubjectService subjectService;

	@EJB
	private SubjectIdentifierDAO subjectIdentifierDAO;

	public String getApplicationUserId(String applicationName, String userId)
			throws ApplicationNotFoundException, SubscriptionNotFoundException {
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		IdScopeType idScope = application.getIdScope();
		if (IdScopeType.USER == idScope)
			return userId;
		else if (IdScopeType.SUBSCRIPTION == idScope)
			return getSubscriptionId(application, userId);
		return null;
	}

	private String getSubscriptionId(ApplicationEntity application,
			String userId) throws SubscriptionNotFoundException {
		SubjectEntity subject = this.subjectService.findSubject(userId);
		SubscriptionEntity subscription = this.subscriptionDAO
				.findSubscription(subject, application);
		if (null == subscription) {
			this.subscriptionDAO.addSubscription(
					SubscriptionOwnerType.APPLICATION, subject, application);
			subscription = this.subscriptionDAO.getSubscription(subject,
					application);
		}
		return subscription.getUserApplicationId();
	}

	public String getUserId(String applicationName, String applicationUserId)
			throws ApplicationNotFoundException {
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		IdScopeType idScope = application.getIdScope();
		if (IdScopeType.USER == idScope)
			return applicationUserId;
		else if (IdScopeType.SUBSCRIPTION == application.getIdScope())
			return getUserIdFromSubscription(applicationUserId);
		return null;
	}

	private String getUserIdFromSubscription(String applicationUserId) {
		return this.subjectIdentifierDAO.findSubject(
				SafeOnlineConstants.APPLICATION_USER_IDENTIFIER_DOMAIN,
				applicationUserId).getUserId();
	}

}
