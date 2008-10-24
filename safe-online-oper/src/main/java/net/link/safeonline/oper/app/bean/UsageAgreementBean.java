/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.oper.app.bean;

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
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.app.UsageAgreement;

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
@Name("operUsageAgreement")
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX + "UsageAgreementBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class UsageAgreementBean implements UsageAgreement {

    private static final Log              LOG                              = LogFactory.getLog(UsageAgreementBean.class);

    private static final String           draftUsageAgreementsTextsModel   = "operDraftUsageAgreementsTexts";

    private static final String           currentUsageAgreementsTextsModel = "operCurrentUsageAgreementsTexts";

    private static final String           usageAgreementsTextsModel        = "operUsageAgreementTextList";

    @In(create = true)
    FacesMessages                         facesMessages;

    @EJB
    private UsageAgreementService         usageAgreementService;

    private String                        language;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private ApplicationEntity             selectedApplication;

    @In(required = false)
    @Out(required = false, scope = ScopeType.SESSION)
    private UsageAgreementEntity          operSelectedUsageAgreement;

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
    @DataModel(value = usageAgreementsTextsModel)
    private Set<UsageAgreementTextEntity> usageAgreementTextList;

    @DataModelSelection(value = usageAgreementsTextsModel)
    @In(value = "operSelectedUsageAgreementText", required = false)
    @Out(value = "operSelectedUsageAgreementText", required = false, scope = ScopeType.SESSION)
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
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(usageAgreementsTextsModel)
    public void usageAgreementTextListFactory() {

        this.usageAgreementTextList = this.operSelectedUsageAgreement.getUsageAgreementTexts();
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(draftUsageAgreementsTextsModel)
    public void draftUsageAgreementsTextsFactory() throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("get draft texts");
        this.draftUsageAgreement = this.usageAgreementService.getDraftUsageAgreement(this.selectedApplication.getName());
        if (null == this.draftUsageAgreement)
            return;
        this.draftUsageAgreementsTexts = this.draftUsageAgreement.getUsageAgreementTexts();
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(currentUsageAgreementsTextsModel)
    public void currentUsageAgreementsTextsFactory() throws PermissionDeniedException, ApplicationNotFoundException {

        LOG.debug("get current texts");
        this.currentUsageAgreement = this.usageAgreementService.getCurrentUsageAgreement(this.selectedApplication.getName());
        if (null == this.currentUsageAgreement)
            return;
        this.currentUsageAgreementsTexts = this.currentUsageAgreement.getUsageAgreementTexts();
    }

    /*
     * Accessors
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public UsageAgreementEntity getCurrentUsageAgreement() throws PermissionDeniedException, ApplicationNotFoundException {

        this.currentUsageAgreement = this.usageAgreementService.getCurrentUsageAgreement(this.selectedApplication.getName());
        return this.currentUsageAgreement;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public UsageAgreementEntity getDraftUsageAgreement() throws ApplicationNotFoundException, PermissionDeniedException {

        this.draftUsageAgreement = this.usageAgreementService.getDraftUsageAgreement(this.selectedApplication.getName());
        return this.draftUsageAgreement;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getLanguage() {

        return this.language;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setLanguage(String language) {

        this.language = language;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
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
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String viewText() {

        LOG.debug("view usage agreement text: language=" + this.selectedUsageAgreementText.getLanguage());
        return "viewtext";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String viewCurrentText() {

        LOG.debug("view text: language=" + this.selectedCurrentUsageAgreementText.getLanguage());
        this.selectedUsageAgreementText = this.selectedCurrentUsageAgreementText;
        return "viewtext";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String viewDraftText() {

        LOG.debug("view draft text: language=" + this.selectedDraftUsageAgreementText.getLanguage());
        this.selectedUsageAgreementText = this.selectedDraftUsageAgreementText;
        return "viewtext";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String createUsageAgreement() throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("create draft usage agreement");
        if (null != this.currentUsageAgreement) {
            this.draftUsageAgreement = this.usageAgreementService.createDraftUsageAgreement(this.selectedApplication.getName(),
                    this.currentUsageAgreement.getUsageAgreementVersion());
        } else {
            this.draftUsageAgreement = this.usageAgreementService.createDraftUsageAgreement(this.selectedApplication.getName(),
                    this.selectedApplication.getCurrentApplicationUsageAgreement());
        }
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String editCurrentText() throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("edit current usage agreement text: language=" + this.selectedCurrentUsageAgreementText.getLanguage());
        UsageAgreementEntity usageAgreement = this.usageAgreementService.createDraftUsageAgreement(this.selectedApplication.getName(),
                this.currentUsageAgreement.getUsageAgreementVersion());
        this.selectedUsageAgreementText = usageAgreement.getUsageAgreementText(this.selectedCurrentUsageAgreementText.getLanguage());
        return "edittext";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String editDraftText() {

        LOG.debug("edit draft usage agreement text: language=" + this.selectedDraftUsageAgreementText.getLanguage());
        this.selectedUsageAgreementText = this.selectedDraftUsageAgreementText;
        return "edittext";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String removeDraftText() throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("remove draft text: language=" + this.selectedDraftUsageAgreementText.getLanguage());
        this.usageAgreementService.removeDraftUsageAgreementText(this.selectedApplication.getName(),
                this.selectedDraftUsageAgreementText.getLanguage());
        this.draftUsageAgreementsTextsFactory();
        return "removed";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String addText() throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("add draft text: language=" + this.language);
        this.selectedUsageAgreementText = this.usageAgreementService.createDraftUsageAgreementText(this.selectedApplication.getName(),
                this.language, "");
        this.draftUsageAgreementsTextsFactory();
        return "edittext";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String saveText() throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("save usage agreement text: language=" + this.selectedUsageAgreementText.getLanguage());
        String text = this.selectedUsageAgreementText.getText();
        this.usageAgreementService.setDraftUsageAgreementText(this.selectedApplication.getName(),
                this.selectedUsageAgreementText.getLanguage(), text);
        this.draftUsageAgreementsTextsFactory();
        return "saved";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String releaseDraft() throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("release draft usage agreement");
        this.usageAgreementService.updateUsageAgreement(this.selectedApplication.getName());
        this.currentUsageAgreementsTextsFactory();
        this.draftUsageAgreementsTextsFactory();
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String removeDraft() throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("remove draft usage agreement");
        this.usageAgreementService.removeDraftUsageAgreement(this.selectedApplication.getName());
        this.currentUsageAgreementsTextsFactory();
        this.draftUsageAgreementsTextsFactory();
        return "success";
    }
}
