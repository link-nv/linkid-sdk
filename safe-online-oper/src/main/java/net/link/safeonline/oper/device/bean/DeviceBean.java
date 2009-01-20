/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.device.bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceClassNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DevicePropertyNotFoundException;
import net.link.safeonline.authentication.exception.ExistingDeviceException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.NodeService;
import net.link.safeonline.ctrl.Convertor;
import net.link.safeonline.ctrl.ConvertorUtil;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.ctrl.error.annotation.Error;
import net.link.safeonline.ctrl.error.annotation.ErrorHandling;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.device.Device;
import net.link.safeonline.pkix.exception.CertificateEncodingException;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.DeviceService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.fileupload.UploadedFile;
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
@Name("operDevice")
@LocalBinding(jndiBinding = Device.JNDI_BINDING)
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class DeviceBean implements Device {

    private static final Log     LOG                                          = LogFactory.getLog(DeviceBean.class);

    public static final String   OPER_DEVICE_LIST_NAME                        = "operDeviceList";

    public static final String   OPER_DEVICE_CLASS_LIST_NAME                  = "deviceClasses";

    public static final String   OPER_DEVICE_ATTRIBUTE_TYPE_LIST_NAME         = "attributeTypes";

    public static final String   OPER_DEVICE_USER_ATTRIBUTE_TYPE_LIST_NAME    = "userAttributeTypes";

    public static final String   OPER_DEVICE_DISABLE_ATTRIBUTE_TYPE_LIST_NAME = "disableAttributeTypes";

    @In(create = true)
    FacesMessages                facesMessages;

    @EJB(mappedName = DeviceService.JNDI_BINDING)
    private DeviceService        deviceService;

    @EJB(mappedName = AttributeTypeService.JNDI_BINDING)
    private AttributeTypeService attributeTypeService;

    @EJB(mappedName = NodeService.JNDI_BINDING)
    private NodeService          nodeService;

    private String               name;

    private String               deviceClass;

    private String               node;

    private String               authenticationPath;

    private String               authenticationWSPath;

    private String               registrationPath;

    private String               removalPath;

    private String               updatePath;

    private String               disablePath;

    private String               enablePath;

    private UploadedFile         certificate;

    private String               attributeType;

    private String               userAttributeType;

    private String               disableAttributeType;

    /*
     * Seam Data models
     */
    @DataModel(OPER_DEVICE_LIST_NAME)
    public List<DeviceEntity>    deviceList;

    @DataModelSelection(OPER_DEVICE_LIST_NAME)
    @Out(value = "selectedDevice", required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private DeviceEntity         selectedDevice;


    /*
     * Lifecyle
     */
    @Remove
    @Destroy
    public void destroyCallback() {

        LOG.debug("destroy");
    }

    /*
     * Factories
     */
    @Factory(OPER_DEVICE_LIST_NAME)
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void deviceListFactory() {

        LOG.debug("device list factory");
        deviceList = deviceService.listDevices();
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(OPER_DEVICE_CLASS_LIST_NAME)
    public List<SelectItem> deviceClassesFactory() {

        List<DeviceClassEntity> deviceClassesList = deviceService.listDeviceClasses();
        List<SelectItem> deviceClasses = ConvertorUtil.convert(deviceClassesList, new DeviceClassSelectItemConvertor());
        return deviceClasses;
    }


    static class DeviceClassSelectItemConvertor implements Convertor<DeviceClassEntity, SelectItem> {

        public SelectItem convert(DeviceClassEntity input) {

            SelectItem output = new SelectItem(input.getName());
            return output;
        }
    }


    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(OPER_DEVICE_ATTRIBUTE_TYPE_LIST_NAME)
    public List<SelectItem> attributeTypesFactory() {

        List<AttributeTypeEntity> attributeTypesList = attributeTypeService.listAttributeTypes();
        List<SelectItem> attributeTypes = ConvertorUtil.convert(attributeTypesList, new AttributeTypeSelectItemConvertor());
        return attributeTypes;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(OPER_DEVICE_USER_ATTRIBUTE_TYPE_LIST_NAME)
    public List<SelectItem> userAttributeTypesFactory() {

        List<AttributeTypeEntity> attributeTypesList = attributeTypeService.listAttributeTypes();
        List<SelectItem> attributeTypes = ConvertorUtil.convert(attributeTypesList, new AttributeTypeSelectItemConvertor());
        attributeTypes.add(0, new SelectItem(null, ""));
        return attributeTypes;

    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(OPER_DEVICE_DISABLE_ATTRIBUTE_TYPE_LIST_NAME)
    public List<SelectItem> disableAttributeTypesFactory() {

        List<AttributeTypeEntity> attributeTypesList = attributeTypeService.listAttributeTypes(DatatypeType.BOOLEAN);
        List<SelectItem> attributeTypes = ConvertorUtil.convert(attributeTypesList, new AttributeTypeSelectItemConvertor());
        attributeTypes.add(0, new SelectItem(null, ""));
        return attributeTypes;

    }


    static class AttributeTypeSelectItemConvertor implements Convertor<AttributeTypeEntity, SelectItem> {

        public SelectItem convert(AttributeTypeEntity input) {

            SelectItem output = new SelectItem(input.getName());
            return output;
        }
    }


    @Factory("deviceNodes")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public List<SelectItem> nodeFactory() {

        List<NodeEntity> nodeList = nodeService.listNodes();
        List<SelectItem> nodes = ConvertorUtil.convert(nodeList, new OlasEntitySelectItemConvertor());
        return nodes;
    }


    static class OlasEntitySelectItemConvertor implements Convertor<NodeEntity, SelectItem> {

        public SelectItem convert(NodeEntity input) {

            SelectItem output = new SelectItem(input.getName());
            return output;
        }
    }


    private byte[] getUpFileContent(UploadedFile file)
            throws IOException {

        InputStream inputStream = file.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /*
     * Actions
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @ErrorHandling( { @Error(exceptionClass = CertificateEncodingException.class, messageId = "errorX509Encoding", fieldId = "fileupload"),
            @Error(exceptionClass = IOException.class, messageId = "errorUploadCertificate") })
    public String add()
            throws ExistingDeviceException, CertificateEncodingException, DeviceClassNotFoundException, AttributeTypeNotFoundException,
            NodeNotFoundException, IOException, PermissionDeniedException {

        LOG.debug("add device: " + name);

        byte[] encodedCertificate = null;
        if (null != certificate) {
            encodedCertificate = getUpFileContent(certificate);
        }

        deviceService.addDevice(name, deviceClass, node, authenticationPath, authenticationWSPath, registrationPath, removalPath,
                updatePath, disablePath, enablePath, encodedCertificate, attributeType, userAttributeType, disableAttributeType);
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String remove()
            throws DeviceNotFoundException, DeviceDescriptionNotFoundException, DevicePropertyNotFoundException {

        LOG.debug("remove device: " + selectedDevice.getName());
        try {
            deviceService.removeDevice(selectedDevice.getName());
        } catch (PermissionDeniedException e) {
            LOG.debug("permission denied: " + e.getMessage());
            facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, e.getResourceMessage(), e.getResourceArgs());
            return null;
        }
        deviceListFactory();
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String edit() {

        LOG.debug("edit device: " + selectedDevice.getName());

        authenticationPath = selectedDevice.getAuthenticationPath();
        authenticationWSPath = selectedDevice.getAuthenticationWSPath();
        registrationPath = selectedDevice.getRegistrationPath();
        removalPath = selectedDevice.getRemovalPath();
        disablePath = selectedDevice.getDisablePath();
        enablePath = selectedDevice.getEnablePath();
        if (null != selectedDevice.getAttributeType()) {
            attributeType = selectedDevice.getAttributeType().getName();
        }
        if (null != selectedDevice.getUserAttributeType()) {
            userAttributeType = selectedDevice.getUserAttributeType().getName();
        }
        if (null != selectedDevice.getDisableAttributeType()) {
            disableAttributeType = selectedDevice.getDisableAttributeType().getName();
        }

        return "edit";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String save()
            throws DeviceNotFoundException, CertificateEncodingException, IOException, AttributeTypeNotFoundException,
            PermissionDeniedException {

        LOG.debug("save device: " + selectedDevice.getName());
        String deviceName = selectedDevice.getName();

        deviceService.updateAuthenticationPath(deviceName, authenticationPath);
        if (null != authenticationWSPath) {
            deviceService.updateAuthenticationWSPath(deviceName, authenticationWSPath);
        }
        if (null != registrationPath) {
            deviceService.updateRegistrationPath(deviceName, registrationPath);
        }
        if (null != removalPath) {
            deviceService.updateRemovalPath(deviceName, removalPath);
        }
        if (null != updatePath) {
            deviceService.updateUpdatePath(deviceName, updatePath);
        }
        if (null != disablePath) {
            deviceService.updateDisablePath(deviceName, disablePath);
        }
        if (null != enablePath) {
            deviceService.updateEnablePath(deviceName, enablePath);
        }

        if (null != certificate) {
            LOG.debug("updating device certificate");
            deviceService.updateDeviceCertificate(deviceName, getUpFileContent(certificate));
        }

        if (null != attributeType) {
            LOG.debug("updating attribute type");
            deviceService.updateAttributeType(deviceName, attributeType);
        }

        if (null != userAttributeType) {
            LOG.debug("updating user attribute type");
            deviceService.updateUserAttributeType(deviceName, userAttributeType);
        }
        if (null != disableAttributeType) {
            LOG.debug("updating disable attribute type");
            deviceService.updateDisableAttributeType(deviceName, disableAttributeType);
        }

        /*
         * Refresh the device
         */
        selectedDevice = deviceService.getDevice(deviceName);
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String view() {

        LOG.debug("view device: " + selectedDevice.getName());
        return "view";
    }

    /*
     * Accessors
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getName() {

        return name;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setName(String name) {

        this.name = name;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getDeviceClass() {

        return deviceClass;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setDeviceClass(String deviceClass) {

        this.deviceClass = deviceClass;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getNode() {

        return node;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setNode(String node) {

        this.node = node;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getAuthenticationPath() {

        return authenticationPath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setAuthenticationPath(String authenticationPath) {

        this.authenticationPath = authenticationPath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getAuthenticationWSPath() {

        return authenticationWSPath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setAuthenticationWSPath(String authenticationWSPath) {

        this.authenticationWSPath = authenticationWSPath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getRegistrationPath() {

        return registrationPath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setRegistrationPath(String registrationPath) {

        this.registrationPath = registrationPath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getRemovalPath() {

        return removalPath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setRemovalPath(String removalPath) {

        this.removalPath = removalPath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getUpdatePath() {

        return updatePath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setUpdatePath(String updatePath) {

        this.updatePath = updatePath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getDisablePath() {

        return disablePath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setDisablePath(String disablePath) {

        this.disablePath = disablePath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getEnablePath() {

        return enablePath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setEnablePath(String enablePath) {

        this.enablePath = enablePath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public UploadedFile getCertificate() {

        return certificate;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setCertificate(UploadedFile certificate) {

        this.certificate = certificate;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getAttributeType() {

        return attributeType;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setAttributeType(String attributeType) {

        this.attributeType = attributeType;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getUserAttributeType() {

        return userAttributeType;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setUserAttributeType(String userAttributeType) {

        this.userAttributeType = userAttributeType;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getDisableAttributeType() {

        return disableAttributeType;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setDisableAttributeType(String disableAttributeType) {

        this.disableAttributeType = disableAttributeType;
    }
}
