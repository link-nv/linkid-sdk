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
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.PkiExpiredException;
import net.link.safeonline.authentication.exception.PkiInvalidException;
import net.link.safeonline.authentication.exception.PkiNotYetValidException;
import net.link.safeonline.authentication.exception.PkiRevokedException;
import net.link.safeonline.authentication.exception.PkiSuspendedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.bean.AuthenticationStatement;
import net.link.safeonline.authentication.service.bean.IdentityStatement;
import net.link.safeonline.authentication.service.bean.IdentityStatementAttributes;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.device.backend.CredentialManager;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.model.PkiProvider;
import net.link.safeonline.pkix.model.PkiProviderManager;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = CredentialManager.JNDI_BINDING)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class CredentialManagerBean implements CredentialManager {

    private static final Log     LOG                                      = LogFactory.getLog(CredentialManagerBean.class);

    public static final String   SECURITY_MESSAGE_SESSION_ID_MISMATCH     = "Session ID mismatch";

    public static final String   SECURITY_MESSAGE_APPLICATION_ID_MISMATCH = "Application ID mismatch";

    public static final String   SECURITY_MESSAGE_USER_MISMATCH           = "User mismatch";

    public static final String   SECURITY_MESSAGE_OPERATION_MISMATCH      = "Operation mismatch";

    @EJB(mappedName = PkiProviderManager.JNDI_BINDING)
    private PkiProviderManager   pkiProviderManager;

    @EJB(mappedName = PkiValidator.JNDI_BINDING)
    private PkiValidator         pkiValidator;

    @EJB(mappedName = SubjectIdentifierDAO.JNDI_BINDING)
    private SubjectIdentifierDAO subjectIdentifierDAO;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService       subjectService;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO     attributeTypeDAO;

    @EJB(mappedName = AttributeDAO.JNDI_BINDING)
    private AttributeDAO         attributeDAO;

    @EJB(mappedName = SecurityAuditLogger.JNDI_BINDING)
    private SecurityAuditLogger  securityAuditLogger;


    public String authenticate(String sessionId, String applicationId, AuthenticationStatement authenticationStatement)
            throws ArgumentIntegrityException, TrustDomainNotFoundException, SubjectNotFoundException, PkiRevokedException,
            PkiSuspendedException, PkiExpiredException, PkiNotYetValidException, PkiInvalidException, DeviceNotFoundException,
            DeviceDisabledException {

        X509Certificate certificate = authenticationStatement.verifyIntegrity();
        if (null == certificate)
            throw new ArgumentIntegrityException();

        String statementSessionId = authenticationStatement.getSessionId();
        String statementApplicationId = authenticationStatement.getApplicationId();

        PkiProvider pkiProvider = this.pkiProviderManager.findPkiProvider(certificate);
        if (null == pkiProvider)
            throw new ArgumentIntegrityException();
        TrustDomainEntity trustDomain = pkiProvider.getTrustDomain();
        PkiResult validationResult = this.pkiValidator.validateCertificate(trustDomain, certificate);
        if (PkiResult.REVOKED == validationResult)
            throw new PkiRevokedException();
        else if (PkiResult.SUSPENDED == validationResult)
            throw new PkiSuspendedException();
        else if (PkiResult.EXPIRED == validationResult)
            throw new PkiExpiredException();
        else if (PkiResult.NOT_YET_VALID == validationResult)
            throw new PkiNotYetValidException();
        else if (PkiResult.INVALID == validationResult)
            throw new PkiInvalidException();

        if (false == sessionId.equals(statementSessionId)) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_SESSION_ID_MISMATCH);
            throw new ArgumentIntegrityException();
        }

        if (false == applicationId.equals(statementApplicationId)) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_APPLICATION_ID_MISMATCH);
            throw new ArgumentIntegrityException();
        }

        String identifierDomainName = pkiProvider.getIdentifierDomainName();
        String identifier = pkiProvider.getSubjectIdentifier(certificate);
        SubjectEntity subject = this.subjectIdentifierDAO.findSubject(identifierDomainName, identifier);
        if (null == subject)
            throw new SubjectNotFoundException();
        if (pkiProvider.isDisabled(subject, certificate))
            throw new DeviceDisabledException();

        return subject.getUserId();

    }

    public void mergeIdentityStatement(String sessionId, String userId, String operation, byte[] identityStatementData)
            throws TrustDomainNotFoundException, PermissionDeniedException, ArgumentIntegrityException, AttributeTypeNotFoundException,
            DeviceNotFoundException, AttributeNotFoundException, AlreadyRegisteredException, PkiRevokedException, PkiSuspendedException,
            PkiExpiredException, PkiNotYetValidException, PkiInvalidException {

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
        if (null == certificate)
            throw new ArgumentIntegrityException();

        String statementSessionId = identityStatement.getSessionId();
        String statementOperation = identityStatement.getOperation();
        String statementUser = identityStatement.getUser();

        PkiProvider pkiProvider = this.pkiProviderManager.findPkiProvider(certificate);
        if (null == pkiProvider)
            throw new ArgumentIntegrityException();

        TrustDomainEntity trustDomain = pkiProvider.getTrustDomain();
        PkiResult validationResult = this.pkiValidator.validateCertificate(trustDomain, certificate);
        if (PkiResult.REVOKED == validationResult)
            throw new PkiRevokedException();
        else if (PkiResult.SUSPENDED == validationResult)
            throw new PkiSuspendedException();
        else if (PkiResult.EXPIRED == validationResult)
            throw new PkiExpiredException();
        else if (PkiResult.NOT_YET_VALID == validationResult)
            throw new PkiNotYetValidException();
        else if (PkiResult.INVALID == validationResult)
            throw new PkiInvalidException();

        /*
         * Check whether the identity statement properties are ok.
         */
        if (false == userId.equals(statementUser)) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_USER_MISMATCH);
            throw new PermissionDeniedException(SECURITY_MESSAGE_USER_MISMATCH);
        }
        if (false == sessionId.equals(statementSessionId)) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_SESSION_ID_MISMATCH);
            throw new ArgumentIntegrityException();
        }
        if (false == operation.equals(statementOperation)) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_OPERATION_MISMATCH);
            throw new ArgumentIntegrityException();
        }

        SubjectEntity subject;

        String domain = pkiProvider.getIdentifierDomainName();
        String identifier = pkiProvider.getSubjectIdentifier(certificate);
        SubjectEntity existingMappedSubject = this.subjectIdentifierDAO.findSubject(domain, identifier);
        if (null == existingMappedSubject) {
            /*
             * Create new subject if needed
             */
            subject = this.subjectService.findSubject(userId);
            if (null == subject) {
                subject = this.subjectService.addSubjectWithoutLogin(userId);
            }
            /*
             * In this case we register a new subject identifier within the system.
             */
            this.subjectIdentifierDAO.addSubjectIdentifier(domain, identifier, subject);
        } else {
            /*
             * The certificate is already linked to another user.
             */
            LOG.debug("device already registered");
            throw new AlreadyRegisteredException();
        }
        /*
         * The user can only have one subject identifier for the domain. We don't want the user to block identifiers of cards that he is no
         * longer using since there is the possibility that these cards are to be used by other subjects. Such a strategy of course only
         * makes sense for authentication devices for which a subject can have only one. This is for example the case for BeID identity
         * cards.
         */
        this.subjectIdentifierDAO.removeOtherSubjectIdentifiers(domain, identifier, subject);

        /*
         * Store the attributes retrieved from the identity statement.
         */
        long index = pkiProvider.listDeviceAttributes(subject).size();

        String surname = identityStatement.getSurname();
        String givenName = identityStatement.getGivenName();

        setOrUpdateAttribute(IdentityStatementAttributes.SURNAME, subject, surname, pkiProvider, index);
        setOrUpdateAttribute(IdentityStatementAttributes.GIVEN_NAME, subject, givenName, pkiProvider, index);

        pkiProvider.storeAdditionalAttributes(subject, certificate, index);

        pkiProvider.storeDeviceAttribute(subject, index);
    }

    private void setOrUpdateAttribute(IdentityStatementAttributes identityStatementAttribute, SubjectEntity subject, String value,
                                      PkiProvider pkiProvider, long index)
            throws AttributeTypeNotFoundException {

        String attributeName = pkiProvider.mapAttribute(identityStatementAttribute);
        AttributeTypeEntity attributeType = this.attributeTypeDAO.getAttributeType(attributeName);
        this.attributeDAO.addOrUpdateAttribute(attributeType, subject, index, value);
    }

    public void removeIdentity(String sessionId, String userId, String operation, byte[] identityStatementData)
            throws TrustDomainNotFoundException, PermissionDeniedException, ArgumentIntegrityException, AttributeTypeNotFoundException,
            SubjectNotFoundException, DeviceNotFoundException, PkiRevokedException, PkiSuspendedException, PkiExpiredException,
            PkiNotYetValidException, PkiInvalidException {

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
        if (null == certificate)
            throw new ArgumentIntegrityException();

        String statementSessionId = identityStatement.getSessionId();
        String statementOperation = identityStatement.getOperation();
        String statementUser = identityStatement.getUser();

        PkiProvider pkiProvider = this.pkiProviderManager.findPkiProvider(certificate);
        if (null == pkiProvider)
            throw new ArgumentIntegrityException();

        TrustDomainEntity trustDomain = pkiProvider.getTrustDomain();
        PkiResult validationResult = this.pkiValidator.validateCertificate(trustDomain, certificate);
        if (PkiResult.REVOKED == validationResult)
            throw new PkiRevokedException();
        else if (PkiResult.SUSPENDED == validationResult)
            throw new PkiSuspendedException();
        else if (PkiResult.EXPIRED == validationResult)
            throw new PkiExpiredException();
        else if (PkiResult.NOT_YET_VALID == validationResult)
            throw new PkiNotYetValidException();
        else if (PkiResult.INVALID == validationResult)
            throw new PkiInvalidException();

        /*
         * Check whether the identity statement properties are ok.
         */
        if (false == userId.equals(statementUser)) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_USER_MISMATCH);
            throw new PermissionDeniedException(SECURITY_MESSAGE_USER_MISMATCH);
        }
        if (false == sessionId.equals(statementSessionId)) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_SESSION_ID_MISMATCH);
            throw new ArgumentIntegrityException();
        }
        if (false == operation.equals(statementOperation)) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_OPERATION_MISMATCH);
            throw new ArgumentIntegrityException();
        }

        String domain = pkiProvider.getIdentifierDomainName();
        String identifier = pkiProvider.getSubjectIdentifier(certificate);
        SubjectEntity existingMappedSubject = this.subjectIdentifierDAO.findSubject(domain, identifier);
        this.subjectIdentifierDAO.removeSubjectIdentifier(existingMappedSubject, domain, identifier);

        // device attribute should contain as member all other device attributes so they are removed all at once.
        pkiProvider.removeDeviceAttribute(existingMappedSubject, certificate);
    }
}
