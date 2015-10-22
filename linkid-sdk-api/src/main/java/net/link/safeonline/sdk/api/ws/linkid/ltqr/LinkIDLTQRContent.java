package net.link.safeonline.sdk.api.ws.linkid.ltqr;

import java.io.Serializable;
import java.util.Date;
import net.link.safeonline.sdk.api.callback.LinkIDCallback;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;


/**
 * Created by wvdhaute
 * Date: 08/10/15
 * Time: 13:02
 */
public class LinkIDLTQRContent implements Serializable {

    private final String                         authenticationMessage;
    private final String                         finishedMessage;
    private final LinkIDPaymentContext           paymentContext;
    private final LinkIDCallback                 callback;
    private final String                         identityProfile;
    private final long                           sessionExpiryOverride;
    private final String                         theme;
    private final String                         mobileLandingSuccess;
    private final String                         mobileLandingError;
    private final String                         mobileLandingCancel;
    private final LinkIDLTQRPollingConfiguration pollingConfiguration;
    private final String                         ltqrStatusLocation;
    private final Date                           expiryDate;
    private final long                           expiryDuration;
    private final boolean                        waitForUnblock;

    private LinkIDLTQRContent(final Builder builder) {

        // initialize
        this.authenticationMessage = builder.authenticationMessage;
        this.finishedMessage = builder.finishedMessage;
        this.paymentContext = builder.paymentContext;
        this.callback = builder.callback;
        this.identityProfile = builder.identityProfile;
        this.sessionExpiryOverride = builder.sessionExpiryOverride;
        this.theme = builder.theme;
        this.mobileLandingSuccess = builder.mobileLandingSuccess;
        this.mobileLandingError = builder.mobileLandingError;
        this.mobileLandingCancel = builder.mobileLandingCancel;
        this.pollingConfiguration = builder.pollingConfiguration;
        this.ltqrStatusLocation = builder.ltqrStatusLocation;
        this.expiryDate = builder.expiryDate;
        this.expiryDuration = builder.expiryDuration;
        this.waitForUnblock = builder.waitForUnblock;

    }

    @Override
    public String toString() {

        return "LinkIDLTQRContent{" +
               "authenticationMessage='" + authenticationMessage + '\'' +
               ", finishedMessage='" + finishedMessage + '\'' +
               ", paymentContext=" + paymentContext +
               ", callback=" + callback +
               ", identityProfile=" + identityProfile +
               ", sessionExpiryOverride=" + sessionExpiryOverride +
               ", theme='" + theme + '\'' +
               ", mobileLandingSuccess='" + mobileLandingSuccess + '\'' +
               ", mobileLandingError='" + mobileLandingError + '\'' +
               ", mobileLandingCancel='" + mobileLandingCancel + '\'' +
               ", pollingConfiguration=" + pollingConfiguration +
               ", ltqrStatusLocation='" + ltqrStatusLocation + '\'' +
               ", expiryDate=" + expiryDate +
               ", expiryDuration=" + expiryDuration +
               ", waitForUnblock=" + waitForUnblock +
               '}';
    }

    // Builder


    public static class Builder {

        // Optional parameters
        private String                         authenticationMessage = null;
        private String                         finishedMessage       = null;
        private LinkIDPaymentContext           paymentContext        = null;
        private LinkIDCallback                 callback              = null;
        private String                         identityProfile       = null;
        private long                           sessionExpiryOverride = -1;
        private String                         theme                 = null;
        private String                         mobileLandingSuccess  = null;
        private String                         mobileLandingError    = null;
        private String                         mobileLandingCancel   = null;
        private LinkIDLTQRPollingConfiguration pollingConfiguration  = null;
        private String                         ltqrStatusLocation    = null;
        private Date                           expiryDate            = null;
        private long                           expiryDuration        = -1;
        private boolean                        waitForUnblock        = false;

        public LinkIDLTQRContent build() {

            return new LinkIDLTQRContent( this );
        }

        public Builder() {
            // no required parameters
        }

        public Builder authenticationMessage(final String authenticationMessage) {

            this.authenticationMessage = authenticationMessage;
            return this;
        }

        public Builder finishedMessage(final String finishedMessage) {

            this.finishedMessage = finishedMessage;
            return this;
        }

        public Builder paymentContext(final LinkIDPaymentContext paymentContext) {

            this.paymentContext = paymentContext;
            return this;
        }

        public Builder callback(final LinkIDCallback callback) {

            this.callback = callback;
            return this;
        }

        public Builder identityProfile(final String identityProfile) {

            this.identityProfile = identityProfile;
            return this;
        }

        public Builder sessionExpiryOverride(final long sessionExpiryOverride) {

            this.sessionExpiryOverride = sessionExpiryOverride;
            return this;
        }

        public Builder theme(final String theme) {

            this.theme = theme;
            return this;
        }

        public Builder mobileLandingSuccess(final String mobileLandingSuccess) {

            this.mobileLandingSuccess = mobileLandingSuccess;
            return this;
        }

        public Builder mobileLandingError(final String mobileLandingError) {

            this.mobileLandingError = mobileLandingError;
            return this;
        }

        public Builder mobileLandingCancel(final String mobileLandingCancel) {

            this.mobileLandingCancel = mobileLandingCancel;
            return this;
        }

        public Builder pollingConfiguration(final LinkIDLTQRPollingConfiguration pollingConfiguration) {

            this.pollingConfiguration = pollingConfiguration;
            return this;
        }

        public Builder ltqrStatusLocation(final String ltqrStatusLocation) {

            this.ltqrStatusLocation = ltqrStatusLocation;
            return this;
        }

        public Builder expiryDate(final Date expiryDate) {

            this.expiryDate = expiryDate;
            return this;
        }

        public Builder expiryDuration(final long expiryDuration) {

            this.expiryDuration = expiryDuration;
            return this;
        }

        public Builder waitForUnblock(final boolean waitForUnblock) {

            this.waitForUnblock = waitForUnblock;
            return this;
        }

    }

    // Accessors

    public String getAuthenticationMessage() {

        return authenticationMessage;
    }

    public String getFinishedMessage() {

        return finishedMessage;
    }

    public LinkIDPaymentContext getPaymentContext() {

        return paymentContext;
    }

    public LinkIDCallback getCallback() {

        return callback;
    }

    public String getIdentityProfile() {

        return identityProfile;
    }

    public long getSessionExpiryOverride() {

        return sessionExpiryOverride;
    }

    public String getTheme() {

        return theme;
    }

    public String getMobileLandingSuccess() {

        return mobileLandingSuccess;
    }

    public String getMobileLandingError() {

        return mobileLandingError;
    }

    public String getMobileLandingCancel() {

        return mobileLandingCancel;
    }

    public LinkIDLTQRPollingConfiguration getPollingConfiguration() {

        return pollingConfiguration;
    }

    public String getLtqrStatusLocation() {

        return ltqrStatusLocation;
    }

    public Date getExpiryDate() {

        return expiryDate;
    }

    public long getExpiryDuration() {

        return expiryDuration;
    }

    public boolean isWaitForUnblock() {

        return waitForUnblock;
    }
}
