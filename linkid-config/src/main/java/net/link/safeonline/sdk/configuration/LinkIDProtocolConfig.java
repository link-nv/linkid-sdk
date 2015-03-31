/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.configuration;

import net.link.util.config.Group;
import net.link.util.config.Property;
import org.joda.time.Duration;


/**
 * <h2>{@link LinkIDProtocolConfig}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>09 15, 2010</i> </p>
 *
 * @author lhunath
 */
@Group(prefix = "proto")
public interface LinkIDProtocolConfig {

    LinkIDSAMLProtocolConfig saml();

    /**
     * The authentication protocol used to begin the session with the linkID authentication web application. See {@link LinkIDProtocol} for the
     * possible values.
     * <p/>
     * <i>[required, default: SAML2]</i>
     *
     * @return authentication protocol to use
     */
    @Property(required = true, unset = "SAML2")
    LinkIDProtocol defaultProtocol();

    /**
     * The maximum deviation in milliseconds between timestamps in WS-Security messages and the current system time.  This is used to
     * compensate for possible differences of the server and client's system clock.
     * <p/>
     * <i>[optional, default: 300000]</i>
     *
     * @return maximum devication (ms) for WS-Security timestamps.
     */
    @Property(required = true, unset = "300000" /* 5 minutes */)
    Duration maxTimeOffset();
}
