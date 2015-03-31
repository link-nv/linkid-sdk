/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.configuration;

import net.link.util.config.ConfigHolder;


/**
 * <h2>{@link LinkIDSDKConfigHolder}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>09 17, 2010</i> </p>
 *
 * @author lhunath
 */
public class LinkIDSDKConfigHolder extends ConfigHolder {

    public LinkIDSDKConfigHolder() {

        this( null );
    }

    public LinkIDSDKConfigHolder(LinkIDSDKConfig customConfig) {

        super( LinkIDSDKConfig.class, new LinkIDSDKDefaultConfigFactory(), customConfig );
    }

    public static LinkIDSDKConfig config() {

        return ConfigHolder.config( LinkIDSDKConfig.class );
    }
}
