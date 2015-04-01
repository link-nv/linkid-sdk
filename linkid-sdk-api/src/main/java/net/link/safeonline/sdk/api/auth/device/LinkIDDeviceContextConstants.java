/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.auth.device;

/**
 * Some general device context parameters supported by certain linkID devices.
 */
public interface LinkIDDeviceContextConstants {

    String AUTHENTICATION_MESSAGE = "linkID.authenticationMessage";
    String FINISHED_MESSAGE       = "linkID.finishedMessage";

    // optional identity profile(s)
    String IDENTITY_PROFILE_PREFIX = "linkID.identityProfile";

    // optional session expiry (seconds)
    String SESSION_EXPIRY_OVERRIDE = "linkID.sessionExpiryOverride";
}
