package net.link.safeonline.sdk.configuration;

import net.link.safeonline.sdk.auth.protocol.Protocol;
import net.link.util.config.Config;
import net.link.util.ws.pkix.wssecurity.WSSecurityClientHandler;


/**
 * <h2>{@link ProtocolConfig}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>09 15, 2010</i> </p>
 *
 * @author lhunath
 */
@Config.Group(prefix = "proto")
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
    @Config.Property(required = true, unset = "SAML2")
    Protocol defaultProtocol();

    /**
     * The maximum deviation in milliseconds between timestamps in WS-Security messages and the current system time.  This is used to
     * compensate for possible differences of the server and client's system clock.
     *
     * <i>[optional, default: {@value WSSecurityClientHandler#DEFAULT_MAX_TIMESTAMP_OFFSET}]</i>
     *
     * @return maximum devication (ms) for WS-Security timestamps.
     */
    @Config.Property(required = false)
    Long maxTimeOffset();
}
