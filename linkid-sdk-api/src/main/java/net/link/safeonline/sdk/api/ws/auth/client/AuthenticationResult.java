/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.auth.client;

import java.io.Serializable;
import java.util.Map;
import net.link.safeonline.sdk.api.ws.auth.AuthenticationStep;
import org.jetbrains.annotations.Nullable;


/**
 * WS Authentication result, contains ( if applicable ) the userId, assertion, device information, authentication step needed.
 */
public class AuthenticationResult<AT extends Serializable> implements Serializable {

    private final String userId;

    private final AT assertion;

    private final Map<String, String> deviceInformation;

    private final AuthenticationStep authenticationStep;

    public AuthenticationResult(@Nullable final String userId, @Nullable final AT assertion, final Map<String, String> deviceInformation,
                                @Nullable final AuthenticationStep authenticationStep) {

        this.userId = userId;
        this.assertion = assertion;
        this.deviceInformation = deviceInformation;
        this.authenticationStep = authenticationStep;
    }

    @Nullable
    public String getUserId() {

        return userId;
    }

    @Nullable
    public AT getAssertion() {

        return assertion;
    }

    public Map<String, String> getDeviceInformation() {

        return deviceInformation;
    }

    @Nullable
    public AuthenticationStep getAuthenticationStep() {

        return authenticationStep;
    }
}
