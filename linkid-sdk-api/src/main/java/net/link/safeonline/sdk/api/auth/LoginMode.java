/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.auth;

import java.util.EnumSet;
import org.jetbrains.annotations.Nullable;


public enum LoginMode {
    REDIRECT, POPUP, POPUP_NO_CLOSE;

    @Nullable
    public static LoginMode fromString(String text) {

        for (LoginMode type : EnumSet.allOf( LoginMode.class )) {
            if (type.toString().equalsIgnoreCase( text ))
                return type;
        }
        return null;
    }

    public static LoginMode fromString(String text, LoginMode fallback) {

        for (LoginMode type : EnumSet.allOf( LoginMode.class )) {
            if (type.toString().equalsIgnoreCase( text ))
                return type;
        }
        return fallback;
    }
}
