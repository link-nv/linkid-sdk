/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.auth.client;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import net.link.safeonline.sdk.api.attribute.AttributeIdentitySDK;
import net.link.safeonline.sdk.api.exception.*;
import net.link.safeonline.sdk.api.ws.auth.Confirmation;
import org.jetbrains.annotations.Nullable;


/**
 * Interface for authentication client. Via components implementing this interface applications can authenticate against linkID. *
 *
 * @author wvdhaute
 */
public interface AuthenticationClient<AT extends Serializable> extends Serializable {

    /**
     * Authenticates for the specified application, using the specified device, given the specified device credentials.
     * <p/>
     * The {@link AuthenticationResult} will contain an assertion and userId if it was complete, else the authentication step in it should
     * be non-null and containing the required next step.
     * <p/>
     *
     * @param applicationName   application name
     * @param deviceName        device name
     * @param language          optional language
     * @param deviceCredentials device credentials, can be empty depending on device and which step in the device authentication process.
     * @param publicKey         optional public key
     *
     * @return authentication result
     *
     * @throws RequestDeniedException     application not allowed to use WS Authentication...
     * @throws WSClientTransportException WS request transport failed, wrong location?
     * @throws WSAuthenticationException  WS authentication failure, look into exception for more detail why
     */
    AuthenticationResult<AT> authenticate(String applicationName, String deviceName, String language,
                                          @Nullable Map<String, String> deviceCredentials, @Nullable PublicKey publicKey)
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * Returns the global usage agreement to be confirmed or null if there is none.
     *
     * @return the global usage agreement if any or {@code null}.
     *
     * @throws RequestDeniedException     application not allowed to use WS Authentication...
     * @throws WSClientTransportException WS request transport failed, wrong location?
     * @throws WSAuthenticationException  WS authentication failure, look into exception for more detail why
     */
    @Nullable
    String getGlobalUsageAgreement()
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * Confirms or rejects the global usage agreement.
     * <p/>
     * The {@link AuthenticationResult} will contain an assertion and userId if it was complete, else the authentication step in it should
     * be non-null and containing the required next step.
     *
     * @param confirmation confirmed or not ?
     *
     * @return authentication result
     *
     * @throws RequestDeniedException     application not allowed to use WS Authentication...
     * @throws WSClientTransportException WS request transport failed, wrong location?
     * @throws WSAuthenticationException  WS authentication failure, look into exception for more detail why
     */
    AuthenticationResult<AT> confirmGlobalUsageAgreement(Confirmation confirmation)
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * Returns the application usage agreement to be confirmed or null if there is none.
     *
     * @return usage agreement if any, else {@code null}.
     *
     * @throws RequestDeniedException     application not allowed to use WS Authentication...
     * @throws WSClientTransportException WS request transport failed, wrong location?
     * @throws WSAuthenticationException  WS authentication failure, look into exception for more detail why
     */
    @Nullable
    String getUsageAgreement()
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * Confirms or rejects the application usage agreement / subscribes to the application.
     * <p/>
     * The {@link AuthenticationResult} will contain an assertion and userId if it was complete, else the authentication step in it should
     * be non-null and containing the required next step.
     *
     * @param confirmation confirmed ?
     *
     * @return authentication result
     *
     * @throws RequestDeniedException     application not allowed to use WS Authentication...
     * @throws WSClientTransportException WS request transport failed, wrong location?
     * @throws WSAuthenticationException  WS authentication failure, look into exception for more detail why
     */
    AuthenticationResult<AT> confirmUsageAgreement(Confirmation confirmation)
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * Returns the application identity to be confirmed or null if not applicable
     * <p/>
     *
     * @return the application identity to confirm or {@code null}.
     *
     * @throws RequestDeniedException     application not allowed to use WS Authentication...
     * @throws WSClientTransportException WS request transport failed, wrong location?
     * @throws WSAuthenticationException  WS authentication failure, look into exception for more detail why
     */
    @Nullable
    List<AttributeIdentitySDK> getIdentity()
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;

    /**
     * Confirms the application's identity.
     * <p/>
     * The {@link AuthenticationResult} will contain an assertion and userId if it was complete, else the authentication step in it should
     * be non-null and containing the required next step.
     *
     * @param attributes application identity attributes
     *
     * @return authentication result
     *
     * @throws RequestDeniedException     application not allowed to use WS Authentication...
     * @throws WSClientTransportException WS request transport failed, wrong location?
     * @throws WSAuthenticationException  WS authentication failure, look into exception for more detail why
     */
    AuthenticationResult<AT> confirmIdentity(List<AttributeIdentitySDK> attributes)
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException;
}
