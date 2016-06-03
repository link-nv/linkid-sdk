/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.voucher;

import java.io.Serializable;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 02/06/16
 * Time: 13:03
 */
public class LinkIDVoucherHistoryEvent implements Serializable {

    private final String                        id;
    private final String                        voucherOrganizationId;
    private final String                        userId;
    @Nullable
    private final String                        voucherId;
    private final long                          points;
    private final String                        applicationName;
    private final LinkIDVoucherHistoryEventType eventType;

    public LinkIDVoucherHistoryEvent(final String id, final String voucherOrganizationId, final String userId, @Nullable final String voucherId,
                                     final long points, final String applicationName, final LinkIDVoucherHistoryEventType eventType) {

        this.id = id;
        this.voucherOrganizationId = voucherOrganizationId;
        this.userId = userId;
        this.voucherId = voucherId;
        this.points = points;
        this.applicationName = applicationName;
        this.eventType = eventType;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDVoucherHistoryEvent{" +
               "id='" + id + '\'' +
               ", voucherOrganizationId='" + voucherOrganizationId + '\'' +
               ", userId='" + userId + '\'' +
               ", voucherId='" + voucherId + '\'' +
               ", points=" + points +
               ", applicationName='" + applicationName + '\'' +
               ", eventType=" + eventType +
               '}';
    }

    // Accessors

    public String getId() {

        return id;
    }

    public String getVoucherOrganizationId() {

        return voucherOrganizationId;
    }

    public String getUserId() {

        return userId;
    }

    @Nullable
    public String getVoucherId() {

        return voucherId;
    }

    public long getPoints() {

        return points;
    }

    public String getApplicationName() {

        return applicationName;
    }

    public LinkIDVoucherHistoryEventType getEventType() {

        return eventType;
    }
}
