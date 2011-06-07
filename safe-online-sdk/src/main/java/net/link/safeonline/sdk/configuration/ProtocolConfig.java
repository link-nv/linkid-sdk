package net.link.safeonline.sdk.configuration;

import net.link.safeonline.sdk.auth.protocol.Protocol;
import net.link.util.config.Group;
import net.link.util.config.Property;
import org.joda.time.Duration;


/**
 * <h2>{@link ProtocolConfig}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>09 15, 2010</i> </p>
 *
 * @author lhunath
 */
@Group(prefix = "proto")
public interface ProtocolConfig {

    OpenIDProtocolConfig openID();

    SAMLProtocolConfig saml();

    /**
     * The authentication protocol used to begin the session with the linkID authentication web application. See {@link Protocol} for the
     * possible values.
     *
     * <i>[required, default: SAML2]</i>
     *
     * @return authentication protocol to use
     */
    @Property(required = true, unset = "SAML2")
    Protocol defaultProtocol();

    /**
     * The maximum deviation in milliseconds between timestamps in WS-Security messages and the current system time.  This is used to
     * compensate for possible differences of the server and client's system clock.
     *
     * <i>[optional, default: 300000]</i>
     *
     * @return maximum devication (ms) for WS-Security timestamps.
     */
    @Property(required = true, unset = "300000" /* 5 minutes */)
    Duration maxTimeOffset();
}
