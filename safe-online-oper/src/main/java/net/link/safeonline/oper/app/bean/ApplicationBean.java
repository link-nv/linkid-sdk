/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.app.bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingApplicationException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.ctrl.Convertor;
import net.link.safeonline.ctrl.ConvertorUtil;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.app.Application;
import net.link.safeonline.oper.app.IdentityAttribute;
import net.link.safeonline.pkix.exception.CertificateEncodingException;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.SubjectService;

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
@Name("operApplication")
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX
		+ "ApplicationBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class ApplicationBean implements Application {

	private static final Log LOG = LogFactory.getLog(ApplicationBean.class);

	@EJB
	private ApplicationService applicationService;

	@EJB
	private SubscriptionService subscriptionService;

	@EJB
	private AttributeTypeService attributeTypeService;

	@EJB
	private SubjectService subjectService;

	private String name;

	private String friendlyName;

	private String description;

	private String applicationUrl;

	private String applicationOwner;

	private UploadedFile upFile;

	private boolean idmapping;

	@SuppressWarnings("unused")
	@Out
	private long numberOfSubscriptions;

	@In(create = true)
	FacesMessages facesMessages;

	public static final String NEW_IDENTITY_ATTRIBUTES_NAME = "newIdentityAttributes";

	@DataModel(NEW_IDENTITY_ATTRIBUTES_NAME)
	private List<IdentityAttribute> newIdentityAttributes;

	public static final String IDENTITY_ATTRIBUTES_NAME = "identityAttributes";

	@DataModel(IDENTITY_ATTRIBUTES_NAME)
	private List<IdentityAttribute> identityAttributes;

	@Remove
	@Destroy
	public void destroyCallback() {
		this.name = null;
		this.description = null;
		this.applicationUrl = null;
	}

	public static final String OPER_APPLICATION_LIST_NAME = "operApplicationList";

	@SuppressWarnings("unused")
	@DataModel(OPER_APPLICATION_LIST_NAME)
	private List<ApplicationEntity> operApplicationList;

	@DataModelSelection(OPER_APPLICATION_LIST_NAME)
	@Out(value = "selectedApplication", required = false, scope = ScopeType.SESSION)
	@In(required = false)
	private ApplicationEntity selectedApplication;

	@SuppressWarnings("unused")
	@Out(required = false)
	private String ownerAdminName;

	public static final String APPLICATION_IDENTITY_ATTRIBUTES_NAME = "applicationIdentityAttributes";

	@SuppressWarnings("unused")
	@DataModel(value = APPLICATION_IDENTITY_ATTRIBUTES_NAME)
	private Set<ApplicationIdentityAttributeEntity> applicationIdentityAttributes;

	@Factory(OPER_APPLICATION_LIST_NAME)
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void applicationListFactory() {
		LOG.debug("application list factory");
		this.operApplicationList = this.applicationService.listApplications();
	}

	@Factory(APPLICATION_IDENTITY_ATTRIBUTES_NAME)
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void applicationIdentityAttributesFactory() {
		LOG.debug("application identity attributes factory");
		String applicationName = this.selectedApplication.getName();
		try {
			this.applicationIdentityAttributes = this.applicationService
					.getCurrentApplicationIdentity(applicationName);
			this.numberOfSubscriptions = this.subscriptionService
					.getNumberOfSubscriptions(applicationName);
			this.ownerAdminName = this.subjectService
					.getSubjectLogin(this.selectedApplication
							.getApplicationOwner().getAdmin().getUserId());
		} catch (ApplicationNotFoundException e) {
			LOG.debug("application not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return;
		} catch (ApplicationIdentityNotFoundException e) {
			LOG.debug("application identity not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR,
					"errorApplicationIdentityNotFound");
			return;
		} catch (PermissionDeniedException e) {
			LOG.debug("permission denied.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return;
		}
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String add() {

		LOG.debug("add application: " + this.name);
		if (null != this.friendlyName)
			LOG.debug("user friendly name: " + this.friendlyName);
		if (null != this.applicationUrl)
			LOG.debug("application url: " + this.applicationUrl);

		URL newApplicationUrl = null;
		try {
			newApplicationUrl = new URL(this.applicationUrl);
		} catch (MalformedURLException e) {
			LOG.debug("illegal URL format: " + this.applicationUrl);
			this.facesMessages.addToControlFromResourceBundle("applicationUrl",
					FacesMessage.SEVERITY_ERROR, "errorIllegalUrl",
					this.applicationUrl);
			return null;
		}

		List<IdentityAttributeTypeDO> identityAttributes = new LinkedList<IdentityAttributeTypeDO>();
		for (IdentityAttribute viewIdentityAttribute : this.newIdentityAttributes) {
			if (false == viewIdentityAttribute.isIncluded()) {
				continue;
			}
			LOG.debug("include attribute: " + viewIdentityAttribute.getName());
			IdentityAttributeTypeDO identityAttribute = new IdentityAttributeTypeDO(
					viewIdentityAttribute.getName(), viewIdentityAttribute
							.isRequired(), viewIdentityAttribute.isDataMining());
			identityAttributes.add(identityAttribute);
		}
		try {
			byte[] encodedCertificate;
			if (null != this.upFile) {
				encodedCertificate = getUpFileContent();
			} else {
				encodedCertificate = null;
			}
			this.applicationService.addApplication(this.name,
					this.friendlyName, this.applicationOwner, this.description,
					this.idmapping, newApplicationUrl, encodedCertificate,
					identityAttributes);
		} catch (ExistingApplicationException e) {
			LOG.debug("application already exists: " + this.name);
			this.facesMessages.addToControlFromResourceBundle("name",
					FacesMessage.SEVERITY_ERROR,
					"errorApplicationAlreadyExists", this.name);
			return null;
		} catch (ApplicationOwnerNotFoundException e) {
			LOG.debug("application owner not found: " + this.applicationOwner);
			this.facesMessages.addToControlFromResourceBundle("owner",
					FacesMessage.SEVERITY_ERROR,
					"errorApplicationOwnerNotFound", this.applicationOwner);
			return null;
		} catch (IOException e) {
			LOG.debug("IO error");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorIO");
			return null;
		} catch (CertificateEncodingException e) {
			LOG.debug("X509 certificate encoding error");
			this.facesMessages.addToControlFromResourceBundle("fileupload",
					FacesMessage.SEVERITY_ERROR, "errorX509Encoding");
			return null;
		} catch (AttributeTypeNotFoundException e) {
			LOG.debug("attribute type not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorAttributeTypeNotFound");
			return null;
		}
		applicationListFactory();
		return "success";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public UploadedFile getUpFile() {
		return this.upFile;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setUpFile(UploadedFile uploadedFile) {
		this.upFile = uploadedFile;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getApplicationUrl() {

		return applicationUrl;
	}

	public void setApplicationUrl(String applicationUrl) {

		this.applicationUrl = applicationUrl;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFriendlyName() {
		return this.friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	public String getApplicationOwner() {
		return this.applicationOwner;
	}

	public void setApplicationOwner(String applicationOwner) {
		this.applicationOwner = applicationOwner;
	}

	public boolean isIdmapping() {
		return this.idmapping;
	}

	public void setIdmapping(boolean idmapping) {
		this.idmapping = idmapping;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String removeApplication() {
		/*
		 * http://jira.jboss.com/jira/browse/EJBTHREE-786
		 */
		String applicationName = this.selectedApplication.getName();
		LOG.debug("remove application: " + applicationName);
		try {
			this.applicationService.removeApplication(applicationName);
		} catch (ApplicationNotFoundException e) {
			LOG.debug("application not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return null;
		} catch (PermissionDeniedException e) {
			LOG.debug("permission denied to remove: " + applicationName);
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return null;
		}
		applicationListFactory();
		return "success";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	@Factory(NEW_IDENTITY_ATTRIBUTES_NAME)
	public void newIdentityAttributesFactory() {
		this.newIdentityAttributes = new LinkedList<IdentityAttribute>();
		List<AttributeTypeEntity> attributeTypes = this.attributeTypeService
				.listAttributeTypes();
		for (AttributeTypeEntity attributeType : attributeTypes) {
			IdentityAttribute identityAttribute = new IdentityAttribute(
					attributeType.getName());
			this.newIdentityAttributes.add(identityAttribute);
		}
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	@Factory(IDENTITY_ATTRIBUTES_NAME)
	public void identityAttributesFactory() {
		Set<ApplicationIdentityAttributeEntity> currentIdentityAttributes;
		try {
			currentIdentityAttributes = this.applicationService
					.getCurrentApplicationIdentity(this.selectedApplication
							.getName());
		} catch (ApplicationNotFoundException e) {
			LOG.debug("application not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return;
		} catch (ApplicationIdentityNotFoundException e) {
			LOG.debug("application identity not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR,
					"errorApplicationIdentityNotFound");
			return;
		} catch (PermissionDeniedException e) {
			LOG.debug("permission denied.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return;
		}

		/*
		 * Construct a map for fast lookup. The key is the attribute type name.
		 */
		Map<String, ApplicationIdentityAttributeEntity> currentIdentity = new HashMap<String, ApplicationIdentityAttributeEntity>();
		for (ApplicationIdentityAttributeEntity applicationIdentityAttribute : currentIdentityAttributes) {
			currentIdentity.put(applicationIdentityAttribute
					.getAttributeTypeName(), applicationIdentityAttribute);
		}

		/*
		 * The view receives a full attribute list, annotated with included and
		 * required flags.
		 */
		this.identityAttributes = new LinkedList<IdentityAttribute>();
		List<AttributeTypeEntity> attributeTypes = this.attributeTypeService
				.listAttributeTypes();
		for (AttributeTypeEntity attributeType : attributeTypes) {
			boolean included = false;
			boolean required = false;
			boolean dataMining = false;
			ApplicationIdentityAttributeEntity currentIdentityAttribute = currentIdentity
					.get(attributeType.getName());
			if (null != currentIdentityAttribute) {
				included = true;
				if (currentIdentityAttribute.isRequired()) {
					required = true;
				}
				if (currentIdentityAttribute.isDataMining()) {
					dataMining = true;
				}
			}
			IdentityAttribute identityAttribute = new IdentityAttribute(
					attributeType.getName(), included, required, dataMining);
			this.identityAttributes.add(identityAttribute);
		}
	}

	private byte[] getUpFileContent() throws IOException {
		InputStream inputStream = this.upFile.getInputStream();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		IOUtils.copy(inputStream, byteArrayOutputStream);
		return byteArrayOutputStream.toByteArray();
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String save() {
		String applicationId = this.selectedApplication.getName();
		LOG.debug("save application: " + applicationId);

		URL newApplicationUrl = null;
		if (null != this.applicationUrl)
			try {
				newApplicationUrl = new URL(applicationUrl);
			} catch (MalformedURLException e) {
				LOG.debug("illegal URL format: " + this.applicationUrl);
				this.facesMessages.addToControlFromResourceBundle(
						"applicationUrl", FacesMessage.SEVERITY_ERROR,
						"errorIllegalUrl", this.applicationUrl);
				return null;
			}

		if (null != this.upFile) {
			LOG.debug("updating application certificate");
			try {
				this.applicationService.updateApplicationCertificate(
						applicationId, getUpFileContent());
			} catch (CertificateEncodingException e) {
				LOG.debug("certificate encoding error");
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "errorX509Encoding");
				return null;
			} catch (ApplicationNotFoundException e) {
				LOG.debug("application not found");
				this.facesMessages
						.addFromResourceBundle(FacesMessage.SEVERITY_ERROR,
								"errorApplicationNotFound");
				return null;
			} catch (IOException e) {
				LOG.debug("IO error: " + e.getMessage());
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "errorIO");
				return null;
			}
		}

		List<IdentityAttributeTypeDO> newIdentityAttributes = new LinkedList<IdentityAttributeTypeDO>();
		for (IdentityAttribute identityAttribute : this.identityAttributes) {
			if (false == identityAttribute.isIncluded()) {
				continue;
			}
			IdentityAttributeTypeDO newIdentityAttribute = new IdentityAttributeTypeDO(
					identityAttribute.getName(),
					identityAttribute.isRequired(), identityAttribute
							.isDataMining());
			newIdentityAttributes.add(newIdentityAttribute);
		}

		try {
			this.applicationService.updateApplicationIdentity(applicationId,
					newIdentityAttributes);
			this.applicationService.updateApplicationUrl(applicationId,
					newApplicationUrl);

			/*
			 * Refresh the selected application.
			 */
			this.selectedApplication = this.applicationService
					.getApplication(applicationId);
			this.applicationIdentityAttributes = this.applicationService
					.getCurrentApplicationIdentity(applicationId);
		} catch (ApplicationNotFoundException e) {
			LOG.debug("application not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return null;
		} catch (ApplicationIdentityNotFoundException e) {
			LOG.debug("application identity not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR,
					"errorApplicationIdentityNotFound");
			return null;
		} catch (AttributeTypeNotFoundException e) {
			LOG.debug("application identity not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorAttributeTypeNotFound");
			return null;
		} catch (PermissionDeniedException e) {
			LOG.debug("permission denied.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return null;
		}

		try {
			this.applicationService.setIdentifierMappingServiceAccess(
					applicationId, this.idmapping);
		} catch (ApplicationNotFoundException e) {
			LOG.debug("application not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return null;
		}

		applicationListFactory();
		return "success";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String view() {
		/*
		 * To set the selected application.
		 */
		LOG.debug("view application: " + this.selectedApplication.getName());
		return "view";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String edit() {
		/*
		 * To set the selected application.
		 */
		LOG.debug("edit application: " + this.selectedApplication.getName());
		if (null != selectedApplication.getApplicationUrl()) {
			applicationUrl = selectedApplication.getApplicationUrl()
					.toExternalForm();
		}
		this.idmapping = this.selectedApplication.isIdentifierMappingAllowed();

		return "edit";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	@Factory("availableApplicationOwners")
	public List<SelectItem> availableApplicationOwnersFactory() {
		List<ApplicationOwnerEntity> applicationOwners = this.applicationService
				.listApplicationOwners();
		List<SelectItem> availableApplicationOwners = ConvertorUtil.convert(
				applicationOwners, new ApplicationOwnerSelectItemConvertor());
		return availableApplicationOwners;
	}

	private static class ApplicationOwnerSelectItemConvertor implements
			Convertor<ApplicationOwnerEntity, SelectItem> {

		public SelectItem convert(ApplicationOwnerEntity input) {
			SelectItem output = new SelectItem(input.getName());
			return output;
		}
	}
}
