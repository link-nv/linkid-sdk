/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.auth;

import java.util.Locale;


/**
 * <h2>{@link LinkIDRequestConstants}</h2>
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
public interface LinkIDRequestConstants {

    /**
     * Which protocol to use, SAML2, HAWS
     */
    String PROTOCOL_PARAM = "protocol";

    /**
     * @see Locale#Locale(String)
     */
    String LANGUAGE_REQUEST_PARAM  = "Language";
    String TARGETURI_REQUEST_PARAM = "return_uri";
}
