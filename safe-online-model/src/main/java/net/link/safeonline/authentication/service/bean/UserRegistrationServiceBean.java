/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.UserRegistrationServiceRemote;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.CredentialManager;
import net.link.safeonline.model.UserRegistrationManager;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;

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
	private SubjectDAO subjectDAO;

	@EJB
	private UserRegistrationManager userRegistrationManager;

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	@EJB
	private CredentialManager credentialManager;

	public void registerUser(String login, String password, String name)
			throws ExistingUserException {
		SubjectEntity newSubject = this.userRegistrationManager
				.registerUser(login);
		try {
			setAttributes(newSubject, password, name);
		} catch (AttributeTypeNotFoundException e) {
			throw new EJBException("attribute type not found");
		}
	}

	private void setAttributes(SubjectEntity subject, String password,
			String name) throws AttributeTypeNotFoundException {
		AttributeTypeEntity passwordAttributeType = this.attributeTypeDAO
				.getAttributeType(SafeOnlineConstants.PASSWORD_ATTRIBUTE);
		this.attributeDAO
				.addAttribute(passwordAttributeType, subject, password);
		if (null != name) {
			AttributeTypeEntity nameAttributeType = this.attributeTypeDAO
					.getAttributeType(SafeOnlineConstants.NAME_ATTRIBUTE);
			this.attributeDAO.addAttribute(nameAttributeType, subject, name);
		}
	}

	public boolean isLoginFree(String login) {
		SubjectEntity existingSubject = this.subjectDAO.findSubject(login);
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
