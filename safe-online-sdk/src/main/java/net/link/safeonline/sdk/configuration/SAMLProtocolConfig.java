package net.link.safeonline.sdk.configuration;

import net.link.safeonline.sdk.auth.protocol.saml2.SAMLBinding;
import net.link.util.config.Config;

/**
 * <h2>{@link SAMLProtocolConfig}<br>
 * <sub>[in short] (TODO).</sub></h2>
 *
 * <p>
 * <i>09 16, 2010</i>
 * </p>
 *
 * @author lhunath
 */
@Config.Group(prefix = "saml")
public interface SAMLProtocolConfig {

    /**
     * Resource path to a custom velocity template to build the browser POST that contains the SAML2 ticket.
     *
     * <i>[required, default: A built-in template]</i>
     */
    @Config.Property(required = true, unset = "/net/link/safeonline/sdk/auth/saml2/saml2-post-binding.vm")
    String postBindingTemplate();

    /**
     * SAML2 binding to use when dispatching requests. See {@link SAMLBinding} for possible values.
     *
     * <i>[required, default: HTTP_POST]</i>
     */
    @Config.Property( required = true, unset = "HTTP_POST")
    SAMLBinding binding();

    /**
     * Saml2 Relay State parameter.
     *
     * <i>[optional, default: don't pass any relay state]</i>
     */
    @Config.Property(required = false)
    String relayState();
}
