/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.auth;

import java.util.Locale;


/**
 * <h2>{@link RequestConstants}</h2>
 *
 * <p>
 * [description / usage].
 * </p>
 *
 * <p>
 * <i>Sep 18, 2009</i>
 * </p>
 *
 * @author lhunath
 */
public interface RequestConstants {

    /**
     * @see Locale#Locale(String)
     */
    public static final String LANGUAGE_REQUEST_PARAM = "Language";
    public static final String THEME_REQUEST_PARAM = "ThemeName";
    public static final String APPLICATION_ID_REQUEST_PARAM = "ApplicationId";
    public static final String CANCELLED_REQUEST_PARAM = "cancelled";
    public static final String TIMEOUT_REQUEST_PARAM = "timeout";
}
