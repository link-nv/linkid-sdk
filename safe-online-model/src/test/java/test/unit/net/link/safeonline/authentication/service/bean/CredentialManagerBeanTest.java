/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.auth.AuthenticationStatementFactory;
import net.link.safeonline.authentication.exception.AlreadyRegisteredException;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.PkiInvalidException;
import net.link.safeonline.authentication.service.bean.AuthenticationStatement;
import net.link.safeonline.authentication.service.bean.IdentityStatementAttributes;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.device.backend.bean.CredentialManagerBean;
import net.link.safeonline.device.sdk.saml2.DeviceOperationType;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.identity.IdentityStatementFactory;
import net.link.safeonline.pkix.model.PkiProvider;
import net.link.safeonline.pkix.model.PkiProviderManager;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.shared.JceSigner;
import net.link.safeonline.shared.Signer;
import net.link.safeonline.shared.statement.IdentityProvider;
import net.link.safeonline.shared.statement.IdentityStatement;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;


public class CredentialManagerBeanTest extends TestCase {

    private CredentialManagerBean testedInstance;

    private AttributeDAO          mockAttributeDAO;

    private PkiProviderManager    mockPkiProviderManager;

    private PkiValidator          mockPkiValidator;

    private PkiProvider           mockPkiProvider;

    private Object[]              mockObjects;

    private String                testCallerLogin;

    private X509Certificate       certificate;

    private Signer                signer;

    private PrivateKey            privateKey;

    private SubjectEntity         testSubject;

    private SubjectIdentifierDAO  mockSubjectIdentifierDAO;

    private AttributeTypeDAO      mockAttributeTypeDAO;

    private SubjectService        mockSubjectService;

    private IdentityProvider      identityProvider;

    private SecurityAuditLogger   mockSecurityAuditLogger;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();

        this.testedInstance = new CredentialManagerBean();

        this.mockAttributeDAO = createMock(AttributeDAO.class);
        EJBTestUtils.inject(this.testedInstance, this.mockAttributeDAO);

        this.mockPkiProviderManager = createMock(PkiProviderManager.class);
        EJBTestUtils.inject(this.testedInstance, this.mockPkiProviderManager);

        this.mockPkiValidator = createMock(PkiValidator.class);
        EJBTestUtils.inject(this.testedInstance, this.mockPkiValidator);

        this.mockPkiProvider = createMock(PkiProvider.class);

        this.mockSubjectIdentifierDAO = createMock(SubjectIdentifierDAO.class);
        EJBTestUtils.inject(this.testedInstance, this.mockSubjectIdentifierDAO);

        this.mockAttributeTypeDAO = createMock(AttributeTypeDAO.class);
        EJBTestUtils.inject(this.testedInstance, this.mockAttributeTypeDAO);

        this.mockSubjectService = createMock(SubjectService.class);
        EJBTestUtils.inject(this.testedInstance, this.mockSubjectService);

        this.mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);
        EJBTestUtils.inject(this.testedInstance, this.mockSecurityAuditLogger);

        this.mockObjects = new Object[] { this.mockAttributeDAO, this.mockPkiProviderManager, this.mockPkiValidator, this.mockPkiProvider,
                this.mockSubjectIdentifierDAO, this.mockAttributeTypeDAO, this.mockSubjectService, this.mockSecurityAuditLogger };

        EJBTestUtils.init(this.testedInstance);

        // stubs
        this.testCallerLogin = "test-caller-login-" + getName();
        this.testSubject = new SubjectEntity(this.testCallerLogin);

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");
        this.signer = new JceSigner(this.privateKey, this.certificate);
        expect(this.mockPkiProviderManager.findPkiProvider(this.certificate)).andStubReturn(this.mockPkiProvider);

        this.identityProvider = new IdentityProvider() {

            public String getGivenName() {

                return "test-given-name";
            }

            public String getSurname() {

                return "test-surname";
            }
        };
    }

    public void testAuthenticateViaAuthenticationStatement()
            throws Exception {

        // setup
        String sessionId = UUID.randomUUID().toString();
        String applicationId = "test-application-id";
        TrustDomainEntity trustDomain = new TrustDomainEntity("test-trust-domain", true);
        String identifierDomain = "test-identifier-domain";
        String identifier = "test-identifier";

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        byte[] authenticationStatementData = AuthenticationStatementFactory.createAuthenticationStatement(sessionId, applicationId,
                this.signer);
        AuthenticationStatement authenticationStatement = new AuthenticationStatement(authenticationStatementData);

        // stubs
        expect(this.mockPkiProviderManager.findPkiProvider(this.certificate)).andStubReturn(this.mockPkiProvider);
        expect(this.mockPkiProvider.getTrustDomain()).andStubReturn(trustDomain);
        expect(this.mockPkiValidator.validateCertificate(trustDomain, this.certificate)).andStubReturn(PkiResult.VALID);
        expect(this.mockPkiProvider.getIdentifierDomainName()).andStubReturn(identifierDomain);
        expect(this.mockPkiProvider.getSubjectIdentifier(this.certificate)).andStubReturn(identifier);
        expect(this.mockSubjectIdentifierDAO.findSubject(identifierDomain, identifier)).andStubReturn(subject);
        expect(this.mockPkiProvider.isDisabled(subject, this.certificate)).andStubReturn(false);

        // prepare
        replay(this.mockObjects);

        // operate
        String resultUserId = this.testedInstance.authenticate(sessionId, applicationId, authenticationStatement);

        // verify
        verify(this.mockObjects);
        assertNotNull(resultUserId);
        assertEquals(userId, resultUserId);
    }

    public void testUnparsableIdentityStatement()
            throws Exception {

        // setup
        byte[] identityStatement = "foobar-identity-statemennt".getBytes();

        String sessionId = UUID.randomUUID().toString();
        String deviceUserId = UUID.randomUUID().toString();
        String operation = DeviceOperationType.REGISTER.name();

        // prepare
        replay(this.mockObjects);

        // operate
        try {
            this.testedInstance.mergeIdentityStatement(sessionId, deviceUserId, operation, identityStatement);
            fail();
        } catch (ArgumentIntegrityException e) {
            // expected
        }

        // verify
        verify(this.mockObjects);
    }

    public void testMergeIdentityStatement()
            throws Exception {

        // setup
        String sessionId = UUID.randomUUID().toString();
        String operation = DeviceOperationType.REGISTER.name();
        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        byte[] identityStatement = IdentityStatementFactory.createIdentityStatement(sessionId, userId, operation, this.signer,
                this.identityProvider);
        TrustDomainEntity trustDomain = new TrustDomainEntity("test-trust-domain", true);
        String surnameAttribute = "test-surname-attribute";
        String givenNameAttribute = "test-given-name-attribute";
        String identifierDomain = "test-identifier-domain";
        String identifier = "test-identifier";

        // stubs
        expect(this.mockPkiProvider.getTrustDomain()).andStubReturn(trustDomain);

        expect(this.mockPkiValidator.validateCertificate(trustDomain, this.certificate)).andStubReturn(PkiResult.VALID);

        expect(this.mockPkiProvider.listDeviceAttributes(subject)).andStubReturn(new LinkedList<AttributeEntity>());

        expect(this.mockPkiProvider.mapAttribute(IdentityStatementAttributes.SURNAME)).andStubReturn(surnameAttribute);
        expect(this.mockAttributeDAO.findAttribute(surnameAttribute, this.testSubject)).andStubReturn(null);

        expect(this.mockPkiProvider.mapAttribute(IdentityStatementAttributes.GIVEN_NAME)).andStubReturn(givenNameAttribute);
        expect(this.mockAttributeDAO.findAttribute(givenNameAttribute, this.testSubject)).andStubReturn(null);
        expect(this.mockPkiProvider.getIdentifierDomainName()).andStubReturn(identifierDomain);
        expect(this.mockPkiProvider.getSubjectIdentifier(this.certificate)).andStubReturn(identifier);
        expect(this.mockSubjectIdentifierDAO.findSubject(identifierDomain, identifier)).andStubReturn(null);
        this.mockPkiProvider.storeAdditionalAttributes(subject, this.certificate, 0);
        this.mockPkiProvider.storeDeviceAttribute(subject, 0);

        AttributeTypeEntity surnameAttributeType = new AttributeTypeEntity();
        expect(this.mockAttributeTypeDAO.getAttributeType(surnameAttribute)).andStubReturn(surnameAttributeType);
        AttributeTypeEntity givenNameAttributeType = new AttributeTypeEntity();
        expect(this.mockAttributeTypeDAO.getAttributeType(givenNameAttribute)).andStubReturn(givenNameAttributeType);

        expect(this.mockSubjectService.findSubject(userId)).andReturn(null);
        expect(this.mockSubjectService.addSubjectWithoutLogin(userId)).andReturn(subject);

        // expectations
        this.mockAttributeDAO.addOrUpdateAttribute(surnameAttributeType, subject, 0, this.identityProvider.getSurname());
        this.mockAttributeDAO.addOrUpdateAttribute(givenNameAttributeType, subject, 0, this.identityProvider.getGivenName());
        this.mockSubjectIdentifierDAO.addSubjectIdentifier(identifierDomain, identifier, subject);
        this.mockSubjectIdentifierDAO.removeOtherSubjectIdentifiers(identifierDomain, identifier, subject);

        // prepare
        replay(this.mockObjects);

        // operate
        this.testedInstance.mergeIdentityStatement(sessionId, userId, operation, identityStatement);

        // verify
        verify(this.mockObjects);
    }

    public void testMergeIdentityStatementFailsIfAnotherSubjectAlreadyRegisteredTheCert()
            throws Exception {

        // setup
        String sessionId = UUID.randomUUID().toString();
        String operation = DeviceOperationType.REGISTER.name();
        String userId = UUID.randomUUID().toString();

        byte[] identityStatement = IdentityStatementFactory.createIdentityStatement(sessionId, userId, operation, this.signer,
                this.identityProvider);
        TrustDomainEntity trustDomain = new TrustDomainEntity("test-trust-domain", true);
        String surnameAttribute = "test-surname-attribute";
        String givenNameAttribute = "test-given-name-attribute";
        String identifierDomain = "test-identifier-domain";
        String identifier = "test-identifier";
        SubjectEntity anotherSubject = new SubjectEntity("another-subject");

        // stubs
        expect(this.mockPkiProvider.getTrustDomain()).andStubReturn(trustDomain);

        expect(this.mockPkiValidator.validateCertificate(trustDomain, this.certificate)).andStubReturn(PkiResult.VALID);

        expect(this.mockPkiProvider.mapAttribute(IdentityStatementAttributes.SURNAME)).andStubReturn(surnameAttribute);
        expect(this.mockAttributeDAO.findAttribute(surnameAttribute, this.testSubject)).andStubReturn(null);

        expect(this.mockPkiProvider.mapAttribute(IdentityStatementAttributes.GIVEN_NAME)).andStubReturn(givenNameAttribute);
        expect(this.mockAttributeDAO.findAttribute(givenNameAttribute, this.testSubject)).andStubReturn(null);
        expect(this.mockPkiProvider.getIdentifierDomainName()).andStubReturn(identifierDomain);
        expect(this.mockPkiProvider.getSubjectIdentifier(this.certificate)).andStubReturn(identifier);
        expect(this.mockSubjectIdentifierDAO.findSubject(identifierDomain, identifier)).andStubReturn(anotherSubject);

        // prepare
        replay(this.mockObjects);

        // operate & verify
        try {
            this.testedInstance.mergeIdentityStatement(sessionId, userId, operation, identityStatement);
            fail();
        } catch (AlreadyRegisteredException e) {
            // expected
            verify(this.mockObjects);
        }
    }

    public void testMergeIdentityStatementFailsIfLoginAndUserDoNotCorrespond()
            throws Exception {

        // setup
        String sessionId = UUID.randomUUID().toString();
        String operation = DeviceOperationType.REGISTER.name();
        String user = "foobar-test-user";
        byte[] identityStatement = IdentityStatementFactory.createIdentityStatement(sessionId, user, operation, this.signer,
                this.identityProvider);
        TrustDomainEntity trustDomain = new TrustDomainEntity("test-trust-domain", true);

        // stubs
        expect(this.mockPkiProvider.getTrustDomain()).andStubReturn(trustDomain);
        expect(this.mockPkiValidator.validateCertificate(trustDomain, this.certificate)).andStubReturn(PkiResult.VALID);
        this.mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, CredentialManagerBean.SECURITY_MESSAGE_USER_MISMATCH);

        // prepare
        replay(this.mockObjects);

        // operate & verify
        try {
            this.testedInstance.mergeIdentityStatement(sessionId, this.testSubject.getUserId(), operation, identityStatement);
            fail();
        } catch (PermissionDeniedException e) {
            // expected
            verify(this.mockObjects);
        }
    }

    public void testMergeIdentityStatementFailsIfSessionIdIsInvalid()
            throws Exception {

        // setup
        String sessionId = UUID.randomUUID().toString();
        String wrongSessionId = "wrong-session-id";
        String operation = DeviceOperationType.REGISTER.name();
        String userId = UUID.randomUUID().toString();
        byte[] identityStatement = IdentityStatementFactory.createIdentityStatement(wrongSessionId, userId, operation, this.signer,
                this.identityProvider);
        TrustDomainEntity trustDomain = new TrustDomainEntity("test-trust-domain", true);

        // stubs
        expect(this.mockPkiProvider.getTrustDomain()).andStubReturn(trustDomain);
        expect(this.mockPkiValidator.validateCertificate(trustDomain, this.certificate)).andStubReturn(PkiResult.VALID);
        this.mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION,
                CredentialManagerBean.SECURITY_MESSAGE_SESSION_ID_MISMATCH);

        // prepare
        replay(this.mockObjects);

        // operate & verify
        try {
            this.testedInstance.mergeIdentityStatement(sessionId, userId, operation, identityStatement);
            fail();
        } catch (ArgumentIntegrityException e) {
            // expected
            verify(this.mockObjects);
        }
    }

    public void testMergeIdentityStatementFailsIfOperationIdIsInvalid()
            throws Exception {

        // setup
        String sessionId = UUID.randomUUID().toString();
        String operation = DeviceOperationType.REGISTER.name();
        String wrongOperation = "wrong-operation";
        String userId = UUID.randomUUID().toString();
        byte[] identityStatement = IdentityStatementFactory.createIdentityStatement(sessionId, userId, wrongOperation, this.signer,
                this.identityProvider);
        TrustDomainEntity trustDomain = new TrustDomainEntity("test-trust-domain", true);

        // stubs
        expect(this.mockPkiProvider.getTrustDomain()).andStubReturn(trustDomain);
        expect(this.mockPkiValidator.validateCertificate(trustDomain, this.certificate)).andStubReturn(PkiResult.VALID);
        this.mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION,
                CredentialManagerBean.SECURITY_MESSAGE_OPERATION_MISMATCH);

        // prepare
        replay(this.mockObjects);

        // operate & verify
        try {
            this.testedInstance.mergeIdentityStatement(sessionId, userId, operation, identityStatement);
            fail();
        } catch (ArgumentIntegrityException e) {
            // expected
            verify(this.mockObjects);
        }
    }

    public void testMergeIdentityStatementFailsIfNotSignedByClaimedAuthCert()
            throws Exception {

        // setup
        String sessionId = UUID.randomUUID().toString();
        String userId = "test-user";
        String operation = DeviceOperationType.REGISTER.name();

        KeyPair otherKeyPair = PkiTestUtils.generateKeyPair();
        Signer otherSigner = new JceSigner(otherKeyPair.getPrivate(), this.certificate);

        IdentityStatement identityStatement = new IdentityStatement(sessionId, userId, operation, this.identityProvider, otherSigner);
        byte[] identityStatementData = identityStatement.generateStatement();

        TrustDomainEntity trustDomain = new TrustDomainEntity("test-trust-domain", true);

        // stubs
        expect(this.mockPkiValidator.validateCertificate(trustDomain, this.certificate)).andStubReturn(PkiResult.VALID);

        // prepare
        replay(this.mockObjects);

        // operate & verify
        try {
            this.testedInstance.mergeIdentityStatement(sessionId, userId, operation, identityStatementData);
            fail();
        } catch (ArgumentIntegrityException e) {
            // expected
            verify(this.mockObjects);
        }
    }

    public void testMergeIdentityStatementFailsIfCertNotTrusted()
            throws Exception {

        // setup
        String sessionId = UUID.randomUUID().toString();
        String userId = "test-user";
        String operation = DeviceOperationType.REGISTER.name();

        byte[] identityStatement = IdentityStatementFactory.createIdentityStatement(sessionId, userId, operation, this.signer,
                this.identityProvider);
        TrustDomainEntity trustDomain = new TrustDomainEntity("test-trust-domain", true);

        // stubs
        expect(this.mockPkiProvider.getTrustDomain()).andStubReturn(trustDomain);
        expect(this.mockPkiValidator.validateCertificate(trustDomain, this.certificate)).andStubReturn(PkiResult.INVALID);

        // prepare
        replay(this.mockObjects);

        // operate
        try {
            this.testedInstance.mergeIdentityStatement(sessionId, userId, operation, identityStatement);
            fail();
        } catch (PkiInvalidException e) {
            // expected
            verify(this.mockObjects);
        }
    }
}
