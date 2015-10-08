package net.link.safeonline.sdk.api.ws.linkid.ltqr;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import net.link.safeonline.sdk.api.callback.LinkIDCallback;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 19/06/15
 * Time: 10:35
 */
@SuppressWarnings("unused")
public class LinkIDLTQRInfo implements Serializable {

    private final String                         ltqrReference;
    private final String                         sessionId;
    private final Date                           created;
    //
    private final byte[]                         qrCodeImage;
    private final String                         qrCodeURL;
    //
    @Nullable
    private final String                         authenticationMessage;
    @Nullable
    private final String                         finishedMessage;
    //
    private final boolean                        oneTimeUse;
    //
    @Nullable
    private final Date                           expiryDate;
    @Nullable
    private final Long                           expiryDuration;
    //
    @Nullable
    private final LinkIDPaymentContext           paymentContext;
    @Nullable
    private final LinkIDCallback                 callback;
    //
    private final Set<String>                    identityProfiles;
    //
    @Nullable
    private final Long                           sessionExpiryOverride;
    @Nullable
    private final String                         theme;
    //
    @Nullable
    private final String                         mobileLandingSuccess;
    @Nullable
    private final String                         mobileLandingError;
    @Nullable
    private final String                         mobileLandingCancel;
    //
    @Nullable
    private final LinkIDLTQRPollingConfiguration pollingConfiguration;
    //
    private final boolean                        waitForUnlock;
    private final boolean                        locked;
    //
    @Nullable
    private final String                         ltqrStatusLocation;

    public LinkIDLTQRInfo(final String ltqrReference, final String sessionId, final Date created, final byte[] qrCodeImage, final String qrCodeURL,
                          @Nullable final String authenticationMessage, @Nullable final String finishedMessage, final boolean oneTimeUse,
                          @Nullable final Date expiryDate, @Nullable final Long expiryDuration, @Nullable final LinkIDPaymentContext paymentContext,
                          @Nullable final LinkIDCallback callback, final Set<String> identityProfiles, @Nullable final Long sessionExpiryOverride,
                          @Nullable final String theme, @Nullable final String mobileLandingSuccess, @Nullable final String mobileLandingError,
                          @Nullable final String mobileLandingCancel, @Nullable final LinkIDLTQRPollingConfiguration pollingConfiguration,
                          boolean waitForUnlock, boolean locked, @Nullable final String ltqrStatusLocation) {

        this.ltqrReference = ltqrReference;
        this.sessionId = sessionId;
        this.created = created;
        this.qrCodeImage = qrCodeImage;
        this.qrCodeURL = qrCodeURL;
        this.authenticationMessage = authenticationMessage;
        this.finishedMessage = finishedMessage;
        this.oneTimeUse = oneTimeUse;
        this.expiryDate = expiryDate;
        this.expiryDuration = expiryDuration;
        this.paymentContext = paymentContext;
        this.callback = callback;
        this.identityProfiles = identityProfiles;
        this.sessionExpiryOverride = sessionExpiryOverride;
        this.theme = theme;
        this.mobileLandingSuccess = mobileLandingSuccess;
        this.mobileLandingError = mobileLandingError;
        this.mobileLandingCancel = mobileLandingCancel;
        this.pollingConfiguration = pollingConfiguration;
        this.waitForUnlock = waitForUnlock;
        this.locked = locked;
        this.ltqrStatusLocation = ltqrStatusLocation;
    }

    @Override
    public String toString() {

        return "LinkIDLTQRInfo{" +
               "ltqrReference='" + ltqrReference + '\'' +
               ", sessionId='" + sessionId + '\'' +
               ", created=" + created +
               ", qrCodeURL='" + qrCodeURL + '\'' +
               ", authenticationMessage='" + authenticationMessage + '\'' +
               ", finishedMessage='" + finishedMessage + '\'' +
               ", oneTimeUse=" + oneTimeUse +
               ", expiryDate=" + expiryDate +
               ", expiryDuration=" + expiryDuration +
               ", paymentContext=" + paymentContext +
               ", callback=" + callback +
               ", identityProfiles=" + identityProfiles +
               ", sessionExpiryOverride=" + sessionExpiryOverride +
               ", theme='" + theme + '\'' +
               ", mobileLandingSuccess='" + mobileLandingSuccess + '\'' +
               ", mobileLandingError='" + mobileLandingError + '\'' +
               ", mobileLandingCancel='" + mobileLandingCancel + '\'' +
               ", pollingConfiguration='" + pollingConfiguration + '\'' +
               ", waitForUnlock='" + waitForUnlock + '\'' +
               ", locked='" + locked + '\'' +
               ", ltqrStatusLocation='" + ltqrStatusLocation + '\'' +
               '}';
    }

    // Accessors

    public String getLtqrReference() {

        return ltqrReference;
    }

    public String getSessionId() {

        return sessionId;
    }

    public Date getCreated() {

        return created;
    }

    public byte[] getQrCodeImage() {

        return qrCodeImage;
    }

    public String getQrCodeURL() {

        return qrCodeURL;
    }

    @Nullable
    public String getAuthenticationMessage() {

        return authenticationMessage;
    }

    @Nullable
    public String getFinishedMessage() {

        return finishedMessage;
    }

    public boolean isOneTimeUse() {

        return oneTimeUse;
    }

    @Nullable
    public Date getExpiryDate() {

        return expiryDate;
    }

    @Nullable
    public Long getExpiryDuration() {

        return expiryDuration;
    }

    @Nullable
    public LinkIDPaymentContext getPaymentContext() {

        return paymentContext;
    }

    @Nullable
    public LinkIDCallback getCallback() {

        return callback;
    }

    public Set<String> getIdentityProfiles() {

        return identityProfiles;
    }

    @Nullable
    public Long getSessionExpiryOverride() {

        return sessionExpiryOverride;
    }

    @Nullable
    public String getTheme() {

        return theme;
    }

    @Nullable
    public String getMobileLandingSuccess() {

        return mobileLandingSuccess;
    }

    @Nullable
    public String getMobileLandingError() {

        return mobileLandingError;
    }

    @Nullable
    public String getMobileLandingCancel() {

        return mobileLandingCancel;
    }

    @Nullable
    public LinkIDLTQRPollingConfiguration getPollingConfiguration() {

        return pollingConfiguration;
    }

    public boolean isWaitForUnlock() {

        return waitForUnlock;
    }

    public boolean isLocked() {

        return locked;
    }

    @Nullable
    public String getLtqrStatusLocation() {

        return ltqrStatusLocation;
    }
}
