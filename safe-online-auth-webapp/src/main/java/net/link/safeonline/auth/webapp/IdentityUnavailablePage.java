/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.webapp;

import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.authentication.ProtocolContext;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;


public class IdentityUnavailablePage extends AuthenticationTemplatePage {

    private static final long  serialVersionUID              = 1L;

    public static final String PATH                          = "identity-unavailable";

    public static final String IDENTITY_CONFIRMATION_LIST_ID = "identityUnavailableList";
    public static final String NAME_ID                       = "name";

    public static final String MAIN_LINK_ID                  = "main";

    @EJB(mappedName = IdentityService.JNDI_BINDING)
    IdentityService            identityService;


    public IdentityUnavailablePage() {

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));
        final String applicationUrl = findApplicationUrl();

        getHeader();

        List<AttributeDO> unavailableList = new LinkedList<AttributeDO>();
        List<AttributeDO> missingAttributes;
        try {
            missingAttributes = identityService.listMissingAttributes(protocolContext.getApplicationId(), getLocale());
        } catch (ApplicationNotFoundException e) {
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
        for (AttributeDO missingAttribute : missingAttributes) {
            if (!missingAttribute.isEditable()) {
                unavailableList.add(missingAttribute);
            }
        }

        getContent().add(new ListView<AttributeDO>(IDENTITY_CONFIRMATION_LIST_ID, unavailableList) {

            private static final long serialVersionUID = 1L;


            @Override
            protected void populateItem(final ListItem<AttributeDO> attributeItem) {

                String name = attributeItem.getModelObject().getHumanReadableName();
                if (null == name) {
                    name = attributeItem.getModelObject().getName();
                }
                attributeItem.add(new Label(NAME_ID, name));
            }

        });

        getContent().add(new Link<String>(MAIN_LINK_ID) {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                getResponse().redirect(applicationUrl);
                setRedirect(false);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isVisible() {

                return null != applicationUrl;
            }
        });

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));
        String title = localize("%l: %s", "authenticatingFor", protocolContext.getApplicationFriendlyName());
        return title;
    }
}
