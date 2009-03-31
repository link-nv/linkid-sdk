/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.common;

import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.webapp.template.TemplatePage;


/**
 * <h2>{@link NotFoundPage}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Nov 6, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class NotFoundPage extends TemplatePage {

    private static final long serialVersionUID = 1L;


    public NotFoundPage() {

        super();

        getHeader();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("pageUnavailable");
    }

    @Override
    protected void configureResponse() {

        super.configureResponse();
        getWebRequestCycle().getWebResponse().getHttpServletResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    public boolean isVersioned() {

        return false;
    }

    @Override
    public boolean isErrorPage() {

        return true;
    }

}
