package net.link.safeonline.sdk.api.reporting;

import java.io.Serializable;
import java.util.Date;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 29/04/15
 * Time: 14:32
 */
public class LinkIDReportDateFilter implements Serializable {

    private final Date startDate;
    @Nullable
    private final Date endDate;

    public LinkIDReportDateFilter(final Date startDate, final Date endDate) {

        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Date getStartDate() {

        return startDate;
    }

    @Nullable
    public Date getEndDate() {

        return endDate;
    }
}
