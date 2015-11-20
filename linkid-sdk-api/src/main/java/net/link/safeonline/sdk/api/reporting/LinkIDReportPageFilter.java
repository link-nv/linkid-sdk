package net.link.safeonline.sdk.api.reporting;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 19/11/15
 * Time: 11:01
 */
public class LinkIDReportPageFilter implements Serializable {

    private final int firstResult;
    private final int maxResults;

    public LinkIDReportPageFilter(final int firstResult, final int maxResults) {

        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    // Helper methods

    public int getPage() {

        return maxResults - firstResult;
    }

    @Override
    public String toString() {

        return "LinkIDReportPageFilter{" +
               "firstResult=" + firstResult +
               ", maxResults=" + maxResults +
               '}';
    }

    // Accessors

    public int getFirstResult() {

        return firstResult;
    }

    public int getMaxResults() {

        return maxResults;
    }
}
