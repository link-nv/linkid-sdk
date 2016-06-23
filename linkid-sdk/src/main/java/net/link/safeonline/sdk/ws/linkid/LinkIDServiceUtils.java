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
import net.lin_k.linkid._3_1.core.ApplicationFilter;
import net.lin_k.linkid._3_1.core.AuthCancelErrorCode;
import net.lin_k.linkid._3_1.core.AuthPollErrorCode;
import net.lin_k.linkid._3_1.core.AuthStartErrorCode;
import net.lin_k.linkid._3_1.core.Callback;
import net.lin_k.linkid._3_1.core.CallbackPullErrorCode;
import net.lin_k.linkid._3_1.core.ConfigApplicationsErrorCode;
import net.lin_k.linkid._3_1.core.ConfigLocalizationErrorCode;
import net.lin_k.linkid._3_1.core.ConfigLocalizationKeyType;
import net.lin_k.linkid._3_1.core.ConfigWalletApplicationsErrorCode;
import net.lin_k.linkid._3_1.core.Currency;
import net.lin_k.linkid._3_1.core.FavoritesConfiguration;
import net.lin_k.linkid._3_1.core.LTQRBulkPushErrorCode;
import net.lin_k.linkid._3_1.core.LTQRChangeErrorCode;
import net.lin_k.linkid._3_1.core.LTQRContent;
import net.lin_k.linkid._3_1.core.LTQRErrorCode;
import net.lin_k.linkid._3_1.core.LTQRLockType;
import net.lin_k.linkid._3_1.core.LTQRPollingConfiguration;
import net.lin_k.linkid._3_1.core.LTQRPushErrorCode;
import net.lin_k.linkid._3_1.core.Localization;
import net.lin_k.linkid._3_1.core.LocalizedImage;
import net.lin_k.linkid._3_1.core.LocalizedImages;
import net.lin_k.linkid._3_1.core.MandatePaymentErrorCode;
import net.lin_k.linkid._3_1.core.PaymentCaptureErrorCode;
import net.lin_k.linkid._3_1.core.PaymentConfiguration;
import net.lin_k.linkid._3_1.core.PaymentConfigurationAddErrorCode;
import net.lin_k.linkid._3_1.core.PaymentContext;
import net.lin_k.linkid._3_1.core.PaymentMethodType;
import net.lin_k.linkid._3_1.core.PaymentRefundErrorCode;
import net.lin_k.linkid._3_1.core.PaymentStatusErrorCode;
import net.lin_k.linkid._3_1.core.PaymentStatusType;
import net.lin_k.linkid._3_1.core.QRCodeInfo;
import net.lin_k.linkid._3_1.core.ReportApplicationFilter;
import net.lin_k.linkid._3_1.core.ReportDateFilter;
import net.lin_k.linkid._3_1.core.ReportErrorCode;
import net.lin_k.linkid._3_1.core.ReportPageFilter;
import net.lin_k.linkid._3_1.core.ReportWalletFilter;
import net.lin_k.linkid._3_1.core.ThemeAddErrorCode;
import net.lin_k.linkid._3_1.core.ThemeColorError;
import net.lin_k.linkid._3_1.core.ThemeColorErrorCode;
import net.lin_k.linkid._3_1.core.ThemeImageError;
import net.lin_k.linkid._3_1.core.ThemeImageErrorCode;
import net.lin_k.linkid._3_1.core.ThemeRemoveErrorCode;
import net.lin_k.linkid._3_1.core.ThemeStatusCode;
import net.lin_k.linkid._3_1.core.ThemeStatusErrorCode;
import net.lin_k.linkid._3_1.core.ThemeStatusErrorReport;
import net.lin_k.linkid._3_1.core.Themes;
import net.lin_k.linkid._3_1.core.ThemesErrorCode;
import net.lin_k.linkid._3_1.core.UserFilter;
import net.lin_k.linkid._3_1.core.Voucher;
import net.lin_k.linkid._3_1.core.VoucherEventTypeFilter;
import net.lin_k.linkid._3_1.core.VoucherHistoryEvent;
import net.lin_k.linkid._3_1.core.VoucherHistoryEventType;
import net.lin_k.linkid._3_1.core.VoucherListErrorCode;
import net.lin_k.linkid._3_1.core.VoucherListRedeemedErrorCode;
import net.lin_k.linkid._3_1.core.VoucherOrganization;
import net.lin_k.linkid._3_1.core.VoucherOrganizationActivateErrorCode;
import net.lin_k.linkid._3_1.core.VoucherOrganizationAddPermissionErrorCode;
import net.lin_k.linkid._3_1.core.VoucherOrganizationAddUpdateErrorCode;
import net.lin_k.linkid._3_1.core.VoucherOrganizationDetails;
import net.lin_k.linkid._3_1.core.VoucherOrganizationHistoryErrorCode;
import net.lin_k.linkid._3_1.core.VoucherOrganizationListErrorCode;
import net.lin_k.linkid._3_1.core.VoucherOrganizationListPermissionsErrorCode;
import net.lin_k.linkid._3_1.core.VoucherOrganizationListUsersErrorCode;
import net.lin_k.linkid._3_1.core.VoucherOrganizationPermissionType;
import net.lin_k.linkid._3_1.core.VoucherOrganizationRemoveErrorCode;
import net.lin_k.linkid._3_1.core.VoucherOrganizationRemovePermissionErrorCode;
import net.lin_k.linkid._3_1.core.VoucherOrganizationStats;
import net.lin_k.linkid._3_1.core.VoucherRedeemErrorCode;
import net.lin_k.linkid._3_1.core.VoucherRewardErrorCode;
import net.lin_k.linkid._3_1.core.WalletAddCreditErrorCode;
import net.lin_k.linkid._3_1.core.WalletCommitErrorCode;
import net.lin_k.linkid._3_1.core.WalletEnrollErrorCode;
import net.lin_k.linkid._3_1.core.WalletGetInfoErrorCode;
import net.lin_k.linkid._3_1.core.WalletInfoReportErrorCode;
import net.lin_k.linkid._3_1.core.WalletOrganization;
import net.lin_k.linkid._3_1.core.WalletOrganizationDetails;
import net.lin_k.linkid._3_1.core.WalletOrganizationListErrorCode;
import net.lin_k.linkid._3_1.core.WalletOrganizationStats;
import net.lin_k.linkid._3_1.core.WalletReleaseErrorCode;
import net.lin_k.linkid._3_1.core.WalletRemoveCreditErrorCode;
import net.lin_k.linkid._3_1.core.WalletRemoveErrorCode;
import net.lin_k.linkid._3_1.core.WalletReportInfo;
import net.lin_k.linkid._3_1.core.WalletReportType;
import net.lin_k.linkid._3_1.core.WalletReportTypeFilter;
import net.lin_k.safe_online.ltqr._5.PollingConfiguration;
import net.link.safeonline.sdk.api.LinkIDConstants;
import net.link.safeonline.sdk.api.callback.LinkIDCallback;
import net.link.safeonline.sdk.api.common.LinkIDApplicationFilter;
import net.link.safeonline.sdk.api.common.LinkIDUserFilter;
import net.link.safeonline.sdk.api.localization.LinkIDLocalizationValue;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentAmount;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentMandate;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentMethodType;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentState;
import net.link.safeonline.sdk.api.paymentconfiguration.LinkIDPaymentConfiguration;
import net.link.safeonline.sdk.api.qr.LinkIDQRInfo;
import net.link.safeonline.sdk.api.reporting.LinkIDReportApplicationFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportDateFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportErrorCode;
import net.link.safeonline.sdk.api.reporting.LinkIDReportPageFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportWalletFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletInfoReportErrorCode;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletReportType;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletReportTypeFilter;
import net.link.safeonline.sdk.api.themes.LinkIDThemeColorError;
import net.link.safeonline.sdk.api.themes.LinkIDThemeColorErrorCode;
import net.link.safeonline.sdk.api.themes.LinkIDThemeImageError;
import net.link.safeonline.sdk.api.themes.LinkIDThemeImageErrorCode;
import net.link.safeonline.sdk.api.themes.LinkIDThemeStatusCode;
import net.link.safeonline.sdk.api.themes.LinkIDThemeStatusErrorReport;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucher;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherEventTypeFilter;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherHistoryEvent;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherHistoryEventType;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherOrganization;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherOrganizationDetails;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherOrganizationStats;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherPermissionType;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletOrganization;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletOrganizationDetails;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletOrganizationStats;
import net.link.safeonline.sdk.api.ws.callback.LinkIDCallbackPullErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthCancelErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthPollErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDConfigApplicationsErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDConfigWalletApplicationsErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalizationErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalizationKeyType;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalizedImage;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalizedImages;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDTheme;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDThemesErrorCode;
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
import net.link.safeonline.sdk.api.ws.linkid.themes.LinkIDThemeAddErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.themes.LinkIDThemeRemoveErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.themes.LinkIDThemeStatusErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherListErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherListRedeemedErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationActivateErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationAddPermissionErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationAddUpdateErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationHistoryErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationListErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationListPermissionsErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationListUsersErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationRemoveErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationRemovePermissionErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherRedeemErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherRewardErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletAddCreditErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletCommitErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletEnrollErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletGetInfoErrorCode;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletOrganizationListErrorCode;
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
                return LinkIDConfigWalletApplicationsErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDConfigWalletApplicationsErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDConfigWalletApplicationsErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDThemesErrorCode convert(final ThemesErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNEXPECTED:
                return LinkIDThemesErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDThemesErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDLocalizationErrorCode convert(final ConfigLocalizationErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNEXPECTED:
                return LinkIDLocalizationErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDLocalizationErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDCallbackPullErrorCode convert(final CallbackPullErrorCode errorCode) {

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

    public static LinkIDPaymentCaptureErrorCode convert(final PaymentCaptureErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CAPTURE_UNKNOWN:
                return LinkIDPaymentCaptureErrorCode.ERROR_CAPTURE_UNKNOWN;
            case ERROR_CAPTURE_FAILED:
                return LinkIDPaymentCaptureErrorCode.ERROR_CAPTURE_FAILED;
            case ERROR_CAPTURE_TOKEN_NOT_FOUND:
                return LinkIDPaymentCaptureErrorCode.ERROR_CAPTURE_TOKEN_NOT_FOUND;
            case ERROR_MAINTENANCE:
                return LinkIDPaymentCaptureErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDPaymentRefundErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDLTQRErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDLTQRErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDLTQRPushErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDLTQRPushErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDLTQRBulkPushErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDLTQRBulkPushErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDReportErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDReportErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDReportErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDWalletInfoReportErrorCode convert(final WalletInfoReportErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_PERMISSION_DENIED:
                return LinkIDWalletInfoReportErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDWalletInfoReportErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletInfoReportErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDMandatePaymentErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDMandatePaymentErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDPaymentStatusErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDPaymentStatusErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDWalletEnrollErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDWalletEnrollErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletEnrollErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDWalletGetInfoErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDWalletGetInfoErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletGetInfoErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDWalletAddCreditErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDWalletAddCreditErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletAddCreditErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDWalletRemoveCreditErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDWalletRemoveCreditErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletRemoveCreditErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDWalletRemoveErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDWalletRemoveErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletRemoveErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDWalletCommitErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletCommitErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDWalletReleaseErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletReleaseErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDVoucherRewardErrorCode convert(final VoucherRewardErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_USER:
                return LinkIDVoucherRewardErrorCode.ERROR_UNEXPECTED;
            case ERROR_UNKNOWN_VOUCHER_ORGANIZATION:
                return LinkIDVoucherRewardErrorCode.ERROR_UNKNOWN_VOUCHER_ORGANIZATION;
            case ERROR_PERMISSION_DENIED:
                return LinkIDVoucherRewardErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDVoucherRewardErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDVoucherRewardErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDVoucherListErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDVoucherListErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDVoucherListErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDVoucherListRedeemedErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDVoucherListRedeemedErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDVoucherListRedeemedErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDVoucherRedeemErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDVoucherRedeemErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDVoucherRedeemErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDVoucherOrganizationAddUpdateErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDVoucherOrganizationAddUpdateErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDVoucherOrganizationAddUpdateErrorCode.ERROR_MAINTENANCE;
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

        return new LinkIDVoucher( voucher.getId(), voucher.getName(), voucher.getDescription(), voucher.getLogoUrl(), voucher.getCounter(), voucher.getLimit(),
                convert( voucher.getActivated() ), convert( voucher.getRedeemed() ), voucher.getVoucherOrganizationId() );
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
    public static LinkIDThemeStatusCode convert(@Nullable final ThemeStatusCode themeStatusCode) {

        if (null == themeStatusCode) {
            return null;
        }

        switch (themeStatusCode) {

            case STATUS_REJECTED:
                return LinkIDThemeStatusCode.REJECTED;
            case STATUS_PENDING:
                return LinkIDThemeStatusCode.PENDING;
            case STATUS_ACCEPTED:
                return LinkIDThemeStatusCode.ACCEPTED;
            case STATUS_RELEASED:
                return LinkIDThemeStatusCode.RELEASED;
        }

        throw new InternalInconsistencyException( String.format( "Unsupported themeStatusCode: \"%s\"", themeStatusCode.name() ) );

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

    public static LinkIDThemeAddErrorCode convert(final ThemeAddErrorCode errorCode) {

        if (null == errorCode) {
            return null;
        }

        switch (errorCode) {

            case ERROR_PERMISSION_DENIED:
                return LinkIDThemeAddErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDThemeAddErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDThemeAddErrorCode.ERROR_MAINTENANCE;
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

            case ERROR_PERMISSION_DENIED:
                return LinkIDThemeRemoveErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_NOT_FOUND:
                return LinkIDThemeRemoveErrorCode.ERROR_NOT_FOUND;
            case ERROR_UNEXPECTED:
                return LinkIDThemeRemoveErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDThemeRemoveErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDThemeStatusErrorCode convert(final ThemeStatusErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_PERMISSION_DENIED:
                return LinkIDThemeStatusErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_NOT_FOUND:
                return LinkIDThemeStatusErrorCode.ERROR_NOT_FOUND;
            case ERROR_UNEXPECTED:
                return LinkIDThemeStatusErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDThemeStatusErrorCode.ERROR_MAINTENANCE;
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

    public static VoucherOrganizationPermissionType convert(final LinkIDVoucherPermissionType permissionType) {

        if (null == permissionType) {
            return null;
        }

        switch (permissionType) {

            case REWARD:
                return VoucherOrganizationPermissionType.PERMISSION_REWARD;
            case LIST:
                return VoucherOrganizationPermissionType.PERMISSION_LIST;
            case REDEEM:
                return VoucherOrganizationPermissionType.PERMISSION_REDEEM;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected permission type %s!", permissionType.name() ) );

    }

    public static LinkIDVoucherPermissionType convert(final VoucherOrganizationPermissionType permissionType) {

        if (null == permissionType) {
            return null;
        }

        switch (permissionType) {

            case PERMISSION_REWARD:
                return LinkIDVoucherPermissionType.REWARD;
            case PERMISSION_LIST:
                return LinkIDVoucherPermissionType.LIST;
            case PERMISSION_REDEEM:
                return LinkIDVoucherPermissionType.REDEEM;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected permission type %s!", permissionType.name() ) );

    }

    public static LinkIDVoucherOrganizationAddPermissionErrorCode convert(final VoucherOrganizationAddPermissionErrorCode errorCode) {

        if (null == errorCode) {
            return null;
        }

        switch (errorCode) {

            case ERROR_UNKNOWN_VOUCHER_ORGANIZATION:
                return LinkIDVoucherOrganizationAddPermissionErrorCode.ERROR_UNKNOWN_VOUCHER_ORGANIZATION;
            case ERROR_UNKNOWN_APPLICATION:
                return LinkIDVoucherOrganizationAddPermissionErrorCode.ERROR_UNKNOWN_APPLICATION;
            case ERROR_PERMISSION_DENIED:
                return LinkIDVoucherOrganizationAddPermissionErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDVoucherOrganizationAddPermissionErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDVoucherOrganizationAddPermissionErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDVoucherOrganizationRemovePermissionErrorCode convert(final VoucherOrganizationRemovePermissionErrorCode errorCode) {

        if (null == errorCode) {
            return null;
        }

        switch (errorCode) {

            case ERROR_UNKNOWN_VOUCHER_ORGANIZATION:
                return LinkIDVoucherOrganizationRemovePermissionErrorCode.ERROR_UNKNOWN_VOUCHER_ORGANIZATION;
            case ERROR_UNKNOWN_APPLICATION:
                return LinkIDVoucherOrganizationRemovePermissionErrorCode.ERROR_UNKNOWN_APPLICATION;
            case ERROR_PERMISSION_DENIED:
                return LinkIDVoucherOrganizationRemovePermissionErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDVoucherOrganizationRemovePermissionErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDVoucherOrganizationRemovePermissionErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDVoucherOrganizationListPermissionsErrorCode convert(final VoucherOrganizationListPermissionsErrorCode errorCode) {

        if (null == errorCode) {
            return null;
        }

        switch (errorCode) {

            case ERROR_UNKNOWN_VOUCHER_ORGANIZATION:
                return LinkIDVoucherOrganizationListPermissionsErrorCode.ERROR_UNKNOWN_VOUCHER_ORGANIZATION;
            case ERROR_UNEXPECTED:
                return LinkIDVoucherOrganizationListPermissionsErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDVoucherOrganizationListPermissionsErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDVoucherOrganizationListErrorCode convert(final VoucherOrganizationListErrorCode errorCode) {

        if (null == errorCode) {
            return null;
        }

        switch (errorCode) {

            case ERROR_PERMISSION_DENIED:
                return LinkIDVoucherOrganizationListErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDVoucherOrganizationListErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDVoucherOrganizationListErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDVoucherOrganizationListUsersErrorCode convert(final VoucherOrganizationListUsersErrorCode errorCode) {

        if (null == errorCode) {
            return null;
        }

        switch (errorCode) {

            case ERROR_UNKNOWN_VOUCHER_ORGANIZATION:
                return LinkIDVoucherOrganizationListUsersErrorCode.ERROR_UNKNOWN_VOUCHER_ORGANIZATION;
            case ERROR_PERMISSION_DENIED:
                return LinkIDVoucherOrganizationListUsersErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDVoucherOrganizationListUsersErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDVoucherOrganizationListUsersErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDVoucherOrganizationDetails convert(final VoucherOrganizationDetails voucherOrganizationDetails) {

        return new LinkIDVoucherOrganizationDetails( convert( voucherOrganizationDetails.getOrganization() ), convert( voucherOrganizationDetails.getStats() ),
                voucherOrganizationDetails.getRewardPermissionApplications(), voucherOrganizationDetails.getListPermissionApplications(),
                voucherOrganizationDetails.getRedeemPermissionApplications() );

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
                return LinkIDVoucherOrganizationRemoveErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDVoucherOrganizationRemoveErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDVoucherOrganizationRemoveErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDVoucherOrganizationActivateErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDVoucherOrganizationActivateErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDVoucherOrganizationActivateErrorCode.ERROR_MAINTENANCE;
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
                return LinkIDVoucherOrganizationHistoryErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDVoucherOrganizationHistoryErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDVoucherOrganizationHistoryErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDConfigApplicationsErrorCode convert(final ConfigApplicationsErrorCode errorCode) {

        if (null == errorCode) {
            return null;
        }

        switch (errorCode) {

            case ERROR_UNEXPECTED:
                return LinkIDConfigApplicationsErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDConfigApplicationsErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDVoucherHistoryEvent convert(final VoucherHistoryEvent wsEvent) {

        return new LinkIDVoucherHistoryEvent( wsEvent.getId(), wsEvent.getVoucherOrganizationId(), wsEvent.getUserId(), wsEvent.getVoucherId(),
                wsEvent.getPoints(), wsEvent.getApplicationName(), convert( wsEvent.getEventType() ) );
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

    public static LinkIDWalletOrganizationListErrorCode convert(final WalletOrganizationListErrorCode errorCode) {

        if (null == errorCode) {
            return null;
        }

        switch (errorCode) {

            case ERROR_PERMISSION_DENIED:
                return LinkIDWalletOrganizationListErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDWalletOrganizationListErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletOrganizationListErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    public static LinkIDWalletOrganizationDetails convert(final WalletOrganizationDetails details) {

        return new LinkIDWalletOrganizationDetails( convert( details.getOrganization() ), convert( details.getStats() ),
                details.getPermissionAddCreditApplications(), details.getPermissionRemoveCreditApplications(), details.getPermissionRemoveApplications(),
                details.getPermissionEnrollApplications(), details.getPermissionUseApplications() );

    }

    public static LinkIDWalletOrganization convert(final WalletOrganization request) {

        return new LinkIDWalletOrganization( request.getWalletOrganizationId(), request.getLogoUrl(), request.getExpirationInSecs(), request.isSticky(),
                request.isAutoEnroll(), convertLocalizations( request.getNameLocalization() ), convertLocalizations( request.getDescriptionLocalization() ) );

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
                return LinkIDPaymentConfigurationAddErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDPaymentConfigurationAddErrorCode.ERROR_MAINTENANCE;
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
}
