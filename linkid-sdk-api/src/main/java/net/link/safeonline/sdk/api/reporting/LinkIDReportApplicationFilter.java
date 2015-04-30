package net.link.safeonline.sdk.api.reporting;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 29/04/15
 * Time: 14:32
 */
public class LinkIDReportApplicationFilter implements Serializable {

    private final String applicationName;

    public LinkIDReportApplicationFilter(final String applicationName) {

        this.applicationName = applicationName;
    }

    public String getApplicationName() {

        return applicationName;
    }
}
