/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.AuthenticationSubscription;
import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.authentication.exception.AlreadySubscribedException;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.shared.helpdesk.LogLevelType;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;


@Stateless
@Name("authSubscription")
@LocalBinding(jndiBinding = AuthenticationSubscription.JNDI_BINDING)
@SecurityDomain(AuthenticationConstants.SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class AuthenticationSubscriptionBean extends AbstractExitBean implements AuthenticationSubscription {

    @Logger
    private Log                   log;

    @In(value = LoginManager.APPLICATION_ID_ATTRIBUTE, required = true)
    private long                  applicationId;

    @EJB(mappedName = SubscriptionService.JNDI_BINDING)
    private SubscriptionService   subscriptionService;

    @EJB(mappedName = UsageAgreementService.JNDI_BINDING)
    private UsageAgreementService usageAgreementService;

    @In(create = true)
    FacesMessages                 facesMessages;

    @EJB(mappedName = IdentityService.JNDI_BINDING)
    private IdentityService       identityService;


    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public String subscribe()
            throws ApplicationNotFoundException, AlreadySubscribedException, PermissionDeniedException, SubscriptionNotFoundException,
            ApplicationIdentityNotFoundException, AttributeTypeNotFoundException, AttributeUnavailableException {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();

        if (!subscriptionService.isSubscribed(applicationId)) {
            log.debug("subscribe to application #0", applicationId);
            subscriptionService.subscribe(applicationId);
            HelpdeskLogger.add("subscribed to application: " + applicationId, LogLevelType.INFO);
        }

        if (usageAgreementService.requiresUsageAgreementAcceptation(applicationId, viewLocale.getLanguage())) {
            log.debug("confirm usage agreement for application #0", applicationId);
            usageAgreementService.confirmUsageAgreementVersion(applicationId);
            HelpdeskLogger.add("confirmed usage agreement of application: " + applicationId, LogLevelType.INFO);
        }

        /*
         * After successful subscription we continue the workflow as usual.
         */

        boolean confirmationRequired = identityService.isConfirmationRequired(applicationId);
        log.debug("confirmation required: " + confirmationRequired);
        if (true == confirmationRequired)
            return "confirmation-required";

        boolean hasMissingAttributes = identityService.hasMissingAttributes(applicationId);

        if (true == hasMissingAttributes)
            return "missing-attributes";

        AuthenticationUtils.commitAuthentication(facesMessages);

        return null;
    }

    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public String getUsageAgreement() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();
        try {
            return usageAgreementService.getUsageAgreementText(applicationId, viewLocale.getLanguage());
        } catch (ApplicationNotFoundException e) {
            log.debug("application not found.");
            facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
            return null;
        }
    }

    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public String getApplicationUrl() {

        return findApplicationUrl();
    }
}
