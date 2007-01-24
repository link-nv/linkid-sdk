/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
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
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.service.bean.CredentialServiceBean;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.identity.IdentityStatementFactory;
import net.link.safeonline.model.PkiProviderManager;
import net.link.safeonline.model.PkiValidator;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.p11sc.SmartCardConfig;
import net.link.safeonline.p11sc.SmartCardPinCallback;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

public class CredentialServiceBeanTest extends TestCase {

	private CredentialServiceBean testedInstance;

	private SubjectManager mockSubjectManager;

	private AttributeDAO mockAttributeDAO;

	private PkiProviderManager mockPkiProviderManager;

	private PkiValidator mockPkiValidator;

	private Object[] mockObjects;

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

		this.mockObjects = new Object[] { this.mockSubjectManager,
				this.mockAttributeDAO, this.mockPkiProviderManager,
				this.mockPkiValidator };

		EJBTestUtils.init(this.testedInstance);
	}

	public void testUnparsableIdentityStatement() throws Exception {
		// setup
		byte[] identityStatement = "foobar-identity-statemennt".getBytes();
		String testCallerLogin = "test-caller-login-" + getName();

		// stubs
		expect(this.mockSubjectManager.getCallerLogin()).andStubReturn(
				testCallerLogin);

		// prepare
		replay(this.mockObjects);

		// operate
		try {
			this.testedInstance.mergeIdentityStatement(identityStatement);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}

		// verify
		verify(this.mockObjects);
	}

	public void testMergeIdentityStatement() throws Exception {
		// setup
		IdentityStatementFactory identityStatementFactory = new IdentityStatementFactory();
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair, "CN=Test");
		SmartCard smartCard = new TestSmartCard(keyPair, certificate);
		byte[] identityStatement = identityStatementFactory
				.createIdentityStatement(smartCard);
		String testCallerLogin = "test-caller-login-" + getName();
		TrustDomainEntity trustDomain = new TrustDomainEntity(
				"test-trust-domain", true);

		// stubs
		expect(this.mockSubjectManager.getCallerLogin()).andStubReturn(
				testCallerLogin);

		expect(
				this.mockAttributeDAO.findAttribute(
						SafeOnlineConstants.SURNAME_ATTRIBUTE, testCallerLogin))
				.andStubReturn(null);
		expect(
				this.mockAttributeDAO.findAttribute(
						SafeOnlineConstants.GIVENNAME_ATTRIBUTE,
						testCallerLogin)).andStubReturn(null);
		expect(this.mockPkiProviderManager.findTrustDomain(certificate))
				.andStubReturn(trustDomain);
		expect(
				this.mockPkiValidator.validateCertificate(trustDomain,
						certificate)).andStubReturn(true);

		// expectations
		this.mockAttributeDAO.addAttribute(
				SafeOnlineConstants.SURNAME_ATTRIBUTE, testCallerLogin,
				smartCard.getSurname());
		this.mockAttributeDAO.addAttribute(
				SafeOnlineConstants.GIVENNAME_ATTRIBUTE, testCallerLogin,
				smartCard.getGivenName());

		// prepare
		replay(this.mockObjects);

		// operate
		this.testedInstance.mergeIdentityStatement(identityStatement);

		// verify
		verify(this.mockObjects);
	}

	private static class TestSmartCard implements SmartCard {

		private final KeyPair keyPair;

		private final X509Certificate certificate;

		private final String surname;

		private final String givenName;

		public TestSmartCard(KeyPair keyPair, X509Certificate certificate) {
			this.keyPair = keyPair;
			this.certificate = certificate;
			this.surname = UUID.randomUUID().toString();
			this.givenName = UUID.randomUUID().toString();
		}

		public void close() {
		}

		public X509Certificate getAuthenticationCertificate() {
			return this.certificate;
		}

		public PrivateKey getAuthenticationPrivateKey() {
			return this.keyPair.getPrivate();
		}

		public String getCity() {
			return null;
		}

		public String getCountryCode() {
			return null;
		}

		public String getGivenName() {
			return this.givenName;
		}

		public String getPostalCode() {
			return null;
		}

		public X509Certificate getSignatureCertificate() {
			return null;
		}

		public PrivateKey getSignaturePrivateKey() {
			return null;
		}

		public String getStreet() {
			return null;
		}

		public String getSurname() {
			return this.surname;
		}

		public void init(List<SmartCardConfig> smartCardConfigs) {
		}

		public boolean isOpen() {
			return false;
		}

		public boolean isReaderPresent() {
			return false;
		}

		public boolean isSupportedCardPresent() {
			return false;
		}

		public void open() {
		}

		public void setSmartCardPinCallback(
				SmartCardPinCallback smartCardPinCallback) {
		}
	}
}
