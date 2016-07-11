/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.themes;

import java.io.Serializable;
import net.link.safeonline.sdk.api.common.LinkIDRequestStatusCode;


/**
 * Created by wvdhaute
 * Date: 17/12/15
 * Time: 11:21
 */
public class LinkIDThemeStatus implements Serializable {

    private final LinkIDRequestStatusCode      status;
    private final String                       infoMessage;
    private final LinkIDThemeStatusErrorReport statusReport;

    public LinkIDThemeStatus(final LinkIDRequestStatusCode status, final String infoMessage, final LinkIDThemeStatusErrorReport statusReport) {

        this.status = status;
        this.infoMessage = infoMessage;
        this.statusReport = statusReport;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDThemeStatus{" +
               "status=" + status +
               ", infoMessage='" + infoMessage + '\'' +
               ", statusReport=" + statusReport +
               '}';
    }

    // Accessors

    public LinkIDRequestStatusCode getStatus() {

        return status;
    }

    public String getInfoMessage() {

        return infoMessage;
    }

    public LinkIDThemeStatusErrorReport getStatusReport() {

        return statusReport;
    }
}
