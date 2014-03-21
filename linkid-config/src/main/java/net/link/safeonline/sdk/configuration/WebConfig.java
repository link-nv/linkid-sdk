/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.configuration;

import net.link.util.config.Group;
import net.link.util.config.Property;


/**
 * <h2>{@link WebConfig}</h2>
 * <p/>
 * <p>
 * Web configuration.  Base URLs, paths, etc.
 * </p>
 * <p/>
 * <p>
 * <i>Sep 8, 2009</i>
 * </p>
 *
 * @author lhunath
 */
@Group(prefix = "web")
public interface WebConfig {

    /**
     * Property that defines the base URL for applications on this host.  It will be used to create absolute URLs that point the user to
     * the
     * application.
     * <p/>
     * <i>[required]</i>
     * <p/>
     * <p>
     * Use the form: {@code [scheme]//[authority]} (eg. {@code http://my.host.be})
     * </p>
     * <p/>
     * <p>
     * <b>NOTE:</b> Do not terminate with a slash.
     * </p>
     */
    @Property(required = true)
    String appBase();

    /**
     * Property that defines the base URL for application landing pages on this host (should be on HTTPS).
     * <p/>
     * <i>[required]</i>
     * <p/>
     * <p>
     * Use the form: {@code [scheme]//[authority]} (eg. {@code https://my.host.be})
     * </p>
     * <p/>
     * <p>
     * <b>NOTE:</b> Do not terminate with a slash.
     * </p>
     */
    @Property(required = true)
    String appConfidentialBase();

    /**
     * Property that defines the application's root path relative to the {@link #appBase()}.
     * <p/>
     * <i>[required]</i>
     * <p/>
     * <p>
     * <b>NOTE:</b> Should begin and end with a slash.
     * </p>
     */
    @Property(required = true, unset = Property.AUTO)
    String appPath();

    /**
     * Property that defines the base URL to the linkID mobile web application to use for user profile management.
     * <p/>
     * <i>[required, default: https://demo.linkid.be/linkid-mobile]</i>
     * <p/>
     * <p>
     * Use the form: {@code [scheme]//[authority]/[path-to-linkid-mobile]} (eg. {@code https://my.linkid.be/linkid-mobile})
     * </p>
     * <p/>
     * <p>
     * <b>NOTE:</b> Do not terminate with a slash.
     * </p>
     */
    @Property(required = true, unset = "https://demo.linkid.be/linkid-mobile")
    String mobileBase();

    /**
     * Property that defines the URL to the linkID QR authentication web application to use for application authentication
     * <p/>
     * <i>[required, default: https://demo.linkid.be/linkid-qr/auth-min]</i>
     * <p/>
     * <p>
     * Use the form: {@code [scheme]//[authority]/[path-to-linkid-qr/auth-min]} (eg. {@code https://my.linkid.be/linkid-qr/auth-min})
     * </p>
     * <p/>
     */
    @Property(required = false, unset = "https://demo.linkid.be/linkid-qr/auth-min")
    String mobileAuthMinimalURL();

    /**
     * Property that defines the URL to the linkID QR force registration web application to use for application authentication
     * <p/>
     * <i>[required, default: https://demo.linkid.be/linkid-qr/reg-min]</i>
     * <p/>
     * <p>
     * Use the form: {@code [scheme]//[authority]/[path-to-linkid-qr/reg-min]} (eg. {@code https://my.linkid.be/linkid-qr/reg-min})
     * </p>
     * <p/>
     */
    @Property(required = false, unset = "https://demo.linkid.be/linkid-qr/reg-min")
    String mobileRegMinimalURL();

    /**
     * Property that defines the URL for Single Logout ( NOT YET IMPLEMENTED FULLY ! )
     * <p/>
     * <i>[required, default: https://demo.linkid.be/linkid-qr/logout]</i>
     * <p/>
     * <p>
     * Use the form: {@code [scheme]//[authority]/[path-to-linkid-qr/logout]} (eg. {@code https://my.linkid.be/linkid-qr/logout})
     * </p>
     * <p/>
     */
    @Property(required = false, unset = "https://demo.linkid.be/linkid-qr/logout")
    String mobileLogoutURL();

    /**
     * Property that defines the URL for Single Logout ( NOT YET IMPLEMENTED FULLY ! )
     * <p/>
     * <i>[required, default: https://demo.linkid.be/linkid-qr/logoutexit]</i>
     * <p/>
     * <p>
     * Use the form: {@code [scheme]//[authority]/[path-to-linkid-qr/logoutexit]} (eg. {@code https://my.linkid.be/linkid-qr/logoutexit})
     * </p>
     * <p/>
     */
    @Property(required = false, unset = "https://demo.linkid.be/linkid-qr/logoutexit")
    String mobileLogoutExitURL();

    /**
     * Property that defines the base URL to the linkID static web application.
     * This houses for example the javascript for running the linkID authentication process in a modal window.
     * <p/>
     * <i>[optional, default: http://demo.linkid.be/linkid-static]</i>
     * <p/>
     * <p>
     * Use the form: {@code [scheme]//[authority]/[path-to-linkid-static]} (eg. {@code http://my.linkid.be/linkid-static})
     * </p>
     * <p/>
     * <p>
     * <b>NOTE:</b> Do not terminate with a slash.
     * </p>
     */
    @Property(required = false, unset = "https://demo.linkid.be/linkid-static")
    String staticBase();

    /**
     * Property that defines the base URL to the linkID web services to use ( with WS-Security X509Token profile )
     * <p/>
     * <i>[required, default: https://demo.linkid.be/linkid-ws]</i>
     * <p/>
     * <p>
     * Use the form: {@code [scheme]//[authority]/[path-to-linkid-ws]} (eg. {@code https://my.linkid.be/linkid-ws})
     * </p>
     * <p/>
     * <p>
     * <b>NOTE:</b> Do not terminate with a slash.
     * </p>
     */
    @Property(required = true, unset = "https://demo.linkid.be/linkid-ws")
    String wsBase();

    /**
     * Property that defines the base URL to the linkID web services to use ( with WS-Security Username profile ).
     * <p/>
     * <i>[required, default: https://demo.linkid.be/linkid-ws-username]</i>
     * <p/>
     * <p>
     * Use the form: {@code [scheme]//[authority]/[path-to-linkid-ws-username]} (eg. {@code https://my.linkid.be/linkid-ws-username})
     * </p>
     * <p/>
     * <p>
     * <b>NOTE:</b> Do not terminate with a slash.
     * </p>
     */
    @Property(required = true, unset = "https://demo.linkid.be/linkid-ws-username")
    String wsUsernameBase();

    /**
     * Property that defines the base URL to the linkID web services to use.
     * <p/>
     * <i>[required, default: https://demo.linkid.be/linkid-ws]</i>
     * <p/>
     * <p>
     * Use the form: {@code [scheme]//[authority]/[path-to-linkid-ws]} (eg. {@code https://my.linkid.be/linkid-ws})
     * </p>
     * <p/>
     * <p>
     * <b>NOTE:</b> Do not terminate with a slash.
     * </p>
     */
    @Property(required = true, unset = "https://demo.linkid.be/linkid-auth-ws")
    String authWsBase();

    /**
     * PATH within our application where linkID's authentication response will be posted to.  Absolute URLs to this path will use the
     * {@link
     * #appConfidentialBase()}.
     * <p/>
     * <i>[optional, default: post the response straight to the authentication's target URL]</i>
     */
    @Property(required = false)
    String landingPath();
}
