package net.link.safeonline.sdk.api.callback;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import net.link.safeonline.sdk.api.exception.LinkIDInvalidCallbackException;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 16/12/14
 * Time: 11:31
 */
public class LinkIDCallback implements Serializable {

    public static final String KEY_LOCATION       = "Callback.location";
    public static final String KEY_APP_SESSION_ID = "Callback.appSessionId";
    public static final String KEY_IN_APP         = "Callback.inApp";

    private final String  location;             // location the linkID client will load when finished
    private final String  appSessionId;         // optional sessionId a SP can provide to load in session state before linkID was started
    private final boolean inApp;                // display the location inApp (webView) or via the client's browser

    public LinkIDCallback(final String location, final String appSessionId, final boolean inApp) {

        if (null == location) {
            throw new LinkIDInvalidCallbackException( "If you provide a callback, the location MUST be not null" );
        }

        this.location = location;
        this.appSessionId = appSessionId;
        this.inApp = inApp;
    }

    // Helper methods

    public Map<String, String> toMap() {

        Map<String, String> map = new HashMap<String, String>();

        map.put( KEY_LOCATION, location );
        if (null != appSessionId) {
            map.put( KEY_APP_SESSION_ID, appSessionId );
        }
        map.put( KEY_IN_APP, Boolean.toString( inApp ) );

        return map;
    }

    @Nullable
    public static LinkIDCallback fromMap(final Map<String, String> callbackMap) {

        // check map valid
        String location = callbackMap.get( KEY_LOCATION );
        if (null == location) {
            throw new LinkIDInvalidCallbackException( "If you provide a callback, the location MUST be not null" );
        }

        // parse the reset
        String appSessionId = callbackMap.get( KEY_APP_SESSION_ID );
        boolean inApp = false;
        if (null != callbackMap.get( KEY_IN_APP )) {
            inApp = Boolean.parseBoolean( callbackMap.get( KEY_IN_APP ) );
        }

        return new LinkIDCallback( location, appSessionId, inApp );
    }

    @Override
    public String toString() {

        return String.format( "Location: \"%s\", appSessionId: \"%s\", inApp: %s", location, appSessionId, inApp );
    }

    // Accessors

    public String getLocation() {

        return location;
    }

    @Nullable
    public String getAppSessionId() {

        return appSessionId;
    }

    public boolean isInApp() {

        return inApp;
    }
}
