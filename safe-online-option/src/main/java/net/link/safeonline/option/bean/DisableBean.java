/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.option.bean;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.data.DeviceRegistrationDO;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionPK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.option.Disable;
import net.link.safeonline.option.OptionConstants;
import net.link.safeonline.service.SubjectService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;


@Stateful
@Name("optionDisable")
@LocalBinding(jndiBinding = OptionConstants.JNDI_PREFIX + "DisableBean/local")
@Interceptors(ErrorMessageInterceptor.class)
public class DisableBean implements Disable {

    private static final String        REGISTRATIONS_LIST_NAME = "optionRegistrations";

    @EJB
    private DeviceDAO                  deviceDAO;

    @EJB
    private AttributeTypeDAO           attributeTypeDAO;

    @EJB
    private AttributeDAO               attributeDAO;

    @EJB
    private SubjectService             subjectService;

    @Logger
    private Log                        log;

    @In(create = true)
    FacesMessages                      facesMessages;

    @In
    private String                     userId;

    @In(value = ProtocolContext.PROTOCOL_CONTEXT)
    private ProtocolContext            protocolContext;

    @DataModel(value = REGISTRATIONS_LIST_NAME)
    private List<DeviceRegistrationDO> registrations;


    @Remove
    @Destroy
    public void destroyCallback() {

        this.log.debug("destroy");
    }

    public String cancel() throws IOException {

        this.protocolContext.setSuccess(false);
        exit();
        return null;
    }

    public String save() throws IOException, SubjectNotFoundException, AttributeNotFoundException {

        SubjectEntity subject = this.subjectService.getSubject(this.userId);

        for (DeviceRegistrationDO registration : this.registrations) {
            AttributeEntity disableAttribute = this.attributeDAO.getAttribute(registration.getDevice()
                    .getDisableAttributeType(), subject, registration.getAttributeIndex());
            disableAttribute.setBooleanValue(registration.isDisabled());
        }

        this.protocolContext.setSuccess(true);
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

    @Factory(REGISTRATIONS_LIST_NAME)
    public List<DeviceRegistrationDO> registrationsFactory() throws SubjectNotFoundException, DeviceNotFoundException {

        Locale locale = getViewLocale();
        this.registrations = listRegistrations(net.link.safeonline.model.option.OptionConstants.OPTION_DEVICE_ID,
                locale);
        return this.registrations;
    }

    private List<DeviceRegistrationDO> listRegistrations(String deviceId, Locale locale)
            throws DeviceNotFoundException, SubjectNotFoundException {

        this.log.debug("list registrations for device: " + deviceId);
        DeviceEntity device = this.deviceDAO.getDevice(deviceId);
        List<AttributeTypeEntity> deviceAttributeTypes = new LinkedList<AttributeTypeEntity>();
        deviceAttributeTypes.add(device.getAttributeType());

        SubjectEntity subject = this.subjectService.getSubject(this.userId);

        return listRegistrations(subject, device, locale);
    }

    private List<DeviceRegistrationDO> listRegistrations(SubjectEntity subject, DeviceEntity device, Locale locale) {

        List<DeviceRegistrationDO> deviceRegistrations = new LinkedList<DeviceRegistrationDO>();

        String humanReadableName = null;
        String description = null;
        AttributeTypeDescriptionEntity attributeTypeDescription = findAttributeTypeDescription(device
                .getUserAttributeType(), locale);
        if (null != attributeTypeDescription) {
            humanReadableName = attributeTypeDescription.getName();
            description = attributeTypeDescription.getDescription();
        }

        List<AttributeEntity> attributes = this.attributeDAO.listAttributes(subject, device.getAttributeType());
        for (AttributeEntity attribute : attributes) {

            AttributeEntity userAttribute = this.attributeDAO.findAttribute(subject, device.getUserAttributeType(),
                    attribute.getAttributeIndex());
            AttributeDO attributeView = new AttributeDO(device.getUserAttributeType().getName(), device
                    .getUserAttributeType().getType(), true, attribute.getAttributeIndex(), humanReadableName,
                    description, false, false, userAttribute.getStringValue(), null);

            AttributeEntity disableAttribute = this.attributeDAO.findAttribute(subject, device
                    .getDisableAttributeType(), attribute.getAttributeIndex());

            deviceRegistrations.add(new DeviceRegistrationDO(device, attributeView, disableAttribute.getBooleanValue(),
                    attribute.getAttributeIndex()));
        }

        return deviceRegistrations;
    }

    private AttributeTypeDescriptionEntity findAttributeTypeDescription(AttributeTypeEntity attributeType, Locale locale) {

        if (null == locale)
            return null;
        String language = locale.getLanguage();
        this.log.debug("trying language: " + language);
        AttributeTypeDescriptionEntity attributeTypeDescription = this.attributeTypeDAO
                .findDescription(new AttributeTypeDescriptionPK(attributeType.getName(), language));
        return attributeTypeDescription;
    }
}
