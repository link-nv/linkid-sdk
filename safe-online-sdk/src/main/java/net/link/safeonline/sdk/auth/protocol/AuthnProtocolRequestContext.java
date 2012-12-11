package net.link.safeonline.sdk.auth.protocol;

/**
 * <h2>{@link AuthnProtocolRequestContext}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * <p/>
 * <p>
 * <i>08 17, 2010</i>
 * </p>
 *
 * @author lhunath
 */
public class AuthnProtocolRequestContext extends ProtocolRequestContext {

    private final boolean mobileAuthentication;
    private final boolean mobileAuthenticationMinimal;

    public AuthnProtocolRequestContext(String id, String issuer, ProtocolHandler protocolHandler, String target,
                                       boolean mobileAuthentication, boolean mobileAuthenticationMinimal) {

        super( id, issuer, protocolHandler, target );

        this.mobileAuthentication = mobileAuthentication;
        this.mobileAuthenticationMinimal = mobileAuthenticationMinimal;
    }

    public boolean isMobileAuthentication() {

        return mobileAuthentication;
    }

    public boolean isMobileAuthenticationMinimal() {

        return mobileAuthenticationMinimal;
    }
}
