/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.owner.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
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
import org.jboss.seam.faces.FacesMessages;


@Stateful
@Name("ownerUsageAgreement")
@LocalBinding(jndiBinding = UsageAgreement.JNDI_BINDING)
@SecurityDomain(OwnerConstants.SAFE_ONLINE_OWNER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class UsageAgreementBean implements UsageAgreement {

    private static final Log              LOG                              = LogFactory.getLog(UsageAgreementBean.class);

    private static final String           draftUsageAgreementsTextsModel   = "draftUsageAgreementsTexts";

    private static final String           currentUsageAgreementsTextsModel = "currentUsageAgreementsTexts";

    @In(create = true)
    FacesMessages                         facesMessages;

    @EJB(mappedName = UsageAgreementService.JNDI_BINDING)
    private UsageAgreementService         usageAgreementService;

    private String                        language;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private ApplicationEntity             selectedApplication;

    @In(required = false)
    @Out(required = false, scope = ScopeType.SESSION)
    private UsageAgreementEntity          selectedUsageAgreement;

    @In(required = false)
    @Out(required = false, scope = ScopeType.SESSION)
    private UsageAgreementEntity          draftUsageAgreement;

    @In(required = false)
    @Out(required = false, scope = ScopeType.SESSION)
    private UsageAgreementEntity          currentUsageAgreement;

    /*
     * Seam Data models
     */
    @SuppressWarnings("unused")
    @DataModel(value = "usageAgreementTextList")
    private Set<UsageAgreementTextEntity> usageAgreementTextList;

    @DataModelSelection(value = "usageAgreementTextList")
    @In(value = "selectedUsageAgreementText", required = false)
    @Out(value = "selectedUsageAgreementText", required = false, scope = ScopeType.SESSION)
    private UsageAgreementTextEntity      selectedUsageAgreementText;

    @SuppressWarnings("unused")
    @DataModel(value = currentUsageAgreementsTextsModel)
    private Set<UsageAgreementTextEntity> currentUsageAgreementsTexts;

    @DataModelSelection(currentUsageAgreementsTextsModel)
    @In(required = false)
    @Out(required = false, scope = ScopeType.SESSION)
    private UsageAgreementTextEntity      selectedCurrentUsageAgreementText;

    @SuppressWarnings("unused")
    @DataModel(value = draftUsageAgreementsTextsModel)
    private Set<UsageAgreementTextEntity> draftUsageAgreementsTexts;

    @DataModelSelection(draftUsageAgreementsTextsModel)
    @In(required = false)
    @Out(required = false, scope = ScopeType.SESSION)
    private UsageAgreementTextEntity      selectedDraftUsageAgreementText;


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

        usageAgreementTextList = selectedUsageAgreement.getUsageAgreementTexts();
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    @Factory(draftUsageAgreementsTextsModel)
    public void draftUsageAgreementsTextsFactory()
            throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("get draft texts");
        draftUsageAgreement = usageAgreementService.getDraftUsageAgreement(selectedApplication.getId());
        if (null == draftUsageAgreement)
            return;
        draftUsageAgreementsTexts = draftUsageAgreement.getUsageAgreementTexts();
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    @Factory(currentUsageAgreementsTextsModel)
    public void currentUsageAgreementsTextsFactory()
            throws PermissionDeniedException, ApplicationNotFoundException {

        LOG.debug("get current texts");
        currentUsageAgreement = usageAgreementService.getCurrentUsageAgreement(selectedApplication.getId());
        if (null == currentUsageAgreement)
            return;
        currentUsageAgreementsTexts = currentUsageAgreement.getUsageAgreementTexts();
    }

    /*
     * Accessors
     */
    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public UsageAgreementEntity getCurrentUsageAgreement()
            throws PermissionDeniedException, ApplicationNotFoundException {

        currentUsageAgreement = usageAgreementService.getCurrentUsageAgreement(selectedApplication.getId());
        return currentUsageAgreement;
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public UsageAgreementEntity getDraftUsageAgreement()
            throws ApplicationNotFoundException, PermissionDeniedException {

        draftUsageAgreement = usageAgreementService.getDraftUsageAgreement(selectedApplication.getId());
        return draftUsageAgreement;
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public String getLanguage() {

        return language;
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public void setLanguage(String language) {

        this.language = language;
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public List<String> autocompleteLanguage(Object event) {

        String languagePrefix = event.toString();
        LOG.debug("auto-complete language: " + languagePrefix);
        List<String> languages = new LinkedList<String>();
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            if (locale.getLanguage().toLowerCase().startsWith(languagePrefix.toLowerCase())) {
                if (!languages.contains(locale.getLanguage())) {
                    languages.add(locale.getLanguage());
                }
            }
        }
        return languages;
    }

    /*
     * Actions
     */
    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public String viewText() {

        LOG.debug("view usage agreement text: language=" + selectedUsageAgreementText.getLanguage());
        return "viewtext";
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public String viewCurrentText() {

        LOG.debug("view text: language=" + selectedCurrentUsageAgreementText.getLanguage());
        selectedUsageAgreementText = selectedCurrentUsageAgreementText;
        return "viewtext";
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public String viewDraftText() {

        LOG.debug("view draft text: language=" + selectedDraftUsageAgreementText.getLanguage());
        selectedUsageAgreementText = selectedDraftUsageAgreementText;
        return "viewtext";
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public String createUsageAgreement()
            throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("create draft usage agreement");
        if (null != currentUsageAgreement) {
            draftUsageAgreement = usageAgreementService.createDraftUsageAgreement(selectedApplication.getId(),
                    currentUsageAgreement.getUsageAgreementVersion());
        } else {
            draftUsageAgreement = usageAgreementService.createDraftUsageAgreement(selectedApplication.getId(),
                    selectedApplication.getCurrentApplicationUsageAgreement());
        }
        return "success";
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public String editCurrentText()
            throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("edit current usage agreement text: language=" + selectedCurrentUsageAgreementText.getLanguage());
        UsageAgreementEntity usageAgreement = usageAgreementService.createDraftUsageAgreement(selectedApplication.getId(),
                currentUsageAgreement.getUsageAgreementVersion());
        selectedUsageAgreementText = usageAgreement.getUsageAgreementText(selectedCurrentUsageAgreementText.getLanguage());
        return "edittext";
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public String editDraftText() {

        LOG.debug("edit draft usage agreement text: language=" + selectedDraftUsageAgreementText.getLanguage());
        selectedUsageAgreementText = selectedDraftUsageAgreementText;
        return "edittext";
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public String removeDraftText()
            throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("remove draft text: language=" + selectedDraftUsageAgreementText.getLanguage());
        usageAgreementService.removeDraftUsageAgreementText(selectedApplication.getId(), selectedDraftUsageAgreementText.getLanguage());
        draftUsageAgreementsTextsFactory();
        return "removed";
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public String addText()
            throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("add draft text: language=" + language);
        selectedUsageAgreementText = usageAgreementService.createDraftUsageAgreementText(selectedApplication.getId(), language, "");
        draftUsageAgreementsTextsFactory();
        return "edittext";
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public String saveText()
            throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("save usage agreement text: language=" + selectedUsageAgreementText.getLanguage());
        String text = selectedUsageAgreementText.getText();
        usageAgreementService.setDraftUsageAgreementText(selectedApplication.getId(), selectedUsageAgreementText.getLanguage(), text);
        draftUsageAgreementsTextsFactory();
        return "saved";
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public String releaseDraft()
            throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("release draft usage agreement");
        usageAgreementService.updateUsageAgreement(selectedApplication.getId());
        currentUsageAgreementsTextsFactory();
        draftUsageAgreementsTextsFactory();
        return "success";
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public String removeDraft()
            throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("remove draft usage agreement");
        usageAgreementService.removeDraftUsageAgreement(selectedApplication.getId());
        currentUsageAgreementsTextsFactory();
        draftUsageAgreementsTextsFactory();
        return "success";
    }
}
