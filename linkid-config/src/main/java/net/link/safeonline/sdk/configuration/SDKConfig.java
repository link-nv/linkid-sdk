/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.configuration;

import net.link.util.config.RootConfig;


/**
 * <h2>{@link SDKConfig}<br>
 * <sub>[in short].</sub></h2>
 * <p/>
 * <p>
 * <i>09 14, 2010</i>
 * </p>
 *
 * @author lhunath
 */
public interface SDKConfig extends RootConfig {

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

    /**
     * @return Protocol configuration.  SAML/OpenID/... parameters.
     */
    ProtocolConfig proto();
}
