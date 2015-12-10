package net.link.safeonline.sdk.api.ws.linkid.ltqr;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 10/12/15
 * Time: 10:07
 */
public class LinkIDLTQRPushContent implements Serializable {

    private final LinkIDLTQRContent  content;
    private final String             userAgent;
    private final LinkIDLTQRLockType lockType;

    public LinkIDLTQRPushContent(final LinkIDLTQRContent content, final String userAgent, final LinkIDLTQRLockType lockType) {

        this.content = content;
        this.userAgent = userAgent;
        this.lockType = lockType;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDLTQRPushContent{" +
               "content=" + content +
               ", userAgent='" + userAgent + '\'' +
               ", lockType=" + lockType +
               '}';
    }

    // Accessors

    public LinkIDLTQRContent getContent() {

        return content;
    }

    public String getUserAgent() {

        return userAgent;
    }

    public LinkIDLTQRLockType getLockType() {

        return lockType;
    }
}
