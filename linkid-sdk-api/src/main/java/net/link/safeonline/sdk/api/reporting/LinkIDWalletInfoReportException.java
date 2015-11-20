package net.link.safeonline.sdk.api.reporting;

/**
 * Created by wvdhaute
 * Date: 29/04/15
 * Time: 13:15
 */
public class LinkIDWalletInfoReportException extends Exception {

    private final LinkIDWalletInfoReportErrorCode errorCode;

    public LinkIDWalletInfoReportException(final LinkIDWalletInfoReportErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public LinkIDWalletInfoReportErrorCode getErrorCode() {

        return errorCode;
    }
}
