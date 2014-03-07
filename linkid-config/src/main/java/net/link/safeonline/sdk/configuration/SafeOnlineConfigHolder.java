/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.configuration;

import net.link.util.config.ConfigHolder;


/**
 * <h2>{@link SafeOnlineConfigHolder}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>09 17, 2010</i> </p>
 *
 * @author lhunath
 */
public class SafeOnlineConfigHolder extends ConfigHolder {

    public SafeOnlineConfigHolder() {

        this( null );
    }

    public SafeOnlineConfigHolder(SafeOnlineConfig customConfig) {

        super( SafeOnlineConfig.class, new SafeOnlineDefaultConfigFactory(), customConfig );
    }

    public static SafeOnlineConfig config() {

        return ConfigHolder.config( SafeOnlineConfig.class );
    }
}
