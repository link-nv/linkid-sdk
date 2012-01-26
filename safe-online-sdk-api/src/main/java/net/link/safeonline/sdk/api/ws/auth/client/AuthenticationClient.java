/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.auth.client;

import java.security.PublicKey;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import net.link.safeonline.sdk.api.attribute.AttributeIdentitySDK;
import net.link.safeonline.sdk.api.exception.*;
import net.link.safeonline.sdk.api.ws.auth.AuthenticationStep;
import net.link.safeonline.sdk.api.ws.auth.Confirmation;
import org.jetbrains.annotations.Nullable;


/**
 * Interface for authentication client. Via components implementing this interface applications can authenticate against linkID. *
 *
 * @author wvdhaute
 */
public interface AuthenticationClient<AT, DI> {

    /**
     * Authenticates for the specified application, using the specified device, given the specified device credentials.
     * <p/>
     * Returns null if authentication is not complete. Check {@link #getAuthenticationStep()} for the list of required additional
     * authentication steps or {@link #getDeviceAuthenticationInformation()} if authentication for the specified device required additional
     * information.
     * <p/>
     * Returns user ID if complete. The SAML v2.0 assertion is returned by {@link #getAssertion()}.
     *
     * @param applicationName   application name
     * @param deviceName        device name
     * @param language          optional language
     * @param deviceCredentials device credentials, can be empty depending on device and which step in the device authentication process.
     * @param publicKey         optional public key
     *
     * @return {@code null} or the user ID of the authenticated subject.
     *
     * @throws RequestDeniedException         in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws DatatypeConfigurationException XML datatype error
     * @throws WSClientTransportException     WS request transport failed
     * @throws WSAuthenticationException      WS authentication failure, look into exception for more detail why
     */
    @Nullable
    String authenticate(String applicationName, String deviceName, String language, Object deviceCredentials, PublicKey publicKey)
            throws RequestDeniedException, DatatypeConfigurationException, WSAuthenticationException, WSClientTransportException;

    /**
     * Returns the global usage agreement to be confirmed or null if not needed.
     * <p/>
     * If null, check {@link #getAuthenticationStep()} for authentication steps to be performed.
     * <p/>
     * If none, check {@link #getAssertion()}.
     *
     * @return the global usage agreement if any or {@code null}.
     *
     * @throws RequestDeniedException     in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws WSClientTransportException WS request transport failed
     * @throws WSAuthenticationException  WS authentication failure, look into exception for more detail why
     */
    @Nullable
    String getGlobalUsageAgreement()
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * Confirms or rejects the global usage agreement.
     * <p/>
     * Returns null if authentication is not complete. Check {@link #getAuthenticationStep()} for the list of required additional
     * authentication steps or {@link #getDeviceAuthenticationInformation()} if authentication for the specified device required additional
     * information.
     * <p/>
     * Returns user ID if complete. The SAML v2.0 assertion is returned by {@link #getAssertion()}.
     *
     * @param confirmation confirmed or not ?
     *
     * @return user ID if complete, {@code null} if not.
     *
     * @throws RequestDeniedException     in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws WSClientTransportException WS request transport failed
     * @throws WSAuthenticationException  WS authentication failure, look into exception for more detail why
     */
    @Nullable
    String confirmGlobalUsageAgreement(Confirmation confirmation)
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * Returns the application usage agreement to be confirmed or null if not needed. Or an empty string if no usage agreement exists but
     * subscription is required.
     * <p/>
     * If null, check {@link #getAuthenticationStep()} for authentication steps to be performed.
     * <p/>
     * If none, check {@link #getAssertion()}.
     *
     * @return usage agreement if any, else {@code null}.
     *
     * @throws RequestDeniedException     in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws WSClientTransportException WS request transport failed
     * @throws WSAuthenticationException  WS authentication failure, look into exception for more detail why
     */
    @Nullable
    String getUsageAgreement()
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * Confirms or rejects the application usage agreement / subscribes to the application.
     * <p/>
     * Returns null if authentication is not complete. Check {@link #getAuthenticationStep()} for the list of required additional
     * authentication steps or {@link #getDeviceAuthenticationInformation()} if authentication for the specified device required additional
     * information.
     * <p/>
     * Returns user ID if complete. The SAML v2.0 assertion is returned by {@link #getAssertion()}.
     *
     * @param confirmation confirmed ?
     *
     * @return user ID if complete, else {@code null}.
     *
     * @throws RequestDeniedException     in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws WSClientTransportException WS request transport failed
     * @throws WSAuthenticationException  WS authentication failure, look into exception for more detail why
     */
    @Nullable
    String confirmUsageAgreement(Confirmation confirmation)
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * Returns the application identity to be confirmed.
     * <p/>
     * If null, check {@link #getAuthenticationStep()} for authentication steps to be performed.
     * <p/>
     * If none, check {@link #getAssertion()}.
     *
     * @return the application identity to confirm or {@code null}.
     *
     * @throws RequestDeniedException     in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws WSClientTransportException WS request transport failed
     * @throws WSAuthenticationException  WS authentication failure, look into exception for more detail why
     */
    @Nullable
    List<AttributeIdentitySDK> getIdentity()
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * Confirms the application's identity.
     * <p/>
     * Returns null if authentication is not complete. Check {@link #getAuthenticationStep()} for the list of required additional
     * authentication steps or {@link #getDeviceAuthenticationInformation()} if authentication for the specified device required additional
     * information.
     * <p/>
     * Returns user ID if complete. The SAML v2.0 assertion is returned by {@link #getAssertion()}.
     *
     * @param attributes application identity attributes
     *
     * @return user ID if complete, {@code null} if not.
     *
     * @throws RequestDeniedException     in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws WSClientTransportException WS request transport failed
     * @throws WSAuthenticationException  WS authentication failure, look into exception for more detail why
     */
    @Nullable
    String confirmIdentity(List<AttributeIdentitySDK> attributes)
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * @return the SAML v2.0 assertion or null if authentication is not yet complete.
     */
    AT getAssertion();

    /**
     * @return the device authentication information returned to complete the authentication.
     */
    DI getDeviceAuthenticationInformation();

    /**
     * @return next authentication step.
     */
    AuthenticationStep getAuthenticationStep();
}
