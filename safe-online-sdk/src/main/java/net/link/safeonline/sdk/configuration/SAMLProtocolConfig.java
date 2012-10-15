package net.link.safeonline.sdk.configuration;

import net.link.safeonline.sdk.auth.protocol.saml2.SAMLBinding;
import net.link.util.config.Group;
import net.link.util.config.Property;


/**
 * <h2>{@link SAMLProtocolConfig}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * <p/>
 * <p>
 * <i>09 16, 2010</i>
 * </p>
 *
 * @author lhunath
 */
@Group(prefix = "saml")
public interface SAMLProtocolConfig {

    public static final String SAML2_POST_BINDING_VM_RESOURCE = "/net/link/safeonline/sdk/auth/saml2/saml2-post-binding.vm";

    /**
     * Resource path to a custom velocity template to build the browser POST that contains the SAML2 ticket.
     * <p/>
     * <i>[required, default: A built-in template]</i>
     */
    @Property(required = true, unset = SAML2_POST_BINDING_VM_RESOURCE)
    String postBindingTemplate();

    /**
     * SAML2 binding to use when dispatching requests. See {@link SAMLBinding} for possible values.
     * <p/>
     * <i>[required, default: HTTP_POST]</i>
     */
    @Property(required = true, unset = "HTTP_POST")
    SAMLBinding binding();

    /**
     * Saml2 Relay State parameter.
     * <p/>
     * <i>[optional, default: don't pass any relay state]</i>
     */
    @Property(required = false)
    String relayState();
}
