package test.unit.net.link.safeonline.sdk.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.link.safeonline.attribute.provider.profile.LinkIDProfileConstants;
import net.link.safeonline.sdk.api.LinkIDConstants;
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import net.link.safeonline.sdk.api.auth.LinkIDAuthenticationContext;
import net.link.safeonline.sdk.api.auth.LinkIDAuthnResponse;
import net.link.safeonline.sdk.api.common.LinkIDApplicationFilter;
import net.link.safeonline.sdk.api.common.LinkIDRequestStatusCode;
import net.link.safeonline.sdk.api.common.LinkIDUserFilter;
import net.link.safeonline.sdk.api.credentials.LinkIDCredential;
import net.link.safeonline.sdk.api.credentials.LinkIDCredentialRequest;
import net.link.safeonline.sdk.api.credentials.LinkIDCredentialType;
import net.link.safeonline.sdk.api.localization.LinkIDLocalizationValue;
import net.link.safeonline.sdk.api.notification.LinkIDNotificationLocation;
import net.link.safeonline.sdk.api.notification.LinkIDNotificationTopic;
import net.link.safeonline.sdk.api.notification.LinkIDNotificationTopicConfiguration;
import net.link.safeonline.sdk.api.parking.LinkIDParkingSession;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.LinkIDMandateRemoveResult;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentAmount;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentInfo;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentMandate;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentMethodType;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentOrder;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentTransaction;
import net.link.safeonline.sdk.api.payment.LinkIDWalletTransaction;
import net.link.safeonline.sdk.api.paymentconfiguration.LinkIDPaymentConfiguration;
import net.link.safeonline.sdk.api.permissions.LinkIDApplicationPermissionType;
import net.link.safeonline.sdk.api.permissions.LinkIDApplicationPermissions;
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
import net.link.safeonline.sdk.api.users.LinkIDUser;
import net.link.safeonline.sdk.api.users.LinkIDUsers;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucher;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherEventTypeFilter;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherHistory;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherHistoryEvent;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherHistoryEventType;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherOrganization;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherOrganizationDetails;
import net.link.safeonline.sdk.api.voucher.LinkIDVouchers;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletInfo;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletOrganization;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletOrganizationDetails;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletOrganizationResult;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletPolicyBalance;
import net.link.safeonline.sdk.api.ws.data.client.LinkIDDataClient;
import net.link.safeonline.sdk.api.ws.linkid.LinkIDServiceClient;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthSession;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDApplication;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDApplicationDetails;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalization;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalizedImage;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDTheme;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDThemes;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRContent;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRInfo;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRLockType;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRPushContent;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRPushResponse;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRSession;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDPaymentStatus;
import net.link.safeonline.sdk.api.ws.linkid.permissions.LinkIDApplicationPermissionAddException;
import net.link.safeonline.sdk.api.ws.linkid.permissions.LinkIDApplicationPermissionRemoveException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletAddCreditException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletEnrollException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletGetInfoException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletRemoveCreditException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletRemoveException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletReportInfo;
import net.link.safeonline.sdk.configuration.LinkIDConfig;
import net.link.safeonline.sdk.ws.LinkIDSDKWSSecurityConfiguration;
import net.link.safeonline.sdk.ws.data.LinkIDDataClientImpl;
import net.link.safeonline.sdk.ws.linkid.LinkIDServiceClientImpl;
import net.link.util.common.ApplicationMode;
import net.link.util.keyprovider.ResourceKeyStoreKeyProvider;
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
    private static final String WS_LOCATION_USERNAME = "https://192.168.5.14:8443/linkid-ws-username";
    private static final String WS_LOCATION          = "https://192.168.5.14:8443/linkid-ws";

    // demo config
    private static final String APP_NAME     = "example-mobile";
    private static final String APP_USERNAME = "example-mobile";
    private static final String APP_PASSWORD = "6E6C1CB7-965C-48A0-B2B0-6B65674BE19F";

    private LinkIDServiceClient client;

    @Before
    public void setUp()
            throws Exception {

        // DEBUG so ssl validation is skipped for local self signed ssl cert, obv do not do this in production, nor even against demo.linkid.be for that matter.
        System.setProperty( ApplicationMode.PROPERTY, ApplicationMode.DEBUG.name() );

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

        LinkIDDataClient client = new LinkIDDataClientImpl( WS_LOCATION_USERNAME, null, getUsernameTokenCallback() );
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

        LinkIDWalletReport walletReport = client.walletReport( new Locale( "nl" ), "urn:linkid:wallet:fake:visa", null, null,
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
    public void testWalletOrganizationAdd()
            throws Exception {

        // setup
        String logoUrl = "https://s3-eu-west-1.amazonaws.com/linkid-production/image/apps/iwish.png";
        List<LinkIDLocalizationValue> nameLocalizations = new LinkedList<>();
        nameLocalizations.add( new LinkIDLocalizationValue( "en", "iWish-en" ) );
        nameLocalizations.add( new LinkIDLocalizationValue( "nl", "iWish-nl" ) );
        List<LinkIDLocalizationValue> descriptionLocalizations = new LinkedList<>();
        descriptionLocalizations.add( new LinkIDLocalizationValue( "en", "iWish description-en" ) );
        descriptionLocalizations.add( new LinkIDLocalizationValue( "nl", "iWish description-nl" ) );

        List<LinkIDLocalizationValue> coinNameLocalization = new LinkedList<>();
        coinNameLocalization.add( new LinkIDLocalizationValue( "en", "Test coin-en" ) );
        List<LinkIDLocalizationValue> coinNameMultipleLocalization = new LinkedList<>();
        coinNameMultipleLocalization.add( new LinkIDLocalizationValue( "en", "Test coins-en" ) );

        LinkIDWalletPolicyBalance policyBalance = new LinkIDWalletPolicyBalance( 99999 );

        LinkIDWalletOrganization walletOrganization = new LinkIDWalletOrganization( "testWallet", logoUrl, 5000, false, true, true, nameLocalizations,
                descriptionLocalizations, coinNameLocalization, coinNameMultipleLocalization, null, "testCoin", policyBalance );

        // operate
        LinkIDWalletOrganizationResult result = client.walletOrganizationAdd( walletOrganization );
        assertNotNull( result );
        logger.dbg( "Organization result: %s\n", result );
    }

    //    @Test
    public void testWalletOrganizationUpdate()
            throws Exception {

        // setup
        String logoUrl = "https://s3-eu-west-1.amazonaws.com/linkid-production/image/apps/iwish.png";
        List<LinkIDLocalizationValue> nameLocalizations = new LinkedList<>();
        nameLocalizations.add( new LinkIDLocalizationValue( "en", "iWish update-en" ) );
        nameLocalizations.add( new LinkIDLocalizationValue( "nl", "iWish update-nl" ) );
        List<LinkIDLocalizationValue> descriptionLocalizations = new LinkedList<>();
        descriptionLocalizations.add( new LinkIDLocalizationValue( "en", "iWish description update-en" ) );
        descriptionLocalizations.add( new LinkIDLocalizationValue( "nl", "iWish description update-nl" ) );

        List<LinkIDLocalizationValue> coinNameLocalization = new LinkedList<>();
        coinNameLocalization.add( new LinkIDLocalizationValue( "en", "Test coin update-en" ) );
        List<LinkIDLocalizationValue> coinNameMultipleLocalization = new LinkedList<>();
        coinNameMultipleLocalization.add( new LinkIDLocalizationValue( "en", "Test coins update-en" ) );

        LinkIDWalletPolicyBalance policyBalance = new LinkIDWalletPolicyBalance( 150 );

        LinkIDWalletOrganization walletOrganization = new LinkIDWalletOrganization( "urn:be:linkid:example-mobile:wallet:organization:testWallet", logoUrl,
                5000, true, false, false, nameLocalizations, descriptionLocalizations, coinNameLocalization, coinNameMultipleLocalization, null,
                "urn:be:linkid:example-mobile:wallet:organization:testWallet:coin:testCoin", policyBalance );

        // operate
        LinkIDWalletOrganizationResult result = client.walletOrganizationUpdate( walletOrganization );
        assertNotNull( result );
        logger.dbg( "Organization result: %s\n", result );
    }

    //    @Test
    public void testWalletOrganizationList()
            throws Exception {

        // setup
        List<String> walletOrganizationIds = Collections.singletonList( "urn:linkid:wallet:fake:visa" );

        // operate
        List<LinkIDWalletOrganizationDetails> organizations = client.walletOrganizationList( null, LinkIDRequestStatusCode.PENDING, false, Locale.ENGLISH );
        for (LinkIDWalletOrganizationDetails organization : organizations) {
            logger.dbg( "Organization: %s\n", organization );
        }
    }

    //    @Test
    public void testWalletOrganizationRemove()
            throws Exception {

        // setup
        String walletOrganizationId = "urn:be:linkid:example-mobile:wallet:organization:testWallet";
        //        String walletOrganizationId = "urn:linkid:wallet:fake:visa";

        // operate
        client.walletOrganizationRemove( walletOrganizationId, false );
    }

    //    @Test
    public void testStartAuthentication()
            throws Exception {

        // setup
        String language = "be";
        LinkIDPaymentContext paymentContext = new LinkIDPaymentContext.Builder( new LinkIDPaymentAmount( 100, LinkIDCurrency.EUR ) ).orderReference(
                "288A6627-81DA-4A4C-9244-C6035B73025A" ).mandate( new LinkIDPaymentMandate( "blaat", "4f3dbedf-b2f4-44f5-92e9-64bd6614089d" ) ).build();

        LinkIDAuthenticationContext context = new LinkIDAuthenticationContext.Builder( APP_NAME ).language( new Locale( language ) )
                                                                                                 .notificationLocation( "https://demo.linkid.be" )
                                                                                                 .paymentContext( paymentContext )
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
            fail();
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
    public void testMandateRemove()
            throws Exception {

        // setup
        List<String> mandateReferences = Arrays.asList( "foo", "bar", "0c9a7c3e-7571-4a0e-8bfc-0a98a0c58e5b" );

        // operate
        LinkIDMandateRemoveResult result = client.mandateRemove( mandateReferences, false );

        // verify
        assertNotNull( result );
        logger.dbg( "Result: %s", result );
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
    public void testConfigApplications()
            throws Exception {

        // Setup
        List<String> applicationNames = Lists.newLinkedList();
        applicationNames.add( "linkID-oper" );
        applicationNames.add( "example-mobile" );

        // operate
        List<LinkIDApplicationDetails> applications = client.configApplications( applicationNames, Locale.ENGLISH );

        // verify
        assertEquals( applicationNames.size(), applications.size() );
        for (LinkIDApplicationDetails application : applications) {
            logger.dbg( "Application details: %s", application );
        }
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
    public void testThemeAdd()
            throws Exception {

        // setup
        String applicationName = "test-shop";
        List<LinkIDLocalizedImage> logos = Collections.singletonList(
                new LinkIDLocalizedImage( "https://s3-eu-west-1.amazonaws.com/linkid-production/image/apps/iwish.png" ) );
        //        List<LinkIDLocalizedImage> logos = Collections.singletonList(
        //                new LinkIDLocalizedImage( "https://service.linkid.be/linkid-static/js/linkid.extra.js" ) );
        //        List<LinkIDLocalizedImage> logos = Collections.singletonList( new LinkIDLocalizedImage( "http://www.kaagent.be/assets/images/icons/icon_200.jpg" ) );
        List<LinkIDLocalizedImage> backgrounds = Collections.singletonList(
                new LinkIDLocalizedImage( "https://s3-eu-west-1.amazonaws.com/linkid-production/image/apps/bg/phone/iwish.png" ) );
        List<LinkIDLocalizedImage> tabletBackgrounds = Collections.singletonList(
                new LinkIDLocalizedImage( "https://s3-eu-west-1.amazonaws.com/linkid-production/image/apps/bg/tablet/iwish.png" ) );
        List<LinkIDLocalizedImage> altBackgrounds = Collections.singletonList(
                new LinkIDLocalizedImage( "https://s3-eu-west-1.amazonaws.com/linkid-production/image/apps/bg/alternative/iwish.png" ) );

        //        LinkIDThemeConfig config = new LinkIDThemeConfig( "urn:be:linkid:example-mobile:theme:themeTest", "Theme test", false, logos, backgrounds,
        //                tabletBackgrounds, altBackgrounds, "#000000", "#FFFFFF" );
        LinkIDThemeConfig config = new LinkIDThemeConfig( "themeTest", "Theme test", false, logos, backgrounds, tabletBackgrounds, altBackgrounds, "#000000",
                "#FFFFFF" );

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
        LinkIDThemeStatus status = client.themeStatus( "urn:be:linkid:example-mobile:theme:themeTest" );

        // verify
        assertNotNull( status );
        logger.dbg( "Status: %s", status );

    }

    //    @Test
    public void testThemeRemove()
            throws Exception {

        // operate
        //        client.themeRemove( "urn:be:linkid:example-mobile:theme:themeTest", false );
        client.themeRemove( "template-blue", true );

    }

    //    @Test
    public void testThemes()
            throws Exception {

        // operate
        //            LinkIDThemes linkIDThemes = client.themes( "urn:be:linkid:example-mobile:theme:themeTest", null );
        LinkIDThemes linkIDThemes = client.themeList( null, null );
        assertNotNull( linkIDThemes );
        for (LinkIDTheme linkIDTheme : linkIDThemes.getThemes()) {
            logger.dbg( "Theme: %s", linkIDTheme );
        }
    }

    //    @Test
    public void testVoucherOrganizationAddUpdate()
            throws Exception {

        // setup
        List<LinkIDLocalizationValue> nameLocalizations = new LinkedList<>();
        List<LinkIDLocalizationValue> descriptionLocalizations = new LinkedList<>();
        nameLocalizations.add( new LinkIDLocalizationValue( Locale.ENGLISH.getLanguage(), "en: Test voucher" ) );
        nameLocalizations.add( new LinkIDLocalizationValue( "nl", "nl: Test voucher" ) );
        descriptionLocalizations.add( new LinkIDLocalizationValue( Locale.ENGLISH.getLanguage(), "en: Test voucher description" ) );
        descriptionLocalizations.add( new LinkIDLocalizationValue( "nl", "nl: Test voucher description" ) );

        LinkIDVoucherOrganization voucherOrganization = new LinkIDVoucherOrganization( "test",
                "https://s3-eu-west-1.amazonaws.com/linkid-production/image/apps/iwish.png", 5, true, nameLocalizations, descriptionLocalizations );

        // operate
        String voucherName = client.voucherOrganizationAddUpdate( voucherOrganization );

        // verify
        assertNotNull( voucherName );
        logger.dbg( "Voucher name: %s", voucherName );
    }

    //    @Test
    public void testVoucherOrganizationList()
            throws Exception {

        // setup
        List<String> organizationIds = Arrays.asList( null, "foo", "bar" );

        // operate
        List<LinkIDVoucherOrganizationDetails> organizations = client.voucherOrganizationList( organizationIds, false );

        // verify
        assertNotNull( organizations );
        for (LinkIDVoucherOrganizationDetails organization : organizations) {
            logger.dbg( "Voucher organization: %s", organization );
        }
    }

    //    @Test
    public void testVoucherOrganizationListFilteredWithStats()
            throws Exception {

        // operate
        List<LinkIDVoucherOrganizationDetails> organizations = client.voucherOrganizationList(
                Collections.singletonList( "urn:be:linkid:example-mobile:theme:test" ), true );

        // verify
        assertNotNull( organizations );
        for (LinkIDVoucherOrganizationDetails organization : organizations) {
            assertNotNull( organization.getStats() );
            logger.dbg( "Voucher organization: %s", organization );
        }
    }

    //    @Test
    public void testVoucherOrganizationRemove()
            throws Exception {

        // operate
        client.voucherOrganizationRemove( "urn:be:linkid:example-mobile:voucher:test" );
    }

    //    @Test
    public void testVoucherOrganizationActivate()
            throws Exception {

        // operate
        client.voucherOrganizationActivate( "urn:be:linkid:example-mobile:voucher:test", true );
    }

    //    @Test
    public void testVoucherOrganizationDeactivate()
            throws Exception {

        // operate
        client.voucherOrganizationActivate( "urn:be:linkid:example-mobile:voucher:test", false );
    }

    //    @Test
    public void testVoucherOrganizationHistory()
            throws Exception {

        // setup
        LinkIDVoucherEventTypeFilter eventTypeFilter = new LinkIDVoucherEventTypeFilter(
                Collections.singletonList( LinkIDVoucherHistoryEventType.VOUCHER_EVENT_ACTIVATE ) );
        LinkIDUserFilter userFilter = new LinkIDUserFilter( Collections.singletonList( "5A878718-C846-4092-AFFE-93CCCDDA83E6" ) );
        LinkIDApplicationFilter applicationFilter = new LinkIDApplicationFilter( Collections.singletonList( "example-mobile" ) );
        LinkIDReportDateFilter dateFilter = new LinkIDReportDateFilter( new DateTime().minusDays( 30 ).toDate(), null );
        LinkIDReportPageFilter pageFilter = new LinkIDReportPageFilter( 0, 5 );

        // operate
        LinkIDVoucherHistory history = client.voucherOrganizationHistory( Collections.singletonList( "urn:be:linkid:example-mobile:theme:test" ), null, null,
                null, null, null, "nl" );

        // verify
        assertNotNull( history );
        logger.dbg( "History.total: %d", history.getTotal() );
        for (LinkIDVoucherHistoryEvent event : history.getEvents()) {
            logger.dbg( "%s", event );
        }
    }

    //    @Test
    public void testVoucherReward()
            throws Exception {

        // Setup
        String userId = "5A878718-C846-4092-AFFE-93CCCDDA83E6";
        String voucherOrganizationId = "urn:be:linkid:example-mobile:theme:test";

        // Operate
        client.voucherReward( userId, voucherOrganizationId, 3 );
    }

    //    @Test
    public void testVoucherList()
            throws Exception {

        // Setup
        String userId = "5A878718-C846-4092-AFFE-93CCCDDA83E6";
        String voucherOrganizationId = "urn:be:linkid:example-mobile:theme:test";

        // Operate
        LinkIDVouchers vouchers = client.voucherList( userId, voucherOrganizationId, true, Locale.ENGLISH );

        // Verify
        assertNotNull( vouchers );
        assertEquals( vouchers.getTotal(), vouchers.getVouchers().size() );
        for (LinkIDVoucher voucher : vouchers.getVouchers()) {
            logger.dbg( "Voucher: %s", voucher );
        }
    }

    //    @Test
    public void voucherListRedeemed()
            throws Exception {

        // Setup
        String userId = "5A878718-C846-4092-AFFE-93CCCDDA83E6";
        String voucherOrganizationId = "urn:be:linkid:example-mobile:theme:test";

        // Operate
        LinkIDVouchers vouchers = client.voucherListRedeemed( userId, voucherOrganizationId, Locale.ENGLISH, null, null );

        // Verify
        assertNotNull( vouchers );
        assertEquals( vouchers.getTotal(), vouchers.getVouchers().size() );
        for (LinkIDVoucher voucher : vouchers.getVouchers()) {
            logger.dbg( "Voucher: %s", voucher );
        }
    }

    //    @Test
    public void testVoucherRedeem()
            throws Exception {

        // Setup
        String voucherId = "5ef3a3c7-78d8-4173-bf47-a75367823618";

        // Operate
        client.voucherRedeem( voucherId );
    }

    //    @Test
    public void testPaymentConfigurationAdd()
            throws Exception {

        // Setup
        List<String> walletOrganizations = Collections.singletonList( "urn:linkid:wallet:fake:visa" );
        List<LinkIDPaymentMethodType> paymentMethods = Collections.singletonList( LinkIDPaymentMethodType.VISA );
        LinkIDPaymentConfiguration paymentConfiguration = new LinkIDPaymentConfiguration( "test", false, false, false, walletOrganizations, paymentMethods );

        // Operate
        String technicalName = client.paymentConfigurationAdd( paymentConfiguration );

        // Verify
        assertNotNull( technicalName );

    }

    //    @Test
    public void testPaymentConfigurationUpdate()
            throws Exception {

        // Setup
        List<String> walletOrganizations = new LinkedList<>();
        List<LinkIDPaymentMethodType> paymentMethods = Arrays.asList( LinkIDPaymentMethodType.MASTERCARD, LinkIDPaymentMethodType.SEPA );
        LinkIDPaymentConfiguration paymentConfiguration = new LinkIDPaymentConfiguration( "urn:be:linkid:example-mobile:payment:configuration:test", false,
                false, false, walletOrganizations, paymentMethods );

        // Operate
        String technicalName = client.paymentConfigurationUpdate( paymentConfiguration );

        // Verify
        assertNotNull( technicalName );
    }

    //    @Test
    public void testPaymentConfigurationRemove()
            throws Exception {

        // Operate
        client.paymentConfigurationRemove( "urn:be:linkid:example-mobile:payment:configuration:test" );
    }

    //    @Test
    public void testPaymentConfigurationList()
            throws Exception {

        // Operate
        List<LinkIDPaymentConfiguration> paymentConfigurations = client.paymentConfigurationList();

        // Verify
        assertNotNull( paymentConfigurations );
        for (LinkIDPaymentConfiguration configuration : paymentConfigurations) {
            logger.inf( "Payment configuration: %s", configuration );
        }
    }

    //    @Test
    public void testApplicationPermissionAdd()
            throws Exception {

        // operate
        try {
            client.applicationPermissionAdd( "urn:be:linkid:example-mobile:theme:test", "linkID-oper", LinkIDApplicationPermissionType.VOUCHER_LIST );
        }
        catch (LinkIDApplicationPermissionAddException e) {
            fail();
        }
    }

    //    @Test
    public void testApplicationPermissionRemove()
            throws Exception {

        // operate
        try {
            client.applicationPermissionRemove( "urn:be:linkid:example-mobile:theme:test", "linkID-oper", LinkIDApplicationPermissionType.VOUCHER_LIST );
        }
        catch (LinkIDApplicationPermissionRemoveException e) {
            fail();
        }
    }

    //    @Test
    public void testApplicationPermissionList()
            throws Exception {

        // operate
        LinkIDApplicationPermissions linkIDApplicationPermissions = client.applicationPermissionList( "urn:be:linkid:example-mobile:theme:test" );

        // verify
        assertNotNull( linkIDApplicationPermissions );
        for (LinkIDApplicationPermissionType permission : linkIDApplicationPermissions.getPermissions()) {
            logger.dbg( "Permission: %s", permission );
        }
    }

    //    @Test
    public void testCommentGet()
            throws Exception {

        // operate
        String comment = client.commentGet( "14b540a1-e665-430d-a72c-70cdd8eace69" );

        // verify
        assertNotNull( comment );
        logger.dbg( "Comment: %s", comment );
    }

    //    @Test
    public void testListUsers()
            throws Exception {

        // Setup
        String voucherOrganizationId = null;
        String walletOrganizationId = null;
        LinkIDReportDateFilter createdFilter = null;
        LinkIDReportDateFilter authenticatedFilter = null;
        LinkIDReportPageFilter pageFilter = null;

        //        operate
        LinkIDUsers users = client.userList( voucherOrganizationId, walletOrganizationId, createdFilter, authenticatedFilter, pageFilter );

        // verify
        assertNotNull( users );
        logger.dbg( "Total: %d", users.getTotal() );
        for (LinkIDUser user : users.getUsers()) {
            logger.dbg( "User: %s", user );
        }
    }

    //    @Test
    public void testCredentialGet()
            throws Exception {

        // Setup
        LinkIDCredentialType type = LinkIDCredentialType.PASSWORD;

        // operate
        LinkIDCredentialRequest request = client.credentialGet( "Unit test 2", type );

        // verify
        assertNotNull( request );
        logger.dbg( "Request: %s", request );
    }

    //    @Test
    public void testCredentialList()
            throws Exception {

        // operate
        List<LinkIDCredential> credentials = client.credentialList();

        // verify
        assertNotNull( credentials );
        for (LinkIDCredential credential : credentials) {
            logger.dbg( "Credential: %s", credential );
        }
    }

    //    @Test
    public void testCredentialRemove()
            throws Exception {

        // setup
        String name = "CN=example-mobile:54tblmQktG";

        // operate
        client.credentialRemove( name );
    }

    //    @Test
    public void testPaymentInfo()
            throws Exception {

        // operate
        LinkIDPaymentInfo linkIDPaymentInfo = client.paymentInfo( Locale.ENGLISH );

        // verify
        assertNotNull( linkIDPaymentInfo );
        logger.dbg( "Result: %s", linkIDPaymentInfo );
    }

    //    @Test
    public void testNotificationAdd()
            throws Exception {

        // Setup
        List<LinkIDNotificationTopicConfiguration> configurations = Lists.newLinkedList();
        configurations.add( new LinkIDNotificationTopicConfiguration( LinkIDNotificationTopic.ATTRIBUTE_UPDATE, LinkIDProfileConstants.EMAIL ) );
        configurations.add( new LinkIDNotificationTopicConfiguration( LinkIDNotificationTopic.CONFIGURATION_UPDATE, null ) );

        // operate
        String urn = client.notificationAdd( "Unit test", "https://linkid.be", configurations );

        // verify
        assertNotNull( urn );
        logger.dbg( "Result: %s", urn );
    }

    //    @Test
    public void testNotificationUpdate()
            throws Exception {

        // Setup
        String urn = "urn:be:linkid:linkID-oper:notification:lRpunl";
        List<LinkIDNotificationTopicConfiguration> configurations = Lists.newLinkedList();
        configurations.add( new LinkIDNotificationTopicConfiguration( LinkIDNotificationTopic.AUTHENTICATION_SUCCESS, null ) );
        configurations.add( new LinkIDNotificationTopicConfiguration( LinkIDNotificationTopic.ATTRIBUTE_REMOVAL, LinkIDProfileConstants.EMAIL ) );

        // operate
        urn = client.notificationUpdate( urn, "Unit test update", "https://service.linkid.be", configurations );

        // verify
        assertNotNull( urn );
        logger.dbg( "Result: %s", urn );
    }

    //    @Test
    public void testNotificationList()
            throws Exception {

        // Setup
        List<String> urns = Arrays.asList( "urn:be:linkid:linkID-oper:notification:lRpunl", "foo" );

        // operate
        List<LinkIDNotificationLocation> locations = client.notificationList( urns );

        // verify
        assertNotNull( locations );
        for (LinkIDNotificationLocation location : locations) {
            logger.dbg( "Location: %s", location );
        }
    }

    //    @Test
    public void testNotificationRemove()
            throws Exception {

        // Setup
        List<String> urns = Collections.singletonList( "urn:be:linkid:linkID-oper:notification:lRpunl" );

        // operate
        client.notificationRemove( urns );
    }

    //    @Test
    public void testApplicationAdd()
            throws Exception {

        // Setup
        String name = "UnitTest";
        String applicationUrl = "https://linkid.be";
        String logoUrl = "https://s3-eu-west-1.amazonaws.com/linkid-production/image/apps/iwish.png";
        List<LinkIDLocalizationValue> nameLocalizations = new LinkedList<>();
        nameLocalizations.add( new LinkIDLocalizationValue( "en", "iWish-en" ) );
        nameLocalizations.add( new LinkIDLocalizationValue( "nl", "iWish-nl" ) );
        List<LinkIDLocalizationValue> descriptionLocalizations = new LinkedList<>();
        descriptionLocalizations.add( new LinkIDLocalizationValue( "en", "iWish description-en" ) );
        descriptionLocalizations.add( new LinkIDLocalizationValue( "nl", "iWish description-nl" ) );

        // operate
        String urn = client.applicationAdd( name, applicationUrl, logoUrl, nameLocalizations, descriptionLocalizations );

        // verify
        assertNotNull( urn );
        logger.dbg( "URN: %s", urn );
    }

    // Auth

    private static LinkIDServiceClient getLinkIDServiceClient() {

        return new LinkIDServiceClientImpl( WS_LOCATION_USERNAME, null, getUsernameTokenCallback() );
    }

    private static LinkIDServiceClient getLinkIDServiceClientX509() {

        String keystorePw = "641de402-5dd6-4548-a888-54e4d4ed6567";

        return new LinkIDServiceClientImpl( WS_LOCATION, null, new LinkIDSDKWSSecurityConfiguration( LinkIDConfig.get(),
                new ResourceKeyStoreKeyProvider( "test-linkid-credentials.jks", keystorePw, LinkIDConstants.IDENTITY_ALIAS, keystorePw ) ) );
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
