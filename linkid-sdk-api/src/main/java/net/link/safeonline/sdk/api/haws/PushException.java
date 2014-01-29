package net.link.safeonline.sdk.api.haws;

/**
 * Created by wvdhaute
 * Date: 14/01/14
 * Time: 11:03
 */
public class PushException extends Exception {

    private final ErrorCode errorCode;
    private final String    info;

    public PushException(final ErrorCode errorCode, final String info) {

        this.errorCode = errorCode;
        this.info = info;
    }

    public ErrorCode getErrorCode() {

        return errorCode;
    }

    public String getInfo() {

        return info;
    }
}
