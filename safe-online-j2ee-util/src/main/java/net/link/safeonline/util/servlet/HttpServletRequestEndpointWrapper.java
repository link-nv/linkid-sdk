/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.servlet;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


/**
 * {@link HttpServletRequest} wrapper used to provide the correct endpoint URI when behind a proxy or load balancer.
 * 
 * @author lhunath
 * 
 */
public class HttpServletRequestEndpointWrapper extends HttpServletRequestWrapper {

    private URI requestBaseUri;


    public HttpServletRequestEndpointWrapper(HttpServletRequest request, String requestBaseUri) {

        super(request);

        this.requestBaseUri = URI.create(requestBaseUri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequestURI() {

        try {
            URI originalRequest = new URI(super.getRequestURI());

            return new URI(requestBaseUri.getScheme(), requestBaseUri.getAuthority(), originalRequest.getPath(),
                    originalRequest.getQuery(), originalRequest.getFragment()).toASCIIString();
        }

        catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public StringBuffer getRequestURL() {

        return new StringBuffer(getRequestURI());
    }

}
