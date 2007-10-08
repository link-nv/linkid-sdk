/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.faces.application.FacesMessage;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.AuthenticationSubscription;
import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.authentication.exception.AlreadySubscribedException;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

@Stateless
@Name("authSubscription")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "AuthenticationSubscriptionBean/local")
@SecurityDomain(AuthenticationConstants.SECURITY_DOMAIN)
public class AuthenticationSubscriptionBean implements
		AuthenticationSubscription {

	@Logger
	private Log log;

	@In(value = "applicationId", required = true)
	private String applicationId;

	@EJB
	private SubscriptionService subscriptionService;

	@In(create = true)
	FacesMessages facesMessages;

	@EJB
	private IdentityService identityService;

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public String subscribe() {
		log.debug("subscribe to application #0", this.applicationId);
		try {
			this.subscriptionService.subscribe(this.applicationId);
		} catch (ApplicationNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return null;
		} catch (AlreadySubscribedException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorAlreadySubscribed");
			return null;
		} catch (PermissionDeniedException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return null;
		}

		/*
		 * After successful subscription we continue the workflow as usual.
		 */

		boolean confirmationRequired;
		try {
			confirmationRequired = this.identityService
					.isConfirmationRequired(applicationId);
		} catch (SubscriptionNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorSubscriptionNotFound");
			return null;
		} catch (ApplicationNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return null;
		} catch (ApplicationIdentityNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR,
					"errorApplicationIdentityNotFound");
			return null;
		}
		log.debug("confirmation required: " + confirmationRequired);
		if (true == confirmationRequired) {
			return "confirmation-required";
		}

		boolean hasMissingAttributes;
		try {
			hasMissingAttributes = this.identityService
					.hasMissingAttributes(this.applicationId);
		} catch (ApplicationNotFoundException e) {
			log.debug("application not found.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return null;
		} catch (ApplicationIdentityNotFoundException e) {
			log.debug("application identity not found.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return null;
		}

		if (true == hasMissingAttributes) {
			return "missing-attributes";
		}

		AuthenticationUtils.commitAuthentication(this.facesMessages);

		return null;
	}
}
