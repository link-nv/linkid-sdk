package test.unit.net.link.safeonline.webapp.filter;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
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
import net.link.safeonline.test.util.JaasTestUtils;
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

	private JAASLoginFilter testedInstance;

	private HttpServletRequest mockHttpServletRequest;

	private ServletResponse mockServletResponse;

	private FilterChain mockFilterChain;

	private HttpSession mockHttpSession;

	private FilterConfig mockFilterConfig;

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

		JaasTestUtils.initJaasLoginModule(TestLoginModule.class);
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

			Callback[] callbacks = new Callback[] { nameCallback };
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
				.andStubReturn("client-login");
		expect(
				this.mockFilterConfig
						.getInitParameter(JAASLoginFilter.SESSION_USERNAME_PARAM))
				.andStubReturn(testUsernameAttributeName);

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
