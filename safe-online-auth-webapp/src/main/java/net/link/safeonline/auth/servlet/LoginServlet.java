/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.webapp.GlobalConfirmationPage;
import net.link.safeonline.auth.webapp.IdentityConfirmationPage;
import net.link.safeonline.auth.webapp.IdentityUnavailablePage;
import net.link.safeonline.auth.webapp.MissingAttributesPage;
import net.link.safeonline.auth.webapp.RegisterDevicePage;
import net.link.safeonline.auth.webapp.SubscriptionPage;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.servlet.annotation.In;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The login servlet. A device (username-password or BeID) confirms successful login by setting the 'username' session attribute. Then the
 * device redirects to this login servlet. This login servlet will decide which is the next step in the authentication process.
 * 
 * 
 * @author fcorneli
 * 
 */
public class LoginServlet extends AbstractInjectionServlet {

    private static final long     serialVersionUID = 1L;

    private static final Log      LOG              = LogFactory.getLog(LoginServlet.class);

    public static final String    SERVLET_PATH     = "login";

    @EJB(mappedName = IdentityService.JNDI_BINDING)
    private IdentityService       identityService;

    @EJB(mappedName = SubscriptionService.JNDI_BINDING)
    private SubscriptionService   subscriptionService;

    @EJB(mappedName = DevicePolicyService.JNDI_BINDING)
    private DevicePolicyService   devicePolicyService;

    @EJB(mappedName = UsageAgreementService.JNDI_BINDING)
    private UsageAgreementService usageAgreementService;

    @In(LoginManager.AUTHENTICATION_DEVICE_ATTRIBUTE)
    DeviceEntity                  device;

    @In(LoginManager.APPLICATION_ID_ATTRIBUTE)
    long                          applicationId;


    @Override
    protected void invokeGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LOG.debug("doGet");
        HttpSession session = request.getSession();
        String language = request.getLocale().getLanguage();

        boolean devicePolicyCheck = performDevicePolicyCheck(session);
        if (false == devicePolicyCheck) {
            redirectToRegisterDevice(response);
            return;
        }
        HelpdeskLogger.add(session, "authn device OK", LogLevelType.INFO);

        boolean globalConfirmationRequired = performGlobalUsageAgreementCheck(language);
        if (true == globalConfirmationRequired) {
            redirectToGlobalConfirmation(response);
            return;
        }
        HelpdeskLogger.add(session, "global usage agreement confirmation found", LogLevelType.INFO);

        boolean subscriptionRequired = performSubscriptionCheck(language);
        if (true == subscriptionRequired) {
            redirectToSubscription(response);
            return;
        }
        HelpdeskLogger.add(session, "subscription found", LogLevelType.INFO);

        boolean confirmationRequired = performConfirmationCheck();
        if (true == confirmationRequired) {
            redirectToIdentityConfirmation(response);
            return;
        }
        HelpdeskLogger.add(session, "confirmation found", LogLevelType.INFO);

        boolean hasMissingAttributes = performMissingAttributesCheck();
        if (true == hasMissingAttributes) {
            redirectToMissingAttributes(response);
            return;
        }
        HelpdeskLogger.add(session, "necessary attributes found", LogLevelType.INFO);

        /*
         * We can commit the authentication process here.
         */
        response.sendRedirect("./exit");
    }

    private void redirectToRegisterDevice(HttpServletResponse response)
            throws IOException {

        response.sendRedirect(RegisterDevicePage.PATH);
    }

    private boolean performGlobalUsageAgreementCheck(String language) {

        return usageAgreementService.requiresGlobalUsageAgreementAcceptation(language);
    }

    private boolean performMissingAttributesCheck()
            throws ServletException {

        boolean hasMissingAttributes;
        try {
            hasMissingAttributes = identityService.hasMissingAttributes(applicationId);
        } catch (ApplicationNotFoundException e) {
            throw new ServletException("application not found");
        } catch (ApplicationIdentityNotFoundException e) {
            throw new ServletException("application identity not found");
        } catch (PermissionDeniedException e) {
            throw new ServletException("permission denied: " + e.getMessage());
        } catch (AttributeTypeNotFoundException e) {
            throw new ServletException("attribute type not found");
        }
        return hasMissingAttributes;
    }

    private boolean performConfirmationCheck()
            throws ServletException {

        boolean confirmationRequired;
        try {
            confirmationRequired = identityService.isConfirmationRequired(applicationId);
        } catch (SubscriptionNotFoundException e) {
            throw new ServletException("subscription not found");
        } catch (ApplicationNotFoundException e) {
            throw new ServletException("application not found");
        } catch (ApplicationIdentityNotFoundException e) {
            throw new ServletException("application identity not found");
        }
        LOG.debug("confirmation required: " + confirmationRequired);
        return confirmationRequired;
    }

    private boolean performSubscriptionCheck(String language)
            throws ServletException {

        boolean subscriptionRequired;
        try {
            subscriptionRequired = !subscriptionService.isSubscribed(applicationId);
            if (!subscriptionRequired) {
                try {
                    subscriptionRequired = usageAgreementService.requiresUsageAgreementAcceptation(applicationId, language);
                } catch (SubscriptionNotFoundException e) {
                    LOG.debug("subscription not found: " + applicationId);
                    throw new ServletException("subscription not found");
                }
            }
        } catch (ApplicationNotFoundException e) {
            LOG.debug("application not found: " + applicationId);
            throw new ServletException("application not found");
        }
        return subscriptionRequired;
    }

    /**
     * Check whether the used authentication device is sufficient for the given application.
     * 
     * @param session
     * @param applicationId
     * @param device
     * @throws ServletException
     */
    private boolean performDevicePolicyCheck(HttpSession session)
            throws ServletException {

        Set<DeviceEntity> requiredDevicePolicy = LoginManager.getRequiredDevices(session);
        List<DeviceEntity> devicePolicy;
        try {
            devicePolicy = devicePolicyService.getDevicePolicy(applicationId, requiredDevicePolicy);
        } catch (ApplicationNotFoundException e) {
            throw new ServletException("application not found: " + applicationId);
        } catch (EmptyDevicePolicyException e) {
            throw new ServletException("empty device policy");
        }
        if (devicePolicy.contains(device))
            return true;
        return false;
    }

    private void redirectToMissingAttributes(HttpServletResponse response)
            throws IOException, ServletException {

        List<AttributeDO> missingAttributes;
        try {
            missingAttributes = identityService.listMissingAttributes(applicationId, response.getLocale());
        } catch (ApplicationNotFoundException e) {
            throw new ServletException("application not found: " + applicationId);
        } catch (ApplicationIdentityNotFoundException e) {
            throw new ServletException("application identity not found: " + applicationId);
        } catch (PermissionDeniedException e) {
            throw new ServletException("permission denied: " + e.getMessage());
        } catch (AttributeTypeNotFoundException e) {
            throw new ServletException("attribute type not found: " + e.getMessage());
        }

        if (false == missingAttributes.isEmpty()) {
            for (AttributeDO missingAttribute : missingAttributes) {
                if (!missingAttribute.isEditable()) {
                    response.sendRedirect(IdentityUnavailablePage.PATH);
                    return;
                }
            }
        }

        response.sendRedirect(MissingAttributesPage.PATH);
    }

    private void redirectToIdentityConfirmation(HttpServletResponse response)
            throws IOException {

        response.sendRedirect(IdentityConfirmationPage.PATH);
    }

    private void redirectToSubscription(HttpServletResponse response)
            throws IOException {

        response.sendRedirect(SubscriptionPage.PATH);
    }

    private void redirectToGlobalConfirmation(HttpServletResponse response)
            throws IOException {

        response.sendRedirect(GlobalConfirmationPage.PATH);
    }
}
