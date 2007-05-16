/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.Device;
import net.link.safeonline.authentication.service.AuthenticationService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("device")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "DeviceBean/local")
@EJB(name = "ejb/AuthenticationService", beanInterface = AuthenticationService.class, beanName = "AuthenticationServiceBean")
public class DeviceBean implements Device {

	private static final Log LOG = LogFactory.getLog(DeviceBean.class);

	private String selection;

	@In(create = true)
	FacesMessages facesMessages;

	@Remove
	@Destroy
	public void destroyCallback() {
		this.selection = null;
	}

	@Resource
	private SessionContext context;

	@SuppressWarnings("unused")
	@Out(required = false, scope = ScopeType.SESSION)
	private AuthenticationService authenticationService;

	public String getSelection() {
		return this.selection;
	}

	public void setSelection(String deviceSelection) {
		this.selection = deviceSelection;
	}

	public String next() {
		LOG.debug("next: " + this.selection);
		if (null == this.selection) {
			String msg = "Please make a selection.";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}
		String outcome = findOutcome();
		if (null == outcome) {
			String msg = "Unsupported authentication device: " + this.selection;
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}

		/*
		 * Here we also init the authentication service instance.
		 */
		AuthenticationService authenticationService = (AuthenticationService) this.context
				.lookup("ejb/AuthenticationService");
		this.authenticationService = authenticationService;

		return outcome;
	}

	private String findOutcome() {
		/*
		 * Later on we could make next list dynamic somehow.
		 */
		if ("password".equals(this.selection)) {
			return "/username-password.xhtml";
		}
		if ("beid".equals(this.selection)) {
			return "/beid.xhtml";
		}
		return null;
	}
}
