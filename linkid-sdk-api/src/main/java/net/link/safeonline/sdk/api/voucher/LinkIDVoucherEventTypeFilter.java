/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.voucher;

import java.io.Serializable;
import java.util.List;


/**
 * Created by wvdhaute
 * Date: 02/06/16
 * Time: 13:10
 */
public class LinkIDVoucherEventTypeFilter implements Serializable {

    private final List<LinkIDVoucherHistoryEventType> eventTypes;

    public LinkIDVoucherEventTypeFilter(final List<LinkIDVoucherHistoryEventType> eventTypes) {

        this.eventTypes = eventTypes;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDVoucherEventTypeFilter{" +
               "eventTypes=" + eventTypes +
               '}';
    }

    // Accessors

    public List<LinkIDVoucherHistoryEventType> getEventTypes() {

        return eventTypes;
    }
}
