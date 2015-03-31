/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2;

import net.link.util.logging.Logger;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.auth.*;
import net.link.util.common.URLUtils;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;


public class LinkIDHttpServletResponseAdapter extends HttpServletResponseAdapter {

    private static final Logger logger = Logger.get( LinkIDHttpServletResponseAdapter.class );

    private final HttpServletResponse httpServletResponse;

    private final Locale language;

    /**
     * Constructor.
     *
     * @param httpServletResponse servlet response to adapt
     * @param isSecure            whether the outbound connection is protected by SSL/TLS
     */
    public LinkIDHttpServletResponseAdapter(final HttpServletResponse httpServletResponse, final boolean isSecure, final Locale language) {

        super( httpServletResponse, isSecure );

        this.httpServletResponse = httpServletResponse;

        this.language = language;
    }

    @Override
    public void sendRedirect(String location) {

        // append linkID's request params
        if (null != language)
            location = URLUtils.addParameter( location, LinkIDRequestConstants.LANGUAGE_REQUEST_PARAM, language.getLanguage() );

        logger.dbg( "location=%s", location );

        try {
            httpServletResponse.sendRedirect( location );
        }
        catch (IOException e) {
            logger.err( e, "Unable to send redirect message" );
        }
    }
}
