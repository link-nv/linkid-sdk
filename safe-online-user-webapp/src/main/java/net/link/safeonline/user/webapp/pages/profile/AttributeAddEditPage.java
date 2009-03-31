/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.webapp.pages.profile;

import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.user.webapp.pages.MainPage;
import net.link.safeonline.user.webapp.template.UserTemplatePage;
import net.link.safeonline.user.webapp.template.NavigationPanel.Panel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.components.attribute.AttributeInputPanel;
import net.link.safeonline.wicket.web.RequireLogin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;


@RequireLogin(loginPage = MainPage.class)
public class AttributeAddEditPage extends UserTemplatePage {

    static final Log           LOG                  = LogFactory.getLog(AttributeAddEditPage.class);

    private static final long  serialVersionUID     = 1L;

    public static final String PATH                 = "attribute_add";

    public static final String ATTRIBUTE_ADD_FORM   = "attribute_add_form";
    public static final String ATTRIBUTE_CONTEXT_ID = "attribute_context";
    public static final String ATTRIBUTE_ID         = "attribute";
    public static final String ADD_BUTTON_ID        = "add";
    public static final String SAVE_BUTTON_ID       = "save";
    public static final String CANCEL_BUTTON_ID     = "cancel";

    @EJB(mappedName = IdentityService.JNDI_BINDING)
    IdentityService            identityService;

    boolean                    addMode              = true;


    public AttributeAddEditPage(AttributeDO selectedAttribute, boolean addMode) {

        super(Panel.profile);
        this.addMode = addMode;

        getSidebar(localize("helpProfileEdit"), false);

        getContent().add(new AttributeAddForm(ATTRIBUTE_ADD_FORM, selectedAttribute));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("addAttribute");
    }


    class AttributeAddForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        List<AttributeDO>         attributeContext;


        public AttributeAddForm(String id, AttributeDO selectedAttribute) {

            super(id);

            try {
                if (addMode) {
                    attributeContext = identityService.getAttributeTemplate(selectedAttribute);
                } else {
                    attributeContext = identityService.getAttributeEditContext(selectedAttribute);
                }
            } catch (AttributeTypeNotFoundException e) {
                error(localize("errorAttributeTypeNotFound"));
                return;
            }
            final List<AttributeInputPanel> attributePanels = new LinkedList<AttributeInputPanel>();
            for (AttributeDO attribute : attributeContext) {
                attributePanels.add(new AttributeInputPanel(ATTRIBUTE_ID, attribute, true));
            }

            add(new ListView<AttributeDO>(ATTRIBUTE_CONTEXT_ID, attributeContext) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void populateItem(final ListItem<AttributeDO> attributeItem) {

                    attributeItem.add(attributePanels.get(attributeItem.getIndex()));
                }

            });

            add(new Button(ADD_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("add");

                    try {
                        identityService.addAttribute(attributeContext);
                    } catch (PermissionDeniedException e) {
                        AttributeAddForm.this.error(localize("errorPermissionDenied"));
                        return;
                    } catch (AttributeTypeNotFoundException e) {
                        AttributeAddForm.this.error(localize("errorAttributeTypeNotFound"));
                        return;
                    }

                    setResponsePage(ProfilePage.class);

                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public boolean isVisible() {

                    return addMode;
                }
            });

            add(new Button(SAVE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("save");

                    try {
                        for (AttributeDO attribute : attributeContext) {
                            identityService.saveAttribute(attribute);
                        }
                    } catch (PermissionDeniedException e) {
                        AttributeAddForm.this.error(localize("errorUserNotAllowedToEditAttribute"));
                        return;
                    } catch (AttributeTypeNotFoundException e) {
                        AttributeAddForm.this.error(localize("errorAttributeTypeNotFound"));
                        return;
                    }

                    setResponsePage(ProfilePage.class);

                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public boolean isVisible() {

                    return !addMode;
                }
            });

            add(new Button(CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("cancel");
                    setResponsePage(ProfilePage.class);
                }
            }.setDefaultFormProcessing(false));

            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
        }
    }

}
