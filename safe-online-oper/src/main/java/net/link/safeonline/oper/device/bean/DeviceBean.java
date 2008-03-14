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

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceClassNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DevicePropertyNotFoundException;
import net.link.safeonline.authentication.exception.ExistingDeviceException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.ctrl.Convertor;
import net.link.safeonline.ctrl.ConvertorUtil;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.device.Device;
import net.link.safeonline.pkix.exception.CertificateEncodingException;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.DeviceService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.trinidad.model.UploadedFile;
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
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("operDevice")
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX + "DeviceBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class DeviceBean implements Device {

	private static final Log LOG = LogFactory.getLog(DeviceBean.class);

	public static final String OPER_DEVICE_LIST_NAME = "operDeviceList";

	public static final String OPER_DEVICE_CLASS_LIST_NAME = "deviceClasses";

	public static final String OPER_DEVICE_ATTRIBUTE_TYPE_LIST_NAME = "attributeTypes";

	@In(create = true)
	FacesMessages facesMessages;

	@EJB
	DeviceService deviceService;

	@EJB
	AttributeTypeService attributeTypeService;

	private String name;

	private String deviceClass;

	private String authenticationURL;

	private String registrationURL;

	private String removalURL;

	private String updateURL;

	private UploadedFile certificate;

	private String attributeType;

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
	public String add() {
		LOG.debug("add device: " + this.name);

		byte[] encodedCertificate = null;
		if (null != this.certificate) {
			try {
				encodedCertificate = getUpFileContent(this.certificate);
			} catch (IOException e) {
				LOG.debug("Failed to upload certificate");
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "errorUploadCertificate");
				return null;
			}
		}

		try {
			this.deviceService.addDevice(this.name, this.deviceClass,
					this.authenticationURL, this.registrationURL,
					this.removalURL, this.updateURL, encodedCertificate,
					this.attributeType);
		} catch (CertificateEncodingException e) {
			LOG.debug("X509 certificate encoding error");
			this.facesMessages.addToControlFromResourceBundle("fileupload",
					FacesMessage.SEVERITY_ERROR, "errorX509Encoding");
			return null;
		} catch (DeviceClassNotFoundException e) {
			LOG.debug("device " + this.deviceClass + " not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceClassNotFound");
			return null;
		} catch (ExistingDeviceException e) {
			LOG.debug("device already exists: " + this.name);
			this.facesMessages.addToControlFromResourceBundle("name",
					FacesMessage.SEVERITY_ERROR, "errorDeviceAlreadyExists",
					this.name);
			return null;
		} catch (AttributeTypeNotFoundException e) {
			LOG.debug("attribute type " + this.attributeType + " not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorAttributeTypeNotFound");
			return null;
		}
		return "success";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String remove() {
		LOG.debug("remove device: " + this.selectedDevice.getName());
		try {
			this.deviceService.removeDevice(this.selectedDevice.getName());
		} catch (DeviceNotFoundException e) {
			LOG.debug("device " + this.selectedDevice.getName() + " not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
			return null;
		} catch (DeviceDescriptionNotFoundException e) {
			LOG.debug("device description not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR,
					"errorDeviceDescriptionNotFound");
			return null;
		} catch (DevicePropertyNotFoundException e) {
			LOG.debug("device property not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDevicePropertyNotFound");
			return null;
		} catch (PermissionDeniedException e) {
			LOG.debug("permission denied: " + e.getMessage());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return null;
		}
		deviceListFactory();
		return "success";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String edit() {
		LOG.debug("edit device: " + this.selectedDevice.getName());

		this.authenticationURL = this.selectedDevice.getAuthenticationURL();
		this.registrationURL = this.selectedDevice.getRegistrationURL();
		this.removalURL = this.selectedDevice.getRemovalURL();
		this.attributeType = this.selectedDevice.getAttributeType().getName();

		return "edit";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String save() {
		LOG.debug("save device: " + this.selectedDevice.getName());
		String deviceName = this.selectedDevice.getName();

		try {
			this.deviceService.updateAuthenticationUrl(deviceName,
					this.authenticationURL);
			if (null != this.registrationURL)
				this.deviceService.updateRegistrationUrl(deviceName,
						this.registrationURL);
			if (null != this.removalURL)
				this.deviceService
						.updateRemovalUrl(deviceName, this.removalURL);
			if (null != this.updateURL)
				this.deviceService.updateUpdateUrl(deviceName, this.updateURL);
		} catch (DeviceNotFoundException e) {
			LOG.debug("device not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
			return null;
		}

		if (null != this.certificate) {
			LOG.debug("updating device certificate");
			try {
				this.deviceService.updateDeviceCertificate(deviceName,
						getUpFileContent(this.certificate));
			} catch (CertificateEncodingException e) {
				LOG.debug("certificate encoding error");
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "errorX509Encoding");
				return null;
			} catch (DeviceNotFoundException e) {
				LOG.debug("device not found");
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
				return null;
			} catch (IOException e) {
				LOG.debug("IO error: " + e.getMessage());
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "errorIO");
				return null;
			}
		}

		if (null != this.attributeType) {
			LOG.debug("updating attribute type");
			try {
				this.deviceService.updateAttributeType(deviceName,
						this.attributeType);
			} catch (DeviceNotFoundException e) {
				LOG.debug("device not found");
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
				return null;
			} catch (AttributeTypeNotFoundException e) {
				LOG.debug("attribute type not found");
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR,
						"errorAttributeTypeNotFound");
				return null;
			}
		}
		/*
		 * Refresh the device
		 */
		try {
			this.selectedDevice = this.deviceService.getDevice(deviceName);
		} catch (DeviceNotFoundException e) {
			LOG.debug("device not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
			return null;
		}
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

}
