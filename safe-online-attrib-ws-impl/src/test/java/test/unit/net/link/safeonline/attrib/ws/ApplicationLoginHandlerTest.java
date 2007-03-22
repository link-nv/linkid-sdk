/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.attrib.ws;

import java.io.IOException;
import java.security.KeyPair;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import junit.framework.TestCase;
import net.link.safeonline.attrib.ws.ApplicationCertificateValidatorHandler;
import net.link.safeonline.attrib.ws.ApplicationLoginHandler;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.TestSOAPMessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.SimplePrincipal;

public class ApplicationLoginHandlerTest extends TestCase {

	private ApplicationLoginHandler testedInstance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new ApplicationLoginHandler();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testHandleMessagePerformsJAASLoginLogout() throws Exception {
		// setup
		SOAPMessageContext context = new TestSOAPMessageContext(null, false);

		String testApplicationName = "test-application-name-" + getName();
		context.put(ApplicationLoginHandler.APPLICATION_NAME_PROPERTY,
				testApplicationName);

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair, "CN=Test");
		context.put(
				ApplicationCertificateValidatorHandler.CERTIFICATE_PROPERTY,
				certificate);

		JaasTestUtils.initJaasLoginModule(TestLoginModule.class);

		// operate
		this.testedInstance.handleMessage(context);
		context.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true);
		this.testedInstance.handleMessage(context);

		// verify
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
			PasswordCallback passwordCallback = new PasswordCallback("X509",
					false);
			Callback[] callbacks = new Callback[] { passwordCallback };
			try {
				this.callbackHandler.handle(callbacks);
			} catch (IOException e) {
				throw new LoginException("IO error: " + e.getMessage());
			} catch (UnsupportedCallbackException e) {
				throw new LoginException("unsupported callback: "
						+ e.getMessage());
			}
			this.authenticatedPrincipal = new SimplePrincipal("test");
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
