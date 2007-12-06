/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.bean;

import java.security.cert.X509Certificate;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.authentication.service.bean.AuthenticationStatement;
import net.link.safeonline.authentication.service.bean.RegistrationStatement;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.device.BeIdDeviceService;
import net.link.safeonline.device.BeIdDeviceServiceRemote;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.model.UserRegistrationManager;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.model.PkiProvider;
import net.link.safeonline.pkix.model.PkiProviderManager;
import net.link.safeonline.pkix.model.PkiValidator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class BeIdDeviceServiceBean implements BeIdDeviceService,
		BeIdDeviceServiceRemote {

	private final static Log LOG = LogFactory
			.getLog(BeIdDeviceServiceBean.class);

	@EJB
	private PkiProviderManager pkiProviderManager;

	@EJB
	private PkiValidator pkiValidator;

	@EJB
	private CredentialService credentialService;

	@EJB
	private UserRegistrationManager userRegistrationManager;

	@EJB
	private SubjectIdentifierDAO subjectIdentifierDAO;

	@EJB
	private SecurityAuditLogger securityAuditLogger;

	public SubjectEntity authenticate(String sessionId,
			AuthenticationStatement authenticationStatement)
			throws ArgumentIntegrityException, TrustDomainNotFoundException,
			SubjectNotFoundException {
		LOG.debug("authenticate session: " + sessionId);

		X509Certificate certificate = authenticationStatement.verifyIntegrity();
		if (null == certificate) {
			throw new ArgumentIntegrityException();
		}

		String statementSessionId = authenticationStatement.getSessionId();

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

		if (false == sessionId.equals(statementSessionId)) {
			this.securityAuditLogger.addSecurityAudit(
					SecurityThreatType.DECEPTION, "session Id mismatch");
			throw new ArgumentIntegrityException();
		}

		String identifierDomainName = pkiProvider.getIdentifierDomainName();
		String identifier = pkiProvider.getSubjectIdentifier(certificate);
		SubjectEntity subject = this.subjectIdentifierDAO.findSubject(
				identifierDomainName, identifier);
		if (null == subject) {
			String event = "no subject was found for the given user certificate";
			LOG.warn(event);
			throw new SubjectNotFoundException();
		}
		LOG.debug("subject: " + subject);

		return subject;
	}

	public void register(byte[] identityStatementData)
			throws TrustDomainNotFoundException, PermissionDeniedException,
			ArgumentIntegrityException, AttributeTypeNotFoundException {
		LOG.debug("register");
		this.credentialService.mergeIdentityStatement(identityStatementData);
	}

	public SubjectEntity registerAndAuthenticate(String sessionId,
			String username, RegistrationStatement registrationStatement)
			throws ArgumentIntegrityException, ExistingUserException,
			AttributeTypeNotFoundException, TrustDomainNotFoundException {
		LOG.debug("register and authenticate");

		X509Certificate certificate = registrationStatement.verifyIntegrity();
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

		String statementSessionId = registrationStatement.getSessionId();
		if (false == sessionId.equals(statementSessionId)) {
			this.securityAuditLogger.addSecurityAudit(
					SecurityThreatType.DECEPTION, "session Id mismatch");
			throw new ArgumentIntegrityException();
		}

		String statementUsername = registrationStatement.getUsername();
		if (false == username.equals(statementUsername)) {
			this.securityAuditLogger.addSecurityAudit(
					SecurityThreatType.DECEPTION, "username mismatch");
			throw new ArgumentIntegrityException();
		}

		String domain = pkiProvider.getIdentifierDomainName();
		String identifier = pkiProvider.getSubjectIdentifier(certificate);
		SubjectEntity existingMappedSubject = this.subjectIdentifierDAO
				.findSubject(domain, identifier);
		if (null != existingMappedSubject) {
			throw new ArgumentIntegrityException();
		}

		SubjectEntity subject = this.userRegistrationManager
				.registerUser(username);
		this.subjectIdentifierDAO.addSubjectIdentifier(domain, identifier,
				subject);

		pkiProvider.storeAdditionalAttributes(subject, certificate);
		return subject;
	}

	public void remove(byte[] identityStatementData)
			throws TrustDomainNotFoundException, PermissionDeniedException,
			ArgumentIntegrityException, AttributeTypeNotFoundException {
		LOG.debug("remove");
		this.credentialService.removeIdentity(identityStatementData);
	}

	public void update(byte[] identityStatementData)
			throws TrustDomainNotFoundException, PermissionDeniedException,
			ArgumentIntegrityException, AttributeTypeNotFoundException {
		LOG.debug("update");
		this.credentialService.mergeIdentityStatement(identityStatementData);
	}

}
