/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.webapp.pages;

import net.link.safeonline.auth.webapp.template.AuthenticationTemplatePage;

public class UnsupportedProtocolPage extends AuthenticationTemplatePage {

    private static final long  serialVersionUID = 1L;

    public static final String PATH             = "unsupported-protocol";


    public UnsupportedProtocolPage() {

        getHeader();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("protocolUnsupported");
    }
}
