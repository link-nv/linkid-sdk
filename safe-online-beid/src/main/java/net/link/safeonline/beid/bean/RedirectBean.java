/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid.bean;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import net.link.safeonline.beid.BeidConstants;
import net.link.safeonline.beid.Redirect;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;

@Stateful
@Name("beidRedirect")
@LocalBinding(jndiBinding = BeidConstants.JNDI_PREFIX + "RedirectBean/local")
public class RedirectBean implements Redirect {

	private final static Log LOG = LogFactory.getLog(RedirectBean.class);

	private String redirectUrl;

	@Remove
	@Destroy
	public void destroyCallback() {
		LOG.debug("destroy");
	}

	@PostConstruct
	public void initCallback() {
		LOG.debug("init");
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext()
				.getSession(true);
		// this.redirectUrl = DeviceManager
		// .getDeviceExitServiceUrl(session);
		try {
			facesContext.getExternalContext().redirect(this.redirectUrl);
		} catch (IOException e) {
			LOG.debug("failed to redirect to: " + this.redirectUrl);
		}
		LOG.debug("redirect to " + this.redirectUrl);
	}

	public String getRedirectUrl() {
		return this.redirectUrl;
	}
}
