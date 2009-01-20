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

    @EJB(mappedName = UsageAgreementService.JNDI_BINDING)
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

        LOG.debug("get language " + language);
        return language;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public GlobalUsageAgreementEntity getCurrentUsageAgreement() {

        return usageAgreementService.getCurrentGlobalUsageAgreement();
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public GlobalUsageAgreementEntity getDraftUsageAgreement() {

        return usageAgreementService.getDraftGlobalUsageAgreement();
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getUsageAgreementVersion() {

        globalCurrentUsageAgreement = usageAgreementService.getCurrentGlobalUsageAgreement();
        LOG.debug("current: " + globalCurrentUsageAgreement);
        if (null == globalCurrentUsageAgreement)
            return "";
        LOG.debug("version: " + globalCurrentUsageAgreement.getUsageAgreementVersion());
        return globalCurrentUsageAgreement.getUsageAgreementVersion().toString();
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
        usageAgreementService.updateGlobalUsageAgreement();
        currentUsageAgreementsTextsFactory();
        draftUsageAgreementsTextsFactory();
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String removeDraft() {

        LOG.debug("remove draft global usage agreement");
        usageAgreementService.removeDraftGlobalUsageAgreement();
        currentUsageAgreementsTextsFactory();
        draftUsageAgreementsTextsFactory();
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String addText() {

        LOG.debug("add draft text: language=" + language);
        selectedUsageAgreementText = usageAgreementService.createDraftGlobalUsageAgreementText(language, "");
        draftUsageAgreementsTextsFactory();
        return "edittext";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String saveText() {

        LOG.debug("save text: language=" + selectedUsageAgreementText.getLanguage());
        String text = selectedUsageAgreementText.getText();
        usageAgreementService.setDraftGlobalUsageAgreementText(selectedUsageAgreementText.getLanguage(), text);
        draftUsageAgreementsTextsFactory();
        return "saved";

    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String viewCurrentText() {

        LOG.debug("view text: language=" + selectedCurrentUsageAgreementText.getLanguage());
        selectedUsageAgreementText = selectedCurrentUsageAgreementText;
        return "viewtext";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String viewDraftText() {

        LOG.debug("view draft text: language=" + selectedDraftUsageAgreementText.getLanguage());
        selectedUsageAgreementText = selectedDraftUsageAgreementText;
        return "viewtext";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String editDraftText() {

        LOG.debug("edit draft usage agreement text: language=" + selectedDraftUsageAgreementText.getLanguage());
        selectedUsageAgreementText = selectedDraftUsageAgreementText;
        return "edittext";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String removeDraftText() {

        LOG.debug("remove draft text: language=" + selectedDraftUsageAgreementText.getLanguage());
        usageAgreementService.removeDraftGlobalUsageAgreementText(selectedDraftUsageAgreementText.getLanguage());
        draftUsageAgreementsTextsFactory();
        return "removed";

    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String editCurrentText() {

        LOG.debug("edit current usage agreement text: language=" + selectedCurrentUsageAgreementText.getLanguage());
        GlobalUsageAgreementEntity draftUsageAgreement = usageAgreementService.getDraftGlobalUsageAgreement();
        if (null == draftUsageAgreement) {
            draftUsageAgreement = usageAgreementService.createDraftGlobalUsageAgreement();
        }
        selectedUsageAgreementText = draftUsageAgreement.getUsageAgreementText(selectedCurrentUsageAgreementText.getLanguage());
        return "edittext";

    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String createUsageAgreement() {

        LOG.debug("create draft usage agreement");
        globalDraftUsageAgreement = usageAgreementService.createDraftGlobalUsageAgreement();
        return "success";

    }

    /*
     * Factories
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(draftUsageAgreementsTextsModel)
    public void draftUsageAgreementsTextsFactory() {

        LOG.debug("get draft texts");
        globalDraftUsageAgreement = usageAgreementService.getDraftGlobalUsageAgreement();
        if (null == globalDraftUsageAgreement)
            return;
        draftUsageAgreementsTexts = globalDraftUsageAgreement.getUsageAgreementTexts();
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(currentUsageAgreementsTextsModel)
    public void currentUsageAgreementsTextsFactory() {

        LOG.debug("get current texts");
        globalCurrentUsageAgreement = usageAgreementService.getCurrentGlobalUsageAgreement();
        if (null == globalCurrentUsageAgreement)
            return;
        currentUsageAgreementsTexts = globalCurrentUsageAgreement.getUsageAgreementTexts();
    }
}
