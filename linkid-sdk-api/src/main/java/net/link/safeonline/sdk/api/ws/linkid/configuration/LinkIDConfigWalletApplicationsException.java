package net.link.safeonline.sdk.api.ws.linkid.configuration;

/**
 * Created by wvdhaute
 * Date: 21/01/16
 * Time: 15:31
 */
public class LinkIDConfigWalletApplicationsException extends Exception {

    private final LinkIDConfigWalletApplicationsErrorCode errorCode;

    public LinkIDConfigWalletApplicationsException(final LinkIDConfigWalletApplicationsErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public LinkIDConfigWalletApplicationsErrorCode getErrorCode() {

        return errorCode;
    }
}
