/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.webapp.pages;

import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.auth.servlet.LoginServlet;
import net.link.safeonline.auth.webapp.template.AuthenticationTemplatePage;
import net.link.safeonline.authentication.ProtocolContext;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.ProgressAuthenticationPanel;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;


public class IdentityConfirmationPage extends AuthenticationTemplatePage {

    private static final long  serialVersionUID              = 1L;

    public static final String PATH                          = "identity-confirmation";

    public static final String IDENTITY_CONFIRMATION_LIST_ID = "identityConfirmationList";
    public static final String NAME_ID                       = "name";
    public static final String DATAMINING_IMAGE_ID           = "dataMiningImage";

    public static final String IDENTITY_FORM_ID              = "identity_form";
    public static final String AGREE_BUTTON_ID               = "agree";
    public static final String REJECT_BUTTON_ID              = "reject";

    @EJB(mappedName = IdentityService.JNDI_BINDING)
    IdentityService            identityService;


    public IdentityConfirmationPage() {

        getSidebar(localize("helpIdentityConfirmation"));

        getHeader();

        getContent().add(new ProgressAuthenticationPanel("progress", ProgressAuthenticationPanel.stage.attributes));

        getContent().add(new ListView<AttributeDO>(IDENTITY_CONFIRMATION_LIST_ID, new AbstractReadOnlyModel<List<AttributeDO>>() {

            private static final long serialVersionUID = 1L;


            @Override
            public List<AttributeDO> getObject() {

                ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession());

                try {
                    return identityService.listIdentityAttributesToConfirm(protocolContext.getApplicationId(), getLocale());
                }

                catch (SubscriptionNotFoundException e) {
                    error(localize("errorSubscriptionNotFound"));
                    throw new RuntimeException(e);
                } catch (ApplicationNotFoundException e) {
                    error(localize("errorApplicationNotFound"));
                    throw new RuntimeException(e);
                } catch (ApplicationIdentityNotFoundException e) {
                    error(localize("errorApplicationIdentityNotFound"));
                    throw new RuntimeException(e);
                }
            }
        }) {

            private static final long serialVersionUID = 1L;


            @Override
            protected void populateItem(final ListItem<AttributeDO> attributeItem) {

                String name = attributeItem.getModelObject().getHumanReadableName();
                if (null == name) {
                    name = attributeItem.getModelObject().getName();
                }
                attributeItem.add(new Label(NAME_ID, name));

                Image dataMiningImage = new Image(DATAMINING_IMAGE_ID, "override");
                dataMiningImage.add(new SimpleAttributeModifier("src", WicketUtil.getServletRequest().getContextPath()
                        + "/images/icons/accept.png"));
                dataMiningImage.setVisible(attributeItem.getModelObject().isDataMining());
                attributeItem.add(dataMiningImage);
            }

        });

        getContent().add(new IdentityForm(IDENTITY_FORM_ID));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession());
        return localize("%l: %s", "authenticatingFor", protocolContext.getApplicationFriendlyName());
    }


    class IdentityForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        @SuppressWarnings("unchecked")
        public IdentityForm(String id) {

            super(id);
            setMarkupId(id);

            add(new Button(AGREE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession());
                    try {
                        identityService.confirmIdentity(protocolContext.getApplicationId());
                    } catch (SubscriptionNotFoundException e) {
                        IdentityForm.this.error(localize("errorSubscriptionNotFound"));
                        return;
                    } catch (ApplicationNotFoundException e) {
                        IdentityForm.this.error(localize("errorApplicationNotFound"));
                        return;
                    } catch (ApplicationIdentityNotFoundException e) {
                        IdentityForm.this.error(localize("errorApplicationIdentityNotFound"));
                        return;
                    }

                    getResponse().redirect(LoginServlet.SERVLET_PATH);
                    setRedirect(false);

                }
            });

            add(new Button(REJECT_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    throw new RestartResponseException(new IdentityRejectionPage());

                }
            });

            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
        }
    }
}
