/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.servlet;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


/**
 * {@link HttpServletResponse} wrapper used to convert relative redirects correctly when behind a proxy or load balancer.
 * 
 * Relative redirects sent to this {@link HttpServletResponse} will be converted in absolute URLs by using:
 * <ul>
 * <li>A response base for the scheme and authority of the redirection target.</li>
 * <li>The current {@link HttpServletRequest}'s requestURL's path for the context of our relative path.</li>
 * </ul>
 * 
 * @author lhunath
 */
public class HttpServletResponseEndpointWrapper extends HttpServletResponseWrapper {

    private URI requestBaseUri;


    /**
     * @param request
     *            The {@link HttpServletRequest} that caused this servlet response. Used to resolve relative redirection paths sent to this
     *            {@link HttpServletResponse}.
     * @param response
     *            The real {@link HttpServletResponse} that we're wrapping.
     * @param responseBase
     *            The base URI that we're using for the scheme and authority part of redirections.
     */
    public HttpServletResponseEndpointWrapper(HttpServletRequest request, HttpServletResponse response, String responseBase) {

        super(response);

        try {
            URI requestUri = new URI(request.getRequestURI());
            URI responseBaseUri = new URI(responseBase);

            requestBaseUri = new URI(responseBaseUri.getScheme(), responseBaseUri.getAuthority(), requestUri.getPath(),
                    requestUri.getQuery(), requestUri.getFragment());
        }

        catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendRedirect(String location)
            throws IOException {

        URI locationUri = URI.create(location);
        // FIXME
        if (location.startsWith("/")) {
            locationUri = URI.create(location.substring(1));
        }

        super.sendRedirect(requestBaseUri.resolve(locationUri).toASCIIString());
    }
}
