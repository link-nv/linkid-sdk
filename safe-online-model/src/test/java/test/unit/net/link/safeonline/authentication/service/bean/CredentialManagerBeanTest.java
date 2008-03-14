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
import java.security.cert.X509Certificate;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.auth.AuthenticationStatementFactory;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.bean.AuthenticationStatement;
import net.link.safeonline.authentication.service.bean.IdentityStatementAttributes;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.device.backend.bean.CredentialManagerBean;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.identity.IdentityStatementFactory;
import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.pkix.model.PkiProvider;
import net.link.safeonline.pkix.model.PkiProviderManager;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.shared.statement.IdentityStatement;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

public class CredentialManagerBeanTest extends TestCase {

	private CredentialManagerBean testedInstance;

	private AttributeDAO mockAttributeDAO;

	private PkiProviderManager mockPkiProviderManager;

	private PkiValidator mockPkiValidator;

	private PkiProvider mockPkiProvider;

	private Object[] mockObjects;

	private String testCallerLogin;

	private X509Certificate certificate;

	private SmartCard smartCard;

	private SubjectEntity testSubject;

	private SubjectIdentifierDAO mockSubjectIdentifierDAO;

	private AttributeTypeDAO mockAttributeTypeDAO;

	private SubjectService mockSubjectService;

	@Override
	protected void setUp() throws Exception {
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

		this.mockObjects = new Object[] { this.mockAttributeDAO,
				this.mockPkiProviderManager, this.mockPkiValidator,
				this.mockPkiProvider, this.mockSubjectIdentifierDAO,
				this.mockAttributeTypeDAO, this.mockSubjectService };

		EJBTestUtils.init(this.testedInstance);

		// stubs
		this.testCallerLogin = "test-caller-login-" + getName();
		this.testSubject = new SubjectEntity(this.testCallerLogin);

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		this.certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair,
				"CN=Test");
		this.smartCard = new SoftwareSmartCard(keyPair, this.certificate);
		expect(this.mockPkiProviderManager.findPkiProvider(this.certificate))
				.andStubReturn(this.mockPkiProvider);
	}

	public void testAuthenticateViaAuthenticationStatement() throws Exception {
		// setup
		String sessionId = UUID.randomUUID().toString();
		String applicationId = "test-application-id";
		TrustDomainEntity trustDomain = new TrustDomainEntity(
				"test-trust-domain", true);
		String identifierDomain = "test-identifier-domain";
		String identifier = "test-identifier";
		String login = "test-subject-login";
		SubjectEntity subject = new SubjectEntity(login);

		byte[] authenticationStatementData = AuthenticationStatementFactory
				.createAuthenticationStatement(sessionId, applicationId,
						this.smartCard);
		AuthenticationStatement authenticationStatement = new AuthenticationStatement(
				authenticationStatementData);

		// stubs
		expect(this.mockPkiProviderManager.findPkiProvider(this.certificate))
				.andStubReturn(this.mockPkiProvider);
		expect(this.mockPkiProvider.getTrustDomain())
				.andStubReturn(trustDomain);
		expect(
				this.mockPkiValidator.validateCertificate(trustDomain,
						this.certificate)).andStubReturn(true);
		expect(this.mockPkiProvider.getIdentifierDomainName()).andStubReturn(
				identifierDomain);
		expect(this.mockPkiProvider.getSubjectIdentifier(this.certificate))
				.andStubReturn(identifier);
		expect(
				this.mockSubjectIdentifierDAO.findSubject(identifierDomain,
						identifier)).andStubReturn(subject);

		// prepare
		replay(this.mockObjects);

		// operate
		String resultDeviceUserId = this.testedInstance.authenticate(sessionId,
				authenticationStatement);

		// verify
		verify(this.mockObjects);
		assertNotNull(resultDeviceUserId);
	}

	public void testUnparsableIdentityStatement() throws Exception {
		// setup
		byte[] identityStatement = "foobar-identity-statemennt".getBytes();

		SubjectEntity subject = new SubjectEntity();

		// prepare
		replay(this.mockObjects);

		// operate
		try {
			this.testedInstance.mergeIdentityStatement(subject.getUserId(),
					identityStatement);
			fail();
		} catch (ArgumentIntegrityException e) {
			// expected
		}

		// verify
		verify(this.mockObjects);
	}

	public void testMergeIdentityStatement() throws Exception {
		// setup
		String deviceId = UUID.randomUUID().toString();
		SubjectEntity deviceSubject = new SubjectEntity(deviceId);

		byte[] identityStatement = IdentityStatementFactory
				.createIdentityStatement(deviceId, this.smartCard);
		TrustDomainEntity trustDomain = new TrustDomainEntity(
				"test-trust-domain", true);
		String surnameAttribute = "test-surname-attribute";
		String givenNameAttribute = "test-given-name-attribute";
		String identifierDomain = "test-identifier-domain";
		String identifier = "test-identifier";

		// stubs
		expect(this.mockPkiProvider.getTrustDomain())
				.andStubReturn(trustDomain);

		expect(
				this.mockPkiValidator.validateCertificate(trustDomain,
						this.certificate)).andStubReturn(true);

		expect(
				this.mockPkiProvider
						.mapAttribute(IdentityStatementAttributes.SURNAME))
				.andStubReturn(surnameAttribute);
		expect(
				this.mockAttributeDAO.findAttribute(surnameAttribute,
						this.testSubject)).andStubReturn(null);

		expect(
				this.mockPkiProvider
						.mapAttribute(IdentityStatementAttributes.GIVEN_NAME))
				.andStubReturn(givenNameAttribute);
		expect(
				this.mockAttributeDAO.findAttribute(givenNameAttribute,
						this.testSubject)).andStubReturn(null);
		expect(this.mockPkiProvider.getIdentifierDomainName()).andStubReturn(
				identifierDomain);
		expect(this.mockPkiProvider.getSubjectIdentifier(this.certificate))
				.andStubReturn(identifier);
		expect(
				this.mockSubjectIdentifierDAO.findSubject(identifierDomain,
						identifier)).andStubReturn(null);
		this.mockPkiProvider.storeAdditionalAttributes(deviceSubject,
				this.certificate);
		this.mockPkiProvider.storeDeviceAttribute(deviceSubject);

		AttributeTypeEntity surnameAttributeType = new AttributeTypeEntity();
		expect(this.mockAttributeTypeDAO.getAttributeType(surnameAttribute))
				.andStubReturn(surnameAttributeType);
		AttributeTypeEntity givenNameAttributeType = new AttributeTypeEntity();
		expect(this.mockAttributeTypeDAO.getAttributeType(givenNameAttribute))
				.andStubReturn(givenNameAttributeType);

		expect(this.mockSubjectService.findSubject(deviceId)).andReturn(null);
		expect(this.mockSubjectService.addDeviceSubject(deviceId)).andReturn(
				deviceSubject);

		// expectations
		this.mockAttributeDAO.addOrUpdateAttribute(surnameAttributeType,
				deviceSubject, 0, this.smartCard.getSurname());
		this.mockAttributeDAO.addOrUpdateAttribute(givenNameAttributeType,
				deviceSubject, 0, this.smartCard.getGivenName());
		this.mockSubjectIdentifierDAO.addSubjectIdentifier(identifierDomain,
				identifier, deviceSubject);
		this.mockSubjectIdentifierDAO.removeOtherSubjectIdentifiers(
				identifierDomain, identifier, deviceSubject);

		// prepare
		replay(this.mockObjects);

		// operate
		this.testedInstance.mergeIdentityStatement(deviceId, identityStatement);

		// verify
		verify(this.mockObjects);
	}

	public void testMergeIdentityStatementFailsIfAnotherSubjectAlreadyRegisteredTheCert()
			throws Exception {
		// setup
		String deviceId = UUID.randomUUID().toString();
		SubjectEntity deviceSubject = new SubjectEntity(deviceId);

		byte[] identityStatement = IdentityStatementFactory
				.createIdentityStatement(deviceId, this.smartCard);
		TrustDomainEntity trustDomain = new TrustDomainEntity(
				"test-trust-domain", true);
		String surnameAttribute = "test-surname-attribute";
		String givenNameAttribute = "test-given-name-attribute";
		String identifierDomain = "test-identifier-domain";
		String identifier = "test-identifier";
		SubjectEntity anotherSubject = new SubjectEntity("another-subject");

		// stubs
		expect(this.mockPkiProvider.getTrustDomain())
				.andStubReturn(trustDomain);

		expect(
				this.mockPkiValidator.validateCertificate(trustDomain,
						this.certificate)).andStubReturn(true);

		expect(
				this.mockPkiProvider
						.mapAttribute(IdentityStatementAttributes.SURNAME))
				.andStubReturn(surnameAttribute);
		expect(
				this.mockAttributeDAO.findAttribute(surnameAttribute,
						this.testSubject)).andStubReturn(null);

		expect(
				this.mockPkiProvider
						.mapAttribute(IdentityStatementAttributes.GIVEN_NAME))
				.andStubReturn(givenNameAttribute);
		expect(
				this.mockAttributeDAO.findAttribute(givenNameAttribute,
						this.testSubject)).andStubReturn(null);
		expect(this.mockPkiProvider.getIdentifierDomainName()).andStubReturn(
				identifierDomain);
		expect(this.mockPkiProvider.getSubjectIdentifier(this.certificate))
				.andStubReturn(identifier);
		expect(
				this.mockSubjectIdentifierDAO.findSubject(identifierDomain,
						identifier)).andStubReturn(anotherSubject);

		expect(this.mockSubjectService.findSubject(deviceId)).andReturn(null);
		expect(this.mockSubjectService.addDeviceSubject(deviceId)).andReturn(
				deviceSubject);

		// prepare
		replay(this.mockObjects);

		// operate & verify
		try {
			this.testedInstance.mergeIdentityStatement(deviceId,
					identityStatement);
			fail();
		} catch (PermissionDeniedException e) {
			// expected
			verify(this.mockObjects);
		}
	}

	public void testMergeIdentityStatementFailsIfLoginAndUserDoNotCorrespond()
			throws Exception {
		// setup
		String user = "foobar-test-user";
		byte[] identityStatement = IdentityStatementFactory
				.createIdentityStatement(user, this.smartCard);
		TrustDomainEntity trustDomain = new TrustDomainEntity(
				"test-trust-domain", true);

		// stubs
		expect(this.mockPkiProvider.getTrustDomain())
				.andStubReturn(trustDomain);
		expect(
				this.mockPkiValidator.validateCertificate(trustDomain,
						this.certificate)).andStubReturn(true);

		// prepare
		replay(this.mockObjects);

		// operate & verify
		try {
			this.testedInstance.mergeIdentityStatement(this.testSubject
					.getUserId(), identityStatement);
			fail();
		} catch (PermissionDeniedException e) {
			// expected
			verify(this.mockObjects);
		}

	}

	public void testMergeIdentityStatementFailsIfNotSignedByClaimedAuthCert()
			throws Exception {
		// setup
		String user = "test-user";
		X509Certificate authCert = this.smartCard
				.getAuthenticationCertificate();

		KeyPair otherKeyPair = PkiTestUtils.generateKeyPair();

		String givenName = this.smartCard.getGivenName();
		String surname = this.smartCard.getSurname();

		IdentityStatement identityStatement = new IdentityStatement(authCert,
				user, givenName, surname, otherKeyPair.getPrivate());
		byte[] identityStatementData = identityStatement.generateStatement();

		TrustDomainEntity trustDomain = new TrustDomainEntity(
				"test-trust-domain", true);

		// stubs
		expect(
				this.mockPkiValidator.validateCertificate(trustDomain,
						this.certificate)).andStubReturn(true);

		// prepare
		replay(this.mockObjects);

		// operate & verify
		try {
			this.testedInstance.mergeIdentityStatement(this.testSubject
					.getUserId(), identityStatementData);
			fail();
		} catch (ArgumentIntegrityException e) {
			// expected
			verify(this.mockObjects);
		}
	}

	public void testMergeIdentityStatementFailsIfCertNotTrusted()
			throws Exception {
		// setup
		String user = "test-user";
		byte[] identityStatement = IdentityStatementFactory
				.createIdentityStatement(user, this.smartCard);
		TrustDomainEntity trustDomain = new TrustDomainEntity(
				"test-trust-domain", true);

		// stubs
		expect(this.mockPkiProvider.getTrustDomain())
				.andStubReturn(trustDomain);
		expect(
				this.mockPkiValidator.validateCertificate(trustDomain,
						this.certificate)).andStubReturn(false);

		// prepare
		replay(this.mockObjects);

		// operate
		try {
			this.testedInstance.mergeIdentityStatement(this.testSubject
					.getUserId(), identityStatement);
			fail();
		} catch (ArgumentIntegrityException e) {
			// expected
			verify(this.mockObjects);
		}
	}
}
