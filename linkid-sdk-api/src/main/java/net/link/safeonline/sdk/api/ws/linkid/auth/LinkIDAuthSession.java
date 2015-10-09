package net.link.safeonline.sdk.api.ws.linkid.auth;

import java.io.Serializable;
import net.link.safeonline.sdk.api.qr.LinkIDQRInfo;


/**
 * Created by wvdhaute
 * Date: 30/04/14
 * Time: 13:33
 */
@SuppressWarnings("unused")
public class LinkIDAuthSession implements Serializable {

    private final String       sessionId;
    private final LinkIDQRInfo qrCodeInfo;

    public LinkIDAuthSession(final String sessionId, final LinkIDQRInfo qrCodeInfo) {

        this.sessionId = sessionId;
        this.qrCodeInfo = qrCodeInfo;
    }

    @Override
    public String toString() {

        return "LinkIDAuthSession{" +
               "sessionId='" + sessionId + '\'' +
               ", qrCodeInfo=" + qrCodeInfo +
               '}';
    }

    // Accessors

    public String getSessionId() {

        return sessionId;
    }

    public LinkIDQRInfo getQrCodeInfo() {

        return qrCodeInfo;
    }
}
