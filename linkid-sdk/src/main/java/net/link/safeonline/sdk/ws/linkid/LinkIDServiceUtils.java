package net.link.safeonline.sdk.ws.linkid;

import com.google.common.collect.Maps;
import java.util.Date;
import java.util.Map;
import javax.xml.datatype.XMLGregorianCalendar;
import net.lin_k.linkid._3.AuthCancelErrorCode;
import net.lin_k.linkid._3.AuthPollErrorCode;
import net.lin_k.linkid._3.AuthStartErrorCode;
import net.lin_k.linkid._3.Callback;
import net.lin_k.linkid._3.ConfigLocalizationKeyType;
import net.lin_k.linkid._3.ConfigLocalizedImage;
import net.lin_k.linkid._3.ConfigLocalizedImages;
import net.lin_k.linkid._3.Currency;
import net.lin_k.linkid._3.LTQRContent;
import net.lin_k.linkid._3.LTQRLockType;
import net.lin_k.linkid._3.LTQRPollingConfiguration;
import net.lin_k.linkid._3.PaymentContext;
import net.lin_k.linkid._3.PaymentMethodType;
import net.lin_k.linkid._3.PaymentStatusType;
import net.lin_k.linkid._3.QRCodeInfo;
import net.link.safeonline.sdk.api.LinkIDConstants;
import net.link.safeonline.sdk.api.callback.LinkIDCallback;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentAmount;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentMandate;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentMethodType;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentState;
import net.link.safeonline.sdk.api.qr.LinkIDQRInfo;
import net.link.safeonline.sdk.api.reporting.LinkIDReportErrorCode;
import net.link.safeonline.sdk.api.ws.callback.LinkIDCallbackPullErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthCancelErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthPollErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.capture.LinkIDCaptureErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalizationErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalizationKeyType;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalizedImage;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalizedImages;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDThemesErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRChangeErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRContent;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRLockType;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRPollingConfiguration;
import net.link.safeonline.sdk.api.ws.linkid.mandate.LinkIDMandatePaymentErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDPaymentStatusErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletAddCreditErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletCommitErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletEnrollErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletGetInfoErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletReleaseErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletRemoveCreditErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletRemoveErrorCode;
import net.link.safeonline.sdk.ws.LinkIDSDKUtils;
import net.link.util.InternalInconsistencyException;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 08/10/15
 * Time: 13:43
 */
public class LinkIDServiceUtils {

    private LinkIDServiceUtils() {

        throw new AssertionError();
    }

    public static LinkIDAuthErrorCode convert(final AuthStartErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_REQUEST_INVALID:
                return LinkIDAuthErrorCode.ERROR_REQUEST_INVALID;
            case ERROR_MAINTENANCE:
                return LinkIDAuthErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDAuthPollErrorCode convert(final AuthPollErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_RESPONSE_INVALID_SESSION_ID:
                return LinkIDAuthPollErrorCode.ERROR_RESPONSE_INVALID_SESSION_ID;
            case ERROR_MAINTENANCE:
                return LinkIDAuthPollErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDAuthCancelErrorCode convert(final AuthCancelErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_INVALID_SESSION_ID:
                return LinkIDAuthCancelErrorCode.ERROR_INVALID_SESSION_ID;
            case ERROR_PERMISSION_DENIED:
                return LinkIDAuthCancelErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDAuthCancelErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDAuthCancelErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDLocalizationKeyType convert(final ConfigLocalizationKeyType type) {

        switch (type) {

            case LOCALIZATION_KEY_FRIENDLY:
                return LinkIDLocalizationKeyType.FRIENDLY;
            case LOCALIZATION_KEY_FRIENDLY_MULTIPLE:
                return LinkIDLocalizationKeyType.FRIENDLY_MULTIPLE;
            case LOCALIZATION_KEY_DESCRIPTION:
                return LinkIDLocalizationKeyType.DESCRIPTION;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected key type %s!", type.name() ) );
    }

    public static LinkIDLocalizedImages convert(final ConfigLocalizedImages localizedImages) {

        if (null == localizedImages)
            return null;

        Map<String, LinkIDLocalizedImage> imageMap = Maps.newHashMap();
        for (ConfigLocalizedImage localizedImage : localizedImages.getImages()) {
            imageMap.put( localizedImage.getLanguage(), new LinkIDLocalizedImage( localizedImage.getUrl(), localizedImage.getLanguage() ) );
        }
        return new LinkIDLocalizedImages( imageMap );
    }

    public static LinkIDThemesErrorCode convert(final net.lin_k.linkid._3.ConfigThemesErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_APPLICATION:
                return LinkIDThemesErrorCode.ERROR_UNKNOWN_APPLICATION;
            case ERROR_MAINTENANCE:
                return LinkIDThemesErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDLocalizationErrorCode convert(final net.lin_k.linkid._3.ConfigLocalizationErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNEXPECTED:
                return LinkIDLocalizationErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDLocalizationErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDCallbackPullErrorCode convert(final net.lin_k.linkid._3.CallbackPullErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_RESPONSE_INVALID_SESSION_ID:
                return LinkIDCallbackPullErrorCode.ERROR_RESPONSE_INVALID_SESSION_ID;
            case ERROR_UNEXPECTED:
                return LinkIDCallbackPullErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDCallbackPullErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDCaptureErrorCode convert(final net.lin_k.linkid._3.PaymentCaptureErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CAPTURE_UNKNOWN:
                return LinkIDCaptureErrorCode.ERROR_CAPTURE_UNKNOWN;
            case ERROR_CAPTURE_FAILED:
                return LinkIDCaptureErrorCode.ERROR_CAPTURE_FAILED;
            case ERROR_CAPTURE_TOKEN_NOT_FOUND:
                return LinkIDCaptureErrorCode.ERROR_CAPTURE_TOKEN_NOT_FOUND;
            case ERROR_MAINTENANCE:
                return LinkIDCaptureErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    @Nullable
    public static PaymentContext convert(@Nullable final LinkIDPaymentContext linkIDPaymentContext) {

        if (null == linkIDPaymentContext) {
            return null;
        }

        PaymentContext paymentContext = new PaymentContext();
        paymentContext.setAmount( linkIDPaymentContext.getAmount().getAmount() );
        if (null != linkIDPaymentContext.getAmount().getCurrency()) {
            paymentContext.setCurrency( convert( linkIDPaymentContext.getAmount().getCurrency() ) );
        }
        if (null != linkIDPaymentContext.getAmount().getWalletCoin()) {
            paymentContext.setWalletCoin( linkIDPaymentContext.getAmount().getWalletCoin() );
        }
        paymentContext.setDescription( linkIDPaymentContext.getDescription() );
        paymentContext.setOrderReference( linkIDPaymentContext.getOrderReference() );
        paymentContext.setPaymentProfile( linkIDPaymentContext.getPaymentProfile() );
        paymentContext.setValidationTime( linkIDPaymentContext.getPaymentValidationTime() );
        paymentContext.setAllowPartial( linkIDPaymentContext.isAllowPartial() );
        paymentContext.setOnlyWallets( linkIDPaymentContext.isOnlyWallets() );
        paymentContext.setMandate( null != linkIDPaymentContext.getMandate() );
        if (null != linkIDPaymentContext.getMandate()) {
            paymentContext.setMandateDescription( linkIDPaymentContext.getMandate().getDescription() );
            paymentContext.setMandateReference( linkIDPaymentContext.getMandate().getReference() );
        }
        paymentContext.setPaymentStatusLocation( linkIDPaymentContext.getPaymentStatusLocation() );

        return paymentContext;
    }

    @Nullable
    public static Callback convert(@Nullable final LinkIDCallback linkIDCallback) {

        if (null == linkIDCallback) {
            return null;
        }

        Callback callback = new Callback();

        callback.setLocation( linkIDCallback.getLocation() );
        callback.setAppSessionId( linkIDCallback.getAppSessionId() );
        callback.setInApp( linkIDCallback.isInApp() );

        return callback;
    }

    public static Currency convert(final LinkIDCurrency currency) {

        if (null == currency)
            return null;

        switch (currency) {

            case EUR:
                return Currency.EUR;
        }

        throw new InternalInconsistencyException( String.format( "Unsupported currency: \"%s\"", currency.name() ) );
    }

    public static LinkIDCurrency convert(final Currency currency) {

        if (null == currency)
            return null;

        switch (currency) {

            case EUR:
                return LinkIDCurrency.EUR;
        }

        throw new InternalInconsistencyException( String.format( "Unsupported currency: \"%s\"", currency.name() ) );
    }

    public static LinkIDPaymentState convert(final PaymentStatusType paymentState) {

        if (null == paymentState)
            return null;

        switch (paymentState) {

            case STARTED:
                return LinkIDPaymentState.STARTED;
            case AUTHORIZED:
                return LinkIDPaymentState.PAYED;
            case FAILED:
                return LinkIDPaymentState.FAILED;
            case REFUNDED:
                return LinkIDPaymentState.REFUNDED;
            case REFUND_STARTED:
                return LinkIDPaymentState.REFUND_STARTED;
            case WAITING_FOR_UPDATE:
                return LinkIDPaymentState.WAITING_FOR_UPDATE;
        }

        throw new InternalInconsistencyException( String.format( "Unsupported payment state: \"%s\"", paymentState.name() ) );
    }

    @Nullable
    public static LTQRPollingConfiguration convert(@Nullable final LinkIDLTQRPollingConfiguration pollingConfiguration) {

        if (null != pollingConfiguration) {
            LTQRPollingConfiguration wsPollingConfiguration = new LTQRPollingConfiguration();

            if (pollingConfiguration.getPollAttempts() > LinkIDConstants.LINKID_LTQR_POLLING_ATTEMPTS_MINIMUM) {
                wsPollingConfiguration.setPollAttempts( pollingConfiguration.getPollAttempts() );
            }
            if (pollingConfiguration.getPollInterval() > LinkIDConstants.LINKID_LTQR_POLLING_INTERVAL_MINIMUM) {
                wsPollingConfiguration.setPollInterval( pollingConfiguration.getPollInterval() );
            }
            if (pollingConfiguration.getPaymentPollAttempts() > LinkIDConstants.LINKID_LTQR_POLLING_ATTEMPTS_MINIMUM) {
                wsPollingConfiguration.setPaymentPollAttempts( pollingConfiguration.getPaymentPollAttempts() );
            }
            if (pollingConfiguration.getPaymentPollInterval() > LinkIDConstants.LINKID_LTQR_POLLING_INTERVAL_MINIMUM) {
                wsPollingConfiguration.setPaymentPollInterval( pollingConfiguration.getPaymentPollInterval() );
            }

            return wsPollingConfiguration;
        }

        return null;

    }

    public static LTQRLockType convert(final LinkIDLTQRLockType lockType) {

        if (null == lockType)
            return null;

        switch (lockType) {

            case NEVER:
                return LTQRLockType.NEVER;
            case ON_SCAN:
                return LTQRLockType.ON_SCAN;
            case ON_FINISH:
                return LTQRLockType.ON_FINISH;
        }

        throw new InternalInconsistencyException( String.format( "Unsupported LTQR lock type: \"%s\"", lockType.name() ) );
    }

    public static LinkIDLTQRLockType convert(final LTQRLockType lockType) {

        if (null == lockType)
            return null;

        switch (lockType) {

            case NEVER:
                return LinkIDLTQRLockType.NEVER;
            case ON_SCAN:
                return LinkIDLTQRLockType.ON_SCAN;
            case ON_FINISH:
                return LinkIDLTQRLockType.ON_FINISH;
        }

        throw new InternalInconsistencyException( String.format( "Unsupported LTQR lock type: \"%s\"", lockType.name() ) );
    }

    public static LTQRContent convert(final LinkIDLTQRContent content) {

        LTQRContent ltqrContent = new LTQRContent();

        // custom msgs
        ltqrContent.setAuthenticationMessage( content.getAuthenticationMessage() );
        ltqrContent.setFinishedMessage( content.getFinishedMessage() );

        // payment context
        ltqrContent.setPaymentContext( LinkIDServiceUtils.convert( content.getPaymentContext() ) );

        // callback
        ltqrContent.setCallback( LinkIDServiceUtils.convert( content.getCallback() ) );

        // identity profile
        ltqrContent.setIdentityProfile( content.getIdentityProfile() );

        if (content.getSessionExpiryOverride() > 0) {
            ltqrContent.setSessionExpiryOverride( content.getSessionExpiryOverride() );
        }
        ltqrContent.setTheme( content.getTheme() );
        ltqrContent.setMobileLandingSuccess( content.getMobileLandingSuccess() );
        ltqrContent.setMobileLandingError( content.getMobileLandingError() );
        ltqrContent.setMobileLandingCancel( content.getMobileLandingCancel() );

        // polling configuration
        ltqrContent.setPollingConfiguration( LinkIDServiceUtils.convert( content.getPollingConfiguration() ) );

        // configuration
        if (null != content.getExpiryDate()) {
            ltqrContent.setExpiryDate( LinkIDSDKUtils.convert( content.getExpiryDate() ) );
        }
        if (content.getExpiryDuration() > 0) {
            ltqrContent.setExpiryDuration( content.getExpiryDuration() );
        }
        ltqrContent.setWaitForUnblock( content.isWaitForUnblock() );
        if (null != content.getLtqrStatusLocation()) {
            ltqrContent.setLtqrStatusLocation( content.getLtqrStatusLocation() );
        }

        return ltqrContent;
    }

    public static LinkIDLTQRContent convert(final LTQRContent ltqrContent) {

        LinkIDLTQRContent.Builder builder = new LinkIDLTQRContent.Builder();

        // custom msgs
        builder.authenticationMessage( ltqrContent.getAuthenticationMessage() );
        builder.finishedMessage( ltqrContent.getFinishedMessage() );

        // payment context
        PaymentContext wsPaymentContext = ltqrContent.getPaymentContext();
        if (null != wsPaymentContext) {

            LinkIDPaymentContext.Builder paymentContextBuilder = new LinkIDPaymentContext.Builder(
                    new LinkIDPaymentAmount( wsPaymentContext.getAmount(), LinkIDServiceUtils.convert( wsPaymentContext.getCurrency() ),
                            wsPaymentContext.getWalletCoin() ) ).description( wsPaymentContext.getDescription() )
                                                                .orderReference( wsPaymentContext.getOrderReference() )
                                                                .paymentProfile( wsPaymentContext.getPaymentProfile() )
                                                                .paymentValidationTime( wsPaymentContext.getValidationTime() )
                                                                .allowPartial( convert( wsPaymentContext.isAllowPartial() ) )
                                                                .onlyWallets( convert( wsPaymentContext.isOnlyWallets() ) )
                                                                .paymentStatusLocation( wsPaymentContext.getPaymentStatusLocation() );

            if (convert( wsPaymentContext.isMandate() )) {
                paymentContextBuilder.mandate( new LinkIDPaymentMandate( wsPaymentContext.getDescription(), wsPaymentContext.getMandateReference() ) );
            }

            builder.paymentContext( paymentContextBuilder.build() );
        }

        // callback
        if (null != ltqrContent.getCallback()) {
            builder.callback( new LinkIDCallback( ltqrContent.getCallback().getLocation(), ltqrContent.getCallback().getAppSessionId(),
                    convert( ltqrContent.getCallback().isInApp() ) ) );
        }

        // identity profile
        builder.identityProfile( ltqrContent.getIdentityProfile() );

        if (convert( ltqrContent.getSessionExpiryOverride() ) > 0) {
            builder.sessionExpiryOverride( ltqrContent.getSessionExpiryOverride() );
        }
        builder.theme( ltqrContent.getTheme() );
        builder.mobileLandingSuccess( ltqrContent.getMobileLandingSuccess() );
        builder.mobileLandingError( ltqrContent.getMobileLandingError() );
        builder.mobileLandingCancel( ltqrContent.getMobileLandingCancel() );

        // polling configuration
        builder.pollingConfiguration( LinkIDServiceUtils.getPollingConfiguration( ltqrContent.getPollingConfiguration() ) );

        // configuration
        if (null != ltqrContent.getExpiryDate()) {
            builder.expiryDate( ltqrContent.getExpiryDate().toGregorianCalendar().getTime() );
        }
        if (convert( ltqrContent.getExpiryDuration() ) > 0) {
            builder.expiryDuration( ltqrContent.getExpiryDuration() );
        }
        builder.waitForUnblock( ltqrContent.isWaitForUnblock() );
        builder.ltqrStatusLocation( ltqrContent.getLtqrStatusLocation() );

        return builder.build();
    }

    public static LinkIDLTQRErrorCode convert(final net.lin_k.linkid._3.LTQRErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CREDENTIALS_INVALID:
                return LinkIDLTQRErrorCode.ERROR_CREDENTIALS_INVALID;
            case ERROR_CONTEXT_INVALID:
                return LinkIDLTQRErrorCode.ERROR_CONTEXT_INVALID;
            case ERROR_UNEXPECTED:
                return LinkIDLTQRErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDLTQRErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDLTQRChangeErrorCode convert(final net.lin_k.linkid._3.LTQRChangeErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CREDENTIALS_INVALID:
                return LinkIDLTQRChangeErrorCode.ERROR_CREDENTIALS_INVALID;
            case ERROR_NOT_FOUND:
                return LinkIDLTQRChangeErrorCode.ERROR_NOT_FOUND;
            case ERROR_UNEXPECTED:
                return LinkIDLTQRChangeErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDLTQRChangeErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static byte[] decodeQR(final String encodedQR) {

        // convert base64 encoded QR image
        byte[] qrCodeImage;
        try {
            qrCodeImage = Base64.decode( encodedQR );
        }
        catch (Base64DecodingException e) {
            throw new InternalInconsistencyException( "Could not decode the QR image!" );
        }
        return qrCodeImage;
    }

    public static LinkIDLTQRPollingConfiguration getPollingConfiguration(@Nullable final LTQRPollingConfiguration pollingConfiguration) {

        if (null == pollingConfiguration) {
            return null;
        }

        return new LinkIDLTQRPollingConfiguration(
                null != pollingConfiguration.getPollAttempts() && pollingConfiguration.getPollAttempts() > LinkIDConstants.LINKID_LTQR_POLLING_ATTEMPTS_MINIMUM
                        ? pollingConfiguration.getPollAttempts(): -1,
                null != pollingConfiguration.getPollInterval() && pollingConfiguration.getPollInterval() > LinkIDConstants.LINKID_LTQR_POLLING_INTERVAL_MINIMUM
                        ? pollingConfiguration.getPollInterval(): -1, null != pollingConfiguration.getPaymentPollAttempts()
                                                                      && pollingConfiguration.getPaymentPollAttempts()
                                                                         > LinkIDConstants.LINKID_LTQR_POLLING_ATTEMPTS_MINIMUM
                ? pollingConfiguration.getPaymentPollAttempts(): -1, null != pollingConfiguration.getPaymentPollInterval()
                                                                     && pollingConfiguration.getPaymentPollInterval()
                                                                        > LinkIDConstants.LINKID_LTQR_POLLING_INTERVAL_MINIMUM
                ? pollingConfiguration.getPaymentPollInterval(): -1 );

    }

    public static LinkIDPaymentMethodType convert(final PaymentMethodType paymentMethodType) {

        if (null == paymentMethodType)
            return null;

        switch (paymentMethodType) {

            case UNKNOWN:
                return LinkIDPaymentMethodType.UNKNOWN;
            case VISA:
                return LinkIDPaymentMethodType.VISA;
            case MASTERCARD:
                return LinkIDPaymentMethodType.MASTERCARD;
            case SEPA:
                return LinkIDPaymentMethodType.SEPA;
            case KLARNA:
                return LinkIDPaymentMethodType.KLARNA;
        }

        return LinkIDPaymentMethodType.UNKNOWN;
    }

    public static Date convert(XMLGregorianCalendar xmlDate) {

        return null != xmlDate? xmlDate.toGregorianCalendar().getTime(): null;
    }

    public static LinkIDReportErrorCode convert(final net.lin_k.linkid._3.ReportErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_TOO_MANY_RESULTS:
                return LinkIDReportErrorCode.ERROR_TOO_MANY_RESULTS;
            case ERROR_UNEXPECTED:
                return LinkIDReportErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDReportErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDMandatePaymentErrorCode convert(final net.lin_k.linkid._3.MandatePaymentErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_MANDATE_ARCHIVED:
                return LinkIDMandatePaymentErrorCode.ERROR_MANDATE_ARCHIVED;
            case ERROR_MANDATE_UNKNOWN:
                return LinkIDMandatePaymentErrorCode.ERROR_MANDATE_UNKNOWN;
            case ERROR_MANDATE_PAYMENT_FAILED:
                return LinkIDMandatePaymentErrorCode.ERROR_MANDATE_PAYMENT_FAILED;
            case ERROR_UNEXPECTED:
                return LinkIDMandatePaymentErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDMandatePaymentErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDPaymentStatusErrorCode convert(final net.lin_k.linkid._3.PaymentStatusErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_NOT_FOUND:
                return LinkIDPaymentStatusErrorCode.ERROR_NOT_FOUND;
            case ERROR_MULTIPLE_ORDERS_FOUND:
                return LinkIDPaymentStatusErrorCode.ERROR_MULTIPLE_ORDERS_FOUND;
            case ERROR_UNEXPECTED:
                return LinkIDPaymentStatusErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDPaymentStatusErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDWalletEnrollErrorCode convert(final net.lin_k.linkid._3.WalletEnrollErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletEnrollErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_WALLET_INVALID_CURRENCY:
                return LinkIDWalletEnrollErrorCode.ERROR_WALLET_INVALID_CURRENCY;
            case ERROR_UNKNOWN_WALLET_COIN:
                return LinkIDWalletEnrollErrorCode.ERROR_UNKNOWN_WALLET_COIN;
            case ERROR_UNKNOWN_USER:
                return LinkIDWalletEnrollErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_USER_ALREADY_ENROLLED:
                return LinkIDWalletEnrollErrorCode.ERROR_USER_ALREADY_ENROLLED;
            case ERROR_UNEXPECTED:
                return LinkIDWalletEnrollErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletEnrollErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDWalletGetInfoErrorCode convert(final net.lin_k.linkid._3.WalletGetInfoErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletGetInfoErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_UNKNOWN_USER:
                return LinkIDWalletGetInfoErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNEXPECTED:
                return LinkIDWalletGetInfoErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletGetInfoErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDWalletAddCreditErrorCode convert(final net.lin_k.linkid._3.WalletAddCreditErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletAddCreditErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_WALLET_INVALID_CURRENCY:
                return LinkIDWalletAddCreditErrorCode.ERROR_WALLET_INVALID_CURRENCY;
            case ERROR_UNKNOWN_WALLET_COIN:
                return LinkIDWalletAddCreditErrorCode.ERROR_UNKNOWN_WALLET_COIN;
            case ERROR_UNKNOWN_USER:
                return LinkIDWalletAddCreditErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNEXPECTED:
                return LinkIDWalletAddCreditErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletAddCreditErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDWalletRemoveCreditErrorCode convert(final net.lin_k.linkid._3.WalletRemoveCreditErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletRemoveCreditErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_WALLET_INVALID_CURRENCY:
                return LinkIDWalletRemoveCreditErrorCode.ERROR_WALLET_INVALID_CURRENCY;
            case ERROR_UNKNOWN_WALLET_COIN:
                return LinkIDWalletRemoveCreditErrorCode.ERROR_UNKNOWN_WALLET_COIN;
            case ERROR_UNKNOWN_USER:
                return LinkIDWalletRemoveCreditErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNEXPECTED:
                return LinkIDWalletRemoveCreditErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletRemoveCreditErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDWalletRemoveErrorCode convert(final net.lin_k.linkid._3.WalletRemoveErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletRemoveErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_UNKNOWN_USER:
                return LinkIDWalletRemoveErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNEXPECTED:
                return LinkIDWalletRemoveErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletRemoveErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDWalletCommitErrorCode convert(final net.lin_k.linkid._3.WalletCommitErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_USER:
                return LinkIDWalletCommitErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletCommitErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_UNKNOWN_WALLET_TRANSACTION:
                return LinkIDWalletCommitErrorCode.ERROR_UNKNOWN_WALLET_TRANSACTION;
            case ERROR_UNEXPECTED:
                return LinkIDWalletCommitErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletCommitErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDWalletReleaseErrorCode convert(final net.lin_k.linkid._3.WalletReleaseErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_USER:
                return LinkIDWalletReleaseErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletReleaseErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_UNKNOWN_WALLET_TRANSACTION:
                return LinkIDWalletReleaseErrorCode.ERROR_UNKNOWN_WALLET_TRANSACTION;
            case ERROR_UNEXPECTED:
                return LinkIDWalletReleaseErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletReleaseErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static boolean convert(@Nullable final Boolean b) {

        return null != b? b: false;
    }

    public static long convert(@Nullable final Long l) {

        return null != l? l: 0;
    }

    public static LinkIDQRInfo convert(final QRCodeInfo qrCodeInfo) {

        return new LinkIDQRInfo( decodeQR( qrCodeInfo.getQrEncoded() ), qrCodeInfo.getQrEncoded(), qrCodeInfo.getQrURL(), qrCodeInfo.getQrContent(),
                qrCodeInfo.isMobile() );

    }
}
