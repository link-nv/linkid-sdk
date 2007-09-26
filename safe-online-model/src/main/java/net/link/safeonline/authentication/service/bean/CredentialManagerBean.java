/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.security.cert.X509Certificate;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DecodingException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.CredentialManager;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.model.PkiProvider;
import net.link.safeonline.pkix.model.PkiProviderManager;
import net.link.safeonline.pkix.model.PkiValidator;

@Stateless
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class CredentialManagerBean implements CredentialManager {

	@EJB
	private PkiProviderManager pkiProviderManager;

	@EJB
	private PkiValidator pkiValidator;

	@EJB
	private SubjectIdentifierDAO subjectIdentifierDAO;

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	@EJB
	private AttributeDAO attributeDAO;

	public void mergeIdentityStatement(SubjectEntity subject,
			byte[] identityStatementData) throws TrustDomainNotFoundException,
			PermissionDeniedException, ArgumentIntegrityException,
			AttributeTypeNotFoundException {
		String login = subject.getLogin();

		/*
		 * First check integrity of the received identity statement.
		 */
		IdentityStatement identityStatement;
		try {
			identityStatement = new IdentityStatement(identityStatementData);
		} catch (DecodingException e) {
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
			throw new PermissionDeniedException("statement user mismatch");
		}

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
			throw new PermissionDeniedException("certificate already in use");
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

		pkiProvider.storeAdditionalAttributes(subject, certificate);
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
