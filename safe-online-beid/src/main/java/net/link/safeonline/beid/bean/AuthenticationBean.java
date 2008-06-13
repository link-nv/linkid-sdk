/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid.bean;

import java.io.IOException;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import net.link.safeonline.beid.Authentication;
import net.link.safeonline.beid.BeidConstants;
import net.link.safeonline.device.sdk.AuthenticationContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;

@Stateful
@Name("beidAuthentication")
@LocalBinding(jndiBinding = BeidConstants.JNDI_PREFIX
		+ "AuthenticationBean/local")
public class AuthenticationBean implements Authentication {

	private final static Log LOG = LogFactory.getLog(AuthenticationBean.class);

	@In(create = true)
	FacesMessages facesMessages;

	@Remove
	@Destroy
	public void destroyCallback() {
		LOG.debug("destroy");
	}

	public String cancel() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();

		HttpSession httpSession = (HttpSession) externalContext
				.getSession(true);

		AuthenticationContext authenticationContext = AuthenticationContext
				.getAuthenticationContext(httpSession);
		authenticationContext
				.setIssuer(net.link.safeonline.model.beid.BeIdConstants.BEID_DEVICE_ID);

		String redirectUrl = "authenticationexit";
		LOG.debug("redirecting to: " + redirectUrl);
		try {
			externalContext.redirect(redirectUrl);
		} catch (IOException e) {
			LOG.debug("IO error: " + e.getMessage());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorIO");
			return null;
		}
		return null;
	}

}
