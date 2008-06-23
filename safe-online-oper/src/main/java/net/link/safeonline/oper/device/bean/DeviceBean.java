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
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.OlasEntity;
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
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX + "DeviceBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class DeviceBean implements Device {

	private static final Log LOG = LogFactory.getLog(DeviceBean.class);

	public static final String OPER_DEVICE_LIST_NAME = "operDeviceList";

	public static final String OPER_DEVICE_CLASS_LIST_NAME = "deviceClasses";

	public static final String OPER_DEVICE_ATTRIBUTE_TYPE_LIST_NAME = "attributeTypes";

	@In(create = true)
	FacesMessages facesMessages;

	@EJB
	private DeviceService deviceService;

	@EJB
	private AttributeTypeService attributeTypeService;

	@EJB
	private NodeService nodeService;

	private String name;

	private String deviceClass;

	private String node;

	private String authenticationURL;

	private String registrationURL;

	private String removalURL;

	private String updateURL;

	private UploadedFile certificate;

	private String attributeType;

	private String userAttributeType;

	/*
	 * Seam Data models
	 */
	@DataModel(OPER_DEVICE_LIST_NAME)
	public List<DeviceEntity> deviceList;

	@DataModelSelection(OPER_DEVICE_LIST_NAME)
	@Out(value = "selectedDevice", required = false, scope = ScopeType.SESSION)
	@In(required = false)
	private DeviceEntity selectedDevice;

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
		List<DeviceClassEntity> deviceClassesList = this.deviceService
				.listDeviceClasses();
		List<SelectItem> deviceClasses = ConvertorUtil.convert(
				deviceClassesList, new DeviceClassSelectItemConvertor());
		return deviceClasses;
	}

	static class DeviceClassSelectItemConvertor implements
			Convertor<DeviceClassEntity, SelectItem> {

		public SelectItem convert(DeviceClassEntity input) {
			SelectItem output = new SelectItem(input.getName());
			return output;
		}
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	@Factory(OPER_DEVICE_ATTRIBUTE_TYPE_LIST_NAME)
	public List<SelectItem> attributeTypesFactory() {
		List<AttributeTypeEntity> attributeTypesList = this.attributeTypeService
				.listAttributeTypes();
		List<SelectItem> attributeTypes = ConvertorUtil.convert(
				attributeTypesList, new AttributeTypeSelectItemConvertor());
		return attributeTypes;
	}

	static class AttributeTypeSelectItemConvertor implements
			Convertor<AttributeTypeEntity, SelectItem> {

		public SelectItem convert(AttributeTypeEntity input) {
			SelectItem output = new SelectItem(input.getName());
			return output;
		}
	}

	@Factory("deviceNodes")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public List<SelectItem> nodeFactory() {
		List<OlasEntity> nodeList = this.nodeService.listNodes();
		List<SelectItem> nodes = ConvertorUtil.convert(nodeList,
				new OlasEntitySelectItemConvertor());
		return nodes;
	}

	static class OlasEntitySelectItemConvertor implements
			Convertor<OlasEntity, SelectItem> {

		public SelectItem convert(OlasEntity input) {
			SelectItem output = new SelectItem(input.getName());
			return output;
		}
	}

	private byte[] getUpFileContent(UploadedFile file) throws IOException {
		InputStream inputStream = file.getInputStream();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		IOUtils.copy(inputStream, byteArrayOutputStream);
		return byteArrayOutputStream.toByteArray();
	}

	/*
	 * Actions
	 */
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	@ErrorHandling( {
			@Error(exceptionClass = CertificateEncodingException.class, messageId = "errorX509Encoding", fieldId = "fileupload"),
			@Error(exceptionClass = IOException.class, messageId = "errorUploadCertificate") })
	public String add() throws ExistingDeviceException,
			CertificateEncodingException, DeviceClassNotFoundException,
			AttributeTypeNotFoundException, NodeNotFoundException, IOException {
		LOG.debug("add device: " + this.name);

		byte[] encodedCertificate = null;
		if (null != this.certificate) {
			encodedCertificate = getUpFileContent(this.certificate);
		}

		this.deviceService.addDevice(this.name, this.deviceClass, this.node,
				this.authenticationURL, this.registrationURL, this.removalURL,
				this.updateURL, encodedCertificate, this.attributeType,
				this.userAttributeType);
		return "success";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String remove() throws DeviceNotFoundException,
			DeviceDescriptionNotFoundException, DevicePropertyNotFoundException {
		LOG.debug("remove device: " + this.selectedDevice.getName());
		try {
			this.deviceService.removeDevice(this.selectedDevice.getName());
		} catch (PermissionDeniedException e) {
			LOG.debug("permission denied: " + e.getMessage());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, e.getResourceMessage(), e
							.getResourceArgs());
			return null;
		}
		deviceListFactory();
		return "success";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String edit() {
		LOG.debug("edit device: " + this.selectedDevice.getName());

		this.authenticationURL = this.selectedDevice.getAuthenticationPath();
		this.registrationURL = this.selectedDevice.getRegistrationPath();
		this.removalURL = this.selectedDevice.getRemovalPath();
		if (null != this.selectedDevice.getAttributeType())
			this.attributeType = this.selectedDevice.getAttributeType()
					.getName();
		if (null != this.selectedDevice.getUserAttributeType())
			this.userAttributeType = this.selectedDevice.getUserAttributeType()
					.getName();

		return "edit";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String save() throws DeviceNotFoundException,
			CertificateEncodingException, IOException,
			AttributeTypeNotFoundException {
		LOG.debug("save device: " + this.selectedDevice.getName());
		String deviceName = this.selectedDevice.getName();

		this.deviceService.updateAuthenticationUrl(deviceName,
				this.authenticationURL);
		if (null != this.registrationURL)
			this.deviceService.updateRegistrationUrl(deviceName,
					this.registrationURL);
		if (null != this.removalURL)
			this.deviceService.updateRemovalUrl(deviceName, this.removalURL);
		if (null != this.updateURL)
			this.deviceService.updateUpdateUrl(deviceName, this.updateURL);

		if (null != this.certificate) {
			LOG.debug("updating device certificate");
			this.deviceService.updateDeviceCertificate(deviceName,
					getUpFileContent(this.certificate));
		}

		if (null != this.attributeType) {
			LOG.debug("updating attribute type");
			this.deviceService.updateAttributeType(deviceName,
					this.attributeType);
		}

		if (null != this.userAttributeType) {
			LOG.debug("updating user attribute type");
			this.deviceService.updateUserAttributeType(deviceName,
					this.userAttributeType);
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
	public String getAuthenticationURL() {
		return this.authenticationURL;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setAuthenticationURL(String authenticationURL) {
		this.authenticationURL = authenticationURL;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String getRegistrationURL() {
		return this.registrationURL;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setRegistrationURL(String registrationURL) {
		this.registrationURL = registrationURL;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String getRemovalURL() {
		return this.removalURL;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setRemovalURL(String removalURL) {
		this.removalURL = removalURL;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String getUpdateURL() {
		return this.updateURL;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setUpdateURL(String updateURL) {
		this.updateURL = updateURL;
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
}
