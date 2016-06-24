package net.link.safeonline.sdk.api.reporting;

/**
 * Created by wvdhaute
 * Date: 29/04/15
 * Time: 13:15
 */
public class LinkIDReportException extends Exception {

    private final LinkIDReportErrorCode errorCode;

    public LinkIDReportException(final LinkIDReportErrorCode errorCode) {

        super( String.format( "Error code: \"%s\"", errorCode ) );
        this.errorCode = errorCode;
    }

    public LinkIDReportErrorCode getErrorCode() {

        return errorCode;
    }
}
