/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.configuration;

import net.link.util.config.Config;

/**
 * <h2>{@link WebConfig}</h2>
 *
 * <p>
 * Web configuration.  Base URLs, paths, etc.
 * </p>
 *
 * <p>
 * <i>Sep 8, 2009</i>
 * </p>
 *
 * @author lhunath
 */
@Config.Group(prefix = "web")
public interface WebConfig {

    /**
     * Property that defines the base URL for applications on this host.  It will be used to create absolute URLs that point the user to the application.
     *
     * <i>[required]</i>
     *
     * <p>
     * Use the form: <code>[scheme]//[authority]</code> (eg. <code>http://my.host.be</code>)
     * </p>
     *
     * <p>
     * <b>NOTE:</b> Do not terminate with a slash.
     * </p>
     */
    @Config.Property(required = true)
    String appBase();

    /**
     * Property that defines the base URL for application landing pages on this host (should be on HTTPS).
     *
     * <i>[required]</i>
     *
     * <p>
     * Use the form: <code>[scheme]//[authority]</code> (eg. <code>https://my.host.be</code>)
     * </p>
     *
     * <p>
     * <b>NOTE:</b> Do not terminate with a slash.
     * </p>
     */
    @Config.Property(required = true)
    String appConfidentialBase();

    /**
     * Property that defines the application's root path relative to the {@link #appBase()}.
     *
     * <i>[required]</i>
     *
     * <p>
     * <b>NOTE:</b> Should begin and end with a slash.
     * </p>
     */
    @Config.Property(required = true, unset = Config.Property.AUTO)
    String appPath();

    /**
     * Property that defines the base URL to the linkID user web application to use for user profile management.
     *
     * <i>[required, default: https://demo.linkid.be/linkid-user]</i>
     *
     * <p>
     * Use the form: <code>[scheme]//[authority]/[path-to-linkid-user]</code> (eg. <code>https://my.linkid.be/linkid-user</code>)
     * </p>
     *
     * <p>
     * <b>NOTE:</b> Do not terminate with a slash.
     * </p>
     */
    @Config.Property(required = true, unset = "https://demo.linkid.be/linkid-user")
    String userBase();

    /**
     * Property that defines the base URL to the linkID authentication web application to use for application authentication.
     *
     * <i>[required, default: https://demo.linkid.be/linkid-auth]</i>
     *
     * <p>
     * Use the form: <code>[scheme]//[authority]/[path-to-linkid-auth]</code> (eg. <code>https://my.linkid.be/linkid-auth</code>)
     * </p>
     *
     * <p>
     * <b>NOTE:</b> Do not terminate with a slash.
     * </p>
     */
    @Config.Property(required = true, unset = "https://demo.linkid.be/linkid-auth")
    String authBase();

    /**
     * Property that defines the base URL to the linkID web services to use.
     *
     * <i>[required, default: https://demo.linkid.be/linkid-ws]</i>
     *
     * <p>
     * Use the form: <code>[scheme]//[authority]/[path-to-linkid-ws]</code> (eg. <code>https://my.linkid.be/linkid-ws</code>)
     * </p>
     *
     * <p>
     * <b>NOTE:</b> Do not terminate with a slash.
     * </p>
     */
    @Config.Property(required = true, unset = "https://demo.linkid.be/linkid-ws")
    String wsBase();

    /**
     * PATH within our application where linkID's authentication response will be posted to.  Absolute URLs to this path will use the {@link #appConfidentialBase()}.
     *
     * <i>[optional, default: post the response straight to the authentication's target URL]</i>
     */
    @Config.Property(required = false)
    String landingPath();
}
