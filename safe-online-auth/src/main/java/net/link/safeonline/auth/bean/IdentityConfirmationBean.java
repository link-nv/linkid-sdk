/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.auth.IdentityConfirmation;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.shared.helpdesk.LogLevelType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;


@Stateful
@Name("identityConfirmation")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX + "IdentityConfirmationBean/local")
@SecurityDomain(AuthenticationConstants.SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class IdentityConfirmationBean extends AbstractExitBean implements IdentityConfirmation {

    private static final Log LOG = LogFactory.getLog(IdentityConfirmationBean.class);

    @In(value = LoginManager.APPLICATION_ID_ATTRIBUTE, required = true)
    private String           application;

    @In(create = true)
    FacesMessages            facesMessages;

    @EJB
    private IdentityService  identityService;


    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public String agree() throws ApplicationNotFoundException, ApplicationIdentityNotFoundException,
            PermissionDeniedException, AttributeTypeNotFoundException, SubscriptionNotFoundException {

        LOG.debug("agree");
        this.identityService.confirmIdentity(this.application);
        HelpdeskLogger.add("confirmed application identity for " + this.application, LogLevelType.INFO);

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();
        List<AttributeDO> missingAttributes = this.identityService.listMissingAttributes(this.application, viewLocale);

        if (false == missingAttributes.isEmpty()) {
            for (AttributeDO missingAttribute : missingAttributes) {
                if (!missingAttribute.isEditable())
                    return "identity-unavailable";
            }
            return "missing-attributes";
        }

        AuthenticationUtils.commitAuthentication(this.facesMessages);

        return null;
    }

    @Remove
    @Destroy
    public void destroyCallback() {

    }

    @Factory("identityConfirmationList")
    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public List<AttributeDO> identityConfirmationListFactory() throws SubscriptionNotFoundException,
            ApplicationNotFoundException, ApplicationIdentityNotFoundException {

        LOG.debug("identityConfirmationList factory");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();

        List<AttributeDO> confirmationList = this.identityService.listIdentityAttributesToConfirm(this.application,
                viewLocale);
        LOG.debug("confirmation list: " + confirmationList);
        return confirmationList;
    }

    @Factory("identityUnavailableList")
    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public List<AttributeDO> identityUnavailableListFactory() throws PermissionDeniedException,
            AttributeTypeNotFoundException, ApplicationNotFoundException, ApplicationIdentityNotFoundException {

        LOG.debug("identityUnavailableList factory");
        List<AttributeDO> unavailableList = new LinkedList<AttributeDO>();

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();
        List<AttributeDO> missingAttributes = this.identityService.listMissingAttributes(this.application, viewLocale);
        for (AttributeDO missingAttribute : missingAttributes) {
            if (!missingAttribute.isEditable()) {
                unavailableList.add(missingAttribute);
            }
        }
        LOG.debug("unavailable list: " + unavailableList);
        return unavailableList;
    }

    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public String getApplicationUrl() {

        return findApplicationUrl();
    }

}
