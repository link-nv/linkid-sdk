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

import junit.framework.TestCase;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.bean.CredentialServiceBean;
import net.link.safeonline.authentication.service.bean.IdentityStatementAttributes;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.identity.IdentityStatementFactory;
import net.link.safeonline.model.PkiProvider;
import net.link.safeonline.model.PkiProviderManager;
import net.link.safeonline.model.PkiValidator;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.shared.identity.IdentityStatement;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

public class CredentialServiceBeanTest extends TestCase {

	private CredentialServiceBean testedInstance;

	private SubjectManager mockSubjectManager;

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

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new CredentialServiceBean();

		this.mockSubjectManager = createMock(SubjectManager.class);
		EJBTestUtils.inject(this.testedInstance, this.mockSubjectManager);

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

		this.mockObjects = new Object[] { this.mockSubjectManager,
				this.mockAttributeDAO, this.mockPkiProviderManager,
				this.mockPkiValidator, this.mockPkiProvider,
				this.mockSubjectIdentifierDAO, this.mockAttributeTypeDAO };

		EJBTestUtils.init(this.testedInstance);

		// stubs
		this.testCallerLogin = "test-caller-login-" + getName();
		expect(this.mockSubjectManager.getCallerLogin()).andStubReturn(
				this.testCallerLogin);
		this.testSubject = new SubjectEntity(this.testCallerLogin);
		expect(this.mockSubjectManager.getCallerSubject()).andStubReturn(
				this.testSubject);

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		this.certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair,
				"CN=Test");
		this.smartCard = new SoftwareSmartCard(keyPair, certificate);
		expect(this.mockPkiProviderManager.findPkiProvider(this.certificate))
				.andStubReturn(this.mockPkiProvider);
	}

	public void testUnparsableIdentityStatement() throws Exception {
		// setup
		byte[] identityStatement = "foobar-identity-statemennt".getBytes();

		// prepare
		replay(this.mockObjects);

		// operate
		try {
			this.testedInstance.mergeIdentityStatement(identityStatement);
			fail();
		} catch (ArgumentIntegrityException e) {
			// expected
		}

		// verify
		verify(this.mockObjects);
	}

	public void testMergeIdentityStatement() throws Exception {
		// setup
		String user = this.testCallerLogin;
		byte[] identityStatement = IdentityStatementFactory
				.createIdentityStatement(user, this.smartCard);
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
		expect(this.mockAttributeDAO.findAttribute(surnameAttribute, user))
				.andStubReturn(null);

		expect(
				this.mockPkiProvider
						.mapAttribute(IdentityStatementAttributes.GIVEN_NAME))
				.andStubReturn(givenNameAttribute);
		expect(this.mockAttributeDAO.findAttribute(givenNameAttribute, user))
				.andStubReturn(null);
		expect(this.mockPkiProvider.getIdentifierDomainName()).andStubReturn(
				identifierDomain);
		expect(this.mockPkiProvider.getSubjectIdentifier(certificate))
				.andStubReturn(identifier);
		expect(
				this.mockSubjectIdentifierDAO.findSubject(identifierDomain,
						identifier)).andStubReturn(null);
		this.mockPkiProvider.storeAdditionalAttributes(certificate);

		AttributeTypeEntity surnameAttributeType = new AttributeTypeEntity();
		expect(this.mockAttributeTypeDAO.getAttributeType(surnameAttribute))
				.andStubReturn(surnameAttributeType);
		AttributeTypeEntity givenNameAttributeType = new AttributeTypeEntity();
		expect(this.mockAttributeTypeDAO.getAttributeType(givenNameAttribute))
				.andStubReturn(givenNameAttributeType);

		// expectations
		this.mockAttributeDAO.addOrUpdateAttribute(surnameAttributeType,
				this.testSubject, 0, this.smartCard.getSurname());
		this.mockAttributeDAO.addOrUpdateAttribute(givenNameAttributeType,
				this.testSubject, 0, this.smartCard.getGivenName());
		this.mockSubjectIdentifierDAO.addSubjectIdentifier(identifierDomain,
				identifier, this.testSubject);
		this.mockSubjectIdentifierDAO.removeOtherSubjectIdentifiers(
				identifierDomain, identifier, this.testSubject);

		// prepare
		replay(this.mockObjects);

		// operate
		this.testedInstance.mergeIdentityStatement(identityStatement);

		// verify
		verify(this.mockObjects);
	}

	public void testMergeIdentityStatementFailsIfAnotherSubjectAlreadyRegisteredTheCert()
			throws Exception {
		// setup
		String user = this.testCallerLogin;
		byte[] identityStatement = IdentityStatementFactory
				.createIdentityStatement(user, this.smartCard);
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
		expect(this.mockAttributeDAO.findAttribute(surnameAttribute, user))
				.andStubReturn(null);

		expect(
				this.mockPkiProvider
						.mapAttribute(IdentityStatementAttributes.GIVEN_NAME))
				.andStubReturn(givenNameAttribute);
		expect(this.mockAttributeDAO.findAttribute(givenNameAttribute, user))
				.andStubReturn(null);
		expect(this.mockPkiProvider.getIdentifierDomainName()).andStubReturn(
				identifierDomain);
		expect(this.mockPkiProvider.getSubjectIdentifier(certificate))
				.andStubReturn(identifier);
		expect(
				this.mockSubjectIdentifierDAO.findSubject(identifierDomain,
						identifier)).andStubReturn(anotherSubject);

		// prepare
		replay(this.mockObjects);

		// operate & verify
		try {
			this.testedInstance.mergeIdentityStatement(identityStatement);
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
				.createIdentityStatement(user, smartCard);
		TrustDomainEntity trustDomain = new TrustDomainEntity(
				"test-trust-domain", true);

		// stubs
		expect(this.mockPkiProvider.getTrustDomain())
				.andStubReturn(trustDomain);
		expect(
				this.mockPkiValidator.validateCertificate(trustDomain,
						certificate)).andStubReturn(true);

		// prepare
		replay(this.mockObjects);

		// operate & verify
		try {
			this.testedInstance.mergeIdentityStatement(identityStatement);
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
		X509Certificate authCert = smartCard.getAuthenticationCertificate();

		KeyPair otherKeyPair = PkiTestUtils.generateKeyPair();

		String givenName = smartCard.getGivenName();
		String surname = smartCard.getSurname();

		IdentityStatement identityStatement = new IdentityStatement(authCert,
				user, givenName, surname, otherKeyPair.getPrivate());
		byte[] identityStatementData = identityStatement
				.generateIdentityStatement();

		TrustDomainEntity trustDomain = new TrustDomainEntity(
				"test-trust-domain", true);

		// stubs
		expect(
				this.mockPkiValidator.validateCertificate(trustDomain,
						certificate)).andStubReturn(true);

		// prepare
		replay(this.mockObjects);

		// operate & verify
		try {
			this.testedInstance.mergeIdentityStatement(identityStatementData);
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
				.createIdentityStatement(user, smartCard);
		TrustDomainEntity trustDomain = new TrustDomainEntity(
				"test-trust-domain", true);

		// stubs
		expect(this.mockPkiProvider.getTrustDomain())
				.andStubReturn(trustDomain);
		expect(
				this.mockPkiValidator.validateCertificate(trustDomain,
						certificate)).andStubReturn(false);

		// prepare
		replay(this.mockObjects);

		// operate
		try {
			this.testedInstance.mergeIdentityStatement(identityStatement);
			fail();
		} catch (ArgumentIntegrityException e) {
			// expected
			verify(this.mockObjects);
		}
	}
}
