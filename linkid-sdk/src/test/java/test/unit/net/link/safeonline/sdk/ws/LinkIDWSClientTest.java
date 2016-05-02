package test.unit.net.link.safeonline.sdk.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import net.link.safeonline.sdk.api.auth.LinkIDAuthenticationContext;
import net.link.safeonline.sdk.api.auth.LinkIDAuthnResponse;
import net.link.safeonline.sdk.api.localization.LinkIDLocalizationValue;
import net.link.safeonline.sdk.api.parking.LinkIDParkingSession;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentAmount;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentOrder;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentTransaction;
import net.link.safeonline.sdk.api.payment.LinkIDWalletTransaction;
import net.link.safeonline.sdk.api.reporting.LinkIDParkingReport;
import net.link.safeonline.sdk.api.reporting.LinkIDPaymentReport;
import net.link.safeonline.sdk.api.reporting.LinkIDReportDateFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportPageFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportWalletFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletInfoReport;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletReport;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletReportTransaction;
import net.link.safeonline.sdk.api.themes.LinkIDThemeConfig;
import net.link.safeonline.sdk.api.themes.LinkIDThemeStatus;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherOrganization;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherPermissionType;
import net.link.safeonline.sdk.api.voucher.LinkIDVouchers;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletInfo;
import net.link.safeonline.sdk.api.ws.data.client.LinkIDDataClient;
import net.link.safeonline.sdk.api.ws.linkid.LinkIDServiceClient;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthSession;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDApplication;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalization;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalizedImage;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDTheme;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDThemes;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDThemesException;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRContent;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRInfo;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRLockType;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRPushContent;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRPushResponse;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRSession;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDPaymentStatus;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationAddPermissionException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationRemovePermissionException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletAddCreditException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletEnrollException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletGetInfoException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletRemoveCreditException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletRemoveException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletReportInfo;
import net.link.safeonline.sdk.ws.data.LinkIDDataClientImpl;
import net.link.safeonline.sdk.ws.linkid.LinkIDServiceClientImpl;
import net.link.util.common.ApplicationMode;
import net.link.util.logging.Logger;
import net.link.util.ws.security.username.AbstractWSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;


/**
 * Created by wvdhaute
 * Date: 02/04/14
 * Time: 13:15
 */
@SuppressWarnings("unused")
public class LinkIDWSClientTest {

    private static final Logger logger = Logger.get( LinkIDWSClientTest.class );

    //private static final String WS_LOCATION = "https://demo.linkid.be/linkid-ws-username";
    private static final String WS_LOCATION = "https://192.168.5.14:8443/linkid-ws-username";

    // demo config
    private static final String APP_NAME     = "example-mobile";
    private static final String APP_USERNAME = "example-mobile";
    private static final String APP_PASSWORD = "6E6C1CB7-965C-48A0-B2B0-6B65674BE19F";

    private String              wsLocation;
    private LinkIDServiceClient client;

    @Before
    public void setUp()
            throws Exception {

        // DEBUG so ssl validation is skipped for local self signed ssl cert, obv do not do this in production, nor even against demo.linkid.be for that matter.
        System.setProperty( ApplicationMode.PROPERTY, ApplicationMode.DEBUG.name() );
        this.wsLocation = WS_LOCATION;

        client = getLinkIDServiceClient();
    }

    @Test
    public void testDummy() {

    }

    //    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testData()
            throws Exception {

        String userId = "2b35dbab-2ba2-403b-8c36-a8399c3af3d5";

        LinkIDDataClient client = new LinkIDDataClientImpl( wsLocation, null, getUsernameTokenCallback() );
        List attributes = client.getAttributes( userId, "profile.givenName" );

        // set
        client.setAttributeValue( userId, attributes );

        LinkIDAttribute<String> attribute = (LinkIDAttribute<String>) attributes.get( 0 );

        // remove
        client.removeAttribute( userId, "profile.givenName", attribute.getId() );

        client.setAttributeValue( userId, attributes );
    }

    //    @Test
    public void testCallbackPull()
            throws Exception {

        // setup
        String sessionId = "00a44454-acbb-488e-ab54-6a7934a54bb1";

        // operate
        LinkIDAuthnResponse response = client.callbackPull( sessionId );

        // verify
        assertNotNull( response );
    }

    //    @Test
    public void testPaymentReport()
            throws Exception {

        //        List<String> orderReferences = Collections.singletonList( "e527bd0748864a07bd7781aa42e9cca2" );

        LinkIDPaymentReport paymentReport = client.paymentReport( new LinkIDReportDateFilter( DateTime.now().minusMonths( 10 ).toDate(), null ), null, null,
                null );
        logger.inf( "# orders = %d", paymentReport.getPaymentOrders().size() );

        for (LinkIDPaymentOrder linkIDPaymentOrder : paymentReport.getPaymentOrders()) {
            logger.inf( "Order: %s", linkIDPaymentOrder );
            for (LinkIDPaymentTransaction linkIDPaymentTransaction : linkIDPaymentOrder.getTransactions()) {
                logger.inf( "Payment transaction: %s", linkIDPaymentTransaction );
            }
            for (LinkIDWalletTransaction linkIDWalletTransaction : linkIDPaymentOrder.getWalletTransactions()) {
                logger.inf( "Wallet transaction: %s", linkIDWalletTransaction );
            }
        }
    }

    //    @Test
    public void testParkingReport()
            throws Exception {

        LinkIDReportDateFilter dateFilter = new LinkIDReportDateFilter( DateTime.now().minusYears( 1 ).toDate(), null );

        LinkIDParkingReport parkingReport = client.parkingReport( dateFilter, null, null, null, null, null );
        logger.inf( "# sessions = %d", parkingReport.getParkingSessions().size() );

        for (LinkIDParkingSession linkIDParkingSession : parkingReport.getParkingSessions()) {
            logger.inf( "Session: %s", linkIDParkingSession );
        }
    }

    //    @Test
    public void testWalletReport()
            throws Exception {

        //        LinkIDWalletReportTypeFilter walletReportTypeFilter = new LinkIDWalletReportTypeFilter( Arrays.asList( LinkIDWalletReportType.USER_TRANSACTION ) );

        LinkIDWalletReport walletReport = client.walletReport( new Locale( "nl" ), "urn:linkid:wallet:fake:visa", null,
                new LinkIDReportWalletFilter( "53EB61D1-731C-4711-A4D4-20AF824AB86C" ), null,
                new LinkIDReportDateFilter( DateTime.now().minusYears( 1 ).toDate(), null ), new LinkIDReportPageFilter( 0, 40 ) );
        logger.inf( "Total = %d", walletReport.getTotal() );
        logger.inf( "# txns = %d", walletReport.getWalletTransactions().size() );

        for (LinkIDWalletReportTransaction transaction : walletReport.getWalletTransactions()) {
            logger.inf( "transaction: %s (wallet: %s)", transaction, transaction.getWalletId() );
        }
    }

    //    @Test
    public void testWalletInfoReport()
            throws Exception {

        // setup
        List<String> walletIds = Lists.newLinkedList();
        walletIds.add( "123b1c22-e6c5-4ebc-9255-e59b72db5abf" );
        walletIds.add( "13ff6203-a086-483a-8e3c-382ce63f9a9a" );
        walletIds.add( "foo" );

        // operate
        List<LinkIDWalletInfoReport> result = client.walletInfoReport( new Locale( "nl" ), walletIds );

        // verify
        assertNotNull( result );
        assertEquals( walletIds.size() - 1, result.size() );
        for (LinkIDWalletInfoReport info : result) {
            logger.inf( "Info: %s", info );
        }
    }

    //    @Test
    public void testWalletEnrollment()
            throws Exception {

        // operate
        try {
            //            String walletId = client.enroll( userId, walletOrganizationId, 500, LinkIDCurrency.EUR, null );
            String walletOrganizationId = "urn:linkid:wallet:leaseplan";
            String userId = "e4269366-ddfb-43dc-838d-01569a8c4c22";
            String walletId = client.walletEnroll( userId, walletOrganizationId, 500, null, "urn:linkid:wallet:coin:coffee", null );
            logger.inf( "Enrolled wallet: %s", walletId );
        }
        catch (LinkIDWalletEnrollException e) {
            logger.err( "Enroll error: %s", e.getErrorCode() );
            fail();
        }
    }

    //    @Test
    public void testWalletGetInfo()
            throws Exception {

        // operate
        try {
            String walletOrganizationId = "urn:linkid:wallet:leaseplan";
            String userId = "e4269366-ddfb-43dc-838d-01569a8c4c22";
            LinkIDWalletInfo walletInfo = client.walletGetInfo( userId, walletOrganizationId );
            logger.inf( "Wallet info: %s", walletInfo );
        }
        catch (LinkIDWalletGetInfoException e) {
            logger.err( "GetInfo error: %s", e.getErrorCode() );
            fail();
        }
    }

    //    @Test
    public void testWalletAdd()
            throws Exception {

        // setup
        String userId = "e4269366-ddfb-43dc-838d-01569a8c4c22";
        String walletOrganizationId = "urn:linkid:wallet:leaseplan";
        String walletCoin = "urn:linkid:wallet:coin:coffee";
        LinkIDWalletReportInfo reportInfo = new LinkIDWalletReportInfo( UUID.randomUUID().toString(), "unit test" );

        // operate
        String walletId = client.walletEnroll( userId, walletOrganizationId, 1, null, walletCoin, reportInfo );

        // verify
        assertNotNull( walletId );
    }

    //    @Test
    public void testWalletAddCredit()
            throws Exception {

        // operate
        try {
            //            client.addCredit( userId, walletId, 100, LinkIDCurrency.EUR, null );
            String walletId = "341a8eca-45d4-4a18-a193-03a150d24185";
            String userId = "e4269366-ddfb-43dc-838d-01569a8c4c22";
            String walletCoin = "urn:linkid:wallet:coin:coffee";
            LinkIDWalletReportInfo reportInfo = new LinkIDWalletReportInfo( UUID.randomUUID().toString(), "unit test" );
            client.walletAddCredit( userId, walletId, 100, null, walletCoin, reportInfo );
        }
        catch (LinkIDWalletAddCreditException e) {
            logger.err( "Add credit error: %s", e.getErrorCode() );
            fail();
        }
    }

    //    @Test
    public void testWalletRemoveCredit()
            throws Exception {

        // operate
        try {
            //            client.removeCredit( userId, walletId, 100, LinkIDCurrency.EUR, null );
            String walletId = "341a8eca-45d4-4a18-a193-03a150d24185";
            String userId = "e4269366-ddfb-43dc-838d-01569a8c4c22";
            String walletCoin = "urn:linkid:wallet:coin:coffee";
            LinkIDWalletReportInfo reportInfo = new LinkIDWalletReportInfo( UUID.randomUUID().toString(), "unit test" );
            client.walletRemoveCredit( userId, walletId, 100, null, walletCoin, reportInfo );
        }
        catch (LinkIDWalletRemoveCreditException e) {
            logger.err( "Remove credit error: %s", e.getErrorCode() );
            fail();
        }
    }

    //    @Test
    public void testWalletRemove()
            throws Exception {

        // operate
        try {
            String walletId = "123b1c22-e6c5-4ebc-9255-e59b72db5abf";
            String userId = "9e4d2818-d9d4-454c-9b1d-1f067a1f7469";
            client.walletRemove( userId, walletId );
        }
        catch (LinkIDWalletRemoveException e) {
            logger.err( "Remove error: %s", e.getErrorCode() );
            fail();
        }
    }

    //    @Test
    public void testStartAuthentication()
            throws Exception {

        // setup
        String language = "be";
        LinkIDPaymentContext paymentContext = new LinkIDPaymentContext.Builder( new LinkIDPaymentAmount( 100, LinkIDCurrency.EUR ) ).build();

        LinkIDAuthenticationContext context = new LinkIDAuthenticationContext.Builder( APP_NAME ).language( new Locale( language ) )
                                                                                                 .notificationLocation( "https://demo.linkid.be" )
                                                                                                 .build();

        // operate: start
        try {
            String userAgent = "unit-test";
            LinkIDAuthSession session = client.authStart( context, userAgent );

            // write out QR image
            ByteArrayInputStream bais = new ByteArrayInputStream( session.getQrCodeInfo().getQrImage() );
            BufferedImage qrImage = ImageIO.read( bais );
            ImageIO.write( qrImage, "png", new File( "qr.png" ) );

        }
        catch (LinkIDAuthException e) {
            logger.inf( "Error message: %s", e.getMessage() );
            assertNotNull( e.getMessage() );
        }

    }

    //    @Test
    public void testCancelAuthentication()
            throws Exception {

        // setup
        String sessionId = "WpowEE";

        // operate
        client.authCancel( sessionId );
    }

    //    @Test
    public void testPaymentStatus()
            throws Exception {

        // setup
        String orderReference = "2efde30165c848e7963a699281042db7";

        // operate
        LinkIDPaymentStatus linkIDPaymentStatus = client.getPaymentStatus( orderReference );

        // verify
        assertNotNull( linkIDPaymentStatus );
        assertEquals( orderReference, linkIDPaymentStatus.getOrderReference() );
        assertNotNull( linkIDPaymentStatus.getUserId() );
        logger.inf( "Payment status: %s", linkIDPaymentStatus );
        logger.inf( "Payment status: %s", linkIDPaymentStatus.getPaymentDetails() );
        for (LinkIDPaymentTransaction paymentTransaction : linkIDPaymentStatus.getPaymentDetails().getTransactions()) {
            logger.inf( "Payment transaction: %s", paymentTransaction );
        }
        for (LinkIDWalletTransaction walletTransaction : linkIDPaymentStatus.getPaymentDetails().getWalletTransactions()) {
            logger.inf( "Wallet transaction: %s", walletTransaction );
        }
    }

    //    @Test
    public void testLTQRPush()
            throws Exception {

        // setup
        LinkIDLTQRPushContent pushContent = generatePushContent();

        // operate
        LinkIDLTQRSession linkIDLTQRSession = client.ltqrPush( pushContent.getContent(), pushContent.getUserAgent(), pushContent.getLockType() );

        // verify
        assertNotNull( linkIDLTQRSession );
        assertNotNull( linkIDLTQRSession.getLtqrReference() );

        logger.dbg( "QR code URL: %s", linkIDLTQRSession.getQrCodeInfo().getQrCodeURL() );
        logger.dbg( "LTQR ref: %s", linkIDLTQRSession.getLtqrReference() );
        logger.dbg( "Payment order ref: %s", linkIDLTQRSession.getPaymentOrderReference() );

        // write out QR image
        ByteArrayInputStream bais = new ByteArrayInputStream( linkIDLTQRSession.getQrCodeInfo().getQrImage() );
        BufferedImage qrImage = ImageIO.read( bais );
        ImageIO.write( qrImage, "png", new File( "qr.png" ) );
    }

    //    @Test
    public void testLTQRBulkPush()
            throws Exception {

        // setup
        List<LinkIDLTQRPushContent> contents = Lists.newLinkedList();
        for (int i = 0; i < 200; i++) {
            contents.add( generatePushContent() );
        }

        // operate
        List<LinkIDLTQRPushResponse> results = client.ltqrBulkPush( contents );

        // verify
        assertNotNull( results );
        assertEquals( contents.size(), results.size() );

        // dump LTQR refs
        for (LinkIDLTQRPushResponse result : results) {
            logger.dbg( "Result: %s", result );
            logger.dbg( "===============================" );
        }

    }

    private static LinkIDLTQRPushContent generatePushContent() {

        LinkIDPaymentContext linkIDPaymentContext = new LinkIDPaymentContext.Builder( new LinkIDPaymentAmount( 10, LinkIDCurrency.EUR ) ).build();
        DateTime expiryDateTime = new DateTime();
        expiryDateTime = expiryDateTime.plusMonths( 2 );

        LinkIDLTQRContent ltqrContent = new LinkIDLTQRContent.Builder().authenticationMessage( "LTQR Test" )
                                                                       .finishedMessage( "LTQR Test finished" )
                                                                       .paymentContext( linkIDPaymentContext )
                                                                       .expiryDate( expiryDateTime.toDate() )
                                                                       .notificationLocation( "https://demo.linkid.be" )
                                                                       .theme( "eidtest" )
                                                                       .build();

        return new LinkIDLTQRPushContent( ltqrContent, null, LinkIDLTQRLockType.NEVER );
    }

    //    @Test
    public void testLTQRInfo()
            throws Exception {

        // setup
        List<String> ltqrReferences = Lists.newLinkedList();
        ltqrReferences.add( "856eed32-2119-4f94-b705-f177079e1b9e" );

        // operate
        List<LinkIDLTQRInfo> linkIDLTQRInfos = client.ltqrInfo( ltqrReferences, null );

        // verify
        assertNotNull( linkIDLTQRInfos );
        assertEquals( ltqrReferences.size(), linkIDLTQRInfos.size() );

        for (LinkIDLTQRInfo linkIDLTQRInfo : linkIDLTQRInfos) {
            logger.dbg( "LTQRInfo: %s", linkIDLTQRInfo );
        }

    }

    //    @Test
    public void testMandatePayment()
            throws Exception {

        // setup
        String mandateReference = "67106414-0020-47dd-b755-ee74e16d9e1f";
        LinkIDPaymentContext linkIDPaymentContext = new LinkIDPaymentContext.Builder( new LinkIDPaymentAmount( 1, LinkIDCurrency.EUR ) ).description(
                "Test description" ).build();

        // operate
        String orderReference = client.mandatePayment( mandateReference, linkIDPaymentContext, null, Locale.ENGLISH );

        // verify
        assertNotNull( orderReference );
    }

    //    @Test
    public void testCapture()
            throws Exception {

        // Setup
        String orderReference = "foo";

        // operate
        client.paymentCapture( orderReference );
    }

    //    @Test
    public void testConfigWalletApplications()
            throws Exception {

        // Setup
        String walletOrganizationId = "urn:linkid:wallet:leaseplan";

        // Operate
        List<LinkIDApplication> applications = client.configWalletApplications( walletOrganizationId, Locale.ENGLISH );

        // Verify
        assertNotNull( applications );
        assertEquals( 3, applications.size() );
    }

    //    @Test
    public void testGetLocalization()
            throws Exception {

        // Setup
        List<String> keys = Lists.newLinkedList();
        keys.add( "urn:linkid:wallet:coin:coffee" );
        keys.add( "urn:linkid:wallet:leaseplan" );

        // operate
        List<LinkIDLocalization> localizations = client.getLocalization( keys );

        // verify
        assertEquals( 4, localizations.size() );
        assertEquals( 4, localizations.get( 0 ).getValues().size() );
    }

    //    @Test
    public void testPaymentRefund()
            throws Exception {

        // Setup
        String orderReference = "24e01f29c9434adb842331f5399b545a";

        // operate
        client.paymentRefund( orderReference );
    }

    //    @Test
    public void testVoucherReward()
            throws Exception {

        // Setup
        String userId = "e4269366-ddfb-43dc-838d-01569a8c4c22";
        String voucherOrganizationId = "E50CCE04-9FFB-44B2-814A-3E08524C50CF";

        // Operate
        client.voucherReward( userId, voucherOrganizationId, 15 );
    }

    //    @Test
    public void testVoucherList()
            throws Exception {

        // Setup
        String userId = "e4269366-ddfb-43dc-838d-01569a8c4c22";
        String voucherOrganizationId = "E50CCE04-9FFB-44B2-814A-3E08524C50CF";

        // Operate
        LinkIDVouchers vouchers = client.voucherList( userId, voucherOrganizationId, Locale.ENGLISH );

        // Verify
        assertNotNull( vouchers );
        assertEquals( vouchers.getTotal(), vouchers.getVouchers().size() );
    }

    //    @Test
    public void voucherListRedeemed()
            throws Exception {

        // Setup
        String userId = "e4269366-ddfb-43dc-838d-01569a8c4c22";
        String voucherOrganizationId = "E50CCE04-9FFB-44B2-814A-3E08524C50CF";

        // Operate
        LinkIDVouchers vouchers = client.voucherListRedeemed( userId, voucherOrganizationId, Locale.ENGLISH, null, null );

        // Verify
        assertNotNull( vouchers );
        assertEquals( vouchers.getTotal(), vouchers.getVouchers().size() );
    }

    //    @Test
    public void testVoucherRedeem()
            throws Exception {

        // Setup
        String voucherId = "089f7b53-c34c-44cf-b64a-769744d854c7";

        // Operate
        client.voucherRedeem( voucherId );
    }

    //    @Test
    public void testThemeAdd()
            throws Exception {

        // setup
        String applicationName = "test-shop";
        List<LinkIDLocalizedImage> logos = Collections.singletonList(
                new LinkIDLocalizedImage( "https://s3-eu-west-1.amazonaws.com/linkid-production/image/apps/iwish.png" ) );
        //        List<LinkIDLocalizedImage> logos = Collections.singletonList( new LinkIDLocalizedImage( "http://www.kaagent.be/assets/images/icons/icon_200.jpg" ) );
        List<LinkIDLocalizedImage> backgrounds = Collections.singletonList(
                new LinkIDLocalizedImage( "https://s3-eu-west-1.amazonaws.com/linkid-production/image/apps/bg/phone/iwish.png" ) );
        List<LinkIDLocalizedImage> tabletBackgrounds = Collections.singletonList(
                new LinkIDLocalizedImage( "https://s3-eu-west-1.amazonaws.com/linkid-production/image/apps/bg/tablet/iwish.png" ) );
        List<LinkIDLocalizedImage> altBackgrounds = Collections.singletonList(
                new LinkIDLocalizedImage( "https://s3-eu-west-1.amazonaws.com/linkid-production/image/apps/bg/alternative/iwish.png" ) );
        //        List<LinkIDLocalizedImage> logos = Collections.singletonList(
        //                new LinkIDLocalizedImage( "https://service.linkid.be/linkid-static/js/linkid.extra.js" ) );

        //        LinkIDThemeConfig config = new LinkIDThemeConfig( "urn:be:linkid:example-mobile:theme:themeTest", "Theme test", false, logos, logos, backgrounds,
        //                tabletBackgrounds, altBackgrounds, "#000000", "#FFFFFF" );
        LinkIDThemeConfig config = new LinkIDThemeConfig( "themeTest", "Theme test", false, logos, logos, backgrounds, tabletBackgrounds, altBackgrounds,
                "#000000", "#FFFFFF" );

        // operate
        String themeName = client.themeAdd( config );

        // verify
        assertNotNull( themeName );
        logger.dbg( "Theme name: %s", themeName );
    }

    //    @Test
    public void testThemeStatus()
            throws Exception {

        // operate
        LinkIDThemeStatus status = client.themeStatus( "urn:be:linkid:example-mobile:themeTest" );

        // verify
        assertNotNull( status );
        logger.dbg( "Status: %s", status );

    }

    //    @Test
    public void testThemeRemove()
            throws Exception {

        // operate
        client.themeRemove( "template-blue", true );

    }

    //    @Test
    public void testThemes()
            throws Exception {

        // operate
        try {
            //            LinkIDThemes linkIDThemes = client.themes( "urn:be:linkid:example-mobile:theme:themeTest", null );
            LinkIDThemes linkIDThemes = client.themes( null, null );
            assertNotNull( linkIDThemes );
            for (LinkIDTheme linkIDTheme : linkIDThemes.getThemes()) {
                logger.dbg( "Theme: %s", linkIDTheme );
            }
        }
        catch (LinkIDThemesException e) {
            logger.err( "Themes error: %s", e.getErrorCode() );
            fail();
        }
    }

    //    @Test
    public void testVoucherOrganizationAdd()
            throws Exception {

        // setup
        LinkIDVoucherOrganization voucherOrganization = new LinkIDVoucherOrganization( "test",
                "https://s3-eu-west-1.amazonaws.com/linkid-production/image/apps/iwish.png", 4,
                Collections.singletonList( new LinkIDLocalizationValue( Locale.ENGLISH.getLanguage(), "Test voucher" ) ),
                Collections.singletonList( new LinkIDLocalizationValue( Locale.ENGLISH.getLanguage(), "Test voucher description" ) ) );

        // operate
        String voucherName = client.voucherOrganizationAddUpdate( voucherOrganization );

        // verify
        assertNotNull( voucherName );
        logger.dbg( "Voucher name: %s", voucherName );
    }

    //    @Test
    public void testVoucherOrganizationAddPermission()
            throws Exception {

        // operate
        try {
            client.voucherOrganizationAddPermission( "urn:be:linkid:example-mobile:theme:test", "linkID-oper", LinkIDVoucherPermissionType.LIST );
            client.voucherOrganizationAddPermission( "urn:be:linkid:example-mobile:theme:test", "linkID-oper", LinkIDVoucherPermissionType.REWARD );
            client.voucherOrganizationAddPermission( "urn:be:linkid:example-mobile:theme:test", "linkID-oper", LinkIDVoucherPermissionType.REDEEM );
        }
        catch (LinkIDVoucherOrganizationAddPermissionException e) {
            fail();
        }
    }

    //    @Test
    public void testVoucherOrganizationRemovePermission()
            throws Exception {

        // operate
        try {
            //            client.voucherOrganizationRemovePermission( "urn:be:linkid:example-mobile:theme:test", null, LinkIDVoucherPermissionType.LIST );
            client.voucherOrganizationRemovePermission( "urn:be:linkid:example-mobile:theme:test", "linkID-oper", LinkIDVoucherPermissionType.LIST );
            client.voucherOrganizationRemovePermission( "urn:be:linkid:example-mobile:theme:test", "linkID-oper", LinkIDVoucherPermissionType.REWARD );
            client.voucherOrganizationRemovePermission( "urn:be:linkid:example-mobile:theme:test", "linkID-oper", LinkIDVoucherPermissionType.REDEEM );
        }
        catch (LinkIDVoucherOrganizationRemovePermissionException e) {
            fail();
        }
    }

    @Test
    public void testVoucherOrganizationListPermissions()
            throws Exception {

        // operate
        List<LinkIDVoucherPermissionType> permissions = client.voucherOrganizationListPermissions( "urn:be:linkid:example-mobile:theme:test" );

        // verify
        assertNotNull( permissions );
        for (LinkIDVoucherPermissionType permission : permissions) {
            logger.dbg( "Permission: %s", permission );
        }
    }

    // Auth

    private LinkIDServiceClient getLinkIDServiceClient() {

        return new LinkIDServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );
    }

    private static WSSecurityUsernameTokenCallback getUsernameTokenCallback() {

        return new AbstractWSSecurityUsernameTokenCallback() {
            @Override
            public String getUsername() {

                return APP_USERNAME;
            }

            @Override
            public String getPassword() {

                return APP_PASSWORD;
            }

            @Override
            public boolean isInboundHeaderOptional() {

                return true;
            }

            @Nullable
            @Override
            public String handle(final String username) {

                return null;
            }
        };
    }
}
