/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.backend.bean;

import java.security.cert.X509Certificate;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.AlreadyRegisteredException;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DecodingException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.bean.AuthenticationStatement;
import net.link.safeonline.authentication.service.bean.IdentityStatement;
import net.link.safeonline.authentication.service.bean.IdentityStatementAttributes;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.device.backend.CredentialManager;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.entity.device.DeviceSubjectEntity;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.model.PkiProvider;
import net.link.safeonline.pkix.model.PkiProviderManager;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class CredentialManagerBean implements CredentialManager {

	private static final Log LOG = LogFactory
			.getLog(CredentialManagerBean.class);

	public static final String SECURITY_MESSAGE_SESSION_ID_MISMATCH = "Session ID mismatch";

	public static final String SECURITY_MESSAGE_APPLICATION_ID_MISMATCH = "Application ID mismatch";

	public static final String SECURITY_MESSAGE_USER_MISMATCH = "User mismatch";

	public static final String SECURITY_MESSAGE_OPERATION_MISMATCH = "Operation mismatch";

	@EJB
	private PkiProviderManager pkiProviderManager;

	@EJB
	private PkiValidator pkiValidator;

	@EJB
	private SubjectIdentifierDAO subjectIdentifierDAO;

	@EJB
	private SubjectService subjectService;

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private SecurityAuditLogger securityAuditLogger;

	public String authenticate(String sessionId, String applicationId,
			AuthenticationStatement authenticationStatement)
			throws ArgumentIntegrityException, TrustDomainNotFoundException,
			SubjectNotFoundException {
		X509Certificate certificate = authenticationStatement.verifyIntegrity();
		if (null == certificate) {
			throw new ArgumentIntegrityException();
		}

		String statementSessionId = authenticationStatement.getSessionId();
		String statementApplicationId = authenticationStatement
				.getApplicationId();

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
					SecurityThreatType.DECEPTION,
					SECURITY_MESSAGE_SESSION_ID_MISMATCH);
			throw new ArgumentIntegrityException();
		}

		if (false == applicationId.equals(statementApplicationId)) {
			this.securityAuditLogger.addSecurityAudit(
					SecurityThreatType.DECEPTION,
					SECURITY_MESSAGE_APPLICATION_ID_MISMATCH);
			throw new ArgumentIntegrityException();
		}

		String identifierDomainName = pkiProvider.getIdentifierDomainName();
		String identifier = pkiProvider.getSubjectIdentifier(certificate);
		SubjectEntity deviceRegistration = this.subjectIdentifierDAO
				.findSubject(identifierDomainName, identifier);
		if (null == deviceRegistration) {
			throw new SubjectNotFoundException();
		}
		DeviceSubjectEntity deviceSubject = this.subjectService
				.getDeviceSubject(deviceRegistration);
		return deviceSubject.getId();

	}

	public void mergeIdentityStatement(String sessionId, String deviceUserId,
			String operation, byte[] identityStatementData)
			throws TrustDomainNotFoundException, PermissionDeniedException,
			ArgumentIntegrityException, AttributeTypeNotFoundException,
			DeviceNotFoundException, AttributeNotFoundException,
			AlreadyRegisteredException {
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

		String statementSessionId = identityStatement.getSessionId();
		String statementOperation = identityStatement.getOperation();
		String statementUser = identityStatement.getUser();

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
		 * Check whether the identity statement properties are ok.
		 */
		if (false == deviceUserId.equals(statementUser)) {
			this.securityAuditLogger.addSecurityAudit(
					SecurityThreatType.DECEPTION,
					SECURITY_MESSAGE_USER_MISMATCH);
			throw new PermissionDeniedException(SECURITY_MESSAGE_USER_MISMATCH);
		}
		if (false == sessionId.equals(statementSessionId)) {
			this.securityAuditLogger.addSecurityAudit(
					SecurityThreatType.DECEPTION,
					SECURITY_MESSAGE_SESSION_ID_MISMATCH);
			throw new ArgumentIntegrityException();
		}
		if (false == operation.equals(statementOperation)) {
			this.securityAuditLogger.addSecurityAudit(
					SecurityThreatType.DECEPTION,
					SECURITY_MESSAGE_OPERATION_MISMATCH);
			throw new ArgumentIntegrityException();
		}

		SubjectEntity deviceRegistration;

		String domain = pkiProvider.getIdentifierDomainName();
		String identifier = pkiProvider.getSubjectIdentifier(certificate);
		SubjectEntity existingMappedSubject = this.subjectIdentifierDAO
				.findSubject(domain, identifier);
		if (null == existingMappedSubject) {
			/*
			 * Create new device subject if needed
			 */
			DeviceSubjectEntity deviceSubject = this.subjectService
					.findDeviceSubject(deviceUserId);
			if (null == deviceSubject) {
				deviceSubject = this.subjectService
						.addDeviceSubject(deviceUserId);
			}

			/*
			 * Create new device registration subject
			 */
			deviceRegistration = this.subjectService.addDeviceRegistration();
			deviceSubject.getRegistrations().add(deviceRegistration);

			/*
			 * In this case we register a new subject identifier within the
			 * system.
			 */
			this.subjectIdentifierDAO.addSubjectIdentifier(domain, identifier,
					deviceRegistration);
		} else {
			/*
			 * The certificate is already linked to another user.
			 */
			LOG.debug("device already registered");
			throw new AlreadyRegisteredException();
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
				identifier, deviceRegistration);

		/*
		 * Store some additional attributes retrieved from the identity
		 * statement.
		 */
		String surname = identityStatement.getSurname();
		String givenName = identityStatement.getGivenName();

		setOrUpdateAttribute(IdentityStatementAttributes.SURNAME,
				deviceRegistration, surname, pkiProvider);
		setOrUpdateAttribute(IdentityStatementAttributes.GIVEN_NAME,
				deviceRegistration, givenName, pkiProvider);

		pkiProvider.storeAdditionalAttributes(deviceRegistration, certificate);

		pkiProvider.storeDeviceAttribute(deviceRegistration);

		pkiProvider.storeDeviceUserAttribute(deviceRegistration);
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

	public void removeIdentity(String sessionId, String deviceUserId,
			String operation, byte[] identityStatementData)
			throws TrustDomainNotFoundException, PermissionDeniedException,
			ArgumentIntegrityException, AttributeTypeNotFoundException,
			SubjectNotFoundException, DeviceNotFoundException {
		DeviceSubjectEntity deviceSubject = this.subjectService
				.getDeviceSubject(deviceUserId);

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

		String statementSessionId = identityStatement.getSessionId();
		String statementOperation = identityStatement.getOperation();
		String statementUser = identityStatement.getUser();

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
		 * Check whether the identity statement properties are ok.
		 */
		if (false == deviceUserId.equals(statementUser)) {
			this.securityAuditLogger.addSecurityAudit(
					SecurityThreatType.DECEPTION,
					SECURITY_MESSAGE_USER_MISMATCH);
			throw new PermissionDeniedException(SECURITY_MESSAGE_USER_MISMATCH);
		}
		if (false == sessionId.equals(statementSessionId)) {
			this.securityAuditLogger.addSecurityAudit(
					SecurityThreatType.DECEPTION,
					SECURITY_MESSAGE_SESSION_ID_MISMATCH);
			throw new ArgumentIntegrityException();
		}
		if (false == operation.equals(statementOperation)) {
			this.securityAuditLogger.addSecurityAudit(
					SecurityThreatType.DECEPTION,
					SECURITY_MESSAGE_OPERATION_MISMATCH);
			throw new ArgumentIntegrityException();
		}

		String domain = pkiProvider.getIdentifierDomainName();
		String identifier = pkiProvider.getSubjectIdentifier(certificate);
		SubjectEntity existingMappedSubject = this.subjectIdentifierDAO
				.findSubject(domain, identifier);
		if (deviceSubject.getRegistrations().contains(existingMappedSubject)) {
			this.subjectIdentifierDAO.removeSubjectIdentifier(
					existingMappedSubject, domain, identifier);
			deviceSubject.getRegistrations().remove(existingMappedSubject);
		}

		removeAttribute(IdentityStatementAttributes.SURNAME,
				existingMappedSubject, pkiProvider);
		removeAttribute(IdentityStatementAttributes.GIVEN_NAME,
				existingMappedSubject, pkiProvider);

		pkiProvider.removeAdditionalAttributes(existingMappedSubject,
				certificate);

		pkiProvider.removeDeviceAttribute(existingMappedSubject);

		pkiProvider.removeDeviceUserAttribute(existingMappedSubject);
	}

	private void removeAttribute(
			IdentityStatementAttributes identityStatementAttribute,
			SubjectEntity subject, PkiProvider pkiProvider)
			throws AttributeTypeNotFoundException {
		String attributeName = pkiProvider
				.mapAttribute(identityStatementAttribute);
		AttributeTypeEntity attributeType = this.attributeTypeDAO
				.getAttributeType(attributeName);
		AttributeEntity attribute = this.attributeDAO.findAttribute(
				attributeType, subject);
		this.attributeDAO.removeAttribute(attribute);

	}
}
