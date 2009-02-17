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
import net.link.safeonline.authentication.exception.DecodingException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.PkiExpiredException;
import net.link.safeonline.authentication.exception.PkiInvalidException;
import net.link.safeonline.authentication.exception.PkiNotYetValidException;
import net.link.safeonline.authentication.exception.PkiRevokedException;
import net.link.safeonline.authentication.exception.PkiSuspendedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationStatement;
import net.link.safeonline.authentication.service.IdentityStatement;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.device.backend.CredentialManager;
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

    @EJB(mappedName = SecurityAuditLogger.JNDI_BINDING)
    private SecurityAuditLogger  securityAuditLogger;


    public String authenticate(String sessionId, String applicationId, AuthenticationStatement authenticationStatement)
            throws ArgumentIntegrityException, TrustDomainNotFoundException, SubjectNotFoundException, PkiRevokedException,
            PkiSuspendedException, PkiExpiredException, PkiNotYetValidException, PkiInvalidException, DeviceDisabledException,
            DeviceRegistrationNotFoundException {

        X509Certificate certificate = authenticationStatement.verifyIntegrity();
        if (null == certificate)
            throw new ArgumentIntegrityException();

        String statementSessionId = authenticationStatement.getSessionId();
        String statementApplicationId = authenticationStatement.getApplicationId();

        PkiProvider pkiProvider = pkiProviderManager.findPkiProvider(certificate);
        if (null == pkiProvider)
            throw new ArgumentIntegrityException();
        TrustDomainEntity trustDomain = pkiProvider.getTrustDomain();
        PkiResult validationResult = pkiValidator.validateCertificate(trustDomain, certificate);
        switch (validationResult) {
            case REVOKED:
                throw new PkiRevokedException();
            case SUSPENDED:
                throw new PkiSuspendedException();
            case EXPIRED:
                throw new PkiExpiredException();
            case NOT_YET_VALID:
                throw new PkiNotYetValidException();
            case INVALID:
                throw new PkiInvalidException();
            case VALID:
        }

        if (false == sessionId.equals(statementSessionId)) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_SESSION_ID_MISMATCH);
            throw new ArgumentIntegrityException();
        }

        if (false == applicationId.equals(statementApplicationId)) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_APPLICATION_ID_MISMATCH);
            throw new ArgumentIntegrityException();
        }

        String identifierDomainName = pkiProvider.getIdentifierDomainName();
        String identifier = pkiProvider.parseIdentifierFromCert(certificate);
        SubjectEntity subject = subjectIdentifierDAO.findSubject(identifierDomainName, identifier);
        if (null == subject)
            throw new SubjectNotFoundException();
        if (pkiProvider.isDisabled(subject, certificate))
            throw new DeviceDisabledException();

        return subject.getUserId();
    }

    public void mergeIdentityStatement(String sessionId, String userId, String operation, byte[] identityStatementData)
            throws TrustDomainNotFoundException, PermissionDeniedException, ArgumentIntegrityException, AlreadyRegisteredException,
            PkiRevokedException, PkiSuspendedException, PkiExpiredException, PkiNotYetValidException, PkiInvalidException {

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

        PkiProvider pkiProvider = pkiProviderManager.findPkiProvider(certificate);
        if (null == pkiProvider)
            throw new ArgumentIntegrityException();

        TrustDomainEntity trustDomain = pkiProvider.getTrustDomain();
        PkiResult validationResult = pkiValidator.validateCertificate(trustDomain, certificate);
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
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_USER_MISMATCH);
            throw new PermissionDeniedException(SECURITY_MESSAGE_USER_MISMATCH);
        }
        if (false == sessionId.equals(statementSessionId)) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_SESSION_ID_MISMATCH);
            throw new ArgumentIntegrityException();
        }
        if (false == operation.equals(statementOperation)) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_OPERATION_MISMATCH);
            throw new ArgumentIntegrityException();
        }

        SubjectEntity subject;

        String domain = pkiProvider.getIdentifierDomainName();
        String identifier = pkiProvider.parseIdentifierFromCert(certificate);
        SubjectEntity existingMappedSubject = subjectIdentifierDAO.findSubject(domain, identifier);
        if (null == existingMappedSubject) {
            /*
             * Create new subject if needed
             */
            subject = subjectService.findSubject(userId);
            if (null == subject) {
                subject = subjectService.addSubjectWithoutLogin(userId);
            }
            /*
             * In this case we register a new subject identifier within the system.
             */
            subjectIdentifierDAO.addSubjectIdentifier(domain, identifier, subject);
        } else {
            /*
             * The certificate is already linked to another user.
             */
            LOG.debug("device already registered");
            throw new AlreadyRegisteredException();
        }

        String surname = identityStatement.getSurname();
        String givenName = identityStatement.getGivenName();

        pkiProvider.storeDeviceAttributes(subject, surname, givenName, certificate);
    }

    public void enable(String sessionId, String userId, String operation, byte[] identityStatementData)
            throws TrustDomainNotFoundException, PermissionDeniedException, ArgumentIntegrityException, SubjectNotFoundException,
            PkiRevokedException, PkiSuspendedException, PkiExpiredException, PkiNotYetValidException, PkiInvalidException,
            DeviceRegistrationNotFoundException {

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

        PkiProvider pkiProvider = pkiProviderManager.findPkiProvider(certificate);
        if (null == pkiProvider)
            throw new ArgumentIntegrityException();

        TrustDomainEntity trustDomain = pkiProvider.getTrustDomain();
        PkiResult validationResult = pkiValidator.validateCertificate(trustDomain, certificate);
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
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_USER_MISMATCH);
            throw new PermissionDeniedException(SECURITY_MESSAGE_USER_MISMATCH);
        }
        if (false == sessionId.equals(statementSessionId)) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_SESSION_ID_MISMATCH);
            throw new ArgumentIntegrityException();
        }
        if (false == operation.equals(statementOperation)) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_OPERATION_MISMATCH);
            throw new ArgumentIntegrityException();
        }

        String domain = pkiProvider.getIdentifierDomainName();
        String identifier = pkiProvider.parseIdentifierFromCert(certificate);
        SubjectEntity existingMappedSubject = subjectIdentifierDAO.findSubject(domain, identifier);

        pkiProvider.enable(existingMappedSubject, certificate);
    }
}
