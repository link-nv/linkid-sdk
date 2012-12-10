package net.link.safeonline.sdk.configuration;

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
