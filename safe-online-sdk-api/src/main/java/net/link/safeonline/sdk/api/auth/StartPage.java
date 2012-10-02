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
