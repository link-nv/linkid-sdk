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
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.components.attribute.AttributeInputPanel;
import net.link.safeonline.webapp.template.ProgressAuthenticationPanel;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;


public class MissingAttributesPage extends AuthenticationTemplatePage {

    static final Log           LOG                         = LogFactory.getLog(MissingAttributesPage.class);

    private static final long  serialVersionUID            = 1L;

    public static final String PATH                        = "missing-attributes";

    public static final String MISSING_ATTRIBUTES_FORM_ID  = "missing_attributes_form";
    public static final String MISSING_ATTRIBUTES_LIST_ID  = "missing_attributes";
    public static final String MISSING_ATTRIBUTE_ID        = "missing_attribute";
    public static final String OPTIONAL_ATTRIBUTES_LIST_ID = "optional_attributes";
    public static final String OPTIONAL_ATTRIBUTE_ID       = "optional_attribute";
    public static final String SAVE_BUTTON_ID              = "save";

    @EJB(mappedName = IdentityService.JNDI_BINDING)
    IdentityService            identityService;

    long                       applicationId;

    String                     applicationFriendlyName;


    public MissingAttributesPage() {

        getSidebar(localize("helpMissingAttributes"));

        getHeader();

        getContent().add(new ProgressAuthenticationPanel("progress", ProgressAuthenticationPanel.stage.attributes));

        getContent().add(new MissingAttributesForm(MISSING_ATTRIBUTES_FORM_ID));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession());
        applicationId = protocolContext.getApplicationId();
        applicationFriendlyName = protocolContext.getApplicationFriendlyName();

        String title = localize("%l: %s", "authenticatingFor", applicationFriendlyName);
        return title;
    }


    class MissingAttributesForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        @SuppressWarnings("unchecked")
        public MissingAttributesForm(String id) {

            super(id);
            setMarkupId(id);

            add(new ListView<AttributeDO>(MISSING_ATTRIBUTES_LIST_ID, new AbstractReadOnlyModel<List<AttributeDO>>() {

                private static final long serialVersionUID = 1L;


                @Override
                public List<AttributeDO> getObject() {

                    try {
                        return identityService.listMissingAttributes(applicationId, getLocale());
                    }

                    catch (ApplicationNotFoundException e) {
                        error(localize("errorApplicationNotFound"));
                        return null;
                    } catch (ApplicationIdentityNotFoundException e) {
                        error(localize("errorApplicationIdentityNotFound"));
                        return null;
                    } catch (PermissionDeniedException e) {
                        error(localize("errorPermissionDenied"));
                        return null;
                    } catch (AttributeTypeNotFoundException e) {
                        error(localize("errorAttributeTypeNotFound"));
                        return null;
                    }
                }
            }) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void populateItem(final ListItem<AttributeDO> attributeItem) {

                    attributeItem.add(new AttributeInputPanel(MISSING_ATTRIBUTE_ID, attributeItem.getModelObject(), true));
                }

            });
            add(new ListView<AttributeDO>(OPTIONAL_ATTRIBUTES_LIST_ID, new AbstractReadOnlyModel<List<AttributeDO>>() {

                private static final long serialVersionUID = 1L;


                @Override
                public List<AttributeDO> getObject() {

                    try {
                        return identityService.listOptionalAttributes(applicationId, getLocale());
                    }

                    catch (ApplicationNotFoundException e) {
                        error(localize("errorApplicationNotFound"));
                        return null;
                    } catch (ApplicationIdentityNotFoundException e) {
                        error(localize("errorApplicationIdentityNotFound"));
                        return null;
                    } catch (PermissionDeniedException e) {
                        error(localize("errorPermissionDenied"));
                        return null;
                    } catch (AttributeTypeNotFoundException e) {
                        error(localize("errorAttributeTypeNotFound"));
                        return null;
                    }
                }
            }) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void populateItem(final ListItem<AttributeDO> attributeItem) {

                    attributeItem.add(new AttributeInputPanel(MISSING_ATTRIBUTE_ID, attributeItem.getModelObject(), true));
                }

            });

            add(new Button(SAVE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    try {
                        LOG.debug("save");
                        for (AttributeDO attribute : identityService.listMissingAttributes(applicationId, getLocale())) {
                            LOG.debug("required attribute to save: " + attribute);
                            try {
                                identityService.saveAttribute(attribute);
                            } catch (PermissionDeniedException e) {
                                LOG.debug("permission denied for attribute: " + attribute.getName());
                                error(localize("errorPermissionDenied"));
                                return;
                            } catch (AttributeTypeNotFoundException e) {
                                LOG.debug("attribute type not found: " + attribute.getName());
                                error(localize("errorAttributeTypeNotFound"));
                                return;
                            }
                        }
                        for (AttributeDO attribute : identityService.listOptionalAttributes(applicationId, getLocale())) {
                            LOG.debug("optional attribute to save: " + attribute);
                            try {
                                identityService.saveAttribute(attribute);
                            } catch (PermissionDeniedException e) {
                                LOG.debug("permission denied for attribute: " + attribute.getName());
                                error(localize("errorPermissionDenied"));
                                return;
                            } catch (AttributeTypeNotFoundException e) {
                                LOG.debug("attribute type not found: " + attribute.getName());
                                error(localize("errorAttributeTypeNotFound"));
                                return;
                            }
                        }
                    }

                    catch (ApplicationNotFoundException e) {
                        error(localize("errorApplicationNotFound"));
                        return;
                    } catch (ApplicationIdentityNotFoundException e) {
                        error(localize("errorApplicationIdentityNotFound"));
                        return;
                    } catch (PermissionDeniedException e) {
                        error(localize("errorPermissionDenied"));
                        return;
                    } catch (AttributeTypeNotFoundException e) {
                        error(localize("errorAttributeTypeNotFound"));
                        return;
                    }

                    HelpdeskLogger.add("missing attributes saved for application: " + applicationFriendlyName, LogLevelType.INFO);

                    getResponse().redirect(LoginServlet.SERVLET_PATH);
                    setRedirect(false);
                }
            });

            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
        }
    }
}
