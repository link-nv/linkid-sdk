/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.UsernamePasswordLogon;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("usernamePasswordLogon")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "UsernamePasswordLogonBean/local")
public class UsernamePasswordLogonBean extends AbstractLoginBean implements
		UsernamePasswordLogon {

	private static final Log LOG = LogFactory
			.getLog(UsernamePasswordLogonBean.class);

	private String username;

	private String password;

	@In(create = true)
	FacesMessages facesMessages;

	@In
	private AuthenticationService authenticationService;

	@Remove
	@Destroy
	public void destroyCallback() {
		this.username = null;
		this.password = null;
	}

	public String getPassword() {
		return this.password;
	}

	public String getUsername() {
		return this.username;
	}

	public String login() {
		LOG.debug("login: " + this.username);
		super.clearUsername();

		try {
			boolean authenticated = this.authenticationService.authenticate(
					this.username, this.password);
			if (false == authenticated) {
				/*
				 * The abort will be correctly handled by the authentication
				 * service manager. That way we allow the user to retry the
				 * initial authentication step.
				 */
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
				return null;
			}
		} catch (SubjectNotFoundException e) {
			this.facesMessages.addToControlFromResourceBundle("username",
					FacesMessage.SEVERITY_ERROR, "subjectNotFoundMsg");
			return null;
		} catch (DeviceNotFoundException e) {
			this.facesMessages.add("password device not configured");
			return null;
		}

		super.login(this.username);
		return null;
	}

	public static final String AUTH_SERVICE_ATTRIBUTE = "authenticationService";

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
