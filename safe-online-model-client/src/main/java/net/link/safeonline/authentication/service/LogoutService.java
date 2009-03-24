/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.LogoutProtocolContext;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AuthenticationInitializationException;
import net.link.safeonline.authentication.exception.InvalidCookieException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;

import org.opensaml.saml2.core.LogoutRequest;


/**
 * Logout service interface. This service allows the authentication web application to logout users. The bean behind this interface is
 * stateful. This means that a certain method invocation pattern must be respected. First the method {@link #initialize(LogoutRequest)} must
 * be invoked. Then for each application being logged out, the methods {@link #getLogoutRequest(ApplicationEntity)} followed by
 * {@link #handleLogoutResponse(HttpServletRequest)} must be invoked. Finally {@link #finalizeLogout(boolean)} has to be invoked. In case
 * the logout process needs to be aborted one should invoke {@link #abort()} .
 * 
 * @author wvdhaute
 */
@Local
public interface LogoutService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "LogoutServiceBean/local";


    /**
     * Returns the current state of the bean.
     */
    LogoutState getLogoutState();

    /**
     * Aborts the current logout procedure.
     */
    void abort();

    /**
     * Returns whether the specified cookie is ok for logout. Meaning all applications specified in the cookie have to be logged out.
     * 
     * @throws ApplicationNotFoundException
     * @throws InvalidCookieException
     */
    boolean checkSsoCookieForLogout(Cookie ssoCookie)
            throws ApplicationNotFoundException, InvalidCookieException;

    /**
     * Initializes a logout process. Validates the incoming logout request and stores the application.
     * 
     * @param samlLogoutRequest
     * @throws TrustDomainNotFoundException
     * @throws ApplicationNotFoundException
     * @throws AuthenticationInitializationException
     * @throws SubjectNotFoundException
     */
    LogoutProtocolContext initialize(LogoutRequest samlLogoutRequest)
            throws AuthenticationInitializationException, ApplicationNotFoundException, TrustDomainNotFoundException,
            SubjectNotFoundException;

    /**
     * Returns the next Application to logout. Returns <code>null</code> if none.
     */
    ApplicationEntity findSsoApplicationToLogout();

    /**
     * Initiate a logout process for the specified application by constructing an encoded SAML logout request to be sent to the application.
     * 
     * 
     * Calling this method is only valid after a call to {@link #initialize(LogoutRequest)}.
     * 
     * @throws ApplicationNotFoundException
     * @throws SubscriptionNotFoundException
     * @throws NodeNotFoundException
     * 
     */
    String getLogoutRequest(ApplicationEntity application)
            throws SubscriptionNotFoundException, ApplicationNotFoundException, NodeNotFoundException;

    /**
     * Validates the returned SAML logout response message. Returns the application name if successful or <code>null</code> if the response
     * did not have status successful.
     * 
     * Calling this method is only valid after a call to {@link #getLogoutRequest(ApplicationEntity)}.
     * 
     * @throws ServletException
     * @throws NodeNotFoundException
     */
    String handleLogoutResponse(HttpServletRequest httpRequest)
            throws ServletException, NodeNotFoundException;

    /**
     * Finalizes a logout process by constructing an encoded SAML logout response to be sent to the application.
     * 
     * Calling this method is only valid after a call to {@link #initialize(LogoutRequest)}.
     * 
     * @param partialLogout
     * 
     * @throws NodeNotFoundException
     */
    String finalizeLogout(boolean partialLogout)
            throws NodeNotFoundException;
}
