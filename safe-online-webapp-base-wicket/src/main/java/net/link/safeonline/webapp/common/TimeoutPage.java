/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.common;

import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.wicket.markup.html.link.PageLink;


public class TimeoutPage extends TemplatePage {

    private static final long  serialVersionUID = 1L;

    public static final String PATH             = "timeout";

    public static final String MAIN_LINK_ID     = "main";


    public TimeoutPage() {

        getHeader();

        getContent().add(new PageLink<String>(MAIN_LINK_ID, getApplication().getHomePage()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("timeout");
    }
}
