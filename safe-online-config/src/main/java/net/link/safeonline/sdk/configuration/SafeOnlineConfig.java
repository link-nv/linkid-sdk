package net.link.safeonline.sdk.configuration;

import net.link.util.config.RootConfig;


/**
 * <h2>{@link SafeOnlineConfig}<br>
 * <sub>[in short] (TODO).</sub></h2>
 *
 * <p>
 * <i>09 14, 2010</i>
 * </p>
 *
 * @author lhunath
 */
public interface SafeOnlineConfig extends RootConfig {

    /**
     * @return Web configuration.  Base URLs, paths, etc.
     */
    WebConfig web();

    /**
     * @return LinkID configuration.  Location of linkID services and linkID specific protocol settings.
     */
    LinkIDConfig linkID();

    /**
     * @return JAAS context configuration.  For applications that want to set a JAAS context based on linkID credentials.
     */
    JAASConfig jaas();
}
