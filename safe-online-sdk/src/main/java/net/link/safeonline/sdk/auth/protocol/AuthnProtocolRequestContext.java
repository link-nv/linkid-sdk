package net.link.safeonline.sdk.auth.protocol;

/**
 * <h2>{@link AuthnProtocolRequestContext}<br>
 * <sub>[in short] (TODO).</sub></h2>
 *
 * <p>
 * <i>08 17, 2010</i>
 * </p>
 *
 * @author lhunath
 */
public class AuthnProtocolRequestContext extends ProtocolRequestContext {

    public AuthnProtocolRequestContext(String id, String issuer, ProtocolHandler protocolHandler, String target) {

        super(id, issuer, protocolHandler, target);
    }
}
