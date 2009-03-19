/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.webapp.pages.applications;

import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.user.webapp.pages.MainPage;
import net.link.safeonline.user.webapp.template.UserTemplatePage;
import net.link.safeonline.user.webapp.template.NavigationPanel.Panel;
import net.link.safeonline.webapp.components.toggle.ToggleBody;
import net.link.safeonline.webapp.components.toggle.ToggleHeader;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.web.RequireLogin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;


@RequireLogin(loginPage = MainPage.class)
public class ViewSubscriptionPage extends UserTemplatePage {

    static final Log           LOG                       = LogFactory.getLog(ViewSubscriptionPage.class);

    private static final long  serialVersionUID          = 1L;

    public static final String INFORMATION_HEADER_ID     = "information_header";
    public static final String INFORMATION_BODY_ID       = "information_body";
    public static final String NAME_ID                   = "name";
    public static final String OWNER_NAME_ID             = "owner_name";
    public static final String DESCRIPTION_ID            = "description";

    public static final String USAGE_AGREEMENT_HEADER_ID = "usage_agreement_header";
    public static final String USAGE_AGREEMENT_BODY_ID   = "usage_agreement_body";
    public static final String USAGE_AGREEMENT_ID        = "usage_agreement";

    public static final String IDENTITY_HEADER_ID        = "identity_header";
    public static final String IDENTITY_BODY_ID          = "identity_body";
    public static final String IDENTITY_ATTRIBUTES       = "identity_attributes";
    public static final String IDENTITY_ATTRIBUTE_NAME   = "identity_attribute_name";
    public static final String DATA_MINING_IMAGE         = "data_mining_image";

    public static final String BACK_FORM_ID              = "back_form";
    public static final String BACK_BUTTON_ID            = "back";

    @EJB(mappedName = UsageAgreementService.JNDI_BINDING)
    UsageAgreementService      usageAgreementService;

    @EJB(mappedName = IdentityService.JNDI_BINDING)
    IdentityService            identityService;


    public ViewSubscriptionPage(SubscriptionEntity subscription) {

        super(Panel.applications);

        String name = subscription.getApplication().getFriendlyName();
        if (null == name) {
            name = subscription.getApplication().getName();
        }

        getSidebar(localize("helpSubscription"), false);

        /*
         * Add Information Toggle Panel
         */
        final ToggleHeader informationHeader = new ToggleHeader(INFORMATION_HEADER_ID, localize("information"), true);
        getContent().add(informationHeader);
        ToggleBody informationBody = new ToggleBody(INFORMATION_BODY_ID, informationHeader);
        informationHeader.addTargetComponent(informationBody);
        informationHeader.add(informationBody);

        informationBody.add(new Label(NAME_ID, name));
        informationBody.add(new Label(OWNER_NAME_ID, subscription.getApplication().getApplicationOwner().getName()));
        informationBody.add(new Label(DESCRIPTION_ID, subscription.getApplication().getDescription()));

        /*
         * Add Usage Agreement toggle panel
         */
        String usageAgreementText;
        try {
            usageAgreementText = usageAgreementService.getUsageAgreementText(subscription.getApplication().getId(),
                    getLocale().getLanguage(), subscription.getConfirmedUsageAgreementVersion());
        } catch (ApplicationNotFoundException e) {
            error(localize("errorApplicationNotFound"));
            return;
        }
        final ToggleHeader usageAgreementHeader = new ToggleHeader(USAGE_AGREEMENT_HEADER_ID, localize("confirmedUsageAgreement"), false);
        getContent().add(usageAgreementHeader);
        ToggleBody usageAgreementBody = new ToggleBody(USAGE_AGREEMENT_BODY_ID, usageAgreementHeader);
        usageAgreementHeader.addTargetComponent(usageAgreementBody);
        usageAgreementHeader.add(usageAgreementBody);
        usageAgreementHeader.setVisible(usageAgreementText != null);

        usageAgreementBody.add(new Label(USAGE_AGREEMENT_ID, usageAgreementText));

        /*
         * Add Identity toggle panel
         */
        List<AttributeDO> confirmedIdentityAttributes;
        try {
            confirmedIdentityAttributes = identityService.listConfirmedIdentity(subscription.getApplication().getName(), getLocale());
        } catch (SubscriptionNotFoundException e) {
            error(localize("errorSubscriptionNotFound"));
            return;
        } catch (ApplicationNotFoundException e) {
            error(localize("errorApplicationNotFound"));
            return;
        } catch (ApplicationIdentityNotFoundException e) {
            error(localize("errorApplicationIdentityNotFound"));
            return;
        }

        final ToggleHeader identityHeader = new ToggleHeader(IDENTITY_HEADER_ID, localize("confirmedIdentity"), false);
        getContent().add(identityHeader);
        ToggleBody identityBody = new ToggleBody(IDENTITY_BODY_ID, identityHeader);
        identityHeader.addTargetComponent(identityBody);
        identityHeader.add(identityBody);
        identityHeader.setVisible(!confirmedIdentityAttributes.isEmpty());

        identityBody.add(new ListView<AttributeDO>(IDENTITY_ATTRIBUTES, confirmedIdentityAttributes) {

            private static final long serialVersionUID = 1L;


            @Override
            protected void populateItem(ListItem<AttributeDO> item) {

                AttributeDO attribute = item.getModelObject();
                String attributeName = attribute.getHumanReadableName();
                if (null == attributeName) {
                    attributeName = attribute.getName();
                }
                item.add(new Label(IDENTITY_ATTRIBUTE_NAME, attributeName));

                Image languageImage = new Image(DATA_MINING_IMAGE, "override");
                languageImage.add(new SimpleAttributeModifier("src", WicketUtil.toServletRequest(getRequest()).getContextPath()
                        + "/images/icons/accept.png"));
                languageImage.setVisible(attribute.isDataMining());
                add(languageImage);

            }
        });

        getContent().add(new BackForm(BACK_FORM_ID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("subscription");
    }


    class BackForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        public BackForm(String id) {

            super(id);

            add(new Button(BACK_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    setResponsePage(ApplicationsPage.class);

                }

            });

        }

    }

}
