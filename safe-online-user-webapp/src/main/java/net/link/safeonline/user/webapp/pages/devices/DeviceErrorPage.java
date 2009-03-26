/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.user.webapp.pages.devices;

import net.link.safeonline.user.servlet.DeviceLandingServlet;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.PageLink;


/**
 * <h2>{@link DeviceErrorPage}<br>
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
public class DeviceErrorPage extends TemplatePage {

    private static final long  serialVersionUID = 1L;

    public static final String PATH             = "device-error";

    public static final String ERROR_MESSAGE_ID = "error_message";
    public static final String MAIN_LINK_ID     = "main";


    public DeviceErrorPage() {

        super();

        getHeader();

        getContent().add(
                new Label(ERROR_MESSAGE_ID, (String) WicketUtil.getHttpSession().getAttribute(
                        DeviceLandingServlet.DEVICE_ERROR_MESSAGE_ATTRIBUTE)));
        getContent().add(new PageLink<String>(MAIN_LINK_ID, getApplication().getHomePage()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("deviceError");
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
