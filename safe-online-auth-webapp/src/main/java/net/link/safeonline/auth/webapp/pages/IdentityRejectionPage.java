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

import org.apache.wicket.markup.html.link.Link;


public class IdentityRejectionPage extends AuthenticationTemplatePage {

    private static final long  serialVersionUID = 1L;

    public static final String MAIN_LINK_ID     = "main";


    public IdentityRejectionPage() {

        getHeader();

        getContent().add(new Link<String>(MAIN_LINK_ID) {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                getResponse().redirect(findApplicationUrl());
                setRedirect(false);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isVisible() {

                return null != findApplicationUrl();
            }
        });

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession());
        return localize("%l: %s", "authenticatingFor", protocolContext.getApplicationFriendlyName());
    }

}
