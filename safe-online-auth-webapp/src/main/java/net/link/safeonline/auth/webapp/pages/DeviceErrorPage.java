/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.webapp.pages;

import net.link.safeonline.auth.webapp.template.AuthenticationTemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;


public class DeviceErrorPage extends AuthenticationTemplatePage {

    private static final long  serialVersionUID               = 1L;

    public static final String PATH                           = "device-error";

    public static final String DEVICE_ERROR_MESSAGE_ATTRIBUTE = "deviceErrorMessage";

    public static final String DEVICE_ERROR_MESSAGE_LABEL_ID  = "deviceErrorMessageLabel";

    public static final String MAIN_LINK_ID                   = "main";


    public DeviceErrorPage() {

        getHeader();

        String deviceErrorMessage = localize("errorMessage");

        if (null != WicketUtil.getHttpSession().getAttribute(DEVICE_ERROR_MESSAGE_ATTRIBUTE)) {
            deviceErrorMessage = (String) WicketUtil.getHttpSession().getAttribute(DEVICE_ERROR_MESSAGE_ATTRIBUTE);
        }

        getContent().add(new Label(DEVICE_ERROR_MESSAGE_LABEL_ID, deviceErrorMessage));

        getContent().add(new Link<String>(MAIN_LINK_ID) {

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

        return localize("deviceError");
    }
}
