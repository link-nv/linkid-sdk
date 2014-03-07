/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.configuration;

import net.link.util.config.ConfigFilter;


/**
 * <h2>{@link SDKConfigFilter}<br>
 * <sub>[in short].</sub></h2>
 * <p/>
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
