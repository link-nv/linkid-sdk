package net.link.safeonline.sdk.configuration;

import java.util.Locale;
import net.link.util.config.Group;
import net.link.util.config.Property;


/**
 * <h2>{@link LinkIDConfig}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>09 15, 2010</i> </p>
 *
 * @author lhunath
 */
@Group(prefix = "linkID")
public interface LinkIDConfig {

    /**
     * @return Application linkID identity configuration.  Identity, keys, etc.
     */
    AppLinkIDConfig app();

    /**
     * PATH within linkID-auth that handles authentication requests.
     *
     * <i>[required, default: /entry]</i>
     *
     * <p> We go here when the user begins an authentication from our application. </p>
     */
    @Property(required = true, unset = "/core/entry")
    String authPath();

    /**
     * PATH within linkID-auth that handles logout requests.
     *
     * <i>[required, default: /logout]</i>
     *
     * <p> We go here when the user begins a logout from our application. </p>
     */
    @Property(required = true, unset = "/logoutentry")
    String logoutPath();

    /**
     * PATH within linkID-auth that handles SSO logout responses caused by another application's logout request.
     *
     * <i>[required, default: /logoutexit]</i>
     *
     * <p> We go here after linkID-auth asked us to clean our session up following a logout request that was initiated from another
     * application in our application's SSO pool.  This response allows linkID to find out whether we successfully logged the user out of
     * the application's SSO session. </p>
     */
    @Property(required = true, unset = "/logoutexit")
    String logoutExitPath();

    /**
     * Specifies a name of a CSS theme that changes the looks of the linkID application.
     *
     * <i>[optional, default: The application's default theme configured at the linkID node]</i>
     *
     * <p> Accepted values are names of themes configured by the operator of the linkID node you'll be using. </p>
     */
    @Property(required = false)
    String theme();

    /**
     * Specifies the language that the linkID service should localize its interaction with the user in.
     *
     * <i>[optional, default: Use the locale of the current browser request]</i>
     */
    @Property(required = false, unset = Property.AUTO)
    Locale language();
}
