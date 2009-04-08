/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.common;

import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.markup.html.basic.Label;


/**
 * <h2>{@link ProtocolErrorPage}<br>
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
public class ProtocolErrorPage extends TemplatePage {

    private static final long serialVersionUID = 1L;


    public ProtocolErrorPage() {

        getHeader();

        String errorMessage = (String) WicketUtil.getServletRequest().getSession().getAttribute("ErrorMessage");

        getContent().add(new Label("errorMessage", errorMessage));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("protocolError");
    }

}
