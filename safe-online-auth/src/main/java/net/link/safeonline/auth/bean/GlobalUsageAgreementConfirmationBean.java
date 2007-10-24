package net.link.safeonline.auth.bean;

import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.auth.GlobalUsageAgreementConfirmation;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UsageAgreementService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

@Stateless
@Name("globalAgreementConfirmation")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "GlobalUsageAgreementConfirmationBean/local")
@SecurityDomain(AuthenticationConstants.SECURITY_DOMAIN)
public class GlobalUsageAgreementConfirmationBean implements
		GlobalUsageAgreementConfirmation {

	@Logger
	private Log log;

	@In(value = "applicationId", required = true)
	private String applicationId;

	@EJB
	private SubscriptionService subscriptionService;

	@EJB
	private UsageAgreementService usageAgreementService;

	@EJB
	private IdentityService identityService;

	@In(create = true)
	FacesMessages facesMessages;

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public String confirm() {

		this.log.debug("confirm global usage agreement");
		this.usageAgreementService.confirmGlobalUsageAgreementVersion();

		/*
		 * After successful confirmation we continue the workflow as usual.
		 */
		boolean subscriptionRequired;
		try {
			subscriptionRequired = !this.subscriptionService
					.isSubscribed(this.applicationId);
			if (!subscriptionRequired)
				try {
					subscriptionRequired = this.usageAgreementService
							.requiresUsageAgreementAcceptation(this.applicationId);
				} catch (SubscriptionNotFoundException e) {
					this.facesMessages.addFromResourceBundle(
							FacesMessage.SEVERITY_ERROR,
							"errorSubscriptionNotFound");
					return null;
				}
		} catch (ApplicationNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return null;
		}
		this.log.debug("subscription required: " + subscriptionRequired);
		if (true == subscriptionRequired)
			return "subscription-required";

		boolean confirmationRequired;
		try {
			confirmationRequired = this.identityService
					.isConfirmationRequired(this.applicationId);
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
		this.log.debug("confirmation required: " + confirmationRequired);
		if (true == confirmationRequired) {
			return "confirmation-required";
		}

		boolean hasMissingAttributes;
		try {
			hasMissingAttributes = this.identityService
					.hasMissingAttributes(this.applicationId);
		} catch (ApplicationNotFoundException e) {
			this.log.debug("application not found.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return null;
		} catch (ApplicationIdentityNotFoundException e) {
			this.log.debug("application identity not found.");
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

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public String getUsageAgreement() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Locale viewLocale = facesContext.getViewRoot().getLocale();
		return this.usageAgreementService
				.getGlobalUsageAgreementText(viewLocale.getLanguage());
	}
}
