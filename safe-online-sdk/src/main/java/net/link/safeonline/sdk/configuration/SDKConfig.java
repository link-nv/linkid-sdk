package net.link.safeonline.sdk.configuration;

import net.link.util.config.AppConfig;
import net.link.util.config.ConfigFilter;
import net.link.util.config.DefaultConfigFactory;

/**
 * <h2>{@link SDKConfig}<br>
 * <sub>[in short] (TODO).</sub></h2>
 *
 * <p>
 * <i>09 14, 2010</i>
 * </p>
 *
 * @author lhunath
 */
public interface SDKConfig extends SafeOnlineConfig {

    /**
     * @return Protocol configuration.  SAML/OpenID/... parameters.
     */
    ProtocolConfig proto();
}
