/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.configuration;

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
public interface SDKConfig extends SafeOnlineConfig {

    /**
     * @return Protocol configuration.  SAML/OpenID/... parameters.
     */
    ProtocolConfig proto();
}
