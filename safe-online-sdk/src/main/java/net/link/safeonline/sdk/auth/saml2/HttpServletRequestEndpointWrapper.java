/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.saml2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


/**
 * HttpServletRequest wrapper used to provide opensaml with the correct endpoint URI.
 * 
 * @author wvdhaute
 * 
 */
public class HttpServletRequestEndpointWrapper extends HttpServletRequestWrapper {

    private String requestUrl;


    public HttpServletRequestEndpointWrapper(HttpServletRequest request, String requestUrl) {

        super(request);
        this.requestUrl = requestUrl;
    }

    @Override
    public StringBuffer getRequestURL() {

        return new StringBuffer(this.requestUrl);
    }

}
