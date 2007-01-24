/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.security.cert.X509Certificate;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.model.PkiProviderManager;
import net.link.safeonline.model.PkiValidator;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.util.ee.SecurityManagerUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class CredentialServiceBean implements CredentialService {

	private static Log LOG = LogFactory.getLog(CredentialServiceBean.class);

	@EJB
	private SubjectManager subjectManager;

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private PkiProviderManager pkiProviderManager;

	@EJB
	private PkiValidator pkiValidator;

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
	public void mergeIdentityStatement(byte[] identityStatementData) {
		LOG.debug("merge identity statement");
		String login = this.subjectManager.getCallerLogin();
		LOG.debug("login: " + login);

		IdentityStatement identityStatement = new IdentityStatement(
				identityStatementData);

		X509Certificate certificate = identityStatement.verifyIntegrity();
		TrustDomainEntity trustDomain = this.pkiProviderManager
				.findTrustDomain(certificate);
		if (null == trustDomain) {
			throw new IllegalArgumentException("no matching trust domain found");
		}
		boolean validationResult = this.pkiValidator.validateCertificate(
				trustDomain, certificate);
		if (false == validationResult) {
			throw new IllegalArgumentException(
					"certificate not found to be valid");
		}

		String surname = identityStatement.getSurname();
		String givenName = identityStatement.getGivenName();

		setOrOverrideAttribute(SafeOnlineConstants.SURNAME_ATTRIBUTE, login,
				surname);
		setOrOverrideAttribute(SafeOnlineConstants.GIVENNAME_ATTRIBUTE, login,
				givenName);
	}

	private void setOrOverrideAttribute(String attributeName, String login,
			String value) {
		AttributeEntity attribute = this.attributeDAO.findAttribute(
				attributeName, login);
		if (null == attribute) {
			this.attributeDAO.addAttribute(attributeName, login, value);
		} else {
			attribute.setStringValue(value);
		}
	}
}
