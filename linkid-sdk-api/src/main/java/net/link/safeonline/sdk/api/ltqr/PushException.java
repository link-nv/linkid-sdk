package net.link.safeonline.sdk.api.ltqr;

/**
 * Created by wvdhaute
 * Date: 14/01/14
 * Time: 11:03
 */
public class PushException extends Exception {

    private final PushErrorCode errorCode;

    public PushException(final PushErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public PushErrorCode getErrorCode() {

        return errorCode;
    }
}
