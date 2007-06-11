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
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.TrustDomainNotFoundException;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.authentication.service.CredentialServiceRemote;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.model.PkiProvider;
import net.link.safeonline.model.PkiProviderManager;
import net.link.safeonline.model.PkiValidator;
import net.link.safeonline.model.SubjectManager;
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
	private AttributeTypeDAO attributeTypeDAO;

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
			ArgumentIntegrityException, AttributeTypeNotFoundException {
		LOG.debug("merge identity statement");
		String login = this.subjectManager.getCallerLogin();
		LOG.debug("login: " + login);

		/*
		 * First check integrity of the received identity statement.
		 */
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

		/*
		 * Check whether the identity statement is owned by the authenticated
		 * user.
		 */
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
		/*
		 * The user can only have one subject identifier for the domain. We
		 * don't want the user to block identifiers of cards that he is no
		 * longer using since there is the possibility that these cards are to
		 * be used by other subjects. Such a strategy of course only makes sense
		 * for authentication devices for which a subject can have only one.
		 * This is for example the case for BeID identity cards.
		 */
		this.subjectIdentifierDAO.removeOtherSubjectIdentifiers(domain,
				identifier, subject);

		/*
		 * Store some additional attributes retrieved from the identity
		 * statement.
		 */
		String surname = identityStatement.getSurname();
		String givenName = identityStatement.getGivenName();

		setOrUpdateAttribute(IdentityStatementAttributes.SURNAME, subject,
				surname, pkiProvider);
		setOrUpdateAttribute(IdentityStatementAttributes.GIVEN_NAME, subject,
				givenName, pkiProvider);

		pkiProvider.storeAdditionalAttributes(certificate);
	}

	private void setOrUpdateAttribute(
			IdentityStatementAttributes identityStatementAttribute,
			SubjectEntity subject, String value, PkiProvider pkiProvider)
			throws AttributeTypeNotFoundException {
		String attributeName = pkiProvider
				.mapAttribute(identityStatementAttribute);
		AttributeTypeEntity attributeType = this.attributeTypeDAO
				.getAttributeType(attributeName);
		this.attributeDAO
				.addOrUpdateAttribute(attributeType, subject, 0, value);
	}
}
