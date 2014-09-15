/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.configuration;

import net.link.util.config.ConfigHolder;


/**
 * <h2>{@link SDKConfigHolder}<br> <sub>[in short].</sub></h2>
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

        super( SDKConfig.class, new SDKDefaultConfigFactory(), customConfig );
    }

    public static SDKConfig config() {

        return ConfigHolder.config( SDKConfig.class );
    }
}
