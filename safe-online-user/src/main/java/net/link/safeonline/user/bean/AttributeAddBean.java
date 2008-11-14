/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.user.AttributeAdd;
import net.link.safeonline.user.UserConstants;

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
@Name("attributeAdd")
@LocalBinding(jndiBinding = AttributeAdd.JNDI_BINDING)
@SecurityDomain(UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class AttributeAddBean implements AttributeAdd {

    private static final Log   LOG                   = LogFactory.getLog(AttributeAddBean.class);

    @EJB(mappedName = IdentityService.JNDI_BINDING)
    private IdentityService    identityService;

    @In
    private AttributeDO        selectedAttribute;

    public static final String ATTRIBUTE_ADD_CONTEXT = "attributeAddContext";

    @DataModel(value = ATTRIBUTE_ADD_CONTEXT)
    private List<AttributeDO>  attributeAddContext;


    @Remove
    @Destroy
    public void destroyCallback() {

    }


    @In(create = true)
    FacesMessages facesMessages;


    @RolesAllowed(UserConstants.USER_ROLE)
    public String commit()
            throws AttributeTypeNotFoundException {

        LOG.debug("commit");
        try {
            this.identityService.addAttribute(this.attributeAddContext);
        } catch (PermissionDeniedException e) {
            String msg = "user not allowed to edit value for attribute";
            LOG.error(msg);
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "errorUserNotAllowedToEditAttribute");
            return null;
        }
        return "success";
    }

    @Factory(ATTRIBUTE_ADD_CONTEXT)
    @RolesAllowed(UserConstants.USER_ROLE)
    public void attributeEditContextFactory()
            throws AttributeTypeNotFoundException {

        this.attributeAddContext = this.identityService.getAttributeTemplate(this.selectedAttribute);
    }
}
