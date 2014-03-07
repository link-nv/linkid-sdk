/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.auth;

import java.util.EnumSet;
import org.jetbrains.annotations.Nullable;


public enum StartPage {
    NONE, REGISTER, AUTHENTICATE;

    @Nullable
    public static StartPage fromString(String text) {

        for (StartPage type : EnumSet.allOf( StartPage.class )) {
            if (type.toString().equalsIgnoreCase( text ))
                return type;
        }
        return null;
    }

    public static StartPage fromString(String text, StartPage fallback) {

        for (StartPage type : EnumSet.allOf( StartPage.class )) {
            if (type.toString().equalsIgnoreCase( text ))
                return type;
        }
        return fallback;
    }
}
