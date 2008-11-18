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

    private String               registrationPath;

    private String               removalPath;

    private String               updatePath;

    private String               disablePath;

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
        this.deviceList = this.deviceService.listDevices();
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(OPER_DEVICE_CLASS_LIST_NAME)
    public List<SelectItem> deviceClassesFactory() {

        List<DeviceClassEntity> deviceClassesList = this.deviceService.listDeviceClasses();
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

        List<AttributeTypeEntity> attributeTypesList = this.attributeTypeService.listAttributeTypes();
        List<SelectItem> attributeTypes = ConvertorUtil.convert(attributeTypesList, new AttributeTypeSelectItemConvertor());
        return attributeTypes;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(OPER_DEVICE_USER_ATTRIBUTE_TYPE_LIST_NAME)
    public List<SelectItem> userAttributeTypesFactory() {

        List<AttributeTypeEntity> attributeTypesList = this.attributeTypeService.listAttributeTypes();
        List<SelectItem> attributeTypes = ConvertorUtil.convert(attributeTypesList, new AttributeTypeSelectItemConvertor());
        attributeTypes.add(0, new SelectItem(null, ""));
        return attributeTypes;

    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(OPER_DEVICE_DISABLE_ATTRIBUTE_TYPE_LIST_NAME)
    public List<SelectItem> disableAttributeTypesFactory() {

        List<AttributeTypeEntity> attributeTypesList = this.attributeTypeService.listAttributeTypes(DatatypeType.BOOLEAN);
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

        List<NodeEntity> nodeList = this.nodeService.listNodes();
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

        LOG.debug("add device: " + this.name);

        byte[] encodedCertificate = null;
        if (null != this.certificate) {
            encodedCertificate = getUpFileContent(this.certificate);
        }

        this.deviceService.addDevice(this.name, this.deviceClass, this.node, this.authenticationPath, this.registrationPath,
                this.removalPath, this.updatePath, this.disablePath, encodedCertificate, this.attributeType, this.userAttributeType,
                this.disableAttributeType);
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String remove()
            throws DeviceNotFoundException, DeviceDescriptionNotFoundException, DevicePropertyNotFoundException {

        LOG.debug("remove device: " + this.selectedDevice.getName());
        try {
            this.deviceService.removeDevice(this.selectedDevice.getName());
        } catch (PermissionDeniedException e) {
            LOG.debug("permission denied: " + e.getMessage());
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, e.getResourceMessage(), e.getResourceArgs());
            return null;
        }
        deviceListFactory();
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String edit() {

        LOG.debug("edit device: " + this.selectedDevice.getName());

        this.authenticationPath = this.selectedDevice.getAuthenticationPath();
        this.registrationPath = this.selectedDevice.getRegistrationPath();
        this.removalPath = this.selectedDevice.getRemovalPath();
        if (null != this.selectedDevice.getAttributeType()) {
            this.attributeType = this.selectedDevice.getAttributeType().getName();
        }
        if (null != this.selectedDevice.getUserAttributeType()) {
            this.userAttributeType = this.selectedDevice.getUserAttributeType().getName();
        }
        if (null != this.selectedDevice.getDisableAttributeType()) {
            this.disableAttributeType = this.selectedDevice.getDisableAttributeType().getName();
        }

        return "edit";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String save()
            throws DeviceNotFoundException, CertificateEncodingException, IOException, AttributeTypeNotFoundException,
            PermissionDeniedException {

        LOG.debug("save device: " + this.selectedDevice.getName());
        String deviceName = this.selectedDevice.getName();

        this.deviceService.updateAuthenticationPath(deviceName, this.authenticationPath);
        if (null != this.registrationPath) {
            this.deviceService.updateRegistrationPath(deviceName, this.registrationPath);
        }
        if (null != this.removalPath) {
            this.deviceService.updateRemovalPath(deviceName, this.removalPath);
        }
        if (null != this.updatePath) {
            this.deviceService.updateUpdatePath(deviceName, this.updatePath);
        }
        if (null != this.disablePath) {
            this.deviceService.updateDisablePath(deviceName, this.disablePath);
        }

        if (null != this.certificate) {
            LOG.debug("updating device certificate");
            this.deviceService.updateDeviceCertificate(deviceName, getUpFileContent(this.certificate));
        }

        if (null != this.attributeType) {
            LOG.debug("updating attribute type");
            this.deviceService.updateAttributeType(deviceName, this.attributeType);
        }

        if (null != this.userAttributeType) {
            LOG.debug("updating user attribute type");
            this.deviceService.updateUserAttributeType(deviceName, this.userAttributeType);
        }
        if (null != this.disableAttributeType) {
            LOG.debug("updating disable attribute type");
            this.deviceService.updateDisableAttributeType(deviceName, this.disableAttributeType);
        }

        /*
         * Refresh the device
         */
        this.selectedDevice = this.deviceService.getDevice(deviceName);
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String view() {

        LOG.debug("view device: " + this.selectedDevice.getName());
        return "view";
    }

    /*
     * Accessors
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getName() {

        return this.name;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setName(String name) {

        this.name = name;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getDeviceClass() {

        return this.deviceClass;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setDeviceClass(String deviceClass) {

        this.deviceClass = deviceClass;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getNode() {

        return this.node;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setNode(String node) {

        this.node = node;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getAuthenticationPath() {

        return this.authenticationPath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setAuthenticationPath(String authenticationPath) {

        this.authenticationPath = authenticationPath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getRegistrationPath() {

        return this.registrationPath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setRegistrationPath(String registrationPath) {

        this.registrationPath = registrationPath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getRemovalPath() {

        return this.removalPath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setRemovalPath(String removalPath) {

        this.removalPath = removalPath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getUpdatePath() {

        return this.updatePath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setUpdatePath(String updatePath) {

        this.updatePath = updatePath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getDisablePath() {

        return this.disablePath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setDisablePath(String disablePath) {

        this.disablePath = disablePath;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public UploadedFile getCertificate() {

        return this.certificate;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setCertificate(UploadedFile certificate) {

        this.certificate = certificate;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getAttributeType() {

        return this.attributeType;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setAttributeType(String attributeType) {

        this.attributeType = attributeType;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getUserAttributeType() {

        return this.userAttributeType;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setUserAttributeType(String userAttributeType) {

        this.userAttributeType = userAttributeType;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getDisableAttributeType() {

        return this.disableAttributeType;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setDisableAttributeType(String disableAttributeType) {

        this.disableAttributeType = disableAttributeType;
    }
}
