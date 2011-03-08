package net.link.safeonline.sdk.configuration;

import net.link.util.config.AppConfig;
import net.link.util.config.ConfigHolder;


/**
 * <h2>{@link SDKConfigHolder}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>09 17, 2010</i> </p>
 *
 * @author lhunath
 */
public class SDKConfigHolder extends ConfigHolder<SDKConfig> {

    public SDKConfigHolder() {

        this( null );
    }

    public SDKConfigHolder(SDKConfig customConfig) {

        super( new SafeOnlineDefaultConfigFactory(), SDKConfig.class, customConfig );
    }

    public static SDKConfig config() {

        return (SDKConfig) ConfigHolder.config();
    }

    public static <A extends AppConfig> SafeOnlineDefaultConfigFactory factory() {

        return (SafeOnlineDefaultConfigFactory) ConfigHolder.factory();
    }
}
