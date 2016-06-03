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
 * Time: 13:03
 */
public class LinkIDVoucherHistory implements Serializable {

    private final List<LinkIDVoucherHistoryEvent> events;
    private final long                            total;

    public LinkIDVoucherHistory(final List<LinkIDVoucherHistoryEvent> events, final long total) {

        this.events = events;
        this.total = total;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDVoucherHistory{" +
               "events=" + events +
               ", total=" + total +
               '}';
    }

    // Accessors

    public List<LinkIDVoucherHistoryEvent> getEvents() {

        return events;
    }

    public long getTotal() {

        return total;
    }
}
