/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.webapp.pages.profile;

import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.user.webapp.UserSession;
import net.link.safeonline.user.webapp.pages.MainPage;
import net.link.safeonline.user.webapp.template.UserTemplatePage;
import net.link.safeonline.user.webapp.template.NavigationPanel.Panel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.components.attribute.AttributeNameOutputPanel;
import net.link.safeonline.webapp.components.attribute.AttributeOutputPanel;
import net.link.safeonline.wicket.web.RequireLogin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;


@RequireLogin(loginPage = MainPage.class)
public class ProfilePage extends UserTemplatePage {

    static final Log           LOG                    = LogFactory.getLog(ProfilePage.class);

    private static final long  serialVersionUID       = 1L;

    public static final String USERNAME_LABEL_ID      = "username";

    public static final String PROFILE_EMPTY_LABEL_ID = "profile_empty";

    public static final String ATTRIBUTES_ID          = "attributes";
    public static final String ATTRIBUTE_NAME_ID      = "attribute_name";
    public static final String ATTRIBUTE_VALUE_ID     = "attribute_value";
    public static final String EDIT_LINK_ID           = "edit";
    public static final String REMOVE_LINK_ID         = "remove";
    public static final String ADD_LINK_ID            = "add";

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService     subjectService;

    @EJB(mappedName = IdentityService.JNDI_BINDING)
    IdentityService            identityService;


    public ProfilePage() {

        super(Panel.profile);

        getSidebar(localize("helpProfile"), false);

        String username = subjectService.getSubjectLogin(UserSession.get().getUserId());
        getContent().add(new Label(USERNAME_LABEL_ID, username));

        List<AttributeDO> attributes = null;
        try {
            attributes = identityService.listAttributes(getLocale());
        } catch (AttributeTypeNotFoundException e) {
            error(localize("errorAttributeTypeNotFound"));
            return;
        } catch (PermissionDeniedException e) {
            error(localize("errorPermissionDenied"));
            return;
        } catch (ApplicationIdentityNotFoundException e) {
            error(localize("errorApplicationIdentityNotFound"));
            return;
        }
        final List<AttributeDO> attributeList = attributes;

        getContent().add(new Label(PROFILE_EMPTY_LABEL_ID, localize("profileEmpty")).setVisible(attributeList.isEmpty()));

        getContent().add(new ListView<AttributeDO>(ATTRIBUTES_ID, attributeList) {

            private static final long serialVersionUID = 1L;


            @Override
            protected void populateItem(final ListItem<AttributeDO> attributeItem) {

                final AttributeDO attribute = attributeItem.getModelObject();
                attributeItem.add(new AttributeNameOutputPanel(ATTRIBUTE_NAME_ID, attribute));
                attributeItem.add(new AttributeOutputPanel(ATTRIBUTE_VALUE_ID, attribute));

                attributeItem.add(new Link<String>(EDIT_LINK_ID) {

                    private static final long serialVersionUID = 1L;


                    @Override
                    public void onClick() {

                        setResponsePage(new AttributeAddEditPage(attribute, false));

                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public boolean isVisible() {

                        return attribute.isEditable();
                    }

                });

                attributeItem.add(new Link<String>(ADD_LINK_ID) {

                    private static final long serialVersionUID = 1L;


                    @Override
                    public void onClick() {

                        setResponsePage(new AttributeAddEditPage(attribute, true));

                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public boolean isVisible() {

                        return attribute.isEditable() && attribute.isMultivalued();
                    }

                });

                attributeItem.add(new Link<String>(REMOVE_LINK_ID) {

                    private static final long serialVersionUID = 1L;


                    @Override
                    public void onClick() {

                        try {
                            identityService.removeAttribute(attribute);
                        } catch (PermissionDeniedException e) {
                            error(localize("errorUserNotAllowedToRemoveAttribute"));
                            return;
                        } catch (AttributeNotFoundException e) {
                            error(localize("errorAttributeNotFound"));
                            return;
                        } catch (AttributeTypeNotFoundException e) {
                            error(localize("errorAttributeTypeNotFound"));
                            return;
                        }
                        setResponsePage(ProfilePage.class);

                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public boolean isVisible() {

                        return attribute.isEditable();
                    }
                });

            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isVisible() {

                return !attributeList.isEmpty();
            }

        });

        getContent().add(new ErrorFeedbackPanel("feedback"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("profile");
    }

}
