/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.FacesMessages;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.UsernamePasswordLogon;
import net.link.safeonline.authentication.service.AuthenticationService;

@Stateful
@Name("usernamePasswordLogon")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "UsernamePasswordLogonBean/local")
public class UsernamePasswordLogonBean implements UsernamePasswordLogon {

	private static final Log LOG = LogFactory
			.getLog(UsernamePasswordLogonBean.class);

	private String username;

	private String password;

	@In(value = "applicationId", required = true)
	private String application;

	@In(required = true)
	private String target;

	@In(create = true)
	FacesMessages facesMessages;

	@EJB
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
		LOG.debug("login: " + this.username + " to application "
				+ this.application);

		boolean authenticated = this.authenticationService.authenticate(
				this.application, this.username, this.password);

		if (false == authenticated) {
			String msg = "Authentication failed.";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}

		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		LOG.debug("redirecting to:  " + this.target);
		String redirectUrl;
		try {
			redirectUrl = this.target + "?username="
					+ URLEncoder.encode(this.username, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			String msg = "UnsupportedEncoding: " + e.getMessage();
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}
		try {
			externalContext.redirect(redirectUrl);
		} catch (IOException e) {
			String msg = "IO error: " + e.getMessage();
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}

		return null;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
