package net.link.safeonline.auth.bean;

import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.auth.GlobalUsageAgreementConfirmation;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

@Stateless
@Name("globalAgreementConfirmation")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "GlobalUsageAgreementConfirmationBean/local")
@SecurityDomain(AuthenticationConstants.SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class GlobalUsageAgreementConfirmationBean implements
		GlobalUsageAgreementConfirmation {

	@Logger
	private Log log;

	@In(value = LoginManager.APPLICATION_ID_ATTRIBUTE, required = true)
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
	public String confirm() throws ApplicationNotFoundException,
			SubscriptionNotFoundException,
			ApplicationIdentityNotFoundException, PermissionDeniedException,
			AttributeTypeNotFoundException {

		this.log.debug("confirm global usage agreement");
		this.usageAgreementService.confirmGlobalUsageAgreementVersion();

		/*
		 * After successful confirmation we continue the workflow as usual.
		 */
		boolean subscriptionRequired = !this.subscriptionService
				.isSubscribed(this.applicationId);
		if (!subscriptionRequired) {
			subscriptionRequired = this.usageAgreementService
					.requiresUsageAgreementAcceptation(this.applicationId);
		}
		this.log.debug("subscription required: " + subscriptionRequired);
		if (true == subscriptionRequired)
			return "subscription-required";

		boolean confirmationRequired = this.identityService
				.isConfirmationRequired(this.applicationId);
		this.log.debug("confirmation required: " + confirmationRequired);
		if (true == confirmationRequired) {
			return "confirmation-required";
		}

		boolean hasMissingAttributes = this.identityService
				.hasMissingAttributes(this.applicationId);

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
