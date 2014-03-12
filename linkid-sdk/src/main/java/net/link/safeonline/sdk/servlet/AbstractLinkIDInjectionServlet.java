/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.servlet;

import net.link.util.logging.Logger;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.*;
import net.link.safeonline.sdk.configuration.ConfigUtils;
import net.link.util.servlet.*;


/**
 * Abstract Injection Servlet.
 * <p/>
 * <ul>
 * <li>Injects request parameters into servlet fields.
 * <li>Injects and outjects session parameters.
 * <li>Injects EJBs.
 * <li>Injects servlet init parameters. If no defaultValue is specified, an {@link UnavailableException} will be thrown.
 * <li>Injects servlet context parameters. If no defaultValue is specified, an {@link UnavailableException} will be thrown.
 * <li>By default checks if the servlet is accessed with a secure connection. If context parameter <code>Protocol</code> is
 * <code>http</code> or <code>securityCheck</code> is set to <code>false</code> this check will be ommitted.
 * </ul>
 *
 * @author fcorneli
 */
public abstract class AbstractLinkIDInjectionServlet extends HttpServlet {

    private static final Logger logger = Logger.get( AbstractLinkIDInjectionServlet.class );

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // force UTF-8 encoding
        try {
            request.setCharacterEncoding( "UTF8" );
            response.setCharacterEncoding( "UTF8" );
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException( e );
        }

        String endpoint = getWrapperEndpoint( request );
        if (endpoint != null) {
            HttpServletRequestEndpointWrapper wrappedRequest = new HttpServletRequestEndpointWrapper( request, endpoint );
            HttpServletResponseEndpointWrapper wrappedResponse = new HttpServletResponseEndpointWrapper( wrappedRequest, response, endpoint );

            logger.dbg( "Wrapped request and response using endpoint: %s", endpoint );
            super.service( wrappedRequest, wrappedResponse );
        } else {
            logger.dbg( "No endpoint defined.  Not wrapping request and response." );
            super.service( request, response );
        }
    }

    protected String getWrapperEndpoint(HttpServletRequest request) {

        return ConfigUtils.getApplicationURL();
    }
}
