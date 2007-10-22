/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.owner.bean;

import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementTextEntity;
import net.link.safeonline.owner.OwnerConstants;
import net.link.safeonline.owner.UsageAgreement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
@Name("ownerUsageAgreement")
@LocalBinding(jndiBinding = OwnerConstants.JNDI_PREFIX
		+ "UsageAgreementBean/local")
@SecurityDomain(OwnerConstants.SAFE_ONLINE_OWNER_SECURITY_DOMAIN)
public class UsageAgreementBean implements UsageAgreement {

	private static final Log LOG = LogFactory.getLog(UsageAgreementBean.class);

	private static final String draftUsageAgreementsTextsModel = "draftUsageAgreementsTexts";

	private static final String currentUsageAgreementsTextsModel = "currentUsageAgreementsTexts";

	@In(create = true)
	FacesMessages facesMessages;

	@EJB
	private UsageAgreementService usageAgreementService;

	private String language;

	@Out(required = false, scope = ScopeType.SESSION)
	@In(required = false)
	private ApplicationEntity selectedApplication;

	@In(required = false)
	@Out(required = false, scope = ScopeType.SESSION)
	private UsageAgreementEntity selectedUsageAgreement;

	@In(required = false)
	@Out(required = false, scope = ScopeType.SESSION)
	private UsageAgreementEntity draftUsageAgreement;

	@In(required = false)
	@Out(required = false, scope = ScopeType.SESSION)
	private UsageAgreementEntity currentUsageAgreement;

	/*
	 * Seam Data models
	 */
	@SuppressWarnings("unused")
	@DataModel(value = "usageAgreementTextList")
	private Set<UsageAgreementTextEntity> usageAgreementTextList;

	@DataModelSelection(value = "usageAgreementTextList")
	@In(value = "selectedUsageAgreementText", required = false)
	@Out(value = "selectedUsageAgreementText", required = false, scope = ScopeType.SESSION)
	private UsageAgreementTextEntity selectedUsageAgreementText;

	@SuppressWarnings("unused")
	@DataModel(value = currentUsageAgreementsTextsModel)
	private Set<UsageAgreementTextEntity> currentUsageAgreementsTexts;

	@DataModelSelection(currentUsageAgreementsTextsModel)
	@In(required = false)
	@Out(required = false, scope = ScopeType.SESSION)
	private UsageAgreementTextEntity selectedCurrentUsageAgreementText;

	@SuppressWarnings("unused")
	@DataModel(value = draftUsageAgreementsTextsModel)
	private Set<UsageAgreementTextEntity> draftUsageAgreementsTexts;

	@DataModelSelection(draftUsageAgreementsTextsModel)
	@In(required = false)
	@Out(required = false, scope = ScopeType.SESSION)
	private UsageAgreementTextEntity selectedDraftUsageAgreementText;

	/*
	 * Life cycle
	 */
	@Remove
	@Destroy
	public void destroyCallback() {
		LOG.debug("destroy");
	}

	/*
	 * Factories
	 */
	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	@Factory(value = "usageAgreementTextList")
	public void usageAgreementTextListFactory() {
		this.usageAgreementTextList = this.selectedUsageAgreement
				.getUsageAgreementTexts();
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	@Factory(draftUsageAgreementsTextsModel)
	public void draftUsageAgreementsTextsFactory() {
		LOG.debug("get draft texts");
		try {
			this.draftUsageAgreement = this.usageAgreementService
					.getDraftUsageAgreement(this.selectedApplication.getName());
		} catch (ApplicationNotFoundException e) {
			LOG.debug("application not found.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return;
		} catch (PermissionDeniedException e) {
			LOG.debug("permission denied.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return;
		}
		if (null == this.draftUsageAgreement)
			return;
		this.draftUsageAgreementsTexts = this.draftUsageAgreement
				.getUsageAgreementTexts();
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	@Factory(currentUsageAgreementsTextsModel)
	public void currentUsageAgreementsTextsFactory() {
		LOG.debug("get current texts");
		try {
			this.currentUsageAgreement = this.usageAgreementService
					.getCurrentUsageAgreement(this.selectedApplication
							.getName());
		} catch (ApplicationNotFoundException e) {
			LOG.debug("application not found.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return;
		} catch (PermissionDeniedException e) {
			LOG.debug("permission denied.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return;
		}
		if (null == this.currentUsageAgreement)
			return;
		this.currentUsageAgreementsTexts = this.currentUsageAgreement
				.getUsageAgreementTexts();
	}

	/*
	 * Accessors
	 */
	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public boolean getCurrentUsageAgreementIsEmpty() {
		return null == this.currentUsageAgreement;
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public boolean getDraftUsageAgreementIsEmpty() {
		return null == this.draftUsageAgreement;
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String getLanguage() {
		return this.language;
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public void setLanguage(String language) {
		this.language = language;
	}

	/*
	 * Actions
	 */
	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String viewText() {
		LOG.debug("view usage agreement text: language="
				+ this.selectedUsageAgreementText.getLanguage());
		return "viewtext";
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String createUsageAgreement() {
		LOG.debug("create draft usage agreement");
		try {
			this.draftUsageAgreement = this.usageAgreementService
					.createDraftUsageAgreement(this.selectedApplication
							.getName(), this.selectedApplication
							.getCurrentApplicationUsageAgreement());
		} catch (ApplicationNotFoundException e) {
			LOG.debug("application not found.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return null;
		} catch (PermissionDeniedException e) {
			LOG.debug("permission denied.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return null;
		}
		return "success";
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String editCurrentText() {
		LOG.debug("edit current usage agreement text: language="
				+ this.selectedCurrentUsageAgreementText.getLanguage());
		try {
			UsageAgreementEntity usageAgreement = this.usageAgreementService
					.createDraftUsageAgreement(this.selectedApplication
							.getName(), this.selectedApplication
							.getCurrentApplicationUsageAgreement());
			this.selectedUsageAgreementText = usageAgreement
					.getUsageAgreementText(this.selectedCurrentUsageAgreementText
							.getLanguage());
		} catch (ApplicationNotFoundException e) {
			LOG.debug("application not found.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return null;
		} catch (PermissionDeniedException e) {
			LOG.debug("permission denied.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return null;
		}
		return "edittext";
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String editDraftText() {
		LOG.debug("edit draft usage agreement text: language="
				+ this.selectedDraftUsageAgreementText.getLanguage());
		this.selectedUsageAgreementText = this.selectedDraftUsageAgreementText;
		return "edittext";
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String removeDraftText() {
		LOG.debug("remove draft text: language="
				+ this.selectedDraftUsageAgreementText.getLanguage());
		try {
			this.usageAgreementService.removeDraftUsageAgreementText(
					this.selectedApplication.getName(),
					this.selectedDraftUsageAgreementText.getLanguage());
		} catch (ApplicationNotFoundException e) {
			LOG.debug("application not found.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return null;
		} catch (PermissionDeniedException e) {
			LOG.debug("permission denied.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return null;
		}
		this.draftUsageAgreementsTextsFactory();
		return "removed";
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String addText() {
		LOG.debug("add draft text: language=" + this.language);
		try {
			this.selectedUsageAgreementText = this.usageAgreementService
					.createDraftUsageAgreementText(this.selectedApplication
							.getName(), this.language, "");
		} catch (ApplicationNotFoundException e) {
			LOG.debug("application not found.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return null;
		} catch (PermissionDeniedException e) {
			LOG.debug("permission denied.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return null;
		}
		this.draftUsageAgreementsTextsFactory();
		return "edittext";
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String saveText() {
		LOG.debug("save usage agreement text: language="
				+ this.selectedUsageAgreementText.getLanguage());
		String text = this.selectedUsageAgreementText.getText();
		try {
			this.usageAgreementService.setDraftUsageAgreementText(
					this.selectedApplication.getName(),
					this.selectedUsageAgreementText.getLanguage(), text);
		} catch (ApplicationNotFoundException e) {
			LOG.debug("application not found.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return null;
		} catch (PermissionDeniedException e) {
			LOG.debug("permission denied.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return null;
		}
		this.draftUsageAgreementsTextsFactory();
		return "saved";
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String releaseDraft() {
		LOG.debug("release draft usage agreement");
		try {
			this.usageAgreementService
					.updateUsageAgreement(this.selectedApplication.getName());
		} catch (ApplicationNotFoundException e) {
			LOG.debug("application not found.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return null;
		} catch (PermissionDeniedException e) {
			LOG.debug("permission denied.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return null;
		}
		this.currentUsageAgreementsTextsFactory();
		return "success";
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String removeDraft() {
		LOG.debug("remove draft usage agreement");
		try {
			this.usageAgreementService
					.removeDraftUsageAgreement(this.selectedApplication
							.getName());
		} catch (ApplicationNotFoundException e) {
			LOG.debug("application not found.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return null;
		} catch (PermissionDeniedException e) {
			LOG.debug("permission denied.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return null;
		}
		this.draftUsageAgreementsTextsFactory();
		return "success";
	}
}
