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
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.FacesMessages;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.IdentityConfirmation;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.entity.AttributeTypeEntity;

@Stateful
@Name("identityConfirmation")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "IdentityConfirmationBean/local")
public class IdentityConfirmationBean implements IdentityConfirmation {

	private static final Log LOG = LogFactory
			.getLog(IdentityConfirmationBean.class);

	@In(value = "applicationId", required = true)
	private String application;

	@In(create = true)
	FacesMessages facesMessages;

	@In(required = true)
	private String target;

	@In(required = true)
	private String username;

	@EJB
	private IdentityService identityService;

	public String agree() {
		LOG.debug("agree");
		try {
			this.identityService.confirmIdentity(this.application);
		} catch (SubscriptionNotFoundException e) {
			String msg = "subscription not found.";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (ApplicationNotFoundException e) {
			String msg = "application not found.";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}

		redirectToApplication();

		return "success";
	}

	private void redirectToApplication() {
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
			return;
		}
		try {
			externalContext.redirect(redirectUrl);
		} catch (IOException e) {
			String msg = "IO error: " + e.getMessage();
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return;
		}
	}

	@Remove
	@Destroy
	public void destroyCallback() {
	}

	@Factory("identityConfirmationList")
	public List<AttributeTypeEntity> identityConfirmationListFactory() {
		LOG.debug("identityConfirmationList factory");
		try {
			List<AttributeTypeEntity> confirmationList = this.identityService
					.getIdentityAttributesToConfirm(this.application);
			return confirmationList;
		} catch (SubscriptionNotFoundException e) {
			String msg = "subscription not found.";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (ApplicationNotFoundException e) {
			String msg = "application not found.";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (ApplicationIdentityNotFoundException e) {
			String msg = "application identity not found.";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}
	}
}
