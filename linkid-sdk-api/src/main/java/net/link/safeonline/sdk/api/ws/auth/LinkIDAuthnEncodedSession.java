package net.link.safeonline.sdk.api.ws.auth;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 30/04/14
 * Time: 13:33
 */
public class LinkIDAuthnEncodedSession implements Serializable {

    private final String sessionId;
    private final String qrCodeImageEncoded;
    private final String qrCodeURL;

    public LinkIDAuthnEncodedSession(final LinkIDAuthnSession linkIDAuthnSession) {

        this.sessionId = linkIDAuthnSession.getSessionId();
        this.qrCodeImageEncoded = linkIDAuthnSession.getQrCodeImageEncoded();
        this.qrCodeURL = linkIDAuthnSession.getQrCodeURL();
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
}
