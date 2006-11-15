package net.link.safeonline.sdk.auth.jaas;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JAAS Login Module using the SafeOnline Authentication.
 * 
 * @author fcorneli
 * 
 */
public class SafeOnlineAuthenticationLoginModule implements LoginModule {

	private static final Log LOG = LogFactory
			.getLog(SafeOnlineAuthenticationLoginModule.class);

	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map<String, ?> sharedState, Map<String, ?> options) {
		LOG.debug("initialize");
	}

	public boolean login() throws LoginException {
		LOG.debug("login");
		return false;
	}

	public boolean commit() throws LoginException {
		LOG.debug("commit");
		return false;
	}

	public boolean abort() throws LoginException {
		LOG.debug("abort");
		return false;
	}

	public boolean logout() throws LoginException {
		LOG.debug("logout");
		return false;
	}
}
