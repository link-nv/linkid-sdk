package test.unit.net.link.safeonline.webapp.filter;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import junit.framework.TestCase;
import net.link.safeonline.webapp.filter.JAASLoginFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easymock.classextension.EasyMock;

/**
 * Unit test for the generic JAAS login module. This unit test also demonstrates
 * the JAAS login/logout workflow.
 * 
 * @author fcorneli
 * 
 */
public class JAASLoginFilterTest extends TestCase {

	private static final Log LOG = LogFactory.getLog(JAASLoginFilterTest.class);

	private JAASLoginFilter testedInstance;

	private HttpServletRequest mockHttpServletRequest;

	private ServletResponse mockServletResponse;

	private FilterChain mockFilterChain;

	private HttpSession mockHttpSession;

	private FilterConfig mockFilterConfig;

	private static final String TEST_LOGIN_CONFIG = "test-login";

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new JAASLoginFilter();

		this.mockHttpServletRequest = createMock(HttpServletRequest.class);
		this.mockServletResponse = createMock(ServletResponse.class);
		this.mockFilterChain = createMock(FilterChain.class);
		this.mockHttpSession = createMock(HttpSession.class);
		expect(this.mockHttpServletRequest.getSession()).andStubReturn(
				this.mockHttpSession);
		this.mockFilterConfig = createMock(FilterConfig.class);

		setupLoginConfig();
	}

	private void setupLoginConfig() throws Exception {
		File tmpConfigFile = File.createTempFile("jaas-", ".conf");
		tmpConfigFile.deleteOnExit();
		PrintWriter configWriter = new PrintWriter(new FileOutputStream(
				tmpConfigFile), true);
		configWriter.println(TEST_LOGIN_CONFIG + " {");
		LOG.debug("jaas login module: " + TestLoginModule.class.getName());
		configWriter.println(TestLoginModule.class.getName() + " required");
		configWriter.println(";");
		configWriter.println("};");
		configWriter.close();
		System.setProperty("java.security.auth.login.config", tmpConfigFile
				.getAbsolutePath());
	}

	public static class TestLoginModule implements LoginModule {

		private static final Log LOG = LogFactory.getLog(TestLoginModule.class);

		public TestLoginModule() {
			LOG.debug("constructor");
		}

		public boolean abort() throws LoginException {
			LOG.debug("abort");
			return true;
		}

		public boolean commit() throws LoginException {
			LOG.debug("commit");
			return true;
		}

		public void initialize(Subject subject,
				CallbackHandler callbackHandler, Map<String, ?> sharedState,
				Map<String, ?> options) {
			LOG.debug("initialize");

			NameCallback nameCallback = new NameCallback("name");
			PasswordCallback passwordCallback = new PasswordCallback(
					"password", false);

			Callback[] callbacks = new Callback[] { nameCallback,
					passwordCallback };
			try {
				callbackHandler.handle(callbacks);
			} catch (IOException e) {
				throw new RuntimeException("IO error: " + e.getMessage(), e);
			} catch (UnsupportedCallbackException e) {
				throw new RuntimeException("callback error: " + e.getMessage(),
						e);
			}

			String name = nameCallback.getName();
			LOG.debug("name: " + name);
			assertNotNull(name);
			char[] password = passwordCallback.getPassword();
			assertNotNull(password);
			LOG.debug("password: " + new String(password));
		}

		public boolean login() throws LoginException {
			LOG.debug("login");
			return true;
		}

		public boolean logout() throws LoginException {
			LOG.debug("logout");
			return true;
		}
	}

	public void testDoFilter() throws Exception {
		// setup
		String testUsernameAttributeName = "test-username";
		String testPasswordAttributeName = "test-password";

		// stubs
		expect(
				this.mockFilterConfig
						.getInitParameter(JAASLoginFilter.LOGIN_CONTEXT_PARAM))
				.andStubReturn(TEST_LOGIN_CONFIG);
		expect(
				this.mockFilterConfig
						.getInitParameter(JAASLoginFilter.SESSION_USERNAME_PARAM))
				.andStubReturn(testUsernameAttributeName);
		expect(
				this.mockFilterConfig
						.getInitParameter(JAASLoginFilter.SESSION_PASSWORD_PARAM))
				.andStubReturn(testPasswordAttributeName);

		expect(this.mockHttpSession.getAttribute(testUsernameAttributeName))
				.andStubReturn("test-username");
		expect(this.mockHttpSession.getAttribute(testPasswordAttributeName))
				.andStubReturn("test-password");

		// expectation
		this.mockHttpServletRequest.setAttribute(EasyMock.eq("login-context"),
				EasyMock.anyObject());
		LoginContext mockLoginContext = createMock(LoginContext.class);
		expect(this.mockHttpServletRequest.getAttribute("login-context"))
				.andStubReturn(mockLoginContext);

		mockLoginContext.logout();

		this.mockFilterChain.doFilter(this.mockHttpServletRequest,
				this.mockServletResponse);

		// prepare
		replay(this.mockHttpServletRequest, this.mockServletResponse,
				this.mockFilterChain, this.mockHttpSession,
				this.mockFilterConfig);
		replay(mockLoginContext);

		// operate
		this.testedInstance.init(this.mockFilterConfig);
		this.testedInstance.doFilter(this.mockHttpServletRequest,
				this.mockServletResponse, this.mockFilterChain);
		this.testedInstance.destroy();

		// verify
		verify(this.mockHttpServletRequest, this.mockServletResponse,
				this.mockFilterChain, this.mockHttpSession,
				this.mockFilterConfig);
		verify(mockLoginContext);
	}
}
