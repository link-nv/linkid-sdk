/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import net.link.safeonline.sdk.auth.jaas.SimplePrincipal;


/**
 * Login HTTP Servlet Request Wrapper. This wrapper adds user principal to the request.
 *
 * @author fcorneli
 *
 */
public class LoginHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final Principal userPrincipal;


    /**
     * Main constructor.
     *
     * @param request
     * @param username
     *            the username to use for user principal.
     *
     */
    public LoginHttpServletRequestWrapper(HttpServletRequest request, String username) {

        super(request);

        this.userPrincipal = new SimplePrincipal(username);
    }

    @Override
    public Principal getUserPrincipal() {

        return this.userPrincipal;
    }
}
