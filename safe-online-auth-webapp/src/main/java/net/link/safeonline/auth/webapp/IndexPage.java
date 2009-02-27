/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.webapp;

public class IndexPage extends AuthenticationTemplatePage {

    private static final long  serialVersionUID = 1L;

    public static final String PATH             = "index";


    public IndexPage() {

        getHeader();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("authWebapp");
    }
}
