/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.webapp.pages;

import net.link.safeonline.auth.webapp.template.AuthenticationTemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.markup.html.basic.Label;


public class AuthenticationProtocolErrorPage extends AuthenticationTemplatePage {

    private static final long  serialVersionUID                 = 1L;

    public static final String PATH                             = "auth-protocol-error";

    public static final String PROTOCOL_ERROR_MESSAGE_ATTRIBUTE = "protocolErrorMessage";
    public static final String PROTOCOL_NAME_ATTRIBUTE          = "protocolName";

    public static final String PROTOCOL_NAME_LABEL_ID           = "protocolNameLabel";
    public static final String PROTOCOL_ERROR_MESSAGE_LABEL_ID  = "protocolErrorMessageLabel";


    public AuthenticationProtocolErrorPage() {

        getHeader();

        String protocolName = localize("errorMessage");
        String protocolErrorMessage = localize("errorMessage");

        if (null != WicketUtil.getHttpSession().getAttribute(PROTOCOL_NAME_ATTRIBUTE)) {
            protocolName = (String) WicketUtil.getHttpSession().getAttribute(PROTOCOL_NAME_ATTRIBUTE);
        }
        if (null != WicketUtil.getHttpSession().getAttribute(PROTOCOL_ERROR_MESSAGE_ATTRIBUTE)) {
            protocolErrorMessage = (String) WicketUtil.getHttpSession().getAttribute(PROTOCOL_ERROR_MESSAGE_ATTRIBUTE);
        }

        getContent().add(new Label(PROTOCOL_NAME_LABEL_ID, protocolName));
        getContent().add(new Label(PROTOCOL_ERROR_MESSAGE_LABEL_ID, protocolErrorMessage));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("protocolError");
    }
}
