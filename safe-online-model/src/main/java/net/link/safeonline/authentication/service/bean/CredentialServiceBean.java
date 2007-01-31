/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.security.cert.X509Certificate;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.TrustDomainNotFoundException;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.model.PkiProvider;
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

	@EJB
	private SubjectIdentifierDAO subjectIdentifierDAO;

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
			ArgumentIntegrityException {
		LOG.debug("merge identity statement");
		String login = this.subjectManager.getCallerLogin();
		LOG.debug("login: " + login);

		IdentityStatement identityStatement;
		try {
			identityStatement = new IdentityStatement(identityStatementData);
		} catch (IllegalArgumentException e) {
			throw new ArgumentIntegrityException();
		}

		X509Certificate certificate = identityStatement.verifyIntegrity();
		if (null == certificate) {
			throw new ArgumentIntegrityException();
		}

		PkiProvider pkiProvider = this.pkiProviderManager
				.findPkiProvider(certificate);
		if (null == pkiProvider) {
			throw new ArgumentIntegrityException();
		}

		TrustDomainEntity trustDomain = pkiProvider.getTrustDomain();
		boolean validationResult = this.pkiValidator.validateCertificate(
				trustDomain, certificate);
		if (false == validationResult) {
			throw new ArgumentIntegrityException();
		}

		String user = identityStatement.getUser();
		if (false == login.equals(user)) {
			throw new PermissionDeniedException();
		}

		SubjectEntity subject = this.subjectManager.getCallerSubject();
		String domain = pkiProvider.getIdentifierDomainName();
		String identifier = pkiProvider.getSubjectIdentifier(certificate);
		SubjectEntity existingMappedSubject = this.subjectIdentifierDAO
				.findSubject(domain, identifier);
		if (null == existingMappedSubject) {
			/*
			 * In this case we register a new subject identifier within the
			 * system.
			 */
			this.subjectIdentifierDAO.addSubjectIdentifier(domain, identifier,
					subject);
		} else if (false == subject.equals(existingMappedSubject)) {
			/*
			 * The certificate is already linked to another user.
			 */
			throw new PermissionDeniedException();
		}

		String surname = identityStatement.getSurname();
		String givenName = identityStatement.getGivenName();

		setOrOverrideAttribute(IdentityStatementAttributes.SURNAME, login,
				surname, pkiProvider);
		setOrOverrideAttribute(IdentityStatementAttributes.GIVEN_NAME, login,
				givenName, pkiProvider);
	}

	private void setOrOverrideAttribute(
			IdentityStatementAttributes identityStatementAttribute,
			String login, String value, PkiProvider pkiProvider) {
		String attributeName = pkiProvider
				.mapAttribute(identityStatementAttribute);
		AttributeEntity attribute = this.attributeDAO.findAttribute(
				attributeName, login);
		if (null == attribute) {
			this.attributeDAO.addAttribute(attributeName, login, value);
		} else {
			attribute.setStringValue(value);
		}
	}
}
