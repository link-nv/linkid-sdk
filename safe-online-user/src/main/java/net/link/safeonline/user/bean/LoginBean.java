package net.link.safeonline.user.bean;

import javax.ejb.Stateless;

import net.link.safeonline.user.Login;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Name;

@Stateless
@Name("login")
@LocalBinding(jndiBinding = "SafeOnline/user/LoginBean/local")
public class LoginBean implements Login {

	private static final Log LOG = LogFactory.getLog(LoginBean.class);

	private String username;

	private String password;

	public String getPassword() {
		return this.password;
	}

	public String getUsername() {
		return this.username;
	}

	public String login() {
		LOG.debug("login with username: " + this.username);
		return null;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
