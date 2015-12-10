package net.link.safeonline.sdk.api.ws.linkid.ltqr;

import java.io.Serializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 10/12/15
 * Time: 10:12
 */
public class LinkIDLTQRPushResponse implements Serializable {

    //
    // success
    @Nullable
    private final LinkIDLTQRSession       ltqrSession;
    //
    // failure
    @Nullable
    private final LinkIDLTQRPushErrorCode errorCode;
    @Nullable
    private final String                  errorMessage;

    public LinkIDLTQRPushResponse(@NotNull final LinkIDLTQRSession ltqrSession) {

        this.ltqrSession = ltqrSession;
        this.errorCode = null;
        this.errorMessage = null;
    }

    public LinkIDLTQRPushResponse(@NotNull final LinkIDLTQRPushErrorCode errorCode, @Nullable final String errorMessage) {

        this.ltqrSession = null;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDLTQRPushResponse{" +
               "ltqrSession=" + ltqrSession +
               ", errorCode=" + errorCode +
               ", errorMessage='" + errorMessage + '\'' +
               '}';
    }

    // Accessors

    @Nullable
    public LinkIDLTQRSession getLtqrSession() {

        return ltqrSession;
    }

    @Nullable
    public LinkIDLTQRPushErrorCode getErrorCode() {

        return errorCode;
    }

    @Nullable
    public String getErrorMessage() {

        return errorMessage;
    }
}
