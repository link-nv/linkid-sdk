/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.webapp.pages;

import net.link.safeonline.auth.webapp.template.AuthenticationTemplatePage;
import net.link.safeonline.authentication.ProtocolContext;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.link.Link;


public class FirstTimePage extends AuthenticationTemplatePage {

    private static final long  serialVersionUID      = 1L;

    public static final String PATH                  = "first-time";

    public static final String NEW_USER_LINK_ID      = "new-user-link";
    public static final String EXISTING_USER_LINK_ID = "existing-user-link";


    public FirstTimePage() {

        getSidebar(localize("helpFirstTime"), false);

        getHeader();

        getContent().add(new Link<String>(NEW_USER_LINK_ID) {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                throw new RestartResponseException(new NewUserPage());
            }
        });
        getContent().add(new Link<String>(EXISTING_USER_LINK_ID) {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                throw new RestartResponseException(new MainPage());
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
