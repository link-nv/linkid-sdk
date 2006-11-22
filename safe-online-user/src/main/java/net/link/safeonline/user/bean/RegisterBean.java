package net.link.safeonline.user.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.service.ExistingUserException;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.user.Register;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.FacesMessages;

@Stateless
@Name("register")
@LocalBinding(jndiBinding = "SafeOnline/user/RegisterBean/local")
public class RegisterBean implements Register {

	private static final Log LOG = LogFactory.getLog(RegisterBean.class);

	private String name;

	private String login;

	private String password;

	@EJB
	private UserRegistrationService userRegistrationService;

	@In(create = true)
	FacesMessages facesMessages;

	public String getName() {
		return this.name;
	}

	public String getPassword() {
		return this.password;
	}

	public String getLogin() {
		return this.login;
	}

	public String register() {
		LOG.debug("registering: " + this.login);
		try {
			this.userRegistrationService.registerUser(this.login,
					this.password, this.name);
		} catch (ExistingUserException e) {
			this.facesMessages.add("login", "login already exists");
			return null;
		}
		return "success";
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setLogin(String login) {
		this.login = login;
	}
}
