/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.CredentialManager;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.UserRegistrationServiceRemote;
import net.link.safeonline.device.PasswordDeviceService;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.UserRegistrationManager;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.service.SubjectService;

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
public class UserRegistrationServiceBean implements UserRegistrationService,
		UserRegistrationServiceRemote {

	private static final Log LOG = LogFactory
			.getLog(UserRegistrationServiceBean.class);

	@EJB
	private SubjectService subjectService;

	@EJB
	private UserRegistrationManager userRegistrationManager;

	@EJB
	private CredentialManager credentialManager;

	@EJB
	private PasswordDeviceService passwordDeviceService;

	public void registerUser(String login, String password)
			throws ExistingUserException, AttributeTypeNotFoundException {
		SubjectEntity newSubject = this.userRegistrationManager
				.registerUser(login);
		this.passwordDeviceService.register(newSubject, password);
	}

	public boolean isLoginFree(String login) {
		SubjectEntity existingSubject;
		existingSubject = this.subjectService.findSubject(login);
		return existingSubject == null;
	}

	public void registerUser(String login, byte[] identityStatementData)
			throws ExistingUserException, TrustDomainNotFoundException,
			PermissionDeniedException, ArgumentIntegrityException,
			AttributeTypeNotFoundException {
		LOG.debug("register user: " + login);
		SubjectEntity newSubject = this.userRegistrationManager
				.registerUser(login);
		this.credentialManager.mergeIdentityStatement(newSubject,
				identityStatementData);
	}
}
