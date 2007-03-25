/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.bean;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.model.SelectItem;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.CertificateEncodingException;
import net.link.safeonline.authentication.exception.ExistingApplicationException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.oper.Application;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.service.AttributeTypeService;

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
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("application")
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

	private String name;

	private String description;

	private String applicationOwner;

	private UploadedFile upFile;

	@SuppressWarnings("unused")
	@Out
	private long numberOfSubscriptions;

	@In(create = true)
	FacesMessages facesMessages;

	@In(value = "selectedNewApplicationAttributeTypes", required = false)
	private String[] selectedAttributeTypes;

	@Out(value = "selectedApplicationIdentityAttributeTypeList", required = false)
	@In(required = false)
	private String[] selectedApplicationIdentityAttributeTypeList;

	@Remove
	@Destroy
	public void destroyCallback() {
		this.name = null;
		this.description = null;
	}

	@SuppressWarnings("unused")
	@DataModel
	private List<ApplicationEntity> operApplicationList;

	@DataModelSelection("operApplicationList")
	@Out(value = "selectedApplication", required = false, scope = ScopeType.SESSION)
	@In(required = false)
	private ApplicationEntity selectedApplication;

	@SuppressWarnings("unused")
	@Out(value = "applicationIdentityAttributeTypeList", required = false)
	private List<AttributeTypeEntity> applicationIdentityAttributeTypeList;

	@Factory("operApplicationList")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void applicationListFactory() {
		LOG.debug("application list factory");
		this.operApplicationList = this.applicationService.listApplications();
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String view() {
		String applicationName = this.selectedApplication.getName();
		LOG.debug("view: " + applicationName);
		try {
			this.numberOfSubscriptions = this.subscriptionService
					.getNumberOfSubscriptions(applicationName);
			this.applicationIdentityAttributeTypeList = this.applicationService
					.getCurrentApplicationIdentity(applicationName);
		} catch (ApplicationNotFoundException e) {
			String msg = "application not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (ApplicationIdentityNotFoundException e) {
			String msg = "application identity not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (PermissionDeniedException e) {
			String msg = "permission denied";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}

		return "view-application";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String add() {
		LOG.debug("add application: " + this.name);
		for (String item : this.selectedAttributeTypes) {
			LOG.debug("selected attribute type: " + item);
		}
		try {
			byte[] encodedCertificate;
			if (null != this.upFile) {
				encodedCertificate = this.upFile.getBytes();
			} else {
				encodedCertificate = null;
			}
			this.applicationService.addApplication(this.name,
					this.applicationOwner, this.description,
					encodedCertificate, this.selectedAttributeTypes);
		} catch (ExistingApplicationException e) {
			String msg = "application already exists: " + this.name;
			LOG.debug(msg);
			this.facesMessages.addToControl("name", msg);
			return null;
		} catch (ApplicationOwnerNotFoundException e) {
			String msg = "application owner not found: "
					+ this.applicationOwner;
			LOG.debug(msg);
			this.facesMessages.addToControl("owner", msg);
			return null;
		} catch (IOException e) {
			String msg = "IO error";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (CertificateEncodingException e) {
			String msg = "X509 certificate encoding error";
			LOG.debug(msg);
			this.facesMessages.addToControl("fileupload", msg);
			return null;
		} catch (AttributeTypeNotFoundException e) {
			String msg = "attribute type not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}
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

	public String getName() {
		return this.name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getApplicationOwner() {
		return this.applicationOwner;
	}

	public void setApplicationOwner(String applicationOwner) {
		this.applicationOwner = applicationOwner;
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
			String msg = "application not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (PermissionDeniedException e) {
			String msg = "permission denied to remove: " + applicationName;
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}
		applicationListFactory();
		return "success";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	@Factory("applicationAttributeTypeList")
	public List<SelectItem> applicationAttributeTypeListFactory() {
		List<AttributeTypeEntity> attributeTypes = this.attributeTypeService
				.listAttributeTypes();
		List<SelectItem> itemList = new LinkedList<SelectItem>();
		for (AttributeTypeEntity attributeType : attributeTypes) {
			SelectItem item = new SelectItem(attributeType.getName());
			itemList.add(item);
		}
		return itemList;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	@Factory("selectedNewApplicationAttributeTypes")
	public String[] selectedNewApplicationAttributeTypesFactory() {
		return new String[] {};
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String edit() {
		List<AttributeTypeEntity> identityAttributeTypes;
		try {
			identityAttributeTypes = this.applicationService
					.getCurrentApplicationIdentity(this.selectedApplication
							.getName());
		} catch (ApplicationNotFoundException e) {
			String msg = "application not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (ApplicationIdentityNotFoundException e) {
			String msg = "application identity not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (PermissionDeniedException e) {
			String msg = "permission denied";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}
		this.selectedApplicationIdentityAttributeTypeList = new String[identityAttributeTypes
				.size()];
		for (int idx = 0; idx < this.selectedApplicationIdentityAttributeTypeList.length; idx++) {
			this.selectedApplicationIdentityAttributeTypeList[idx] = identityAttributeTypes
					.get(idx).getName();
		}
		return "edit";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String save() {
		String applicationId = this.selectedApplication.getName();
		LOG.debug("save application: " + applicationId);
		for (String item : this.selectedApplicationIdentityAttributeTypeList) {
			LOG.debug("application identity attribute type: " + item);
		}
		try {
			this.applicationService.updateApplicationIdentity(applicationId,
					this.selectedApplicationIdentityAttributeTypeList);
			/*
			 * Refresh the selected application.
			 */
			this.selectedApplication = this.applicationService
					.getApplication(applicationId);
			this.applicationIdentityAttributeTypeList = this.applicationService
					.getCurrentApplicationIdentity(applicationId);
		} catch (ApplicationNotFoundException e) {
			String msg = "application not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (ApplicationIdentityNotFoundException e) {
			String msg = "application identity not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (AttributeTypeNotFoundException e) {
			String msg = "attribute type not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (PermissionDeniedException e) {
			String msg = "permission denied";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}
		return "success";
	}
}
