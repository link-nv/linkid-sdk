package net.link.safeonline.sdk.api.ws.linkid.auth;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 30/04/14
 * Time: 13:33
 */
@SuppressWarnings("unused")
public class LinkIDAuthSession implements Serializable {

    private final String  sessionId;
    private final byte[]  qrCodeImage;
    private final String  qrCodeImageEncoded;
    private final String  qrCodeURL;
    private final boolean mobile;           // If user agent was specified, will return whether the request was started from a mobile client or not. Else is false
    private final boolean targetBlank;      // If user agent was specified, some devices require the target to be _blank in order for the link to open the linkID app. Else is false

    public LinkIDAuthSession(final String sessionId, final byte[] qrCodeImage, final String qrCodeImageEncoded, final String qrCodeURL, final boolean mobile,
                             final boolean targetBlank) {

        this.sessionId = sessionId;
        this.qrCodeImage = qrCodeImage;
        this.qrCodeImageEncoded = qrCodeImageEncoded;
        this.qrCodeURL = qrCodeURL;
        this.mobile = mobile;
        this.targetBlank = targetBlank;
    }

    @Override
    public String toString() {

        return String.format( "AuthnResponse: sessionId: \"%s\", qrCodeURL: \"%s\", mobile: \"%s\", targetBlank: \"%s\"", sessionId, qrCodeURL, mobile,
                targetBlank );
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

    public boolean isMobile() {

        return mobile;
    }

    public boolean isTargetBlank() {

        return targetBlank;
    }
}
