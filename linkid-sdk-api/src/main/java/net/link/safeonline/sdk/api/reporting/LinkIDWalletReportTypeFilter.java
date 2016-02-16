package net.link.safeonline.sdk.api.reporting;

import java.io.Serializable;
import java.util.List;


/**
 * Created by wvdhaute
 * Date: 29/04/15
 * Time: 14:32
 */
public class LinkIDWalletReportTypeFilter implements Serializable {

    private final List<LinkIDWalletReportType> types;

    public LinkIDWalletReportTypeFilter(final List<LinkIDWalletReportType> types) {

        this.types = types;
    }

    public List<LinkIDWalletReportType> getTypes() {

        return types;
    }

}
