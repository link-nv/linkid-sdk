/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.auth;

import java.security.PublicKey;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;

import net.lin_k.safe_online.auth.DeviceAuthenticationInformationType;
import net.link.safeonline.auth.ws.AuthenticationStep;
import net.link.safeonline.auth.ws.Confirmation;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.MessageAccessor;
import net.link.safeonline.sdk.ws.exception.WSAuthenticationException;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import oasis.names.tc.saml._2_0.assertion.AssertionType;


/**
 * Interface for authentication client. Via components implementing this interface applications can authenticate against OLAS. *
 * 
 * @author wvdhaute
 * 
 */
public interface AuthenticationClient extends MessageAccessor {

    /**
     * Authenticates for the specified application, using the specified device, given the specified device credentials.
     * 
     * Returns null if authentication is not complete. Check {@link #getAuthenticationStep()} for the list of required additional
     * authentication steps or {@link #getDeviceAuthenticationInformation()} if authentication for the specified device required additional
     * information.
     * 
     * Returns user ID if complete. The SAML v2.0 assertion is returned by {@link #getAssertion()}.
     * 
     * 
     * @param applicationId
     * @param deviceName
     * @param deviceCredentials
     * @param publicKey
     * 
     * @throws RequestDeniedException
     *             in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws DatatypeConfigurationException
     * @throws WSClientTransportException
     * @throws WSClientTransportException
     */
    String authenticate(String applicationId, String deviceName, String language, Object deviceCredentials, PublicKey publicKey)
            throws RequestDeniedException, DatatypeConfigurationException, WSAuthenticationException, WSClientTransportException;

    /**
     * Returns the global usage agreement to be confirmed or null if not needed.
     * 
     * If null, check {@link #getAuthenticationStep()} for authentication steps to be performed.
     * 
     * If none, check {@link #getAssertion()}.
     */
    String getGlobalUsageAgreement()
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * Confirms or rejects the global usage agreement.
     * 
     * Returns null if authentication is not complete. Check {@link #getAuthenticationStep()} for the list of required additional
     * authentication steps or {@link #getDeviceAuthenticationInformation()} if authentication for the specified device required additional
     * information.
     * 
     * Returns user ID if complete. The SAML v2.0 assertion is returned by {@link #getAssertion()}.
     * 
     * @throws WSClientTransportException
     */
    String confirmGlobalUsageAgreement(Confirmation confirmation)
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * Returns the application usage agreement to be confirmed or null if not needed. Or an empty string if no usage agreement exists but
     * subscription is required.
     * 
     * If null, check {@link #getAuthenticationStep()} for authentication steps to be performed.
     * 
     * If none, check {@link #getAssertion()}.
     * 
     * @throws WSClientTransportException
     */
    String getUsageAgreement()
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * Confirms or rejects the application usage agreement / subscribes to the application.
     * 
     * Returns null if authentication is not complete. Check {@link #getAuthenticationStep()} for the list of required additional
     * authentication steps or {@link #getDeviceAuthenticationInformation()} if authentication for the specified device required additional
     * information.
     * 
     * Returns user ID if complete. The SAML v2.0 assertion is returned by {@link #getAssertion()}.
     * 
     * @throws WSClientTransportException
     */
    String confirmUsageAgreement(Confirmation confirmation)
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * Returns the application identity to be confirmed.
     * 
     * If null, check {@link #getAuthenticationStep()} for authentication steps to be performed.
     * 
     * If none, check {@link #getAssertion()}.
     * 
     * @throws WSClientTransportException
     */
    List<Attribute> getIdentity()
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * Confirms or rejects the application's identity.
     * 
     * * Returns null if authentication is not complete. Check {@link #getAuthenticationStep()} for the list of required additional
     * authentication steps or {@link #getDeviceAuthenticationInformation()} if authentication for the specified device required additional
     * information.
     * 
     * Returns user ID if complete. The SAML v2.0 assertion is returned by {@link #getAssertion()}.
     * 
     * @throws WSClientTransportException
     */
    String confirmIdentity(Confirmation confirmation)
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * Returns the application's misssing attributes.
     * 
     * If null, check {@link #getAuthenticationStep()} for authentication steps to be performed.
     * 
     * If none, check {@link #getAssertion()}.
     * 
     * @throws WSAuthenticationException
     * @throws RequestDeniedException
     * @throws WSClientTransportException
     */
    List<Attribute> getMissingAttributes()
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * Saves the missing attributes to be provided. * Returns null if authentication is not complete. Check {@link #getAuthenticationStep()}
     * for the list of required additional authentication steps or {@link #getDeviceAuthenticationInformation()} if authentication for the
     * specified device required additional information.
     * 
     * Returns user ID if complete. The SAML v2.0 assertion is returned by {@link #getAssertion()}.
     * 
     * @throws WSAuthenticationException
     * @throws RequestDeniedException
     * @throws WSClientTransportException
     */
    String saveMissingAttributes(List<Attribute> missingAttributes)
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * Returns the SAML v2.0 assertion or null if authentication is not yet complete.
     */
    AssertionType getAssertion();

    /**
     * Returns the device authentication information returned to complete the authentication.
     */
    DeviceAuthenticationInformationType getDeviceAuthenticationInformation();

    /**
     * Returns next authentication step.
     */
    AuthenticationStep getAuthenticationStep();
}
