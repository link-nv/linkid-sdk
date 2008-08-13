/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap.bean;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.ctrl.error.annotation.Error;
import net.link.safeonline.ctrl.error.annotation.ErrorHandling;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.encap.EncapConstants;
import net.link.safeonline.encap.Removal;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionPK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.device.DeviceSubjectEntity;
import net.link.safeonline.model.encap.EncapDeviceService;
import net.link.safeonline.service.SubjectService;

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
@LocalBinding(jndiBinding = EncapConstants.JNDI_PREFIX + "RemovalBean/local")
@Interceptors(ErrorMessageInterceptor.class)
public class RemovalBean implements Removal {

    private static final String  MOBILE_ATTRIBUTE_LIST_NAME = "mobileAttributes";

    @EJB
    private EncapDeviceService   encapDeviceService;

    @EJB
    private DeviceDAO            deviceDAO;

    @EJB
    private AttributeTypeDAO     attributeTypeDAO;

    @EJB
    private AttributeDAO         attributeDAO;

    @EJB
    private SubjectService       subjectService;

    @EJB
    private SamlAuthorityService samlAuthorityService;

    @Logger
    private Log                  log;

    @In(create = true)
    FacesMessages                facesMessages;

    @In
    private String               userId;

    @In(value = ProtocolContext.PROTOCOL_CONTEXT)
    private ProtocolContext      protocolContext;

    @DataModel(MOBILE_ATTRIBUTE_LIST_NAME)
    List<AttributeDO>            mobileAttributes;

    @DataModelSelection(MOBILE_ATTRIBUTE_LIST_NAME)
    private AttributeDO          selectedMobile;


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
        this.protocolContext.setValidity(this.samlAuthorityService.getAuthnAssertionValidity());
    }

    private Locale getViewLocale() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();
        return viewLocale;
    }

    @Factory(MOBILE_ATTRIBUTE_LIST_NAME)
    public List<AttributeDO> mobileAttributesFactory() throws SubjectNotFoundException, DeviceNotFoundException {

        Locale locale = getViewLocale();
        this.mobileAttributes = listAttributes(net.link.safeonline.model.encap.EncapConstants.ENCAP_DEVICE_ID, locale);
        return this.mobileAttributes;
    }

    private List<AttributeDO> listAttributes(String deviceId, Locale locale) throws DeviceNotFoundException,
            SubjectNotFoundException {

        this.log.debug("list attributes for device: " + deviceId);
        DeviceEntity device = this.deviceDAO.getDevice(deviceId);
        List<AttributeTypeEntity> deviceAttributeTypes = new LinkedList<AttributeTypeEntity>();
        deviceAttributeTypes.add(device.getAttributeType());
        List<AttributeDO> attributes = new LinkedList<AttributeDO>();

        DeviceSubjectEntity deviceSubject = this.subjectService.getDeviceSubject(this.userId);
        for (SubjectEntity deviceRegistration : deviceSubject.getRegistrations()) {
            List<AttributeDO> mobileAttribute = listAttributes(deviceRegistration, deviceAttributeTypes, locale);
            attributes.addAll(mobileAttribute);
        }
        return attributes;
    }

    private List<AttributeDO> listAttributes(SubjectEntity deviceRegistration,
            List<AttributeTypeEntity> deviceAttributeTypes, Locale locale) {

        List<AttributeDO> attributes = new LinkedList<AttributeDO>();

        String language;
        if (null == locale) {
            language = null;
        } else {
            language = locale.getLanguage();
        }

        this.log.debug("# device attributes: " + deviceAttributeTypes.size());
        for (AttributeTypeEntity attributeType : deviceAttributeTypes) {
            this.log.debug("attribute type: " + attributeType.getName());
            if (false == attributeType.isUserVisible()) {
                continue;
            }

            boolean multivalued = attributeType.isMultivalued();
            String name = attributeType.getName();
            DatatypeType type = attributeType.getType();
            boolean editable = attributeType.isUserEditable();
            boolean dataMining = false;
            String humanReabableName = null;
            String description = null;
            if (null != language) {
                AttributeTypeDescriptionEntity attributeTypeDescription = this.attributeTypeDAO
                        .findDescription(new AttributeTypeDescriptionPK(name, language));
                if (null != attributeTypeDescription) {
                    humanReabableName = attributeTypeDescription.getName();
                    description = attributeTypeDescription.getDescription();
                }
            }
            List<AttributeEntity> attributeList = this.attributeDAO.listAttributes(deviceRegistration, attributeType);
            for (AttributeEntity attribute : attributeList) {
                String stringValue;
                Boolean booleanValue;
                long index;
                if (null != attribute) {
                    stringValue = attribute.getStringValue();
                    booleanValue = attribute.getBooleanValue();
                    index = attribute.getAttributeIndex();
                } else {
                    stringValue = null;
                    booleanValue = null;
                    index = 0;
                }
                AttributeDO attributeView = new AttributeDO(name, type, multivalued, index, humanReabableName,
                        description, editable, dataMining, stringValue, booleanValue);
                attributes.add(attributeView);
            }
        }
        return attributes;
    }

    @ErrorHandling( { @Error(exceptionClass = MalformedURLException.class, messageId = "mobileCommunicationFailed") })
    public String mobileRemove() throws SubjectNotFoundException, MobileException, IOException {

        this.encapDeviceService.remove(this.userId, this.selectedMobile.getStringValue());
        this.protocolContext.setSuccess(true);
        exit();
        return null;
    }

}
