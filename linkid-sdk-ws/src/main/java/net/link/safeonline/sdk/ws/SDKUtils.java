package net.link.safeonline.sdk.ws;

import java.util.ResourceBundle;


public abstract class SDKUtils {

    public static String getSDKProperty(final String key) {

        ResourceBundle properties = ResourceBundle.getBundle( "sdk_config" );
        return properties.getString( key );
    }
}
