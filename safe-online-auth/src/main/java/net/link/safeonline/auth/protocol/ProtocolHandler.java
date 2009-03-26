/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.protocol;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.LogoutProtocolContext;
import net.link.safeonline.authentication.ProtocolContext;
import net.link.safeonline.authentication.service.AuthenticationAssertion;
import net.link.safeonline.entity.ApplicationEntity;


/**
 * Interface for server-side authentication protocol handlers.
 * 
 * <p>
 * Protocol handlers should be implemented as stateless POJOs. The server-side protocol handlers are stateless. That way they can never be
 * subject to input validation attacks in case they would pass state from input message to output message without checking for its
 * correctness.
 * </p>
 * 
 * @author fcorneli
 * 
 */
public interface ProtocolHandler {

    /**
     * Authentication Request handle method. The protocol handler should return a filled in protocol context if it could handle the
     * authentication request. If the handler cannot handle the authentication request then it should return <code>null</code>. A
     * {@link ProtocolException} should be thrown in case this handler can handle the authentication request but the request itself violates
     * the authentication protocol supported by this handler.
     * 
     * @param request
     * @return the protocol context or <code>null</code>.
     * @throws ProtocolException
     *             in case the authentication request violates the authentication protocol supported by this handler.
     */
    ProtocolContext handleAuthnRequest(HttpServletRequest request, Locale language, Integer color, Boolean minimal)
            throws ProtocolException;

    /**
     * Device authentication response handle method. The protocol handler should return the authentication assertion if successful or
     * <code>null</code> if the authentication was canceled. A {@link ProtocolException} should be thrown in case this handler can handle
     * the authentication request but the request itself violates the authentication protocol supported by this handler.
     * 
     * @param request
     * @return the authentication user's ID or <code>null</code>.
     * @throws ProtocolException
     *             in case the authentication request violates the authentication protocol supported by this handler.
     */
    AuthenticationAssertion handleDeviceAuthnResponse(HttpServletRequest request)
            throws ProtocolException;

    /**
     * Device registration response handle method. The protocol handler should return authentication assertion (
     * {@link AuthenticationAssertion} or <code>null</code> if the authentication was canceled. A {@link ProtocolException} should be thrown
     * in case this handler can handle the authentication request but the request itself violates the authentication protocol supported by
     * this handler.
     * 
     * @param request
     * @return the authentication assertion or <code>null</code>.
     * @throws ProtocolException
     *             in case the authentication request violates the authentication protocol supported by this handler.
     */
    AuthenticationAssertion handleDeviceRegistrationResponse(HttpServletRequest request)
            throws ProtocolException;

    /**
     * Performs the authentication response according to the protocol supported by the handler that implements this interface.
     * 
     * @param session
     * @param response
     * @throws ProtocolException
     */
    void sendAuthnResponse(HttpSession session, HttpServletResponse response)
            throws ProtocolException;

    /**
     * Logout Request handle method. The protocol handler should return a filled in logout protocol context if it could handle the logout
     * request. If the handler cannot handle the logout request then it should return <code>null</code>. A {@link ProtocolException} should
     * be thrown in case this handler can handle the logout request but the request itself violates the protocol supported by this handler.
     * 
     * @param request
     * @return the protocol context or <code>null</code>.
     * @throws ProtocolException
     *             in case the logout request violates the logout protocol supported by this handler.
     */
    LogoutProtocolContext handleLogoutRequest(HttpServletRequest request)
            throws ProtocolException;

    /**
     * Logout Response handle method. The protocol handler should return the application name if it could handle the logout response. A
     * {@link ProtocolException} should be thrown in case a violation against the protocol was detected. Returns null if the logout response
     * did not have status SUCCESS.
     */
    String handleLogoutResponse(HttpServletRequest response)
            throws ProtocolException;

    /**
     * Sends a logout request to the specified application
     * 
     * @param application
     * @param session
     * @param response
     * @throws ProtocolException
     */
    void sendLogoutRequest(ApplicationEntity application, HttpSession session, HttpServletResponse response)
            throws ProtocolException;

    /**
     * Performs the logout response according to the protocol supported by the handler that implements this interface.
     * 
     * @param partialLogout
     * @param target
     * @param session
     * @param response
     * @throws ProtocolException
     */
    void sendLogoutResponse(boolean partialLogout, String target, HttpSession session, HttpServletResponse response)
            throws ProtocolException;

    /**
     * Gives back the informal human-readable name of the authentication protocol that this protocol handler supports. This name can be used
     * on error pages.
     * 
     */
    String getName();
}
