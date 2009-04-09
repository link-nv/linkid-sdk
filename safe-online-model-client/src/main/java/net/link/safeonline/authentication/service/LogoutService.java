/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.List;

import javax.ejb.Local;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.LogoutProtocolContext;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.LogoutInitializationException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SignatureValidationException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;

import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;


/**
 * Logout service interface. This service allows the authentication web application to logout users. The bean behind this interface is
 * stateful. This means that a certain method invocation pattern must be respected. First the method {@link #initialize(LogoutRequest)} must
 * be invoked. Then for each application being logged out, the methods {@link #getLogoutRequest(ApplicationEntity)} followed by
 * {@link #handleLogoutResponse(LogoutResponse)} must be invoked. Finally {@link #finalizeLogout()} has to be invoked. In case the logout
 * process needs to be aborted one should invoke {@link #abort()} .
 * 
 * @author wvdhaute
 */
@Local
public interface LogoutService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "LogoutServiceBean/local";


    /**
     * @return The {@link LogoutState} of the given application that is part of the current SSO application logout process.
     */
    LogoutState getSSoApplicationState(ApplicationEntity application);

    /**
     * Aborts the current logout procedure.
     */
    void abort();

    /**
     * Initializes a logout process. Validates the incoming logout request and stores the application.
     * 
     * @param samlLogoutRequest
     * @throws TrustDomainNotFoundException
     * @throws ApplicationNotFoundException
     * @throws SubjectNotFoundException
     * @throws SignatureValidationException
     * @throws LogoutInitializationException
     */
    LogoutProtocolContext initialize(LogoutRequest samlLogoutRequest)
            throws ApplicationNotFoundException, TrustDomainNotFoundException, SubjectNotFoundException, SignatureValidationException,
            LogoutInitializationException;

    /**
     * @return A list of applications to logout. Returns an empty list if there are none.
     */
    List<ApplicationEntity> getSsoApplicationsToLogout();

    /**
     * @return The application from the SSO logout application list that has the given application ID.
     */
    ApplicationEntity getSsoApplicationToLogout(long applicationId)
            throws ApplicationNotFoundException;

    /**
     * @return An application to send the logout request to when we're doing sequential logout instead of parallel. <code>null</code> means
     *         all applications have been processed.
     */
    ApplicationEntity findSsoApplicationToLogout();

    /**
     * Checks the given list of SSO cookies and extract all applications that need to be logged out.
     * 
     * @param ssoCookies
     */
    void logout(List<Cookie> ssoCookies);

    /**
     * Returns list of invalid sso cookie ( expired, ... )
     */
    List<Cookie> getInvalidCookies();

    /**
     * Initiate a logout process for the specified application by constructing an encoded SAML logout request to be sent to the application.
     * 
     * 
     * Calling this method is only valid after a call to {@link #initialize(LogoutRequest)}.
     * 
     * @throws ApplicationNotFoundException
     * @throws NodeNotFoundException
     * 
     */
    String getLogoutRequest(ApplicationEntity application)
            throws ApplicationNotFoundException, NodeNotFoundException;

    /**
     * Validates the returned SAML logout response message. Returns the application name if successful or <code>null</code> if the response
     * did not have status successful.
     * 
     * Calling this method is only valid after a call to {@link #getLogoutRequest(ApplicationEntity)}.
     * 
     * @param logoutResponse
     * @throws ServletException
     * @throws NodeNotFoundException
     * @throws ApplicationNotFoundException
     * @throws TrustDomainNotFoundException
     * @throws SignatureValidationException
     */
    String handleLogoutResponse(LogoutResponse logoutResponse)
            throws ServletException, NodeNotFoundException, ApplicationNotFoundException, TrustDomainNotFoundException,
            SignatureValidationException;

    /**
     * Finalizes a logout process by constructing an encoded SAML logout response to be sent to the application.
     * 
     * Calling this method is only valid after a call to {@link #initialize(LogoutRequest)}.
     * 
     * @throws NodeNotFoundException
     */
    String finalizeLogout()
            throws NodeNotFoundException;

    /**
     * @return <code>true</code>: Not all applications have successfully completed their logout process.
     */
    public boolean isPartial();

    /**
     * @param sequential
     *            <code>true</code>: The logout sequence has been initiated as a sequential (not parallel!) operation.
     */
    void setSequential(boolean sequential);

    /**
     * @return <code>true</code>: The logout sequence is being performed as a sequential (not parallel!) operation.
     */
    boolean isSequential();
}
