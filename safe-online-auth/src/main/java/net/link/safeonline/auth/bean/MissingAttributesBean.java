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
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.MissingAttributes;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
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
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.faces.FacesMessages;


@Stateful
@Name("missingAttributes")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX + "MissingAttributesBean/local")
@SecurityDomain(AuthenticationConstants.SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class MissingAttributesBean implements MissingAttributes {

    private static final Log   LOG                     = LogFactory.getLog(MissingAttributesBean.class);

    @EJB
    private IdentityService    identityService;

    @In(value = LoginManager.APPLICATION_ID_ATTRIBUTE, required = true)
    private String             application;

    @In(create = true)
    FacesMessages              facesMessages;

    public static final String MISSING_ATTRIBUTE_LIST  = "missingAttributeList";

    public static final String OPTIONAL_ATTRIBUTE_LIST = "optionalAttributeList";

    @DataModel(MISSING_ATTRIBUTE_LIST)
    private List<AttributeDO>  missingAttributeList;

    @DataModel(OPTIONAL_ATTRIBUTE_LIST)
    private List<AttributeDO>  optionalAttributeList;


    @Factory(MISSING_ATTRIBUTE_LIST)
    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public void missingAttributeListFactory() throws ApplicationNotFoundException, ApplicationIdentityNotFoundException,
                                             PermissionDeniedException, AttributeTypeNotFoundException, AttributeUnavailableException {

        LOG.debug("missing attribute list factory");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();
        this.missingAttributeList = this.identityService.listMissingAttributes(this.application, viewLocale);
    }

    @Factory(OPTIONAL_ATTRIBUTE_LIST)
    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public void optionalAttributeListFactory() throws ApplicationNotFoundException, ApplicationIdentityNotFoundException,
                                              PermissionDeniedException, AttributeTypeNotFoundException, AttributeUnavailableException {

        LOG.debug("optional attribute list factory");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();
        this.optionalAttributeList = this.identityService.listOptionalAttributes(this.application, viewLocale);
    }

    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public String save() {

        LOG.debug("save");
        for (AttributeDO attribute : this.missingAttributeList) {
            LOG.debug("required attribute to save: " + attribute);
            try {
                this.identityService.saveAttribute(attribute);
            } catch (PermissionDeniedException e) {
                LOG.debug("permission denied for attribute: " + attribute.getName());
                this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "errorPermissionDeniedForAttribute",
                        attribute.getName());
                return null;
            } catch (AttributeTypeNotFoundException e) {
                LOG.debug("attribute type not found: " + attribute.getName());
                this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "errorAttributeTypeNotFoundSpecific",
                        attribute.getName());
                return null;
            }
        }
        for (AttributeDO attribute : this.optionalAttributeList) {
            LOG.debug("optional attribute to save: " + attribute);
            try {
                this.identityService.saveAttribute(attribute);
            } catch (PermissionDeniedException e) {
                LOG.debug("permission denied for attribute: " + attribute.getName());
                this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "errorPermissionDeniedForAttribute",
                        attribute.getName());
                return null;
            } catch (AttributeTypeNotFoundException e) {
                LOG.debug("attribute type not found: " + attribute.getName());
                this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "errorAttributeTypeNotFoundSpecific",
                        attribute.getName());
                return null;
            }
        }

        HelpdeskLogger.add("missing attributes saved for application: " + this.application, LogLevelType.INFO);

        AuthenticationUtils.commitAuthentication(this.facesMessages);

        return null;
    }

    @Remove
    @Destroy
    public void destroyCallback() {

    }
}
