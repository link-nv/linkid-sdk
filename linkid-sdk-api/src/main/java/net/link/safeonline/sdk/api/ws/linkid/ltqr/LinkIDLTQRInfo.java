package net.link.safeonline.sdk.api.ws.linkid.ltqr;

import java.io.Serializable;
import java.util.Date;
import net.link.safeonline.sdk.api.qr.LinkIDQRInfo;


/**
 * Created by wvdhaute
 * Date: 19/06/15
 * Time: 10:35
 */
@SuppressWarnings("unused")
public class LinkIDLTQRInfo implements Serializable {

    private final String            ltqrReference;
    private final String            sessionId;
    private final Date              created;
    //
    private final LinkIDQRInfo      qrCodeInfo;
    //
    private final boolean           oneTimeUse;
    private final LinkIDLTQRContent content;
    //
    private final boolean           locked;

    public LinkIDLTQRInfo(final String ltqrReference, final String sessionId, final Date created, final LinkIDQRInfo qrCodeInfo, final boolean oneTimeUse,
                          final LinkIDLTQRContent content, final boolean locked) {

        this.ltqrReference = ltqrReference;
        this.sessionId = sessionId;
        this.created = created;
        this.qrCodeInfo = qrCodeInfo;
        this.oneTimeUse = oneTimeUse;
        this.content = content;
        this.locked = locked;
    }

    @Override
    public String toString() {

        return "LinkIDLTQRInfo{" +
               "ltqrReference='" + ltqrReference + '\'' +
               ", sessionId='" + sessionId + '\'' +
               ", created=" + created +
               ", qrCodeInfo='" + qrCodeInfo + '\'' +
               ", oneTimeUse=" + oneTimeUse +
               ", content=" + content +
               ", locked=" + locked +
               '}';
    }

    // Accessors

    public String getLtqrReference() {

        return ltqrReference;
    }

    public String getSessionId() {

        return sessionId;
    }

    public Date getCreated() {

        return created;
    }

    public LinkIDQRInfo getQrCodeInfo() {

        return qrCodeInfo;
    }

    public boolean isOneTimeUse() {

        return oneTimeUse;
    }

    public LinkIDLTQRContent getContent() {

        return content;
    }

    public boolean isLocked() {

        return locked;
    }
}
