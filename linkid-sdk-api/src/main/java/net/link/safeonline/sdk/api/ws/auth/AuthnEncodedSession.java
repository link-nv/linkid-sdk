package net.link.safeonline.sdk.api.ws.auth;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 30/04/14
 * Time: 13:33
 */
public class AuthnEncodedSession implements Serializable {

    private final String sessionId;
    private final String qrCodeImageEncoded;
    private final String qrCodeURL;
    private final String authenticationContext;

    public AuthnEncodedSession(final AuthnSession authnSession) {

        this.sessionId = authnSession.getSessionId();
        this.qrCodeImageEncoded = authnSession.getQrCodeImageEncoded();
        this.qrCodeURL = authnSession.getQrCodeURL();
        this.authenticationContext = authnSession.getAuthenticationContext();
    }

    // Accessors

    public String getSessionId() {

        return sessionId;
    }

    public String getQrCodeImageEncoded() {

        return qrCodeImageEncoded;
    }

    public String getQrCodeURL() {

        return qrCodeURL;
    }

    public String getAuthenticationContext() {

        return authenticationContext;
    }
}