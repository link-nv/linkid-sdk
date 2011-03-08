package net.link.safeonline.sdk.configuration;

import net.link.util.config.ConfigFilter;

/**
 * <h2>{@link SDKConfigFilter}<br>
 * <sub>[in short] (TODO).</sub></h2>
 *
 * <p>
 * <i>09 23, 2010</i>
 * </p>
 *
 * @author lhunath
 */
public class SDKConfigFilter extends ConfigFilter {

    public SDKConfigFilter() {

        this( new SDKConfigHolder() );
    }

    protected SDKConfigFilter(SDKConfigHolder configHolder) {

        super( configHolder );
    }
}
