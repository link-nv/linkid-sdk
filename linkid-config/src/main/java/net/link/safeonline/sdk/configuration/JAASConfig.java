/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.configuration;

import java.util.List;
import net.link.util.config.Group;
import net.link.util.config.Property;


/**
 * <h2>{@link JAASConfig}<br>
 * <sub>[in short].</sub></h2>
 * <p/>
 * <p>
 * <i>09 16, 2010</i>
 * </p>
 *
 * @author lhunath
 */
@Group(prefix = "jaas")
public interface JAASConfig {

    /**
     * The JAAS context for which the user will be logged in when receiving linkID credentials.
     * <p/>
     * <i>[required, default: client-login]</i>
     */
    @Property(required = true, unset = "client-login")
    String context();

    /**
     * The path to which users will be redirected if they navigate to JAAS filtered paths that are not excluded by {@link #publicPaths()}.
     * <p/>
     * <i>[optional, default: don't force unauthenticated users away]</i>
     */
    @Property(required = false)
    String loginPath();

    /**
     * JAAS filtered paths that unauthenticated users are allowed to visit.
     * <p/>
     * <i>[required, default: all filtered paths require credentials]</i>
     * <p/>
     * <p>
     * If {@link #loginPath()} is set and users navigate to filtered paths that are not in this list, they will be redirected to the {@link #loginPath()}.
     * </p>
     */
    @Property(required = true, unset = "")
    List<String> publicPaths();
}
