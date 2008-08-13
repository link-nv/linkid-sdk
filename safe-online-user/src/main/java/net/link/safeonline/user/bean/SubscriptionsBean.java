/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.bean;

import java.util.List;
import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.user.Subscriptions;
import net.link.safeonline.user.UserConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.faces.FacesMessages;


@Stateful
@Name("subscriptions")
@LocalBinding(jndiBinding = UserConstants.JNDI_PREFIX + "SubscriptionsBean/local")
@SecurityDomain(UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class SubscriptionsBean implements Subscriptions {

    private static final Log         LOG = LogFactory.getLog(SubscriptionsBean.class);

    @EJB
    private SubscriptionService      subscriptionService;

    @EJB
    private IdentityService          identityService;

    @EJB
    private UsageAgreementService    usageAgreementService;

    @EJB
    private SubjectManager           subjectManager;

    @In(create = true)
    FacesMessages                    facesMessages;

    @SuppressWarnings("unused")
    @DataModel
    private List<SubscriptionEntity> subscriptionList;

    @DataModelSelection("subscriptionList")
    @Out(value = "selectedSubscription", required = false, scope = ScopeType.SESSION)
    private SubscriptionEntity       selectedSubscription;

    @SuppressWarnings("unused")
    @Out(value = "confirmedIdentityAttributes", required = false)
    private List<AttributeDO>        confirmedIdentityAttributes;


    @RolesAllowed(UserConstants.USER_ROLE)
    @Factory("subscriptionList")
    public void subscriptionListFactory() {

        LOG.debug("subscription list factory");
        this.subscriptionList = this.subscriptionService.listSubscriptions();

    }

    @RolesAllowed(UserConstants.USER_ROLE)
    public String viewSubscription() throws SubscriptionNotFoundException, ApplicationNotFoundException,
            ApplicationIdentityNotFoundException {

        String applicationName = this.selectedSubscription.getApplication().getName();
        LOG.debug("view subscription: " + applicationName);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();
        this.confirmedIdentityAttributes = this.identityService.listConfirmedIdentity(applicationName, viewLocale);
        return "view-subscription";
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    public String unsubscribe() throws SubscriptionNotFoundException, ApplicationNotFoundException {

        LOG.debug("unsubscribe from: " + this.selectedSubscription.getApplication().getName());
        String applicationName = this.selectedSubscription.getApplication().getName();
        try {
            this.subscriptionService.unsubscribe(applicationName);
        } catch (PermissionDeniedException e) {
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "errorUserMayNotUnsubscribeFrom",
                    applicationName);
            return null;
        }
        subscriptionListFactory();
        return "unsubscribed";
    }

    @Remove
    @Destroy
    public void destroyCallback() {

    }

    @RolesAllowed(UserConstants.USER_ROLE)
    public String getUsageAgreement() throws ApplicationNotFoundException {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();
        if (null == this.selectedSubscription)
            return null;
        return this.usageAgreementService.getUsageAgreementText(this.selectedSubscription.getApplication().getName(),
                viewLocale.getLanguage(), this.selectedSubscription.getConfirmedUsageAgreementVersion());
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    public String getGlobalUsageAgreement() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();
        SubjectEntity subject = this.subjectManager.getCallerSubject();
        return this.usageAgreementService.getGlobalUsageAgreementText(viewLocale.getLanguage(), subject
                .getConfirmedUsageAgreementVersion());
    }
}
