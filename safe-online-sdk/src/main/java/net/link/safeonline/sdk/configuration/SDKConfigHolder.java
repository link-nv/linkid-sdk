package net.link.safeonline.sdk.configuration;

import net.link.util.config.ConfigHolder;


/**
 * <h2>{@link SDKConfigHolder}<br> <sub>[in short] (TODO).</sub></h2>
 * <p/>
 * <p> <i>09 17, 2010</i> </p>
 *
 * @author lhunath
 */
public class SDKConfigHolder extends ConfigHolder {

    public SDKConfigHolder() {

        this( null );
    }

    public SDKConfigHolder(SDKConfig customConfig) {

        super( SDKConfig.class, new SafeOnlineDefaultConfigFactory(), customConfig );
    }

    public static SDKConfig config() {

        return ConfigHolder.config( SDKConfig.class );
    }
}
