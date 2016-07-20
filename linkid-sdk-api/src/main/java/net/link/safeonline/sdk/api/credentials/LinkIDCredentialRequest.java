/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.credentials;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Date;


/**
 * Created by wvdhaute
 * Date: 19/07/16
 * Time: 16:12
 */
@SuppressWarnings("unused")
public class LinkIDCredentialRequest implements Serializable {

    private final String downloadUrl;
    private final String sessionId;
    private final Date   expiryDate;

    public LinkIDCredentialRequest(final String downloadUrl, final String sessionId, final Date expiryDate) {

        this.downloadUrl = downloadUrl;
        this.sessionId = sessionId;
        this.expiryDate = expiryDate;
    }

    // Helper methods

    @Override
    public String toString() {

        return MoreObjects.toStringHelper( this ).add( "downloadUrl", downloadUrl ).add( "sessionId", sessionId ).add( "expiryDate", expiryDate ).toString();
    }

    // Accessors

    public String getDownloadUrl() {

        return downloadUrl;
    }

    public String getSessionId() {

        return sessionId;
    }

    public Date getExpiryDate() {

        return expiryDate;
    }
}
