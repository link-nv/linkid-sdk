/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.oper.usage.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.entity.GlobalUsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementTextEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.usage.GlobalUsageAgreement;

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
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.faces.FacesMessages;


@Stateful
@Name("globalUsageAgreement")
@Scope(ScopeType.SESSION)
@LocalBinding(jndiBinding = GlobalUsageAgreement.JNDI_BINDING)
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class GlobalUsageAgreementBean implements GlobalUsageAgreement {

    private static final Log              LOG                              = LogFactory.getLog(GlobalUsageAgreementBean.class);

    private static final String           draftUsageAgreementsTextsModel   = "globalDraftUsageAgreementsTexts";

    private static final String           currentUsageAgreementsTextsModel = "globalCurrentUsageAgreementsTexts";

    private GlobalUsageAgreementEntity    globalDraftUsageAgreement;

    private GlobalUsageAgreementEntity    globalCurrentUsageAgreement;

    @Out(required = false, scope = ScopeType.SESSION)
    private UsageAgreementTextEntity      selectedUsageAgreementText;

    private String                        language;

    @In(create = true)
    FacesMessages                         facesMessages;

    @EJB
    private UsageAgreementService         usageAgreementService;

    /*
     * Seam Data models
     */
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
     * Lifecycle
     */
    @Remove
    @Destroy
    public void destroyCallback() {

        LOG.debug("destroy");
    }

    /*
     * Accessors
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setLanguage(String language) {

        LOG.debug("set language " + language);
        this.language = language;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getLanguage() {

        LOG.debug("get language " + this.language);
        return this.language;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public GlobalUsageAgreementEntity getCurrentUsageAgreement() {

        return this.usageAgreementService.getCurrentGlobalUsageAgreement();
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public GlobalUsageAgreementEntity getDraftUsageAgreement() {

        return this.usageAgreementService.getDraftGlobalUsageAgreement();
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getUsageAgreementVersion() {

        this.globalCurrentUsageAgreement = this.usageAgreementService.getCurrentGlobalUsageAgreement();
        LOG.debug("current: " + this.globalCurrentUsageAgreement);
        if (null == this.globalCurrentUsageAgreement)
            return "";
        LOG.debug("version: " + this.globalCurrentUsageAgreement.getUsageAgreementVersion());
        return this.globalCurrentUsageAgreement.getUsageAgreementVersion().toString();
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
    public String releaseDraft() {

        LOG.debug("release draft global usage agreement");
        this.usageAgreementService.updateGlobalUsageAgreement();
        this.currentUsageAgreementsTextsFactory();
        this.draftUsageAgreementsTextsFactory();
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String removeDraft() {

        LOG.debug("remove draft global usage agreement");
        this.usageAgreementService.removeDraftGlobalUsageAgreement();
        this.currentUsageAgreementsTextsFactory();
        this.draftUsageAgreementsTextsFactory();
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String addText() {

        LOG.debug("add draft text: language=" + this.language);
        this.selectedUsageAgreementText = this.usageAgreementService.createDraftGlobalUsageAgreementText(this.language, "");
        this.draftUsageAgreementsTextsFactory();
        return "edittext";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String saveText() {

        LOG.debug("save text: language=" + this.selectedUsageAgreementText.getLanguage());
        String text = this.selectedUsageAgreementText.getText();
        this.usageAgreementService.setDraftGlobalUsageAgreementText(this.selectedUsageAgreementText.getLanguage(), text);
        this.draftUsageAgreementsTextsFactory();
        return "saved";

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
    public String editDraftText() {

        LOG.debug("edit draft usage agreement text: language=" + this.selectedDraftUsageAgreementText.getLanguage());
        this.selectedUsageAgreementText = this.selectedDraftUsageAgreementText;
        return "edittext";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String removeDraftText() {

        LOG.debug("remove draft text: language=" + this.selectedDraftUsageAgreementText.getLanguage());
        this.usageAgreementService.removeDraftGlobalUsageAgreementText(this.selectedDraftUsageAgreementText.getLanguage());
        this.draftUsageAgreementsTextsFactory();
        return "removed";

    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String editCurrentText() {

        LOG.debug("edit current usage agreement text: language=" + this.selectedCurrentUsageAgreementText.getLanguage());
        GlobalUsageAgreementEntity draftUsageAgreement = this.usageAgreementService.getDraftGlobalUsageAgreement();
        if (null == draftUsageAgreement) {
            draftUsageAgreement = this.usageAgreementService.createDraftGlobalUsageAgreement();
        }
        this.selectedUsageAgreementText = draftUsageAgreement.getUsageAgreementText(this.selectedCurrentUsageAgreementText.getLanguage());
        return "edittext";

    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String createUsageAgreement() {

        LOG.debug("create draft usage agreement");
        this.globalDraftUsageAgreement = this.usageAgreementService.createDraftGlobalUsageAgreement();
        return "success";

    }

    /*
     * Factories
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(draftUsageAgreementsTextsModel)
    public void draftUsageAgreementsTextsFactory() {

        LOG.debug("get draft texts");
        this.globalDraftUsageAgreement = this.usageAgreementService.getDraftGlobalUsageAgreement();
        if (null == this.globalDraftUsageAgreement)
            return;
        this.draftUsageAgreementsTexts = this.globalDraftUsageAgreement.getUsageAgreementTexts();
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(currentUsageAgreementsTextsModel)
    public void currentUsageAgreementsTextsFactory() {

        LOG.debug("get current texts");
        this.globalCurrentUsageAgreement = this.usageAgreementService.getCurrentGlobalUsageAgreement();
        if (null == this.globalCurrentUsageAgreement)
            return;
        this.currentUsageAgreementsTexts = this.globalCurrentUsageAgreement.getUsageAgreementTexts();
    }
}
