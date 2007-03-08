/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of user registration service interface. This component does
 * not live within the SafeOnline core security domain. This because a user that
 * is about to register himself is not yet logged on into the system.
 * 
 * @author fcorneli
 * 
 */
@Stateless
public class UserRegistrationServiceBean implements UserRegistrationService {

	private static final Log LOG = LogFactory
			.getLog(UserRegistrationServiceBean.class);

	@EJB
	private SubjectDAO subjectDAO;

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	@EJB
	private AttributeDAO attributeDAO;

	public void registerUser(String login, String password, String name)
			throws ExistingUserException, ApplicationNotFoundException {
		LOG.debug("register user: " + login);

		SubjectEntity existingSubject = this.subjectDAO.findSubject(login);
		if (null != existingSubject) {
			throw new ExistingUserException();
		}

		ApplicationEntity safeOnlineUserApplication = this.applicationDAO
				.findApplication(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME);
		if (null == safeOnlineUserApplication) {
			throw new ApplicationNotFoundException();
		}

		SubjectEntity newSubject = this.subjectDAO.addSubject(login);

		this.attributeDAO.addAttribute(SafeOnlineConstants.PASSWORD_ATTRIBUTE,
				login, password);
		if (null != name) {
			this.attributeDAO.addAttribute(SafeOnlineConstants.NAME_ATTRIBUTE,
					login, name);
		}

		this.subscriptionDAO.addSubscription(SubscriptionOwnerType.APPLICATION,
				newSubject, safeOnlineUserApplication);
	}
}
