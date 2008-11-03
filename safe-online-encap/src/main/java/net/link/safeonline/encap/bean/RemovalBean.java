/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap.bean;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.ctrl.error.annotation.Error;
import net.link.safeonline.ctrl.error.annotation.ErrorHandling;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.encap.EncapConstants;
import net.link.safeonline.encap.Removal;
import net.link.safeonline.model.encap.EncapDeviceService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;


@Stateful
@Name("removal")
@LocalBinding(jndiBinding = Removal.JNDI_BINDING)
@Interceptors(ErrorMessageInterceptor.class)
public class RemovalBean implements Removal {

    private static final String MOBILE_ATTRIBUTE_LIST_NAME = "mobileAttributes";

    @EJB
    private EncapDeviceService  encapDeviceService;

    @Logger
    private Log                 log;

    @In(create = true)
    FacesMessages               facesMessages;

    @In(value = ProtocolContext.PROTOCOL_CONTEXT)
    private ProtocolContext     protocolContext;

    @DataModel(MOBILE_ATTRIBUTE_LIST_NAME)
    List<AttributeDO>           mobileAttributes;

    @DataModelSelection(MOBILE_ATTRIBUTE_LIST_NAME)
    private AttributeDO         selectedMobile;


    @Remove
    @Destroy
    public void destroyCallback() {

        this.log.debug("destroy");
    }

    public String mobileCancel() throws IOException {

        this.protocolContext.setSuccess(false);
        exit();
        return null;
    }

    private void exit() throws IOException {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        externalContext.redirect("./deviceexit");
    }

    private Locale getViewLocale() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();
        return viewLocale;
    }

    @Factory(MOBILE_ATTRIBUTE_LIST_NAME)
    public List<AttributeDO> mobileAttributesFactory() throws SubjectNotFoundException, DeviceNotFoundException {

        Locale locale = getViewLocale();
        this.mobileAttributes = this.encapDeviceService.getMobiles(this.protocolContext.getSubject(), locale);
        return this.mobileAttributes;
    }

    @ErrorHandling( { @Error(exceptionClass = MalformedURLException.class, messageId = "mobileCommunicationFailed") })
    public String mobileRemove() throws SubjectNotFoundException, MobileException, IOException, AttributeTypeNotFoundException {

        this.encapDeviceService.remove(this.protocolContext.getSubject(), this.selectedMobile.getStringValue());
        this.protocolContext.setSuccess(true);
        exit();
        return null;
    }

}
