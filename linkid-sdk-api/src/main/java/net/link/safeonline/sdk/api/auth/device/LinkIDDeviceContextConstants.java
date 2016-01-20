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

    String AUTHENTICATION_MESSAGE     = "linkID.authenticationMessage";
    String FINISHED_MESSAGE           = "linkID.finishedMessage";
    //
    // optional identity profile
    String IDENTITY_PROFILE           = "linkID.identityProfile";
    //
    // optional session expiry (seconds)
    String SESSION_EXPIRY_OVERRIDE    = "linkID.sessionExpiryOverride";
    //
    // optional theme, if not specified default application theme will be chosen
    String THEME                      = "linkID.theme";
    //
    // optional mobile landing pages for when started from mobile iOS browser
    String MOBILE_LANDING_SUCCESS_URL = "linkID.mobileLandingSuccess";
    String MOBILE_LANDING_ERROR_URL   = "linkID.mobileLandingError";
    String MOBILE_LANDING_CANCEL_URL  = "linkID.mobileLandingCancel";
    //
    // optional notification location override
    String NOTIFICATION_LOCATION      = "linkID.notificationLocation";
}
