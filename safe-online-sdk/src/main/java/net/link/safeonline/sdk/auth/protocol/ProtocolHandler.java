/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol;

import com.google.common.base.Function;
import java.io.IOException;
import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.configuration.*;
import net.link.util.exception.ValidationFailedException;
import org.jetbrains.annotations.Nullable;


/**
 * Interface for protocol handlers. Protocol handlers are stateful since they must be capable of handling the challenge-response aspect of
 * the authentication protocol. Since protocol handlers are stored in the HTTP session they must be serializable.
 *
 * @author fcorneli
 */
public interface ProtocolHandler extends Serializable {

    Protocol getProtocol();

    /**
     * Initiates the authentication request towards the SafeOnline authentication web application.
     *
     * @param response the servlet response.
     * @param context  authentication context containing optional device policy, ...
     *
     * @return Authentication protocol request context
     *
     * @throws IOException The request could not be written to the response.
     */
    AuthnProtocolRequestContext sendAuthnRequest(HttpServletResponse response, AuthenticationContext context)
            throws IOException;

    /**
     * Finalize the active authentication process.
     *
     * @param request HTTP Servlet Request
     *
     * @return Details about the authentication such as the authenticated user's application identifier or <code>null</code> if the handler
     *         thinks the request has nothing to do with authentication.
     *
     * @throws ValidationFailedException Validation failed for the incoming authentication response.
     */
    @Nullable
    AuthnProtocolResponseContext findAndValidateAuthnResponse(HttpServletRequest request)
            throws ValidationFailedException;

    /**
     * Complete a detached (without request) authentication.
     *
     * @return Details about the authentication such as the authenticated user's application identifier or <code>null</code> if the handler
     *         finds no detached authentication assertion in the request.
     */
    @Nullable
    public AuthnProtocolResponseContext findAndValidateAuthnAssertion(HttpServletRequest request,
                                                                      Function<AuthnProtocolResponseContext, AuthenticationContext> responseToContext)
            throws ValidationFailedException;

    /**
     * Initiates the logout request towards the SafeOnline authentication web application.
     * <p/>
     * The landing code is at a URL specified by the linkID configuration for this application. (SSO Logout URL)
     *
     * @param response HTTP Servlet Response.
     * @param userId   User ID of the subject to logout.
     * @param context  Logout context
     *
     * @return logout protocol request context
     *
     * @throws IOException The request could not be written to the response.
     */
    LogoutProtocolRequestContext sendLogoutRequest(HttpServletResponse response, String userId, LogoutContext context)
            throws IOException;

    /**
     * Finalize the logout process.
     *
     * @param request HTTP Servlet Request
     *
     * @return Details about the logout such as whether it was successful or <code>null</code> if there is no logout response in the
     *         request.
     *
     * @throws ValidationFailedException validation of the logout response failed.
     */
    @Nullable
    LogoutProtocolResponseContext findAndValidateLogoutResponse(HttpServletRequest request)
            throws ValidationFailedException;

    /**
     * Handle an incoming logout request, sent from the authentication webapp due to a logout request from another application.
     *
     * @param request          HTTP Servlet Request
     * @param requestToContext logout request context
     *
     * @return Details about the logout request such as the application identifier of the user that requested it or <code>null</code> if
     *         there is no logout request in the request.
     *
     * @throws ValidationFailedException validation of the logout request failed
     */
    @Nullable
    LogoutProtocolRequestContext findAndValidateLogoutRequest(HttpServletRequest request,
                                                              Function<LogoutProtocolRequestContext, LogoutContext> requestToContext)
            throws ValidationFailedException;

    /**
     * Sends back a logout response towards the SafeOnline authentication web application.
     *
     * @param response             HTTP Servlet Response
     * @param logoutRequestContext Logout Request Context
     * @param partialLogout        did logout succeed or not?
     *
     * @return logout response context
     *
     * @throws IOException The request could not be written to the response.
     */
    LogoutProtocolResponseContext sendLogoutResponse(HttpServletResponse response, LogoutProtocolRequestContext logoutRequestContext,
                                                     boolean partialLogout)
            throws IOException;
}
