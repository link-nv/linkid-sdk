package net.link.safeonline.sdk.api.ltqr;

/**
 * Created by wvdhaute
 * Date: 14/01/14
 * Time: 11:03
 */
public class RemoveException extends Exception {

    private final ErrorCode errorCode;

    public RemoveException(final ErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {

        return errorCode;
    }
}
