package net.link.safeonline.sdk.ws.linkid;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import net.lin_k.linkid._3_1.core.*;
import net.lin_k.safe_online.ltqr._5.PollingConfiguration;
import net.link.safeonline.sdk.api.LinkIDConstants;
import net.link.safeonline.sdk.api.callback.LinkIDCallback;
import net.link.safeonline.sdk.api.common.LinkIDApplicationFilter;
import net.link.safeonline.sdk.api.common.LinkIDRequestStatusCode;
import net.link.safeonline.sdk.api.common.LinkIDUserFilter;
import net.link.safeonline.sdk.api.credentials.LinkIDCredentialType;
import net.link.safeonline.sdk.api.exception.LinkIDDeprecatedException;
import net.link.safeonline.sdk.api.exception.LinkIDMaintenanceException;
import net.link.safeonline.sdk.api.exception.LinkIDPermissionDeniedException;
import net.link.safeonline.sdk.api.exception.LinkIDUnexpectedException;
import net.link.safeonline.sdk.api.localization.LinkIDLocalizationValue;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentAmount;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentMandate;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentMethodType;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentState;
import net.link.safeonline.sdk.api.paymentconfiguration.LinkIDPaymentConfiguration;
import net.link.safeonline.sdk.api.permissions.LinkIDApplicationPermissionType;
import net.link.safeonline.sdk.api.qr.LinkIDQRInfo;
import net.link.safeonline.sdk.api.reporting.LinkIDReportApplicationFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportDateFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportErrorCode;
import net.link.safeonline.sdk.api.reporting.LinkIDReportPageFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportWalletFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletReportType;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletReportTypeFilter;
import net.link.safeonline.sdk.api.themes.LinkIDThemeColorError;
import net.link.safeonline.sdk.api.themes.LinkIDThemeColorErrorCode;
import net.link.safeonline.sdk.api.themes.LinkIDThemeImageError;
import net.link.safeonline.sdk.api.themes.LinkIDThemeImageErrorCode;
import net.link.safeonline.sdk.api.themes.LinkIDThemeStatusErrorReport;
import net.link.safeonline.sdk.api.users.LinkIDUser;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucher;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherEventTypeFilter;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherHistoryEvent;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherHistoryEventType;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherOrganization;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherOrganizationDetails;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherOrganizationStats;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletOrganization;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletOrganizationDetails;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletOrganizationStats;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletPolicyBalance;
import net.link.safeonline.sdk.api.ws.callback.LinkIDCallbackPullErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthCancelErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthPollErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthenticationState;
import net.link.safeonline.sdk.api.ws.linkid.comments.LinkIDCommentGetErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.comments.LinkIDCommentGetException;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDConfigWalletApplicationsErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalizationKeyType;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalizedImage;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalizedImages;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDTheme;
import net.link.safeonline.sdk.api.ws.linkid.credentials.LinkIDCredentialRemoveErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.credentials.LinkIDCredentialRemoveException;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDFavoritesConfiguration;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRBulkPushErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRChangeErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRContent;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRLockType;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRPollingConfiguration;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRPushErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDMandatePaymentErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDPaymentCaptureErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDPaymentRefundErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDPaymentStatusErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.paymentconfiguration.LinkIDPaymentConfigurationAddErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.paymentconfiguration.LinkIDPaymentConfigurationRemoveErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.paymentconfiguration.LinkIDPaymentConfigurationUpdateErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.permissions.LinkIDApplicationPermissionAddErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.permissions.LinkIDApplicationPermissionAddException;
import net.link.safeonline.sdk.api.ws.linkid.permissions.LinkIDApplicationPermissionListErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.permissions.LinkIDApplicationPermissionListException;
import net.link.safeonline.sdk.api.ws.linkid.permissions.LinkIDApplicationPermissionRemoveErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.permissions.LinkIDApplicationPermissionRemoveException;
import net.link.safeonline.sdk.api.ws.linkid.themes.LinkIDThemeRemoveErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.themes.LinkIDThemeStatusErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDUserListErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDUserListException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherInfoErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherInfoException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherListErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherListRedeemedErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationActivateErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationAddUpdateErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationHistoryErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationRemoveErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherRedeemErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherRewardErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletAddCreditErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletCommitErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletEnrollErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletGetInfoErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletOrganizationAddErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletOrganizationAddException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletOrganizationRemoveErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletOrganizationRemoveException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletOrganizationUpdateErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletOrganizationUpdateException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletReleaseErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletRemoveCreditErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletRemoveErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletReportInfo;
import net.link.util.InternalInconsistencyException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 08/10/15
 * Time: 13:43
 */
@SuppressWarnings("unused")
public class LinkIDServiceUtils {

    private LinkIDServiceUtils() {

        throw new AssertionError();
    }

    public static LinkIDAuthErrorCode convert(final AuthStartErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_REQUEST_INVALID:
                return LinkIDAuthErrorCode.ERROR_REQUEST_INVALID;
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDAuthPollErrorCode convert(final AuthPollErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_RESPONSE_INVALID_SESSION_ID:
                return LinkIDAuthPollErrorCode.ERROR_RESPONSE_INVALID_SESSION_ID;
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDAuthCancelErrorCode convert(final AuthCancelErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_INVALID_SESSION_ID:
                return LinkIDAuthCancelErrorCode.ERROR_INVALID_SESSION_ID;
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
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

    public static LinkIDLocalizedImages convert(final LocalizedImages localizedImages) {

        if (null == localizedImages)
            return null;

        Map<String, LinkIDLocalizedImage> imageMap = Maps.newHashMap();
        for (LocalizedImage localizedImage : localizedImages.getImages()) {
            imageMap.put( localizedImage.getLanguage(), new LinkIDLocalizedImage( localizedImage.getUrl(), localizedImage.getLanguage() ) );
        }
        return new LinkIDLocalizedImages( imageMap );
    }

    public static LinkIDConfigWalletApplicationsErrorCode convert(final ConfigWalletApplicationsErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET_ORGANIZATION:
                return LinkIDConfigWalletApplicationsErrorCode.ERROR_UNKNOWN_WALLET_ORGANIZATION;
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static void convert(final ThemesErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static void convert(final ConfigLocalizationErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDCallbackPullErrorCode convert(final CallbackPullErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_RESPONSE_INVALID_SESSION_ID:
                return LinkIDCallbackPullErrorCode.ERROR_RESPONSE_INVALID_SESSION_ID;
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDPaymentCaptureErrorCode convert(final PaymentCaptureErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CAPTURE_UNKNOWN:
                return LinkIDPaymentCaptureErrorCode.ERROR_CAPTURE_UNKNOWN;
            case ERROR_CAPTURE_FAILED:
                return LinkIDPaymentCaptureErrorCode.ERROR_CAPTURE_FAILED;
            case ERROR_CAPTURE_TOKEN_NOT_FOUND:
                return LinkIDPaymentCaptureErrorCode.ERROR_CAPTURE_TOKEN_NOT_FOUND;
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDPaymentRefundErrorCode convert(final PaymentRefundErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_ORDER_UNKNOWN:
                return LinkIDPaymentRefundErrorCode.ERROR_ORDER_UNKNOWN;
            case ERROR_ORDER_ALREADY_REFUNDED:
                return LinkIDPaymentRefundErrorCode.ERROR_ORDER_ALREADY_REFUNDED;
            case ERROR_REFUND_FAILED:
                return LinkIDPaymentRefundErrorCode.ERROR_REFUND_FAILED;
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
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
        paymentContext.setConfiguration( linkIDPaymentContext.getConfiguration() );
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

    @Nullable
    public static FavoritesConfiguration convert(@Nullable final LinkIDFavoritesConfiguration favoritesConfiguration) {

        if (null != favoritesConfiguration) {
            FavoritesConfiguration wsFavoritesConfiguration = new FavoritesConfiguration();
            wsFavoritesConfiguration.setTitle( favoritesConfiguration.getTitle() );
            wsFavoritesConfiguration.setInfo( favoritesConfiguration.getInfo() );
            wsFavoritesConfiguration.setLogoUrl( favoritesConfiguration.getLogoUrl() );
            wsFavoritesConfiguration.setBackgroundColor( favoritesConfiguration.getBackgroundColor() );
            wsFavoritesConfiguration.setTextColor( favoritesConfiguration.getTextColor() );
            return wsFavoritesConfiguration;
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
        ltqrContent.setPaymentContext( convert( content.getPaymentContext() ) );

        // callback
        ltqrContent.setCallback( convert( content.getCallback() ) );

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
        ltqrContent.setPollingConfiguration( convert( content.getPollingConfiguration() ) );

        // configuration
        if (null != content.getExpiryDate()) {
            ltqrContent.setExpiryDate( convert( content.getExpiryDate() ) );
        }
        if (content.getExpiryDuration() > 0) {
            ltqrContent.setExpiryDuration( content.getExpiryDuration() );
        }
        ltqrContent.setWaitForUnblock( content.isWaitForUnblock() );
        if (null != content.getLtqrStatusLocation()) {
            ltqrContent.setLtqrStatusLocation( content.getLtqrStatusLocation() );
        }

        // favorites configuration
        if (null != content.getFavoritesConfiguration()) {
            ltqrContent.setFavoritesConfiguration( convert( content.getFavoritesConfiguration() ) );
        }

        // notification location
        if (null != content.getNotificationLocation()) {
            ltqrContent.setNotificationLocation( content.getNotificationLocation() );
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
                    new LinkIDPaymentAmount( wsPaymentContext.getAmount(), convert( wsPaymentContext.getCurrency() ),
                            wsPaymentContext.getWalletCoin() ) ).description( wsPaymentContext.getDescription() )
                                                                .orderReference( wsPaymentContext.getOrderReference() )
                                                                .configuration( wsPaymentContext.getConfiguration() )
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
        builder.pollingConfiguration( getPollingConfiguration( ltqrContent.getPollingConfiguration() ) );

        // favorites configuration
        builder.favoritesConfiguration( getFavoritesConfiguration( ltqrContent.getFavoritesConfiguration() ) );

        // configuration
        if (null != ltqrContent.getExpiryDate()) {
            builder.expiryDate( ltqrContent.getExpiryDate().toGregorianCalendar().getTime() );
        }
        if (convert( ltqrContent.getExpiryDuration() ) > 0) {
            builder.expiryDuration( ltqrContent.getExpiryDuration() );
        }
        builder.waitForUnblock( ltqrContent.isWaitForUnblock() );
        builder.ltqrStatusLocation( ltqrContent.getLtqrStatusLocation() );

        // notification location
        builder.notificationLocation( ltqrContent.getNotificationLocation() );

        return builder.build();
    }

    public static LinkIDLTQRErrorCode convert(final LTQRErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CREDENTIALS_INVALID:
                return LinkIDLTQRErrorCode.ERROR_CREDENTIALS_INVALID;
            case ERROR_CONTEXT_INVALID:
                return LinkIDLTQRErrorCode.ERROR_CONTEXT_INVALID;
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDLTQRPushErrorCode convert(final LTQRPushErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CREDENTIALS_INVALID:
                return LinkIDLTQRPushErrorCode.ERROR_CREDENTIALS_INVALID;
            case ERROR_CONTEXT_INVALID:
                return LinkIDLTQRPushErrorCode.ERROR_CONTEXT_INVALID;
            case ERROR_FAVORITES_LOGO_ENCODING:
                return LinkIDLTQRPushErrorCode.ERROR_FAVORITES_LOGO_ENCODING;
            case ERROR_FAVORITES_LOGO_FORMAT:
                return LinkIDLTQRPushErrorCode.ERROR_FAVORITES_LOGO_FORMAT;
            case ERROR_FAVORITES_LOGO_SIZE:
                return LinkIDLTQRPushErrorCode.ERROR_FAVORITES_LOGO_SIZE;
            case ERROR_FAVORITES_BACKGROUND_COLOR_INVALID:
                return LinkIDLTQRPushErrorCode.ERROR_FAVORITES_BACKGROUND_COLOR_INVALID;
            case ERROR_FAVORITES_TEXT_COLOR_INVALID:
                return LinkIDLTQRPushErrorCode.ERROR_FAVORITES_TEXT_COLOR_INVALID;
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDLTQRBulkPushErrorCode convert(final LTQRBulkPushErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CREDENTIALS_INVALID:
                return LinkIDLTQRBulkPushErrorCode.ERROR_CREDENTIALS_INVALID;
            case ERROR_TOO_MANY_REQUESTS:
                return LinkIDLTQRBulkPushErrorCode.ERROR_TOO_MANY_REQUESTS;
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDLTQRChangeErrorCode convert(final LTQRChangeErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CREDENTIALS_INVALID:
                return LinkIDLTQRChangeErrorCode.ERROR_CREDENTIALS_INVALID;
            case ERROR_CONTEXT_INVALID:
                return LinkIDLTQRChangeErrorCode.ERROR_CONTEXT_INVALID;
            case ERROR_NOT_FOUND:
                return LinkIDLTQRChangeErrorCode.ERROR_NOT_FOUND;
            case ERROR_FAVORITES_LOGO_ENCODING:
                return LinkIDLTQRChangeErrorCode.ERROR_FAVORITES_LOGO_ENCODING;
            case ERROR_FAVORITES_LOGO_FORMAT:
                return LinkIDLTQRChangeErrorCode.ERROR_FAVORITES_LOGO_FORMAT;
            case ERROR_FAVORITES_LOGO_SIZE:
                return LinkIDLTQRChangeErrorCode.ERROR_FAVORITES_LOGO_SIZE;
            case ERROR_FAVORITES_BACKGROUND_COLOR_INVALID:
                return LinkIDLTQRChangeErrorCode.ERROR_FAVORITES_BACKGROUND_COLOR_INVALID;
            case ERROR_FAVORITES_TEXT_COLOR_INVALID:
                return LinkIDLTQRChangeErrorCode.ERROR_FAVORITES_TEXT_COLOR_INVALID;
            case ERROR_CONFLICT:
                return LinkIDLTQRChangeErrorCode.ERROR_CONFLICT;
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
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
            throw new InternalInconsistencyException( "Could not decode the QR image!", e );
        }
        return qrCodeImage;
    }

    @Nullable
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

    @Nullable
    public static LinkIDFavoritesConfiguration getFavoritesConfiguration(@Nullable final FavoritesConfiguration favoritesConfiguration) {

        if (null == favoritesConfiguration) {
            return null;
        }

        return new LinkIDFavoritesConfiguration( favoritesConfiguration.getTitle(), favoritesConfiguration.getInfo(), favoritesConfiguration.getLogoUrl(),
                favoritesConfiguration.getBackgroundColor(), favoritesConfiguration.getTextColor() );
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

    public static LinkIDReportErrorCode convert(final ReportErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_TOO_MANY_RESULTS:
                return LinkIDReportErrorCode.ERROR_TOO_MANY_RESULTS;
            case ERROR_INVALID_PAGE:
                return LinkIDReportErrorCode.ERROR_INVALID_PAGE;
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static void convert(final WalletInfoReportErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected wallet info report error code %s!", errorCode.name() ) );
    }

    public static LinkIDMandatePaymentErrorCode convert(final MandatePaymentErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_MANDATE_ARCHIVED:
                return LinkIDMandatePaymentErrorCode.ERROR_MANDATE_ARCHIVED;
            case ERROR_MANDATE_UNKNOWN:
                return LinkIDMandatePaymentErrorCode.ERROR_MANDATE_UNKNOWN;
            case ERROR_MANDATE_PAYMENT_FAILED:
                return LinkIDMandatePaymentErrorCode.ERROR_MANDATE_PAYMENT_FAILED;
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDPaymentStatusErrorCode convert(final PaymentStatusErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_NOT_FOUND:
                return LinkIDPaymentStatusErrorCode.ERROR_NOT_FOUND;
            case ERROR_MULTIPLE_ORDERS_FOUND:
                return LinkIDPaymentStatusErrorCode.ERROR_MULTIPLE_ORDERS_FOUND;
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDWalletEnrollErrorCode convert(final WalletEnrollErrorCode errorCode) {

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
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDWalletGetInfoErrorCode convert(final WalletGetInfoErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletGetInfoErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_UNKNOWN_USER:
                return LinkIDWalletGetInfoErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDWalletAddCreditErrorCode convert(final WalletAddCreditErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletAddCreditErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_WALLET_INVALID_CURRENCY:
                return LinkIDWalletAddCreditErrorCode.ERROR_WALLET_INVALID_CURRENCY;
            case ERROR_UNKNOWN_WALLET_COIN:
                return LinkIDWalletAddCreditErrorCode.ERROR_UNKNOWN_WALLET_COIN;
            case ERROR_UNKNOWN_USER:
                return LinkIDWalletAddCreditErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDWalletRemoveCreditErrorCode convert(final WalletRemoveCreditErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletRemoveCreditErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_WALLET_INVALID_CURRENCY:
                return LinkIDWalletRemoveCreditErrorCode.ERROR_WALLET_INVALID_CURRENCY;
            case ERROR_UNKNOWN_WALLET_COIN:
                return LinkIDWalletRemoveCreditErrorCode.ERROR_UNKNOWN_WALLET_COIN;
            case ERROR_UNKNOWN_USER:
                return LinkIDWalletRemoveCreditErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDWalletRemoveErrorCode convert(final WalletRemoveErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletRemoveErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_UNKNOWN_USER:
                return LinkIDWalletRemoveErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDWalletCommitErrorCode convert(final WalletCommitErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_USER:
                return LinkIDWalletCommitErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletCommitErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_UNKNOWN_WALLET_TRANSACTION:
                return LinkIDWalletCommitErrorCode.ERROR_UNKNOWN_WALLET_TRANSACTION;
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDWalletReleaseErrorCode convert(final WalletReleaseErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_USER:
                return LinkIDWalletReleaseErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletReleaseErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_UNKNOWN_WALLET_TRANSACTION:
                return LinkIDWalletReleaseErrorCode.ERROR_UNKNOWN_WALLET_TRANSACTION;
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDVoucherRewardErrorCode convert(final VoucherRewardErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_USER:
                return LinkIDVoucherRewardErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNKNOWN_VOUCHER_ORGANIZATION:
                return LinkIDVoucherRewardErrorCode.ERROR_UNKNOWN_VOUCHER_ORGANIZATION;
            case ERROR_INACTIVE_VOUCHER:
                return LinkIDVoucherRewardErrorCode.ERROR_INACTIVE_VOUCHER;
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDVoucherListErrorCode convert(final VoucherListErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_USER:
                return LinkIDVoucherListErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNKNOWN_VOUCHER_ORGANIZATION:
                return LinkIDVoucherListErrorCode.ERROR_UNKNOWN_VOUCHER_ORGANIZATION;
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDVoucherListRedeemedErrorCode convert(final VoucherListRedeemedErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_USER:
                return LinkIDVoucherListRedeemedErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNKNOWN_VOUCHER_ORGANIZATION:
                return LinkIDVoucherListRedeemedErrorCode.ERROR_UNKNOWN_VOUCHER_ORGANIZATION;
            case ERROR_TOO_MANY_RESULTS:
                return LinkIDVoucherListRedeemedErrorCode.ERROR_TOO_MANY_RESULTS;
            case ERROR_INVALID_PAGE:
                return LinkIDVoucherListRedeemedErrorCode.ERROR_INVALID_PAGE;
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static void handle(final VoucherInfoError error)
            throws LinkIDVoucherInfoException {

        if (null != error.getCommonErrorCode()) {
            handle( error.getCommonErrorCode(), error.getErrorMessage() );
        } else if (null != error.getErrorCode()) {
            switch (error.getErrorCode()) {

                case ERROR_UNKNOWN_VOUCHER_ID:
                    throw new LinkIDVoucherInfoException( error.getErrorMessage(), LinkIDVoucherInfoErrorCode.ERROR_UNKNOWN_VOUCHER_ID );
            }
        } else {
            throw new InternalInconsistencyException( String.format( "No error code found in error, message=\"%s\"", error.getErrorMessage() ) );
        }
    }

    public static LinkIDVoucherInfoErrorCode convert(final VoucherInfoErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_VOUCHER_ID:
                return LinkIDVoucherInfoErrorCode.ERROR_UNKNOWN_VOUCHER_ID;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDVoucherRedeemErrorCode convert(final VoucherRedeemErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_VOUCHER:
                return LinkIDVoucherRedeemErrorCode.ERROR_UNKNOWN_VOUCHER;
            case ERROR_ALREADY_REDEEMED:
                return LinkIDVoucherRedeemErrorCode.ERROR_ALREADY_REDEEMED;
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDVoucherOrganizationAddUpdateErrorCode convert(final VoucherOrganizationAddUpdateErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_LOGO_FORMAT:
                return LinkIDVoucherOrganizationAddUpdateErrorCode.ERROR_LOGO_FORMAT;
            case ERROR_LOGO_SIZE:
                return LinkIDVoucherOrganizationAddUpdateErrorCode.ERROR_LOGO_SIZE;
            case ERROR_LOGO_DIMENSION:
                return LinkIDVoucherOrganizationAddUpdateErrorCode.ERROR_LOGO_DIMENSION;
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
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

    public static LinkIDWalletReportType convert(final WalletReportType type) {

        if (null == type) {
            return null;
        }

        switch (type) {

            case USER_TRANSACTION:
                return LinkIDWalletReportType.USER_TRANSACTION;
            case WALLET_ADD:
                return LinkIDWalletReportType.WALLET_ADD;
            case WALLET_REMOVE:
                return LinkIDWalletReportType.WALLET_REMOVE;
            case WALLET_UNREMOVE:
                return LinkIDWalletReportType.WALLET_UNREMOVE;
            case APPLICATION_ADD_CREDIT_INITIAL:
                return LinkIDWalletReportType.APPLICATION_ADD_CREDIT_INITIAL;
            case APPLICATION_ADD_CREDIT:
                return LinkIDWalletReportType.APPLICATION_ADD_CREDIT;
            case APPLICATION_REMOVE_CREDIT:
                return LinkIDWalletReportType.APPLICATION_REMOVE_CREDIT;
            case APPLICATION_REFUND:
                return LinkIDWalletReportType.APPLICATION_REFUND;
        }

        throw new InternalInconsistencyException( String.format( "Unsupported wallet report type: \"%s\"", type.name() ) );
    }

    public static WalletReportType convert(final LinkIDWalletReportType walletReportType) {

        switch (walletReportType) {

            case USER_TRANSACTION:
                return WalletReportType.USER_TRANSACTION;
            case WALLET_ADD:
                return WalletReportType.WALLET_ADD;
            case WALLET_REMOVE:
                return WalletReportType.WALLET_REMOVE;
            case WALLET_UNREMOVE:
                return WalletReportType.WALLET_UNREMOVE;
            case APPLICATION_ADD_CREDIT_INITIAL:
                return WalletReportType.APPLICATION_ADD_CREDIT_INITIAL;
            case APPLICATION_ADD_CREDIT:
                return WalletReportType.APPLICATION_ADD_CREDIT;
            case APPLICATION_REMOVE_CREDIT:
                return WalletReportType.APPLICATION_REMOVE_CREDIT;
            case APPLICATION_REFUND:
                return WalletReportType.APPLICATION_REFUND;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected wallet report type %s!", walletReportType.name() ) );

    }

    @Nullable
    public static WalletReportInfo convert(@Nullable final LinkIDWalletReportInfo reportInfo) {

        if (null != reportInfo) {
            WalletReportInfo wsReportInfo = new WalletReportInfo();
            wsReportInfo.setReference( reportInfo.getReference() );
            wsReportInfo.setDescription( reportInfo.getDescription() );
            return wsReportInfo;
        }

        return null;
    }

    @Nullable
    public static LinkIDWalletReportInfo convert(@Nullable final WalletReportInfo wsReportInfo) {

        if (null != wsReportInfo) {
            return new LinkIDWalletReportInfo( wsReportInfo.getReference(), wsReportInfo.getDescription() );
        }

        return null;
    }

    public static String convert(@Nullable final Locale locale) {

        return null != locale? locale.getLanguage(): Locale.ENGLISH.getLanguage();
    }

    @Nullable
    public static ReportDateFilter convert(@Nullable final LinkIDReportDateFilter dateFilter) {

        if (null != dateFilter) {
            ReportDateFilter wsDateFilter = new ReportDateFilter();
            wsDateFilter.setStartDate( convert( dateFilter.getStartDate() ) );
            if (null != dateFilter.getEndDate()) {
                wsDateFilter.setEndDate( convert( dateFilter.getEndDate() ) );
            }
            return wsDateFilter;
        }

        return null;
    }

    @Nullable
    public static ReportPageFilter convert(@Nullable final LinkIDReportPageFilter pageFilter) {

        if (null != pageFilter) {
            ReportPageFilter wsPageFilter = new ReportPageFilter();
            wsPageFilter.setFirstResult( pageFilter.getFirstResult() );
            wsPageFilter.setMaxResults( pageFilter.getMaxResults() );
            return wsPageFilter;
        }

        return null;
    }

    @Nullable
    public static ReportApplicationFilter convert(@Nullable final LinkIDReportApplicationFilter applicationFilter) {

        if (null != applicationFilter) {
            ReportApplicationFilter wsApplicationFilter = new ReportApplicationFilter();
            wsApplicationFilter.setApplicationName( applicationFilter.getApplicationName() );
            return wsApplicationFilter;
        }

        return null;
    }

    @Nullable
    public static ReportWalletFilter convert(@Nullable final LinkIDReportWalletFilter walletFilter) {

        if (null != walletFilter) {
            ReportWalletFilter wsWalletFilter = new ReportWalletFilter();
            wsWalletFilter.setWalletId( walletFilter.getWalletId() );
            return wsWalletFilter;
        }

        return null;
    }

    @Nullable
    public static WalletReportTypeFilter convert(@Nullable final LinkIDWalletReportTypeFilter walletReportTypeFilter) {

        if (null != walletReportTypeFilter) {
            WalletReportTypeFilter filter = new WalletReportTypeFilter();
            for (LinkIDWalletReportType type : walletReportTypeFilter.getTypes()) {
                filter.getType().add( convert( type ) );
            }
            return filter;
        }

        return null;
    }

    @Nullable
    public static VoucherEventTypeFilter convert(@Nullable final LinkIDVoucherEventTypeFilter filter) {

        if (null != filter) {
            VoucherEventTypeFilter wsFilter = new VoucherEventTypeFilter();
            for (LinkIDVoucherHistoryEventType eventType : filter.getEventTypes()) {
                wsFilter.getEventTypes().add( convert( eventType ) );
            }
            return wsFilter;
        }

        return null;
    }

    @Nullable
    public static UserFilter convert(@Nullable final LinkIDUserFilter filter) {

        if (null != filter) {
            UserFilter wsFilter = new UserFilter();
            wsFilter.getUserIds().addAll( filter.getUserIds() );
            return wsFilter;
        }

        return null;
    }

    @Nullable
    public static ApplicationFilter convert(@Nullable final LinkIDApplicationFilter filter) {

        if (null != filter) {
            ApplicationFilter wsFilter = new ApplicationFilter();
            wsFilter.getApplications().addAll( filter.getApplications() );
            return wsFilter;
        }

        return null;
    }

    public static LinkIDVoucher convert(final Voucher voucher) {

        return new LinkIDVoucher( voucher.getId(), voucher.getUserId(), voucher.getName(), voucher.getDescription(), voucher.getLogoUrl(), voucher.getCounter(),
                voucher.getLimit(), convert( voucher.getActivated() ), convert( voucher.getRedeemed() ), voucher.getVoucherOrganizationId() );
    }

    public static XMLGregorianCalendar convert(final Date date) {

        if (null == date)
            return null;

        GregorianCalendar c = new GregorianCalendar();
        c.setTime( date );
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar( c );
        }
        catch (DatatypeConfigurationException e) {
            throw new InternalInconsistencyException( e );
        }
    }

    public static Date convert(final XMLGregorianCalendar calender) {

        if (null == calender)
            return null;

        return calender.toGregorianCalendar().getTime();
    }

    public static LinkIDCurrency convert(final net.lin_k.safe_online.common.Currency currency) {

        if (null == currency)
            return null;

        switch (currency) {

            case EUR:
                return LinkIDCurrency.EUR;
        }

        throw new InternalInconsistencyException( String.format( "Unsupported currency: \"%s\"", currency.name() ) );
    }

    public static net.lin_k.safe_online.common.Currency convertCommon(final LinkIDCurrency currency) {

        if (null == currency)
            return null;

        switch (currency) {

            case EUR:
                return net.lin_k.safe_online.common.Currency.EUR;
        }

        throw new InternalInconsistencyException( String.format( "Unsupported currency: \"%s\"", currency.name() ) );
    }

    public static LinkIDPaymentState convert(final net.lin_k.safe_online.common.PaymentStatusType paymentState) {

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

    public static net.lin_k.safe_online.common.PaymentMethodType convert(final LinkIDPaymentMethodType linkIDPaymentMethodType) {

        if (null == linkIDPaymentMethodType)
            return null;

        switch (linkIDPaymentMethodType) {

            case UNKNOWN:
                return net.lin_k.safe_online.common.PaymentMethodType.UNKNOWN;
            case VISA:
                return net.lin_k.safe_online.common.PaymentMethodType.VISA;
            case MASTERCARD:
                return net.lin_k.safe_online.common.PaymentMethodType.MASTERCARD;
            case SEPA:
                return net.lin_k.safe_online.common.PaymentMethodType.SEPA;
            case KLARNA:
                return net.lin_k.safe_online.common.PaymentMethodType.KLARNA;
        }

        return net.lin_k.safe_online.common.PaymentMethodType.UNKNOWN;
    }

    public static PaymentMethodType convertPaymentMethod(final LinkIDPaymentMethodType linkIDPaymentMethodType) {

        if (null == linkIDPaymentMethodType)
            return null;

        switch (linkIDPaymentMethodType) {

            case UNKNOWN:
                return PaymentMethodType.UNKNOWN;
            case VISA:
                return PaymentMethodType.VISA;
            case MASTERCARD:
                return PaymentMethodType.MASTERCARD;
            case SEPA:
                return PaymentMethodType.SEPA;
            case KLARNA:
                return PaymentMethodType.KLARNA;
        }

        return PaymentMethodType.UNKNOWN;
    }

    public static LinkIDPaymentMethodType convert(final net.lin_k.safe_online.common.PaymentMethodType paymentMethodType) {

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

    public static LinkIDLTQRPollingConfiguration getPollingConfiguration(@Nullable final PollingConfiguration pollingConfiguration) {

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

    public static LinkIDLTQRPollingConfiguration getPollingConfiguration(
            @Nullable final net.lin_k.safe_online.ltqr._4.PollingConfiguration pollingConfiguration) {

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

    @Nullable
    public static LinkIDRequestStatusCode convert(@Nullable final ThemeStatusCode themeStatusCode) {

        if (null == themeStatusCode) {
            return null;
        }

        switch (themeStatusCode) {

            case STATUS_REJECTED:
                return LinkIDRequestStatusCode.REJECTED;
            case STATUS_PENDING:
                return LinkIDRequestStatusCode.PENDING;
            case STATUS_ACCEPTED:
                return LinkIDRequestStatusCode.ACCEPTED;
            case STATUS_RELEASED:
                return LinkIDRequestStatusCode.RELEASED;
        }

        throw new InternalInconsistencyException( String.format( "Unsupported themeStatusCode: \"%s\"", themeStatusCode.name() ) );

    }

    @Nullable
    public static LinkIDRequestStatusCode convert(@Nullable final RequestStatusCode requestStatusCode) {

        if (null == requestStatusCode) {
            return null;
        }

        switch (requestStatusCode) {

            case STATUS_REJECTED:
                return LinkIDRequestStatusCode.REJECTED;
            case STATUS_PENDING:
                return LinkIDRequestStatusCode.PENDING;
            case STATUS_ACCEPTED:
                return LinkIDRequestStatusCode.ACCEPTED;
            case STATUS_RELEASED:
                return LinkIDRequestStatusCode.RELEASED;
        }

        throw new InternalInconsistencyException( String.format( "Unsupported requestStatusCode: \"%s\"", requestStatusCode.name() ) );

    }

    public static LinkIDTheme convert(@Nullable final Themes themes) {

        if (null == themes) {
            return null;
        }

        return new LinkIDTheme( themes.getName(), themes.getFriendlyName(), convert( themes.getStatusCode() ), themes.isDefaultTheme(),
                convert( themes.isOwner() ), convert( themes.getLogo() ), convert( themes.getBackground() ), convert( themes.getTabletBackground() ),
                convert( themes.getAlternativeBackground() ), themes.getBackgroundColor(), themes.getTextColor() );
    }

    @Nullable
    public static LocalizedImages convert(@Nullable final List<LinkIDLocalizedImage> linkIDLocalizedImages) {

        if (null == linkIDLocalizedImages || linkIDLocalizedImages.isEmpty()) {
            return null;
        }

        LocalizedImages localizedImages = new LocalizedImages();
        for (LinkIDLocalizedImage linkIDLocalizedImage : linkIDLocalizedImages) {
            LocalizedImage image = new LocalizedImage();
            image.setLanguage( linkIDLocalizedImage.getLanguage() );
            image.setUrl( linkIDLocalizedImage.getUrl() );
            localizedImages.getImages().add( image );
        }

        return localizedImages;
    }

    public static void convert(final ThemeAddErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDThemeColorError convert(final ThemeColorError themeColorError) {

        return new LinkIDThemeColorError( convert( themeColorError.getErrorCode() ), themeColorError.getErrorMessage() );
    }

    private static LinkIDThemeColorErrorCode convert(final ThemeColorErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_FORMAT:
                return LinkIDThemeColorErrorCode.ERROR_FORMAT;
            case ERROR_UNEXPECTED:
                return LinkIDThemeColorErrorCode.ERROR_UNEXPECTED;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDThemeRemoveErrorCode convert(final ThemeRemoveErrorCode errorCode) {

        if (null == errorCode) {
            return null;
        }

        switch (errorCode) {

            case ERROR_NOT_FOUND:
                return LinkIDThemeRemoveErrorCode.ERROR_NOT_FOUND;
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDThemeStatusErrorCode convert(final ThemeStatusErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_NOT_FOUND:
                return LinkIDThemeStatusErrorCode.ERROR_NOT_FOUND;
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDThemeStatusErrorReport convert(final ThemeStatusErrorReport errorReport) {

        return new LinkIDThemeStatusErrorReport( convertThemeImageErrors( errorReport.getLogoErrors() ),
                convertThemeImageErrors( errorReport.getBackgroundErrors() ), convertThemeImageErrors( errorReport.getTabletBackgroundErrors() ),
                convertThemeImageErrors( errorReport.getAlternativeBackgroundErrors() ) );
    }

    @Nullable
    public static List<LinkIDThemeImageError> convertThemeImageErrors(final List<ThemeImageError> themeImageErrors) {

        if (null == themeImageErrors) {
            return null;
        }

        List<LinkIDThemeImageError> errors = Lists.newLinkedList();
        for (ThemeImageError themeImageError : themeImageErrors) {
            errors.add( convert( themeImageError ) );
        }
        return errors;
    }

    public static LinkIDThemeImageError convert(final ThemeImageError themeImageError) {

        return new LinkIDThemeImageError( themeImageError.getLanguage(), convert( themeImageError.getErrorCode() ), themeImageError.getErrorMessage() );
    }

    public static LinkIDThemeImageErrorCode convert(final ThemeImageErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_FORMAT:
                return LinkIDThemeImageErrorCode.ERROR_FORMAT;
            case ERROR_SIZE:
                return LinkIDThemeImageErrorCode.ERROR_SIZE;
            case ERROR_DIMENSION:
                return LinkIDThemeImageErrorCode.ERROR_DIMENSION;
            case ERROR_UNEXPECTED:
                return LinkIDThemeImageErrorCode.ERROR_UNEXPECTED;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static List<LinkIDLocalizationValue> convertLocalizations(final List<Localization> localizations) {

        List<LinkIDLocalizationValue> values = Lists.newLinkedList();
        if (CollectionUtils.isEmpty( localizations )) {
            return values;
        }

        for (Localization localization : localizations) {
            values.add( new LinkIDLocalizationValue( localization.getLanguageCode(), localization.getValue() ) );
        }

        return values;
    }

    public static void convert(final VoucherOrganizationListErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDVoucherOrganizationDetails convert(final VoucherOrganizationDetails voucherOrganizationDetails) {

        return new LinkIDVoucherOrganizationDetails( convert( voucherOrganizationDetails.getOrganization() ), voucherOrganizationDetails.isOwner(),
                convert( voucherOrganizationDetails.getStats() ), voucherOrganizationDetails.getRewardPermissionApplications(),
                voucherOrganizationDetails.getListPermissionApplications(), voucherOrganizationDetails.getRedeemPermissionApplications() );

    }

    public static LinkIDVoucherOrganization convert(final VoucherOrganization request) {

        return new LinkIDVoucherOrganization( request.getVoucherOrganizationId(), request.getLogoUrl(), request.getVoucherLimit(), request.isActive(),
                convertLocalizations( request.getNameLocalization() ), convertLocalizations( request.getDescriptionLocalization() ) );

    }

    @Nullable
    public static LinkIDVoucherOrganizationStats convert(@Nullable final VoucherOrganizationStats voucherOrganizationStats) {

        if (null != voucherOrganizationStats) {
            return new LinkIDVoucherOrganizationStats( voucherOrganizationStats.getNumberOfVouchers(), voucherOrganizationStats.getNumberOfInactiveVouchers(),
                    voucherOrganizationStats.getNumberOfActiveVouchers(), voucherOrganizationStats.getNumberOfRedeemedVouchers() );
        }

        return null;
    }

    public static LinkIDVoucherOrganizationRemoveErrorCode convert(final VoucherOrganizationRemoveErrorCode errorCode) {

        if (null == errorCode) {
            return null;
        }

        switch (errorCode) {

            case ERROR_UNKNOWN_VOUCHER_ORGANIZATION:
                return LinkIDVoucherOrganizationRemoveErrorCode.ERROR_UNKNOWN_VOUCHER_ORGANIZATION;
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDVoucherOrganizationActivateErrorCode convert(final VoucherOrganizationActivateErrorCode errorCode) {

        if (null == errorCode) {
            return null;
        }

        switch (errorCode) {

            case ERROR_UNKNOWN_VOUCHER_ORGANIZATION:
                return LinkIDVoucherOrganizationActivateErrorCode.ERROR_UNKNOWN_VOUCHER_ORGANIZATION;
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDVoucherOrganizationHistoryErrorCode convert(final VoucherOrganizationHistoryErrorCode errorCode) {

        if (null == errorCode) {
            return null;
        }

        switch (errorCode) {

            case ERROR_UNKNOWN_VOUCHER_ORGANIZATION:
                return LinkIDVoucherOrganizationHistoryErrorCode.ERROR_UNKNOWN_VOUCHER_ORGANIZATION;
            case ERROR_TOO_MANY_RESULTS:
                return LinkIDVoucherOrganizationHistoryErrorCode.ERROR_TOO_MANY_RESULTS;
            case ERROR_INVALID_PAGE:
                return LinkIDVoucherOrganizationHistoryErrorCode.ERROR_INVALID_PAGE;
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static void convert(final ConfigApplicationsErrorCode errorCode) {

        if (null == errorCode) {
            throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
        }

        switch (errorCode) {

            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDVoucherHistoryEvent convert(final VoucherHistoryEvent wsEvent) {

        return new LinkIDVoucherHistoryEvent( wsEvent.getId(), convert( wsEvent.getDate() ), wsEvent.getVoucherOrganizationId(), wsEvent.getUserId(),
                wsEvent.getVoucherId(), wsEvent.getPoints(), wsEvent.getApplicationName(), wsEvent.getApplicationNameFriendly(),
                convert( wsEvent.getEventType() ) );
    }

    public static LinkIDVoucherHistoryEventType convert(final VoucherHistoryEventType eventType) {

        switch (eventType) {

            case VOUCHER_EVENT_REWARD:
                return LinkIDVoucherHistoryEventType.VOUCHER_EVENT_REWARD;
            case VOUCHER_EVENT_ACTIVATE:
                return LinkIDVoucherHistoryEventType.VOUCHER_EVENT_ACTIVATE;
            case VOUCHER_EVENT_REDEEM:
                return LinkIDVoucherHistoryEventType.VOUCHER_EVENT_REDEEM;
        }

        throw new InternalInconsistencyException( String.format( "Unsupported event type: \"%s\"", eventType.name() ) );
    }

    public static VoucherHistoryEventType convert(final LinkIDVoucherHistoryEventType eventType) {

        switch (eventType) {

            case VOUCHER_EVENT_REWARD:
                return VoucherHistoryEventType.VOUCHER_EVENT_REWARD;
            case VOUCHER_EVENT_ACTIVATE:
                return VoucherHistoryEventType.VOUCHER_EVENT_ACTIVATE;
            case VOUCHER_EVENT_REDEEM:
                return VoucherHistoryEventType.VOUCHER_EVENT_REDEEM;
        }
        throw new InternalInconsistencyException( String.format( "Unsupported event type: \"%s\"", eventType.name() ) );
    }

    public static void convert(final WalletOrganizationListErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static WalletOrganization convert(final LinkIDWalletOrganization organization) {

        WalletOrganization wsOrganization = new WalletOrganization();
        wsOrganization.setWalletOrganizationId( organization.getId() );
        wsOrganization.setLogoUrl( organization.getLogoUrl() );
        wsOrganization.setExpirationInSecs( organization.getExpirationInSecs() );
        wsOrganization.setSticky( organization.isSticky() );
        wsOrganization.setAutoEnroll( organization.isAutoEnroll() );
        wsOrganization.setRemoveWalletOnUnsubscribe( organization.isRemoveWalletOnUnsubscribe() );
        wsOrganization.setCurrency( convert( organization.getCurrency() ) );
        wsOrganization.setCoinId( organization.getCoinId() );
        wsOrganization.setPolicyBalance( convert( organization.getPolicyBalance() ) );
        wsOrganization.getNameLocalization().addAll( convertSDKLocalizations( organization.getNameLocalizations() ) );
        wsOrganization.getDescriptionLocalization().addAll( convertSDKLocalizations( organization.getDescriptionLocalizations() ) );
        wsOrganization.getCoinNameLocalization().addAll( convertSDKLocalizations( organization.getCoinNameLocalization() ) );
        wsOrganization.getCoinNameMultipleLocalization().addAll( convertSDKLocalizations( organization.getCoinNameMultipleLocalization() ) );
        return wsOrganization;
    }

    public static List<Localization> convertSDKLocalizations(final List<LinkIDLocalizationValue> localizations) {

        List<Localization> values = Lists.newLinkedList();
        if (CollectionUtils.isEmpty( localizations )) {
            return values;
        }

        for (LinkIDLocalizationValue localization : localizations) {
            Localization l = new Localization();
            l.setLanguageCode( localization.getLanguageCode() );
            l.setValue( localization.getValue() );
            values.add( l );
        }

        return values;
    }

    @Nullable
    public static WalletPolicyBalance convert(@Nullable final LinkIDWalletPolicyBalance walletPolicyBalance) {

        if (null == walletPolicyBalance) {
            return null;
        }

        WalletPolicyBalance balancePolicy = new WalletPolicyBalance();
        balancePolicy.setBalance( walletPolicyBalance.getBalance() );
        return balancePolicy;
    }

    public static LinkIDWalletOrganizationDetails convert(final WalletOrganizationDetails details) {

        return new LinkIDWalletOrganizationDetails( convert( details.getOrganization() ), details.isOwner(), convertPermissions( details.getPermissions() ),
                convert( details.getStats() ), details.getPermissionAddCreditApplications(), details.getPermissionRemoveCreditApplications(),
                details.getPermissionRemoveApplications(), details.getPermissionEnrollApplications(), details.getPermissionUseApplications() );

    }

    private static List<LinkIDApplicationPermissionType> convertPermissions(final List<ApplicationPermissionType> permissions) {

        List<LinkIDApplicationPermissionType> sdkPermissions = Lists.newLinkedList();

        for (ApplicationPermissionType permission : permissions) {
            sdkPermissions.add( convert( permission ) );
        }

        return sdkPermissions;
    }

    public static LinkIDWalletOrganization convert(final WalletOrganization request) {

        return new LinkIDWalletOrganization( request.getWalletOrganizationId(), request.getLogoUrl(), request.getExpirationInSecs(), request.isSticky(),
                request.isAutoEnroll(), request.isRemoveWalletOnUnsubscribe(), convertLocalizations( request.getNameLocalization() ),
                convertLocalizations( request.getDescriptionLocalization() ), convertLocalizations( request.getCoinNameLocalization() ),
                convertLocalizations( request.getCoinNameMultipleLocalization() ), convert( request.getCurrency() ), request.getCoinId(),
                convert( request.getPolicyBalance() ), convert( request.getStatusCode() ) );

    }

    @Nullable
    public static LinkIDWalletPolicyBalance convert(@Nullable final WalletPolicyBalance walletPolicyBalance) {

        if (null == walletPolicyBalance) {
            return null;
        }

        return new LinkIDWalletPolicyBalance( walletPolicyBalance.getBalance() );
    }

    @Nullable
    public static LinkIDWalletOrganizationStats convert(@Nullable final WalletOrganizationStats stats) {

        if (null != stats) {
            return new LinkIDWalletOrganizationStats( stats.getNumberOfWallets() );
        }

        return null;
    }

    public static LinkIDPaymentConfigurationAddErrorCode convert(final PaymentConfigurationAddErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CONFIGURATION_ALREADY_EXISTS:
                return LinkIDPaymentConfigurationAddErrorCode.ERROR_CONFIGURATION_ALREADY_EXISTS;
            case ERROR_CONFIGURATION_INVALID:
                return LinkIDPaymentConfigurationAddErrorCode.ERROR_CONFIGURATION_INVALID;
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDPaymentConfigurationUpdateErrorCode convert(final PaymentConfigurationUpdateErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CONFIGURATION_NOT_EXISTS:
                return LinkIDPaymentConfigurationUpdateErrorCode.ERROR_CONFIGURATION_NOT_EXISTS;
            case ERROR_CONFIGURATION_INVALID:
                return LinkIDPaymentConfigurationUpdateErrorCode.ERROR_CONFIGURATION_INVALID;
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDPaymentConfigurationRemoveErrorCode convert(final PaymentConfigurationRemoveErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CONFIGURATION_NOT_EXISTS:
                return LinkIDPaymentConfigurationRemoveErrorCode.ERROR_CONFIGURATION_NOT_EXISTS;
            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( errorCode.value() );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static void convert(final PaymentConfigurationListErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( errorCode.value() );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( errorCode.value() );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static PaymentConfiguration convert(final LinkIDPaymentConfiguration paymentConfiguration) {

        PaymentConfiguration configuration = new PaymentConfiguration();
        configuration.setName( paymentConfiguration.getName() );
        configuration.setDefaultConfiguration( paymentConfiguration.isDefaultConfiguration() );
        configuration.setOnlyWallets( paymentConfiguration.isOnlyWallets() );
        configuration.setNoWallets( paymentConfiguration.isNoWallets() );
        if (!CollectionUtils.isEmpty( paymentConfiguration.getWalletOrganizations() )) {
            configuration.getWalletOrganizations().addAll( paymentConfiguration.getWalletOrganizations() );
        }
        if (!CollectionUtils.isEmpty( paymentConfiguration.getPaymentMethods() )) {
            for (LinkIDPaymentMethodType paymentMethod : paymentConfiguration.getPaymentMethods()) {
                configuration.getPaymentMethods().add( convertPaymentMethod( paymentMethod ) );
            }
        }
        return configuration;
    }

    public static LinkIDPaymentConfiguration convert(final PaymentConfiguration paymentConfiguration) {

        List<LinkIDPaymentMethodType> paymentMethods = Lists.newLinkedList();
        if (!CollectionUtils.isEmpty( paymentConfiguration.getPaymentMethods() )) {
            paymentMethods = ImmutableList.copyOf(
                    Collections2.transform( paymentConfiguration.getPaymentMethods(), new Function<PaymentMethodType, LinkIDPaymentMethodType>() {
                        @javax.annotation.Nullable
                        @Override
                        public LinkIDPaymentMethodType apply(@javax.annotation.Nullable final PaymentMethodType input) {

                            return convert( input );

                        }
                    } ) );
        }

        return new LinkIDPaymentConfiguration( paymentConfiguration.getName(), paymentConfiguration.isDefaultConfiguration(),
                paymentConfiguration.isOnlyWallets(), paymentConfiguration.isNoWallets(), paymentConfiguration.getWalletOrganizations(), paymentMethods );
    }

    public static LinkIDAuthenticationState convert(final AuthAuthenticationState authenticationState) {

        switch (authenticationState) {

            case LINKID_STATE_STARTED:
                return LinkIDAuthenticationState.STARTED;
            case LINKID_STATE_RETRIEVED:
                return LinkIDAuthenticationState.RETRIEVED;
            case LINKID_STATE_AUTHENTICATED:
                return LinkIDAuthenticationState.AUTHENTICATED;
            case LINKID_STATE_EXPIRED:
                return LinkIDAuthenticationState.EXPIRED;
            case LINKID_STATE_FAILED:
                return LinkIDAuthenticationState.FAILED;
            case LINKID_STATE_PAYMENT_ADD:
                return LinkIDAuthenticationState.FAILED;
        }

        throw new InternalInconsistencyException( String.format( "Invalid authentication state %s!", authenticationState ) );
    }

    @Nullable
    public static ThemeStatusCode convertOld(@Nullable final LinkIDRequestStatusCode linkIDRequestStatusCode) {

        if (null == linkIDRequestStatusCode) {
            return null;
        }

        switch (linkIDRequestStatusCode) {

            case REJECTED:
                return ThemeStatusCode.STATUS_REJECTED;
            case PENDING:
                return ThemeStatusCode.STATUS_PENDING;
            case ACCEPTED:
                return ThemeStatusCode.STATUS_ACCEPTED;
            case RELEASED:
                return ThemeStatusCode.STATUS_RELEASED;
        }

        throw new InternalInconsistencyException( String.format( "Invalid LinkIDThemeStatusCode %s!", linkIDRequestStatusCode ) );

    }

    @Nullable
    public static RequestStatusCode convert(@Nullable final LinkIDRequestStatusCode linkIDRequestStatusCode) {

        if (null == linkIDRequestStatusCode) {
            return null;
        }

        switch (linkIDRequestStatusCode) {

            case REJECTED:
                return RequestStatusCode.STATUS_REJECTED;
            case PENDING:
                return RequestStatusCode.STATUS_PENDING;
            case ACCEPTED:
                return RequestStatusCode.STATUS_ACCEPTED;
            case RELEASED:
                return RequestStatusCode.STATUS_RELEASED;
        }

        throw new InternalInconsistencyException( String.format( "Invalid LinkIDRequestStatusCode %s!", linkIDRequestStatusCode ) );

    }

    public static void handle(final CommonErrorCode errorCode, final String message) {

        switch (errorCode) {

            case ERROR_PERMISSION_DENIED:
                throw new LinkIDPermissionDeniedException( message );
            case ERROR_UNEXPECTED:
                throw new LinkIDUnexpectedException( message );
            case ERROR_MAINTENANCE:
                throw new LinkIDMaintenanceException( message );
            case ERROR_DEPRECATED:
                throw new LinkIDDeprecatedException( message );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static void handle(final WalletOrganizationAddError error)
            throws LinkIDWalletOrganizationAddException {

        if (null != error.getCommonErrorCode()) {
            handle( error.getCommonErrorCode(), error.getErrorMessage() );
        } else if (null != error.getErrorCode()) {
            switch (error.getErrorCode()) {

                case ERROR_LOGO_FORMAT:
                    throw new LinkIDWalletOrganizationAddException( error.getErrorMessage(), LinkIDWalletOrganizationAddErrorCode.ERROR_LOGO_FORMAT );
                case ERROR_LOGO_SIZE:
                    throw new LinkIDWalletOrganizationAddException( error.getErrorMessage(), LinkIDWalletOrganizationAddErrorCode.ERROR_LOGO_SIZE );
                case ERROR_LOGO_DIMENSION:
                    throw new LinkIDWalletOrganizationAddException( error.getErrorMessage(), LinkIDWalletOrganizationAddErrorCode.ERROR_LOGO_DIMENSION );
            }
        } else {
            throw new InternalInconsistencyException( String.format( "No error code found in error, message=\"%s\"", error.getErrorMessage() ) );
        }
    }

    public static void handle(final WalletOrganizationUpdateError error)
            throws LinkIDWalletOrganizationUpdateException {

        if (null != error.getCommonErrorCode()) {
            handle( error.getCommonErrorCode(), error.getErrorMessage() );
        } else if (null != error.getErrorCode()) {
            switch (error.getErrorCode()) {

                case ERROR_UNKNOWN_WALLET_ORGANIZATION:
                    throw new LinkIDWalletOrganizationUpdateException( error.getErrorMessage(),
                            LinkIDWalletOrganizationUpdateErrorCode.ERROR_UNKNOWN_WALLET_ORGANIZATION );
                case ERROR_LOGO_FORMAT:
                    throw new LinkIDWalletOrganizationUpdateException( error.getErrorMessage(), LinkIDWalletOrganizationUpdateErrorCode.ERROR_LOGO_FORMAT );
                case ERROR_LOGO_SIZE:
                    throw new LinkIDWalletOrganizationUpdateException( error.getErrorMessage(), LinkIDWalletOrganizationUpdateErrorCode.ERROR_LOGO_SIZE );
                case ERROR_LOGO_DIMENSION:
                    throw new LinkIDWalletOrganizationUpdateException( error.getErrorMessage(), LinkIDWalletOrganizationUpdateErrorCode.ERROR_LOGO_DIMENSION );
            }
        } else {
            throw new InternalInconsistencyException( String.format( "No error code found in error, message=\"%s\"", error.getErrorMessage() ) );
        }
    }

    public static void handle(final WalletOrganizationRemoveError error)
            throws LinkIDWalletOrganizationRemoveException {

        if (null != error.getCommonErrorCode()) {
            handle( error.getCommonErrorCode(), error.getErrorMessage() );
        } else if (null != error.getErrorCode()) {
            switch (error.getErrorCode()) {

                case ERROR_UNKNOWN_WALLET_ORGANIZATION:
                    throw new LinkIDWalletOrganizationRemoveException( error.getErrorMessage(),
                            LinkIDWalletOrganizationRemoveErrorCode.ERROR_UNKNOWN_WALLET_ORGANIZATION );
            }
        } else {
            throw new InternalInconsistencyException( String.format( "No error code found in error, message=\"%s\"", error.getErrorMessage() ) );
        }
    }

    public static ApplicationPermissionType convert(final LinkIDApplicationPermissionType permissionType) {

        if (null == permissionType) {
            return null;
        }

        switch (permissionType) {

            case WALLET_ADD_CREDIT:
                return ApplicationPermissionType.PERMISSION_WALLET_ADD_CREDIT;
            case WALLET_REMOVE_CREDIT:
                return ApplicationPermissionType.PERMISSION_WALLET_REMOVE_CREDIT;
            case WALLET_REMOVE:
                return ApplicationPermissionType.PERMISSION_WALLET_REMOVE;
            case WALLET_ENROLL:
                return ApplicationPermissionType.PERMISSION_WALLET_ENROLL;
            case WALLET_USE:
                return ApplicationPermissionType.PERMISSION_WALLET_USE;
            case VOUCHER_REWARD:
                return ApplicationPermissionType.PERMISSION_VOUCHER_REWARD;
            case VOUCHER_LIST:
                return ApplicationPermissionType.PERMISSION_VOUCHER_LIST;
            case VOUCHER_REDEEM:
                return ApplicationPermissionType.PERMISSION_VOUCHER_REDEEM;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected permission type %s!", permissionType.name() ) );

    }

    public static LinkIDApplicationPermissionType convert(final ApplicationPermissionType permissionType) {

        if (null == permissionType) {
            return null;
        }

        switch (permissionType) {

            case PERMISSION_WALLET_ADD_CREDIT:
                return LinkIDApplicationPermissionType.WALLET_ADD_CREDIT;
            case PERMISSION_WALLET_REMOVE_CREDIT:
                return LinkIDApplicationPermissionType.WALLET_REMOVE_CREDIT;
            case PERMISSION_WALLET_REMOVE:
                return LinkIDApplicationPermissionType.WALLET_REMOVE;
            case PERMISSION_WALLET_ENROLL:
                return LinkIDApplicationPermissionType.WALLET_ENROLL;
            case PERMISSION_WALLET_USE:
                return LinkIDApplicationPermissionType.WALLET_USE;
            case PERMISSION_VOUCHER_REWARD:
                return LinkIDApplicationPermissionType.VOUCHER_REWARD;
            case PERMISSION_VOUCHER_LIST:
                return LinkIDApplicationPermissionType.VOUCHER_LIST;
            case PERMISSION_VOUCHER_REDEEM:
                return LinkIDApplicationPermissionType.VOUCHER_REDEEM;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected permission type %s!", permissionType.name() ) );

    }

    public static void handle(final ApplicationPermissionAddError error)
            throws LinkIDApplicationPermissionAddException {

        if (null != error.getCommonErrorCode()) {
            handle( error.getCommonErrorCode(), error.getErrorMessage() );
        } else if (null != error.getErrorCode()) {
            switch (error.getErrorCode()) {

                case ERROR_UNKNOWN_ID:
                    throw new LinkIDApplicationPermissionAddException( error.getErrorMessage(), LinkIDApplicationPermissionAddErrorCode.ERROR_UNKNOWN_ID );
                case ERROR_UNKNOWN_APPLICATION:
                    throw new LinkIDApplicationPermissionAddException( error.getErrorMessage(),
                            LinkIDApplicationPermissionAddErrorCode.ERROR_UNKNOWN_APPLICATION );
            }
        } else {
            throw new InternalInconsistencyException( String.format( "No error code found in error, message=\"%s\"", error.getErrorMessage() ) );
        }

    }

    public static void handle(final ApplicationPermissionRemoveError error)
            throws LinkIDApplicationPermissionRemoveException {

        if (null != error.getCommonErrorCode()) {
            handle( error.getCommonErrorCode(), error.getErrorMessage() );
        } else if (null != error.getErrorCode()) {
            switch (error.getErrorCode()) {

                case ERROR_UNKNOWN_ID:
                    throw new LinkIDApplicationPermissionRemoveException( error.getErrorMessage(),
                            LinkIDApplicationPermissionRemoveErrorCode.ERROR_UNKNOWN_ID );
                case ERROR_UNKNOWN_APPLICATION:
                    throw new LinkIDApplicationPermissionRemoveException( error.getErrorMessage(),
                            LinkIDApplicationPermissionRemoveErrorCode.ERROR_UNKNOWN_APPLICATION );
            }
        } else {
            throw new InternalInconsistencyException( String.format( "No error code found in error, message=\"%s\"", error.getErrorMessage() ) );
        }

    }

    public static void handle(final ApplicationPermissionListError error)
            throws LinkIDApplicationPermissionListException {

        if (null != error.getCommonErrorCode()) {
            handle( error.getCommonErrorCode(), error.getErrorMessage() );
        } else if (null != error.getErrorCode()) {
            switch (error.getErrorCode()) {

                case ERROR_UNKNOWN_ID:
                    throw new LinkIDApplicationPermissionListException( error.getErrorMessage(), LinkIDApplicationPermissionListErrorCode.ERROR_UNKNOWN_ID );
            }
        } else {
            throw new InternalInconsistencyException( String.format( "No error code found in error, message=\"%s\"", error.getErrorMessage() ) );
        }

    }

    public static void handle(final CommentGetError error)
            throws LinkIDCommentGetException {

        if (null != error.getCommonErrorCode()) {
            handle( error.getCommonErrorCode(), error.getErrorMessage() );
        } else if (null != error.getErrorCode()) {
            switch (error.getErrorCode()) {

                case ERROR_UNKNOWN_ID:
                    throw new LinkIDCommentGetException( error.getErrorMessage(), LinkIDCommentGetErrorCode.ERROR_UNKNOWN_ID );
            }
        } else {
            throw new InternalInconsistencyException( String.format( "No error code found in error, message=\"%s\"", error.getErrorMessage() ) );
        }

    }

    public static void handle(final UserListError error)
            throws LinkIDUserListException {

        if (null != error.getCommonErrorCode()) {
            handle( error.getCommonErrorCode(), error.getErrorMessage() );
        } else if (null != error.getErrorCode()) {
            switch (error.getErrorCode()) {

                case ERROR_UNKNOWN_VOUCHER_ORGANIZATION_ID:
                    throw new LinkIDUserListException( error.getErrorMessage(), LinkIDUserListErrorCode.ERROR_UNKNOWN_VOUCHER_ORGANIZATION );
                case ERROR_UNKNOWN_WALLET_ORGANIZATION_ID:
                    throw new LinkIDUserListException( error.getErrorMessage(), LinkIDUserListErrorCode.ERROR_UNKNOWN_WALLET_ORGANIZATION );
            }
        } else {
            throw new InternalInconsistencyException( String.format( "No error code found in error, message=\"%s\"", error.getErrorMessage() ) );
        }

    }

    public static LinkIDUser convert(final User user) {

        return new LinkIDUser( user.getUserId(), convert( user.getCreated() ), convert( user.getLastAuthenticated() ), convert( user.getRemoved() ) );
    }

    public static CredentialType convert(final LinkIDCredentialType type) {

        if (null == type) {
            return null;
        }

        switch (type) {

            case PASSWORD:
                return CredentialType.PASSWORD;
            case JKS:
                return CredentialType.JKS;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected type %s!", type.name() ) );

    }

    public static LinkIDCredentialType convert(final CredentialType type) {

        if (null == type) {
            return null;
        }

        switch (type) {

            case PASSWORD:
                return LinkIDCredentialType.PASSWORD;
            case JKS:
                return LinkIDCredentialType.JKS;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected type %s!", type.name() ) );

    }

    public static void handle(final CredentialGetError error) {

        if (null != error.getCommonErrorCode()) {
            handle( error.getCommonErrorCode(), error.getErrorMessage() );
        } else {
            throw new InternalInconsistencyException( String.format( "No error code found in error, message=\"%s\"", error.getErrorMessage() ) );
        }

    }

    public static void handle(final CredentialRemoveError error)
            throws LinkIDCredentialRemoveException {

        if (null != error.getCommonErrorCode()) {
            handle( error.getCommonErrorCode(), error.getErrorMessage() );
        } else if (null != error.getErrorCode()) {
            switch (error.getErrorCode()) {

                case ERROR_UNKNOWN_CREDENTIAL:
                    throw new LinkIDCredentialRemoveException( error.getErrorMessage(), LinkIDCredentialRemoveErrorCode.ERROR_UNKNOWN_CREDENTIAL );
            }
        } else {
            throw new InternalInconsistencyException( String.format( "No error code found in error, message=\"%s\"", error.getErrorMessage() ) );
        }

    }

    public static void handle(final CredentialListError error) {

        if (null != error.getCommonErrorCode()) {
            handle( error.getCommonErrorCode(), error.getErrorMessage() );
        } else {
            throw new InternalInconsistencyException( String.format( "No error code found in error, message=\"%s\"", error.getErrorMessage() ) );
        }
    }

    public static <E extends CommonError> void handle(final E error, final ErrorHandler<E> handler) {

        if (null != error.getCommonErrorCode()) {
            handle( error.getCommonErrorCode(), error.getErrorMessage() );
        } else {
            handler.handle( error );
        }
    }

    public interface ErrorHandler<E extends CommonError> {

        void handle(E error);
    }

}
