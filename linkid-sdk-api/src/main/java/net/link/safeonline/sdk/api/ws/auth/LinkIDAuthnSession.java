package net.link.safeonline.sdk.api.ws.auth;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 30/04/14
 * Time: 13:33
 */
public class LinkIDAuthnSession implements Serializable {

    private final String sessionId;
    private final byte[] qrCodeImage;
    private final String qrCodeImageEncoded;
    private final String qrCodeURL;

    public LinkIDAuthnSession(final String sessionId, final byte[] qrCodeImage, final String qrCodeImageEncoded, final String qrCodeURL) {

        this.sessionId = sessionId;
        this.qrCodeImage = qrCodeImage;
        this.qrCodeImageEncoded = qrCodeImageEncoded;
        this.qrCodeURL = qrCodeURL;
    }

    @Override
    public String toString() {

        return String.format( "AuthnResponse: sessionId: \"%s\", qrCodeURL: \"%s\"", sessionId, qrCodeURL );
    }

    // Accessors

    public String getSessionId() {

        return sessionId;
    }

    public byte[] getQrCodeImage() {

        return qrCodeImage;
    }

    public String getQrCodeImageEncoded() {

        return qrCodeImageEncoded;
    }

    public String getQrCodeURL() {

        return qrCodeURL;
    }
}
