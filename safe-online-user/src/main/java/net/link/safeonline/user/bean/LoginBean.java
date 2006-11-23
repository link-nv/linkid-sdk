package net.link.safeonline.user.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.user.Login;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.core.FacesMessages;

@Stateless
@Name("login")
@LocalBinding(jndiBinding = "SafeOnline/user/LoginBean/local")
public class LoginBean implements Login {

	private static final Log LOG = LogFactory.getLog(LoginBean.class);

	@In
	Context sessionContext;

	private String username;

	private String password;

	@EJB
	private AuthenticationService authenticationService;

	@In(create = true)
	FacesMessages facesMessages;

	public String getPassword() {
		return "";
	}

	public String getUsername() {
		return this.username;
	}

	public String login() {
		LOG.debug("login with username: " + this.username);

		boolean authenticated = this.authenticationService.authenticate(
				UserRegistrationService.SAFE_ONLINE_USER_APPLICATION_NAME,
				this.username, new String(this.password));
		if (!authenticated) {
			this.facesMessages.add("username", "login failed");
			Seam.invalidateSession();
			return null;
		}

		this.sessionContext.set("username", this.username);
		this.sessionContext.set("password", this.password);

		return "login-success";
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String logout() {
		this.sessionContext.set("username", null);
		this.sessionContext.set("password", null);
		Seam.invalidateSession();
		return "logout-success";
	}
}
