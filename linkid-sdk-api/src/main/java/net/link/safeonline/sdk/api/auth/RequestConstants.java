/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.auth;

import java.util.Locale;


/**
 * <h2>{@link RequestConstants}</h2>
 * <p/>
 * <p>
 * [description / usage].
 * </p>
 * <p/>
 * <p>
 * <i>Sep 18, 2009</i>
 * </p>
 *
 * @author lhunath
 */
public interface RequestConstants {

    /**
     * Trigger the QR Mobile authentication flow
     */
    String MOBILE_AUTHN_REQUEST_PARAM = "mobileAuthn";

    /**
     * Trigger the QR Mobile 'minimal' authentication flow
     */
    String MOBILE_AUTHN_MINIMAL_REQUEST_PARAM = "mobileAuthnMinimal";

    /**
     * Trigger the QR Mobile force registration flow
     */
    String MOBILE_FORCE_REG_REQUEST_PARAM = "mobileForceReg";

    /**
     * Case we want to force the auth webapp to go straight to register or authentication page.
     */
    String START_PAGE_REQUEST_PARAM = "start_page";

    /**
     * Which protocol to use, SAML2, HAWS, OPENID, OAUTH
     */
    String PROTOCOL_PARAM = "protocol";

    /**
     * @see Locale#Locale(String)
     */
    String LANGUAGE_REQUEST_PARAM       = "Language";
    String THEME_REQUEST_PARAM          = "ThemeName";
    String LOGINMODE_REQUEST_PARAM      = "login_mode";
    String TARGETURI_REQUEST_PARAM      = "return_uri";
    String APPLICATION_ID_REQUEST_PARAM = "ApplicationId";
    String CANCELLED_REQUEST_PARAM      = "cancelled";
    String TIMEOUT_REQUEST_PARAM        = "timeout";
    String OAUTH2_FORCE_AUTHN           = "approval_prompt";

    String HAWS_SESSION_ID_PARAM = "hawsId";
}
