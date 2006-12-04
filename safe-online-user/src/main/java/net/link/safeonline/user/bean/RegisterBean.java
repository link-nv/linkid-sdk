package net.link.safeonline.user.bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.user.Register;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.FacesMessages;

/**
 * Component implementation of the register interface. This component cannot
 * live within the security domain of the SafeOnline user web application since
 * the user is not yet logged on onto the system.
 * 
 * @author fcorneli
 * 
 */
@Stateful
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

	public RegisterBean() {
		LOG.debug("constructor");
	}

	@PostConstruct
	public void postConstructCallback() {
		LOG.debug("post construct");
	}

	@PreDestroy
	public void preDestroyCallback() {
		LOG.debug("pre destroy");
	}

	public String getName() {
		LOG.debug("get name: " + this.name);
		return this.name;
	}

	public String getPassword() {
		return this.password;
	}

	public String getLogin() {
		LOG.debug("get login: " + this.login);
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

	@Remove
	@Destroy
	public void destroyCallback() {
		LOG.debug("destroy: " + this);
		this.name = null;
		this.login = null;
		this.password = null;
	}
}
