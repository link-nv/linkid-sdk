/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.attrib.ws;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.IOException;
import java.security.KeyPair;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import junit.framework.TestCase;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.attrib.ws.ApplicationCertificateLoginHandler;
import net.link.safeonline.model.PkiValidator;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.TestSOAPMessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.callback.ObjectCallback;

public class ApplicationCertificateLoginHandlerTest extends TestCase {

	private JndiTestUtils jndiTestUtils;

	private ApplicationCertificateLoginHandler testedInstance;

	private PkiValidator mockPkiValidator;

	protected void setUp() throws Exception {
		super.setUp();

		this.jndiTestUtils = new JndiTestUtils();
		this.jndiTestUtils.setUp();

		this.mockPkiValidator = createMock(PkiValidator.class);
		this.jndiTestUtils.bindComponent("SafeOnline/PkiValidatorBean/local",
				this.mockPkiValidator);

		this.testedInstance = new ApplicationCertificateLoginHandler();
		EJBTestUtils.init(this.testedInstance);
	}

	protected void tearDown() throws Exception {
		this.jndiTestUtils.tearDown();
		super.tearDown();
	}

	public void testHandleMessagePerformsJAASLoginLogout() throws Exception {
		// setup
		SOAPMessageContext context = new TestSOAPMessageContext(null, false);

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair, "CN=Test");
		context.put(ApplicationCertificateLoginHandler.CERTIFICATE_PROPERTY,
				certificate);

		JaasTestUtils.initJaasLoginModule(TestLoginModule.class);

		// expectations
		expect(
				this.mockPkiValidator
						.validateCertificate(
								SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
								certificate)).andReturn(true);

		// prepare
		replay(this.mockPkiValidator);

		// operate
		this.testedInstance.handleMessage(context);
		context.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true);
		this.testedInstance.handleMessage(context);

		// verify
		verify(this.mockPkiValidator);
	}

	public static class TestLoginModule implements LoginModule {

		private static final Log LOG = LogFactory.getLog(TestLoginModule.class);

		private CallbackHandler callbackHandler;

		private Subject subject;

		private Principal authenticatedPrincipal;

		public boolean abort() throws LoginException {
			LOG.debug("abort");
			return false;
		}

		public boolean commit() throws LoginException {
			LOG.debug("commit");
			this.subject.getPrincipals().add(this.authenticatedPrincipal);
			return true;
		}

		public void initialize(Subject subject,
				CallbackHandler callbackHandler, Map<String, ?> sharedState,
				Map<String, ?> options) {
			LOG.debug("initialize");
			this.subject = subject;
			this.callbackHandler = callbackHandler;
		}

		public boolean login() throws LoginException {
			LOG.debug("login");
			ObjectCallback objectCallback = new ObjectCallback("X509");
			Callback[] callbacks = new Callback[] { objectCallback };
			try {
				this.callbackHandler.handle(callbacks);
			} catch (IOException e) {
				throw new LoginException("IO error: " + e.getMessage());
			} catch (UnsupportedCallbackException e) {
				throw new LoginException("unsupported callback: "
						+ e.getMessage());
			}
			Object credential = objectCallback.getCredential();
			X509Certificate certificate = (X509Certificate) credential;
			LOG.debug("certificate: " + certificate);
			this.authenticatedPrincipal = new SimplePrincipal(certificate
					.getIssuerX500Principal().toString());
			return true;
		}

		public boolean logout() throws LoginException {
			LOG.debug("logout");
			if (null == this.authenticatedPrincipal) {
				throw new LoginException("no auth principal");
			}
			boolean result = this.subject.getPrincipals().remove(
					this.authenticatedPrincipal);
			if (false == result) {
				throw new LoginException(
						"subject did not contain auth principal");
			}
			return true;
		}
	}
}
