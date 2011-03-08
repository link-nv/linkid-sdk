package net.link.safeonline.sdk.configuration;

import java.util.List;
import net.link.util.config.Config;

/**
 * <h2>{@link JAASConfig}<br>
 * <sub>[in short] (TODO).</sub></h2>
 *
 * <p>
 * <i>09 16, 2010</i>
 * </p>
 *
 * @author lhunath
 */
@Config.Group(prefix = "jaas")
public interface JAASConfig {

    /**
     * The JAAS context for which the user will be logged in when receiving linkID credentials.
     *
     * <i>[required, default: client-login]</i>
     */
    @Config.Property(required = true, unset = "client-login")
    String context();

    /**
     * The path to which users will be redirected if they navigate to JAAS filtered paths that are not excluded by {@link #publicPaths()}.
     *
     * <i>[optional, default: don't force unauthenticated users away]</i>
     */
    @Config.Property(required = false)
    String loginPath();

    /**
     * JAAS filtered paths that unauthenticated users are allowed to visit.
     *
     * <i>[required, default: all filtered paths require credentials]</i>
     *
     * <p>
     * If {@link #loginPath()} is set and users navigate to filtered paths that are not in this list, they will be redirected to the {@link #loginPath()}.
     * </p>
     */
    @Config.Property(required = true, unset = "")
    List<String> publicPaths();
}
