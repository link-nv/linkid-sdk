/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import java.util.List;
import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.FacesContext;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.auth.IdentityConfirmation;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.IdentityConfirmationRequiredException;
import net.link.safeonline.authentication.exception.MissingAttributeException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.IdentityService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("identityConfirmation")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "IdentityConfirmationBean/local")
@SecurityDomain(AuthenticationConstants.SECURITY_DOMAIN)
public class IdentityConfirmationBean implements IdentityConfirmation {

	private static final Log LOG = LogFactory
			.getLog(IdentityConfirmationBean.class);

	@In(value = "applicationId", required = true)
	private String application;

	@In(create = true)
	FacesMessages facesMessages;

	@EJB
	private IdentityService identityService;

	@In
	private Context sessionContext;

	@In
	private AuthenticationService authenticationService;

	private void commitAuthentication() throws SubscriptionNotFoundException,
			ApplicationNotFoundException, ApplicationIdentityNotFoundException,
			IdentityConfirmationRequiredException, MissingAttributeException {
		try {
			this.authenticationService.commitAuthentication(this.application);
		} finally {
			/*
			 * We have to remove the authentication service reference from the
			 * http session, else the authentication service manager will try to
			 * abort on it.
			 */
			cleanupAuthenticationServiceReference();
		}
	}

	public static final String AUTH_SERVICE_ATTRIBUTE = "authenticationService";

	private void cleanupAuthenticationServiceReference() {
		this.sessionContext.set(AUTH_SERVICE_ATTRIBUTE, null);
	}

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public String agree() {
		LOG.debug("agree");
		boolean hasMissingAttributes;
		try {
			this.identityService.confirmIdentity(this.application);
			hasMissingAttributes = this.identityService
					.hasMissingAttributes(this.application);
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

		if (true == hasMissingAttributes) {
			return "missing-attributes";
		}

		try {
			commitAuthentication();
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
		} catch (IdentityConfirmationRequiredException e) {
			String msg = "identity confirmation required.";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (MissingAttributeException e) {
			String msg = "missing attributes.";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}

		AuthenticationUtils.redirectToApplication(this.facesMessages);

		return null;
	}

	@Remove
	@Destroy
	public void destroyCallback() {
	}

	@Factory("identityConfirmationList")
	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public List<AttributeDO> identityConfirmationListFactory() {
		LOG.debug("identityConfirmationList factory");
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Locale viewLocale = facesContext.getViewRoot().getLocale();
		try {
			List<AttributeDO> confirmationList = this.identityService
					.listIdentityAttributesToConfirm(this.application,
							viewLocale);
			LOG.debug("confirmation list: " + confirmationList);
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
