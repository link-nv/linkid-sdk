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
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.AlreadyRegisteredException;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.PkiInvalidException;
import net.link.safeonline.authentication.service.AuthenticationStatement;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.device.backend.bean.CredentialManagerBean;
import net.link.safeonline.device.sdk.saml2.DeviceOperationType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.pkix.model.PkiProvider;
import net.link.safeonline.pkix.model.PkiProviderManager;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.sc.pkcs11.auth.AuthenticationStatementFactory;
import net.link.safeonline.sc.pkcs11.identity.IdentityStatementFactory;
import net.link.safeonline.service.NodeMappingService;
import net.link.safeonline.shared.JceSigner;
import net.link.safeonline.shared.Signer;
import net.link.safeonline.shared.statement.IdentityProvider;
import net.link.safeonline.shared.statement.IdentityStatement;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;


public class CredentialManagerBeanTest extends TestCase {

    private static final String   testSurname   = "test-surname";
    private static final String   testGivenName = "test-givenname";

    private CredentialManagerBean testedInstance;

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

    private NodeMappingService    mockNodeMappingService;

    private IdentityProvider      identityProvider;

    private SecurityAuditLogger   mockSecurityAuditLogger;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();

        testedInstance = new CredentialManagerBean();

        mockPkiProviderManager = createMock(PkiProviderManager.class);
        EJBTestUtils.inject(testedInstance, mockPkiProviderManager);

        mockPkiValidator = createMock(PkiValidator.class);
        EJBTestUtils.inject(testedInstance, mockPkiValidator);

        mockPkiProvider = createMock(PkiProvider.class);

        mockSubjectIdentifierDAO = createMock(SubjectIdentifierDAO.class);
        EJBTestUtils.inject(testedInstance, mockSubjectIdentifierDAO);

        mockNodeMappingService = createMock(NodeMappingService.class);
        EJBTestUtils.inject(testedInstance, mockNodeMappingService);

        mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);
        EJBTestUtils.inject(testedInstance, mockSecurityAuditLogger);

        mockObjects = new Object[] { mockPkiProviderManager, mockPkiValidator, mockPkiProvider, mockSubjectIdentifierDAO,
                mockNodeMappingService, mockSecurityAuditLogger };

        EJBTestUtils.init(testedInstance);

        // stubs
        testCallerLogin = "test-caller-login-" + getName();
        testSubject = new SubjectEntity(testCallerLogin);

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        privateKey = keyPair.getPrivate();
        certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");
        signer = new JceSigner(privateKey, certificate);
        expect(mockPkiProviderManager.findPkiProvider(certificate)).andStubReturn(mockPkiProvider);

        identityProvider = new IdentityProvider() {

            public String getGivenName() {

                return testGivenName;
            }

            public String getSurname() {

                return testSurname;
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

        byte[] authenticationStatementData = AuthenticationStatementFactory.createAuthenticationStatement(sessionId, applicationId, signer);
        AuthenticationStatement authenticationStatement = new AuthenticationStatement(authenticationStatementData);

        // stubs
        expect(mockPkiProviderManager.findPkiProvider(certificate)).andStubReturn(mockPkiProvider);
        expect(mockPkiProvider.getTrustDomain()).andStubReturn(trustDomain);
        expect(mockPkiValidator.validateCertificate(trustDomain, certificate)).andStubReturn(PkiResult.VALID);
        expect(mockPkiProvider.getIdentifierDomainName()).andStubReturn(identifierDomain);
        expect(mockPkiProvider.parseIdentifierFromCert(certificate)).andStubReturn(identifier);
        expect(mockSubjectIdentifierDAO.findSubject(identifierDomain, identifier)).andStubReturn(subject);
        expect(mockPkiProvider.isDisabled(subject, certificate)).andStubReturn(false);

        // prepare
        replay(mockObjects);

        // operate
        String resultUserId = testedInstance.authenticate(sessionId, applicationId, authenticationStatement);

        // verify
        verify(mockObjects);
        assertNotNull(resultUserId);
        assertEquals(userId, resultUserId);
    }

    public void testUnparsableIdentityStatement()
            throws Exception {

        // setup
        byte[] identityStatement = "foobar-identity-statemennt".getBytes();

        String sessionId = UUID.randomUUID().toString();
        String nodeName = "test-node-name";
        String deviceUserId = UUID.randomUUID().toString();
        String operation = DeviceOperationType.REGISTER.name();

        // prepare
        replay(mockObjects);

        // operate
        try {
            testedInstance.mergeIdentityStatement(sessionId, nodeName, deviceUserId, operation, identityStatement);
            fail();
        } catch (ArgumentIntegrityException e) {
            // expected
        }

        // verify
        verify(mockObjects);
    }

    public void testMergeIdentityStatement()
            throws Exception {

        // setup
        String sessionId = UUID.randomUUID().toString();
        String nodeName = "test-node-name";
        String operation = DeviceOperationType.REGISTER.name();
        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        byte[] identityStatement = IdentityStatementFactory.createIdentityStatement(sessionId, userId, operation, signer, identityProvider);
        TrustDomainEntity trustDomain = new TrustDomainEntity("test-trust-domain", true);
        String identifierDomain = "test-identifier-domain";
        String identifier = "test-identifier";

        // stubs
        expect(mockPkiProvider.getTrustDomain()).andStubReturn(trustDomain);

        expect(mockPkiValidator.validateCertificate(trustDomain, certificate)).andStubReturn(PkiResult.VALID);

        expect(mockPkiProvider.getIdentifierDomainName()).andStubReturn(identifierDomain);
        expect(mockPkiProvider.parseIdentifierFromCert(certificate)).andStubReturn(identifier);
        expect(mockSubjectIdentifierDAO.findSubject(identifierDomain, identifier)).andStubReturn(null);

        expect(mockNodeMappingService.getSubject(userId, nodeName)).andStubReturn(subject);

        // expectations
        mockSubjectIdentifierDAO.addSubjectIdentifier(identifierDomain, identifier, subject);
        // this.mockSubjectIdentifierDAO.removeOtherSubjectIdentifiers(identifierDomain, identifier, subject);

        mockPkiProvider.storeDeviceAttributes(subject, testSurname, testGivenName, certificate);

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.mergeIdentityStatement(sessionId, nodeName, userId, operation, identityStatement);

        // verify
        verify(mockObjects);
    }

    public void testMergeIdentityStatementFailsIfAnotherSubjectAlreadyRegisteredTheCert()
            throws Exception {

        // setup
        String sessionId = UUID.randomUUID().toString();
        String nodeName = "test-node-name";
        String operation = DeviceOperationType.REGISTER.name();
        String userId = UUID.randomUUID().toString();

        byte[] identityStatement = IdentityStatementFactory.createIdentityStatement(sessionId, userId, operation, signer, identityProvider);
        TrustDomainEntity trustDomain = new TrustDomainEntity("test-trust-domain", true);
        String identifierDomain = "test-identifier-domain";
        String identifier = "test-identifier";
        SubjectEntity anotherSubject = new SubjectEntity("another-subject");

        // stubs
        expect(mockPkiProvider.getTrustDomain()).andStubReturn(trustDomain);

        expect(mockPkiValidator.validateCertificate(trustDomain, certificate)).andStubReturn(PkiResult.VALID);

        expect(mockPkiProvider.getIdentifierDomainName()).andStubReturn(identifierDomain);
        expect(mockPkiProvider.parseIdentifierFromCert(certificate)).andStubReturn(identifier);
        expect(mockSubjectIdentifierDAO.findSubject(identifierDomain, identifier)).andStubReturn(anotherSubject);

        // prepare
        replay(mockObjects);

        // operate & verify
        try {
            testedInstance.mergeIdentityStatement(sessionId, nodeName, userId, operation, identityStatement);
            fail();
        } catch (AlreadyRegisteredException e) {
            // expected
            verify(mockObjects);
        }
    }

    public void testMergeIdentityStatementFailsIfLoginAndUserDoNotCorrespond()
            throws Exception {

        // setup
        String sessionId = UUID.randomUUID().toString();
        String nodeName = "test-node-name";
        String operation = DeviceOperationType.REGISTER.name();
        String user = "foobar-test-user";
        byte[] identityStatement = IdentityStatementFactory.createIdentityStatement(sessionId, user, operation, signer, identityProvider);
        TrustDomainEntity trustDomain = new TrustDomainEntity("test-trust-domain", true);

        // stubs
        expect(mockPkiProvider.getTrustDomain()).andStubReturn(trustDomain);
        expect(mockPkiValidator.validateCertificate(trustDomain, certificate)).andStubReturn(PkiResult.VALID);
        mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, CredentialManagerBean.SECURITY_MESSAGE_USER_MISMATCH);

        // prepare
        replay(mockObjects);

        // operate & verify
        try {
            testedInstance.mergeIdentityStatement(sessionId, nodeName, testSubject.getUserId(), operation, identityStatement);
            fail();
        } catch (PermissionDeniedException e) {
            // expected
            verify(mockObjects);
        }
    }

    public void testMergeIdentityStatementFailsIfSessionIdIsInvalid()
            throws Exception {

        // setup
        String sessionId = UUID.randomUUID().toString();
        String nodeName = "test-node-name";
        String wrongSessionId = "wrong-session-id";
        String operation = DeviceOperationType.REGISTER.name();
        String userId = UUID.randomUUID().toString();
        byte[] identityStatement = IdentityStatementFactory.createIdentityStatement(wrongSessionId, userId, operation, signer,
                identityProvider);
        TrustDomainEntity trustDomain = new TrustDomainEntity("test-trust-domain", true);

        // stubs
        expect(mockPkiProvider.getTrustDomain()).andStubReturn(trustDomain);
        expect(mockPkiValidator.validateCertificate(trustDomain, certificate)).andStubReturn(PkiResult.VALID);
        mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, CredentialManagerBean.SECURITY_MESSAGE_SESSION_ID_MISMATCH);

        // prepare
        replay(mockObjects);

        // operate & verify
        try {
            testedInstance.mergeIdentityStatement(sessionId, nodeName, userId, operation, identityStatement);
            fail();
        } catch (ArgumentIntegrityException e) {
            // expected
            verify(mockObjects);
        }
    }

    public void testMergeIdentityStatementFailsIfOperationIdIsInvalid()
            throws Exception {

        // setup
        String sessionId = UUID.randomUUID().toString();
        String nodeName = "test-node-name";
        String operation = DeviceOperationType.REGISTER.name();
        String wrongOperation = "wrong-operation";
        String userId = UUID.randomUUID().toString();
        byte[] identityStatement = IdentityStatementFactory.createIdentityStatement(sessionId, userId, wrongOperation, signer,
                identityProvider);
        TrustDomainEntity trustDomain = new TrustDomainEntity("test-trust-domain", true);

        // stubs
        expect(mockPkiProvider.getTrustDomain()).andStubReturn(trustDomain);
        expect(mockPkiValidator.validateCertificate(trustDomain, certificate)).andStubReturn(PkiResult.VALID);
        mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, CredentialManagerBean.SECURITY_MESSAGE_OPERATION_MISMATCH);

        // prepare
        replay(mockObjects);

        // operate & verify
        try {
            testedInstance.mergeIdentityStatement(sessionId, nodeName, userId, operation, identityStatement);
            fail();
        } catch (ArgumentIntegrityException e) {
            // expected
            verify(mockObjects);
        }
    }

    public void testMergeIdentityStatementFailsIfNotSignedByClaimedAuthCert()
            throws Exception {

        // setup
        String sessionId = UUID.randomUUID().toString();
        String nodeName = "test-node-name";
        String userId = "test-user";
        String operation = DeviceOperationType.REGISTER.name();

        KeyPair otherKeyPair = PkiTestUtils.generateKeyPair();
        Signer otherSigner = new JceSigner(otherKeyPair.getPrivate(), certificate);

        IdentityStatement identityStatement = new IdentityStatement(sessionId, userId, operation, identityProvider, otherSigner);
        byte[] identityStatementData = identityStatement.generateStatement();

        TrustDomainEntity trustDomain = new TrustDomainEntity("test-trust-domain", true);

        // stubs
        expect(mockPkiValidator.validateCertificate(trustDomain, certificate)).andStubReturn(PkiResult.VALID);

        // prepare
        replay(mockObjects);

        // operate & verify
        try {
            testedInstance.mergeIdentityStatement(sessionId, nodeName, userId, operation, identityStatementData);
            fail();
        } catch (ArgumentIntegrityException e) {
            // expected
            verify(mockObjects);
        }
    }

    public void testMergeIdentityStatementFailsIfCertNotTrusted()
            throws Exception {

        // setup
        String sessionId = UUID.randomUUID().toString();
        String nodeName = "test-node-name";
        String userId = "test-user";
        String operation = DeviceOperationType.REGISTER.name();

        byte[] identityStatement = IdentityStatementFactory.createIdentityStatement(sessionId, userId, operation, signer, identityProvider);
        TrustDomainEntity trustDomain = new TrustDomainEntity("test-trust-domain", true);

        // stubs
        expect(mockPkiProvider.getTrustDomain()).andStubReturn(trustDomain);
        expect(mockPkiValidator.validateCertificate(trustDomain, certificate)).andStubReturn(PkiResult.INVALID);

        // prepare
        replay(mockObjects);

        // operate
        try {
            testedInstance.mergeIdentityStatement(sessionId, nodeName, userId, operation, identityStatement);
            fail();
        } catch (PkiInvalidException e) {
            // expected
            verify(mockObjects);
        }
    }
}
