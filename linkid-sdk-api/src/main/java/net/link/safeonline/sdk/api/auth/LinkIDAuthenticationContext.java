/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.auth;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.link.safeonline.sdk.api.callback.LinkIDCallback;
import net.link.safeonline.sdk.api.configuration.LinkIDConfigService;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;


public class LinkIDAuthenticationContext implements Serializable {

    private final String                          applicationName;
    //
    private final Locale                          language;
    //
    private final String                          authenticationMessage;
    private final String                          finishedMessage;
    private final String                          identityProfile;
    private final Long                            sessionExpiryOverride;
    private final String                          theme;
    private final String                          notificationLocation;
    //
    private final String                          mobileLandingSuccess;       // landing page for an authn/payment started on iOS browser
    private final String                          mobileLandingError;         // landing page for an authn/payment started on iOS browser
    private final String                          mobileLandingCancel;        // landing page for an authn/payment started on iOS browser
    //
    private final Map<String, List<Serializable>> attributeSuggestions;
    private final LinkIDPaymentContext            paymentContext;
    private final LinkIDCallback                  callback;

    private LinkIDAuthenticationContext(final Builder builder) {

        this.applicationName = builder.applicationName;
        this.language = builder.language;
        this.authenticationMessage = builder.authenticationMessage;
        this.finishedMessage = builder.finishedMessage;
        this.identityProfile = builder.identityProfile;
        this.sessionExpiryOverride = builder.sessionExpiryOverride;
        this.theme = builder.theme;
        this.notificationLocation = builder.notificationLocation;
        this.mobileLandingSuccess = builder.mobileLandingSuccess;
        this.mobileLandingError = builder.mobileLandingError;
        this.mobileLandingCancel = builder.mobileLandingCancel;
        this.attributeSuggestions = builder.attributeSuggestions;
        this.paymentContext = builder.paymentContext;
        this.callback = builder.callback;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDAuthenticationContext{" +
               "applicationName='" + applicationName + '\'' +
               ", language=" + language +
               ", authenticationMessage='" + authenticationMessage + '\'' +
               ", finishedMessage='" + finishedMessage + '\'' +
               ", identityProfile='" + identityProfile + '\'' +
               ", sessionExpiryOverride=" + sessionExpiryOverride +
               ", theme='" + theme + '\'' +
               ", notificationLocation='" + notificationLocation + '\'' +
               ", mobileLandingSuccess='" + mobileLandingSuccess + '\'' +
               ", mobileLandingError='" + mobileLandingError + '\'' +
               ", mobileLandingCancel='" + mobileLandingCancel + '\'' +
               ", attributeSuggestions=" + attributeSuggestions +
               ", paymentContext=" + paymentContext +
               ", callback=" + callback +
               '}';
    }

    // Builder


    public static class Builder {

        // Required parameters
        private final String applicationName;
        //
        // Optional parameters - initialized to default values
        private Locale                          language              = Locale.ENGLISH;
        private String                          authenticationMessage = null;
        private String                          finishedMessage       = null;
        private String                          identityProfile       = null;
        private Long                            sessionExpiryOverride = null;
        private String                          theme                 = null;
        private String                          notificationLocation  = null;
        private String                          mobileLandingSuccess  = null;
        private String                          mobileLandingError    = null;
        private String                          mobileLandingCancel   = null;
        //
        private Map<String, List<Serializable>> attributeSuggestions  = null;
        private LinkIDPaymentContext            paymentContext        = null;
        private LinkIDCallback                  callback              = null;

        public LinkIDAuthenticationContext build() {

            return new LinkIDAuthenticationContext( this );
        }

        public Builder(final String applicationName) {

            this.applicationName = applicationName;
        }

        public Builder(final LinkIDConfigService linkIDConfigService) {

            this.applicationName = linkIDConfigService.name();
        }

        public Builder language(final Locale language) {

            this.language = language;
            return this;
        }

        public Builder authenticationMessage(final String authenticationMessage) {

            this.authenticationMessage = authenticationMessage;
            return this;
        }

        public Builder finishedMessage(final String finishedMessage) {

            this.finishedMessage = finishedMessage;
            return this;
        }

        public Builder identityProfile(final String identityProfile) {

            this.identityProfile = identityProfile;
            return this;
        }

        public Builder sessionExpiryOverride(final Long sessionExpiryOverride) {

            this.sessionExpiryOverride = sessionExpiryOverride;
            return this;
        }

        public Builder theme(final String theme) {

            this.theme = theme;
            return this;
        }

        public Builder notificationLocation(final String notificationLocation) {

            this.notificationLocation = notificationLocation;
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

        public Builder attributeSuggestions(final Map<String, List<Serializable>> attributeSuggestions) {

            this.attributeSuggestions = attributeSuggestions;
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

    }

    // Accessors

    public String getApplicationName() {

        return applicationName;
    }

    public Locale getLanguage() {

        return language;
    }

    public String getAuthenticationMessage() {

        return authenticationMessage;
    }

    public String getFinishedMessage() {

        return finishedMessage;
    }

    public String getIdentityProfile() {

        return identityProfile;
    }

    public Long getSessionExpiryOverride() {

        return sessionExpiryOverride;
    }

    public String getTheme() {

        return theme;
    }

    public String getNotificationLocation() {

        return notificationLocation;
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

    public Map<String, List<Serializable>> getAttributeSuggestions() {

        return attributeSuggestions;
    }

    public LinkIDPaymentContext getPaymentContext() {

        return paymentContext;
    }

    public LinkIDCallback getCallback() {

        return callback;
    }
}
