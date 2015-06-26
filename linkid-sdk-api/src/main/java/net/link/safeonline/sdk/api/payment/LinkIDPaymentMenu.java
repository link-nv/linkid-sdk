package net.link.safeonline.sdk.api.payment;

import java.io.Serializable;


/**
 * Payment menu configuration override. If not specified, default that is configured in linkID will be taken
 * <p/>
 * Created by wvdhaute
 * Date: 26/06/15
 * Time: 08:23
 */
public class LinkIDPaymentMenu implements Serializable {

    private final String menuResultSuccess;
    private final String menuResultCanceled;
    private final String menuResultPending;
    private final String menuResultError;

    public LinkIDPaymentMenu(final String menuResultSuccess, final String menuResultCanceled, final String menuResultPending, final String menuResultError) {

        this.menuResultSuccess = menuResultSuccess;
        this.menuResultCanceled = menuResultCanceled;
        this.menuResultPending = menuResultPending;
        this.menuResultError = menuResultError;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDPaymentMenu{" +
               "menuResultSuccess='" + menuResultSuccess + '\'' +
               ", menuResultCanceled='" + menuResultCanceled + '\'' +
               ", menuResultPending='" + menuResultPending + '\'' +
               ", menuResultError='" + menuResultError + '\'' +
               '}';
    }

    // Accessors

    public String getMenuResultSuccess() {

        return menuResultSuccess;
    }

    public String getMenuResultCanceled() {

        return menuResultCanceled;
    }

    public String getMenuResultPending() {

        return menuResultPending;
    }

    public String getMenuResultError() {

        return menuResultError;
    }
}
