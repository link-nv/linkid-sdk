/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.configuration;

import java.util.Locale;
import net.link.util.config.Group;
import net.link.util.config.Property;


/**
 * <h2>{@link LinkIDConfig}<br> <sub>[in short].</sub></h2>
 * <p/>
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
     * <p/>
     * <i>[required, default: /entry]</i>
     * <p/>
     * <p> We go here when the user begins an authentication from our application. </p>
     */
    @Property(required = true, unset = "/entry")
    String authPath();

    /**
     * PATH within linkID-auth that handles logout requests.
     * <p/>
     * <i>[required, default: /logout]</i>
     * <p/>
     * <p> We go here when the user begins a logout from our application. </p>
     */
    @Property(required = true, unset = "/logoutentry")
    String logoutPath();

    /**
     * PATH within linkID-auth that handles SSO logout responses caused by another application's logout request.
     * <p/>
     * <i>[required, default: /logoutexit]</i>
     * <p/>
     * <p> We go here after linkID-auth asked us to clean our session up following a logout request that was initiated from another
     * application in our application's SSO pool.  This response allows linkID to find out whether we successfully logged the user out of
     * the application's SSO session. </p>
     */
    @Property(required = true, unset = "/logoutexit")
    String logoutExitPath();

    /**
     * Specifies a name of a CSS theme that changes the looks of the linkID application.
     * <p/>
     * <i>[optional, default: The application's default theme configured at the linkID node]</i>
     * <p/>
     * <p> Accepted values are names of themes configured by the operator of the linkID node you'll be using. </p>
     */
    @Property(required = false)
    String theme();

    /**
     * Specifies the language that the linkID service should localize its interaction with the user in.
     * <p/>
     * <i>[optional, default: Use the locale of the current browser request]</i>
     */
    @Property(required = false, unset = Property.AUTO)
    Locale language();
}
