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

    private final String             ltqrReference;
    private final String             sessionId;
    private final Date               created;
    //
    private final LinkIDQRInfo       qrCodeInfo;
    //
    private final LinkIDLTQRContent  content;
    //
    private final LinkIDLTQRLockType lockType;
    private final boolean            locked;
    //
    private final boolean            waitForUnblock;
    private final boolean            blocked;

    public LinkIDLTQRInfo(final String ltqrReference, final String sessionId, final Date created, final LinkIDQRInfo qrCodeInfo,
                          final LinkIDLTQRContent content, final LinkIDLTQRLockType lockType, final boolean locked, final boolean waitForUnblock,
                          final boolean blocked) {

        this.ltqrReference = ltqrReference;
        this.sessionId = sessionId;
        this.created = created;
        this.qrCodeInfo = qrCodeInfo;
        this.content = content;
        this.lockType = lockType;
        this.locked = locked;
        this.waitForUnblock = waitForUnblock;
        this.blocked = blocked;
    }

    @Override
    public String toString() {

        return "LinkIDLTQRInfo{" +
               "ltqrReference='" + ltqrReference + '\'' +
               ", sessionId='" + sessionId + '\'' +
               ", created=" + created +
               ", qrCodeInfo=" + qrCodeInfo +
               ", content=" + content +
               ", lockType=" + lockType +
               ", locked=" + locked +
               ", waitForUnblock=" + waitForUnblock +
               ", blocked=" + blocked +
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

    public LinkIDLTQRContent getContent() {

        return content;
    }

    public LinkIDLTQRLockType getLockType() {

        return lockType;
    }

    public boolean isLocked() {

        return locked;
    }

    public boolean isWaitForUnblock() {

        return waitForUnblock;
    }

    public boolean isBlocked() {

        return blocked;
    }
}
