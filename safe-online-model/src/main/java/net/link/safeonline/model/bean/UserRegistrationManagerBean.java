/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.model.UserRegistrationManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class UserRegistrationManagerBean implements UserRegistrationManager {

	private static final Log LOG = LogFactory
			.getLog(UserRegistrationManagerBean.class);

	@EJB
	private SubjectDAO subjectDAO;

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	public SubjectEntity registerUser(String login)
			throws ExistingUserException {
		LOG.debug("register user: " + login);
		checkExistingUser(login);
		SubjectEntity newSubject = this.subjectDAO.addSubject(login);
		ApplicationEntity safeOnlineUserApplication = getSafeOnlineUserApplication();
		/*
		 * Make sure the user can at least login into the SafeOnline user web
		 * application.
		 */
		this.subscriptionDAO.addSubscription(SubscriptionOwnerType.APPLICATION,
				newSubject, safeOnlineUserApplication);
		return newSubject;
	}

	private void checkExistingUser(String login) throws ExistingUserException {
		SubjectEntity existingSubject = this.subjectDAO.findSubject(login);
		if (null != existingSubject) {
			throw new ExistingUserException();
		}
	}

	private ApplicationEntity getSafeOnlineUserApplication() {
		ApplicationEntity safeOnlineUserApplication = this.applicationDAO
				.findApplication(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME);
		if (null == safeOnlineUserApplication) {
			throw new EJBException("SafeOnline user application not found");
		}
		return safeOnlineUserApplication;
	}
}
