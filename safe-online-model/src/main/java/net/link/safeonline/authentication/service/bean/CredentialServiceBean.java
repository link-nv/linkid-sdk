/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.authentication.service.CredentialServiceRemote;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.CredentialManager;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.util.ee.SecurityManagerUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

/**
 * Implementation of the credential service interface.
 * 
 * @author fcorneli
 * 
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class CredentialServiceBean implements CredentialService,
		CredentialServiceRemote {

	private static Log LOG = LogFactory.getLog(CredentialServiceBean.class);

	@EJB
	private SubjectManager subjectManager;

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private CredentialManager credentialManager;

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void changePassword(String oldPassword, String newPassword)
			throws PermissionDeniedException {
		LOG.debug("change password");
		String login = this.subjectManager.getCallerLogin();

		AttributeEntity passwordAttribute = this.attributeDAO.findAttribute(
				SafeOnlineConstants.PASSWORD_ATTRIBUTE, login);
		if (null == passwordAttribute) {
			throw new EJBException(
					"password attribute not present for subject: " + login);
		}

		String currentPassword = passwordAttribute.getStringValue();
		if (null == currentPassword) {
			throw new EJBException("current password is null");
		}

		if (!currentPassword.equals(oldPassword)) {
			throw new PermissionDeniedException();
		}

		passwordAttribute.setStringValue(newPassword);

		SecurityManagerUtils.flushCredentialCache(login,
				SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void mergeIdentityStatement(byte[] identityStatementData)
			throws TrustDomainNotFoundException, PermissionDeniedException,
			ArgumentIntegrityException, AttributeTypeNotFoundException {
		LOG.debug("merge identity statement");
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		this.credentialManager.mergeIdentityStatement(subject,
				identityStatementData);
	}
}
