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
     * PATH within our application where linkID's authentication response will be posted to.  Absolute URLs to this path will use the
     * {@link
     * #appConfidentialBase()}.
     * <p/>
     * <i>[optional, default: post the response straight to the authentication's target URL]</i>
     */
    @Property(required = false)
    String landingPath();

    /**
     * Property that defines the base URL to linkID.
     * <p/>
     * <i>[required, default: https://demo.linkid.be]</i>
     * <p/>
     * <p>
     * Use the form: {@code [scheme]//[authority]/[path-to-linkid]} (eg. {@code https://demo.linkid.be})
     * </p>
     * <p/>
     * <p>
     * <b>NOTE:</b> Do not terminate with a slash.
     * </p>
     */
    @Property(required = true, unset = "https://demo.linkid.be")
    String linkIDBase();
}
