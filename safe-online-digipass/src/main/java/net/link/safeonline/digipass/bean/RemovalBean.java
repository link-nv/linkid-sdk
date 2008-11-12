/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.digipass.bean;

import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.ctrl.error.annotation.Error;
import net.link.safeonline.ctrl.error.annotation.ErrorHandling;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.digipass.Removal;
import net.link.safeonline.model.digipass.DigipassDeviceService;
import net.link.safeonline.model.digipass.DigipassException;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;


@Stateful
@Name("digipassRemoval")
@LocalBinding(jndiBinding = Removal.JNDI_BINDING)
@Interceptors(ErrorMessageInterceptor.class)
public class RemovalBean implements Removal {

    private static final String   DIGIPASS_ATTRIBUTE_LIST_NAME = "digipassAttributes";

    @EJB
    private DigipassDeviceService digipassDeviceService;

    @Logger
    private Log                   log;

    @In(create = true)
    FacesMessages                 facesMessages;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private String                loginName;

    @DataModel(DIGIPASS_ATTRIBUTE_LIST_NAME)
    List<AttributeDO>             digipassAttributes;

    @DataModelSelection(DIGIPASS_ATTRIBUTE_LIST_NAME)
    private AttributeDO           selectedDigipass;


    @Remove
    @Destroy
    public void destroyCallback() {

        this.log.debug("destroy");
    }

    private Locale getViewLocale() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();
        return viewLocale;
    }

    @Factory(DIGIPASS_ATTRIBUTE_LIST_NAME)
    @ErrorHandling( { @Error(exceptionClass = SubjectNotFoundException.class, messageId = "errorSubjectNotFound", fieldId = "login") })
    public List<AttributeDO> digipassAttributesFactory()
            throws SubjectNotFoundException, PermissionDeniedException, DeviceNotFoundException {

        Locale locale = getViewLocale();
        this.digipassAttributes = this.digipassDeviceService.getDigipasses(this.loginName, locale);
        return this.digipassAttributes;
    }

    @ErrorHandling( { @Error(exceptionClass = DigipassException.class, messageId = "errorDeviceRegistrationNotFound") })
<<<<<<< HEAD:safe-online-digipass/src/main/java/net/link/safeonline/digipass/bean/RemovalBean.java
    public String remove()
            throws SubjectNotFoundException, DigipassException, PermissionDeniedException, DeviceNotFoundException,
            AttributeTypeNotFoundException {
=======
    public String remove() throws DigipassException, AttributeTypeNotFoundException {
>>>>>>> wicket-digipass:safe-online-digipass/src/main/java/net/link/safeonline/digipass/bean/RemovalBean.java

        this.log.debug("remove digipass: " + this.selectedDigipass.getStringValue() + " for user " + this.loginName);
        this.digipassDeviceService.remove(this.selectedDigipass.getStringValue());
        return "success";
    }

    public String getRegistrations()
            throws SubjectNotFoundException, PermissionDeniedException, DeviceNotFoundException {

        this.log.debug("get digipasses for: " + this.loginName);
        digipassAttributesFactory();
        return "";
    }

    public String getLoginName() {

        return this.loginName;
    }

    public void setLoginName(String loginName) {

        this.log.debug("set login name: " + loginName);
        this.loginName = loginName;
    }
}
