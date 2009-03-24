/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.Locale;

import javax.ejb.Local;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.ProtocolContext;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.AuthenticationInitializationException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DevicePolicyException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.exception.IdentityConfirmationRequiredException;
import net.link.safeonline.authentication.exception.InvalidCookieException;
import net.link.safeonline.authentication.exception.MissingAttributeException;
import net.link.safeonline.authentication.exception.NodeMappingNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SignatureValidationException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.exception.UsageAgreementAcceptationRequiredException;
import net.link.safeonline.device.sdk.saml2.response.DeviceOperationResponse;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;

import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;


/**
 * Authentication service interface. This service allows the authentication web application to authenticate users. The bean behind this
 * interface is stateful. This means that a certain method invocation pattern must be respected. First the method
 * {@link #initialize(Locale, Integer, Boolean, AuthnRequest)} must be invoked. Then the method {@link #authenticate(Response)} must be
 * invoked. After this the method {@link #commitAuthentication(String)} must be invoked and finally {@link #finalizeAuthentication()}. In
 * case the authentication process needs to be aborted one should invoke {@link #abort()} .
 * 
 * @author fcorneli
 */
@Local
public interface AuthenticationService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "AuthenticationServiceBean/local";


    /**
     * Authenticates a user for a certain application. This method is used by the authentication web service. If <code>true</code> is
     * returned the authentication process can proceed, else {@link #abort()} should be invoked.
     * 
     * @param applicationName
     * @param loginName
     * @param password
     * @return <code>true</code> if the user was authenticated correctly, <code>false</code> otherwise.
     * @throws SubjectNotFoundException
     * @throws DeviceNotFoundException
     *             in case the user did not configure the password device.
     * @throws DeviceDisabledException
     */
    boolean authenticate(String loginName, String password)
            throws SubjectNotFoundException, DeviceNotFoundException, DeviceDisabledException;

    /**
     * Commits the authentication.
     * 
     * Calling this method is only valid after a call to {@link #authenticate(Response)}.
     * 
     * @param language
     * 
     * @throws SubscriptionNotFoundException
     *             in case the subject is not subscribed to the application.
     * @throws ApplicationNotFoundException
     *             in case the application does not exist.
     * @throws ApplicationIdentityNotFoundException
     * @throws IdentityConfirmationRequiredException
     * @throws MissingAttributeException
     * @throws EmptyDevicePolicyException
     * @throws DevicePolicyException
     * @throws UsageAgreementAcceptationRequiredException
     * @throws AttributeTypeNotFoundException
     * @throws PermissionDeniedException
     * @throws AttributeUnavailableException
     */
    void commitAuthentication(String language)
            throws ApplicationNotFoundException, SubscriptionNotFoundException, ApplicationIdentityNotFoundException,
            IdentityConfirmationRequiredException, MissingAttributeException, EmptyDevicePolicyException, DevicePolicyException,
            UsageAgreementAcceptationRequiredException, PermissionDeniedException, AttributeTypeNotFoundException,
            AttributeUnavailableException;

    /**
     * Sets the password of a user. This method should be used in case the user did not yet had a password registered as authentication
     * device.
     * 
     * @param userId
     * @param password
     * @throws DeviceNotFoundException
     */
    void setPassword(String userId, String password)
            throws SubjectNotFoundException, DeviceNotFoundException;

    /**
     * Aborts the current authentication procedure.
     */
    void abort();

    /**
     * Gives back the user Id of the user that we're trying to authenticate. Calling this method in only valid after a call to
     * {@link #authenticate(String, String)}.
     * 
     */
    String getUserId();

    /**
     * Gives back the username of the user that we're trying to authenticate. Calling this method is only valid after a call to
     * {@link #authenticate(String, String)}.
     * 
     */
    String getUsername();

    /**
     * Authenticates a user for a certain application. The method is used by the device landing servlet. The actual device authentication is
     * done by an external device provider in this case. We validate the return SAML response message.
     * 
     * Calling this method is only valid after a call to {@link #redirectAuthentication(String, String, String)}.
     * 
     * Returns the user ID of the authenticated user.
     * 
     * @throws {@linkSignatureValidationException}
     * @throws {@linkTrustDomainNotFoundException}
     * @throws {@linkApplicationNotFoundException}
     * @throws {@link DeviceNotFoundException}
     * @throws {@link NodeMappingNotFoundException}
     * @throws {@link ServletException}
     * @throws {@link NodeNotFoundException}
     * @throws {@link SubjectNotFoundException}
     */
    String authenticate(Response response)
            throws NodeNotFoundException, ServletException, NodeMappingNotFoundException, DeviceNotFoundException,
            SubjectNotFoundException, ApplicationNotFoundException, TrustDomainNotFoundException, SignatureValidationException;

    /**
     * Initializes an authentication process. Validates the incoming authentication request and stores the application, device policy and
     * assertion consumer service.
     * 
     * @param language
     * @param minimal
     * @param color
     * @param samlAuthnRequest
     * @throws AuthenticationInitializationException
     * @throws ApplicationNotFoundException
     * @throws TrustDomainNotFoundException
     * @throws SignatureValidationException
     */
    ProtocolContext initialize(Locale language, Integer color, Boolean minimal, AuthnRequest samlAuthnRequest)
            throws AuthenticationInitializationException, ApplicationNotFoundException, TrustDomainNotFoundException,
            SignatureValidationException;

    /**
     * Constructs a signed and encoded SAML authentication request for the requested external device issuer.
     * 
     * Calling this method is only valid after a call to {@link #initialize(Locale, Integer, Boolean, AuthnRequest)}.
     * 
     * @param authenticationServiceUrl
     * @param encodedLandingUrl
     * @param device
     * @throws NodeNotFoundException
     */
    String redirectAuthentication(String authenticationServiceUrl, String targetUrl, String device)
            throws NodeNotFoundException;

    /**
     * Finalizes an authentication process by constructing an encoded SAML response to be sent to the application.
     * 
     * Calling this method is only valid after a call to {@link #commitAuthentication(String)}.
     * 
     * @throws NodeNotFoundException
     * @throws ApplicationNotFoundException
     * @throws SubscriptionNotFoundException
     * 
     */
    String finalizeAuthentication()
            throws NodeNotFoundException, SubscriptionNotFoundException, ApplicationNotFoundException;

    /**
     * Gives back the current authentication state.
     * 
     */
    AuthenticationState getAuthenticationState();

    /**
     * Gives back the used authentication device.
     * 
     */
    DeviceEntity getAuthenticationDevice();

    /**
     * Gives back the Single Sign-On Cookie.
     * 
     */
    Cookie getSsoCookie();

    /**
     * Returns whether the specified cookie allows the user to use single sign-on for the current application.
     * 
     * @throws ApplicationNotFoundException
     * @throws InvalidCookieException
     * @throws DevicePolicyException
     * @throws EmptyDevicePolicyException
     */
    boolean checkSsoCookie(Cookie ssoCookie)
            throws ApplicationNotFoundException, InvalidCookieException, EmptyDevicePolicyException, DevicePolicyException;

    /**
     * Constructs a signed and encoded SAML authentication request for the requested external device issuer.
     * 
     * Calling this method is only valid after a call to {@link #initialize(Locale, Integer, Boolean, AuthnRequest)}.
     * 
     * @param registrationServiceUrl
     * @param targetUrl
     * @param device
     * @param userId
     *            OLAS user ID
     * @throws NodeNotFoundException
     * @throws DeviceNotFoundException
     * @throws SubjectNotFoundException
     */
    String redirectRegistration(String registrationServiceUrl, String targetUrl, String device, String userId)
            throws NodeNotFoundException, SubjectNotFoundException, DeviceNotFoundException;

    /**
     * Finalizes a remote device registration for a user. The method is used by the device registration landing servlet. The device
     * registration was done by an external device provider in this case. We validate the return SAML response message.
     * 
     * Calling this method is only valid after a call to {@link #redirectRegistration(String, String, String, String)}.
     * 
     * @throws NodeMappingNotFoundException
     * @throws ServletException
     * @throws NodeNotFoundException
     * @throws DeviceNotFoundException
     * @throws SubjectNotFoundException
     * @throws SignatureValidationException
     * @throws TrustDomainNotFoundException
     * @throws ApplicationNotFoundException
     * 
     */
    String register(DeviceOperationResponse response)
            throws NodeNotFoundException, ServletException, NodeMappingNotFoundException, DeviceNotFoundException,
            SubjectNotFoundException, ApplicationNotFoundException, TrustDomainNotFoundException, SignatureValidationException;
}
