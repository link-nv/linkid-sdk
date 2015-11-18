package test.unit.net.link.safeonline.sdk.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import net.link.safeonline.sdk.api.auth.LinkIDAuthenticationContext;
import net.link.safeonline.sdk.api.auth.LinkIDAuthnResponse;
import net.link.safeonline.sdk.api.parking.LinkIDParkingSession;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentAddBrowser;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentAmount;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentOrder;
import net.link.safeonline.sdk.api.reporting.LinkIDReportDateFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletReportTransaction;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletInfo;
import net.link.safeonline.sdk.api.ws.data.client.LinkIDDataClient;
import net.link.safeonline.sdk.api.ws.idmapping.LinkIDNameIdentifierMappingClient;
import net.link.safeonline.sdk.api.ws.linkid.LinkIDServiceClient;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthSession;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalization;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDTheme;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDThemes;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDThemesException;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRContent;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRInfo;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRLockType;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRSession;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDPaymentStatus;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletAddCreditException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletEnrollException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletGetInfoException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletRemoveCreditException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletRemoveException;
import net.link.safeonline.sdk.configuration.LinkIDProtocol;
import net.link.safeonline.sdk.configuration.LinkIDTestConfigHolder;
import net.link.safeonline.sdk.ws.data.LinkIDDataClientImpl;
import net.link.safeonline.sdk.ws.idmapping.LinkIDNameIdentifierMappingClientImpl;
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
public class LinkIDWSClientTest {

    private static final Logger logger = Logger.get( LinkIDWSClientTest.class );

    //private static final String WS_LOCATION = "https://demo.linkid.be/linkid-ws-username";
    private static final String WS_LOCATION = "https://192.168.5.14:8443/linkid-ws-username";

    //private static final String APP_NAME = "example-mobile";
    //private static final String APP_USERNAME = "example-mobile";
    //private static final String APP_PASSWORD = "6E6C1CB7-965C-48A0-B2B0-6B65674BE19F";
    private static final String APP_NAME     = "test-shop";
    private static final String APP_USERNAME = "test-shop";
    private static final String APP_PASSWORD = "5E017416-23B2-47E1-A9E0-43EE3C75A1B0";

    private String wsLocation;

    @Before
    public void setUp()
            throws Exception {

        // DEBUG so ssl validation is skipped for local self signed ssl cert, obv do not do this in production, nor even against demo.linkid.be for that matter.
        System.setProperty( ApplicationMode.PROPERTY, ApplicationMode.DEBUG.name() );
        this.wsLocation = WS_LOCATION;

        new LinkIDTestConfigHolder( "http://linkid.be", null, null ).install();
    }

    @Test
    public void testDummy() {

    }

    //    @Test
    @SuppressWarnings("unchecked")
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
        LinkIDServiceClient client = new LinkIDServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );

        // operate
        LinkIDAuthnResponse response = client.callbackPull( sessionId );

        // verify
        assertNotNull( response );
    }

    //    @Test
    public void testIdMapping()
            throws Exception {

        LinkIDNameIdentifierMappingClient client = new LinkIDNameIdentifierMappingClientImpl( wsLocation, null, getUsernameTokenCallback() );
        String userId = client.getUserId( "profile.email.address", "wim.vandenhaute@gmail.com" );
    }

    //    @Test
    public void testReportingPayment()
            throws Exception {

        LinkIDServiceClient client = new LinkIDServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );

        List<String> orderReferences = Arrays.asList( "QR-SHOP-f0c5b593-0754-4ec0-a45c-664bb86bab11" );

        List<LinkIDPaymentOrder> linkIDPaymentOrders = client.getPaymentReportForOrderReferences( orderReferences );
        logger.inf( "# orders = %d", linkIDPaymentOrders.size() );

        for (LinkIDPaymentOrder linkIDPaymentOrder : linkIDPaymentOrders) {
            logger.inf( "Order: %s", linkIDPaymentOrder );
        }
    }

    //    @Test
    public void testReportingParking()
            throws Exception {

        LinkIDServiceClient client = new LinkIDServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );

        Date startDate = DateTime.now().minusYears( 1 ).toDate();

        List<LinkIDParkingSession> linkIDParkingSessions = client.getParkingReport( startDate, null );
        logger.inf( "# orders = %d", linkIDParkingSessions.size() );

        for (LinkIDParkingSession linkIDParkingSession : linkIDParkingSessions) {
            logger.inf( "Session: %s", linkIDParkingSession );
        }
    }

    //    @Test
    public void testReportingWallet()
            throws Exception {

        LinkIDServiceClient client = new LinkIDServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );

        String walletOrganizationId = "urn:linkid:wallet:leaseplan";
        Date startDate = DateTime.now().minusYears( 1 ).toDate();
        String applicationName = "test-shop";
        String walletId = "ff52177f-8f80-4640-9e86-558f6b1b24c3";
        String userId = "e4269366-ddfb-43dc-838d-01569a8c4c22";

        List<LinkIDWalletReportTransaction> transactions = client.getWalletReport( walletOrganizationId, new LinkIDReportDateFilter( startDate, null ) );
        //        List<LinkIDWalletReportTransaction> transactions = client.getWalletReport( walletOrganizationId, new LinkIDReportApplicationFilter( applicationName ) );
        //        List<LinkIDWalletReportTransaction> transactions = client.getWalletReport( walletOrganizationId, new LinkIDReportWalletFilter( walletId, userId ) );
        logger.inf( "# txns = %d", transactions.size() );

        for (LinkIDWalletReportTransaction transaction : transactions) {
            logger.inf( "transaction: %s (wallet: %s)", transaction, transaction.getWalletId() );
        }
    }

    //    @Test
    public void testWalletEnrollment()
            throws Exception {

        // setup
        LinkIDServiceClient client = new LinkIDServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );
        String userId = "e4269366-ddfb-43dc-838d-01569a8c4c22";
        String walletOrganizationId = "urn:linkid:wallet:leaseplan";

        // operate
        try {
            //            String walletId = client.enroll( userId, walletOrganizationId, 500, LinkIDCurrency.EUR, null );
            String walletId = client.walletEnroll( userId, walletOrganizationId, 500, null, "urn:linkid:wallet:coin:coffee" );
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

        // setup
        LinkIDServiceClient client = new LinkIDServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );
        String userId = "e4269366-ddfb-43dc-838d-01569a8c4c22";
        String walletOrganizationId = "urn:linkid:wallet:leaseplan";

        // operate
        try {
            LinkIDWalletInfo walletInfo = client.walletGetInfo( userId, walletOrganizationId );
            logger.inf( "Wallet info: %s", walletInfo );
        }
        catch (LinkIDWalletGetInfoException e) {
            logger.err( "GetInfo error: %s", e.getErrorCode() );
            fail();
        }
    }

    //    @Test
    public void testWalletAddCredit()
            throws Exception {

        // setup
        LinkIDServiceClient client = new LinkIDServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );
        String userId = "e4269366-ddfb-43dc-838d-01569a8c4c22";
        String walletId = "6e2cc86f-4178-46e5-a483-ca5fd0ebd4a1";

        // operate
        try {
            //            client.addCredit( userId, walletId, 100, LinkIDCurrency.EUR, null );
            client.walletAddCredit( userId, walletId, 100, null, "urn:linkid:wallet:coin:coffee" );
        }
        catch (LinkIDWalletAddCreditException e) {
            logger.err( "Add credit error: %s", e.getErrorCode() );
            fail();
        }
    }

    //    @Test
    public void testWalletRemoveCredit()
            throws Exception {

        // setup
        LinkIDServiceClient client = new LinkIDServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );
        String userId = "e4269366-ddfb-43dc-838d-01569a8c4c22";
        String walletId = "6e2cc86f-4178-46e5-a483-ca5fd0ebd4a1";

        // operate
        try {
            //            client.removeCredit( userId, walletId, 100, LinkIDCurrency.EUR, null );
            client.walletRemoveCredit( userId, walletId, 100, null, "urn:linkid:wallet:coin:coffee" );
        }
        catch (LinkIDWalletRemoveCreditException e) {
            logger.err( "Remove credit error: %s", e.getErrorCode() );
            fail();
        }
    }

    //    @Test
    public void testWalletRemove()
            throws Exception {

        // setup
        LinkIDServiceClient client = new LinkIDServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );
        String userId = "9e4d2818-d9d4-454c-9b1d-1f067a1f7469";
        String walletId = "123b1c22-e6c5-4ebc-9255-e59b72db5abf";

        // operate
        try {
            client.walletRemove( userId, walletId );
        }
        catch (LinkIDWalletRemoveException e) {
            logger.err( "Remove error: %s", e.getErrorCode() );
            fail();
        }
    }

    //    @Test
    public void testThemes()
            throws Exception {

        // setup
        LinkIDServiceClient client = getLinkIDServiceClient();
        String applicationName = "test-shop";

        // operate
        try {
            LinkIDThemes linkIDThemes = client.getThemes( applicationName );
            assertNotNull( linkIDThemes );
            assertNotNull( linkIDThemes.findDefaultTheme() );
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
    public void testStartAuthentication()
            throws Exception {

        // setup
        LinkIDServiceClient client = new LinkIDServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );
        String language = "be";
        String userAgent = "unit-test";
        LinkIDPaymentContext paymentContext = new LinkIDPaymentContext.Builder( new LinkIDPaymentAmount( 0.512123, LinkIDCurrency.EUR ) ).build();

        LinkIDAuthenticationContext linkIDAuthenticationContext = new LinkIDAuthenticationContext( APP_NAME, null, LinkIDProtocol.WS );
        linkIDAuthenticationContext.setLanguage( new Locale( language ) );
        linkIDAuthenticationContext.setPaymentContext( paymentContext );

        // operate: start
        try {
            LinkIDAuthSession session = client.authStart( linkIDAuthenticationContext, userAgent );

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
        LinkIDServiceClient client = new LinkIDServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );
        String sessionId = "WpowEE";

        // operate
        client.authCancel( sessionId );
    }

    //    @Test
    public void testGetPaymentStatus()
            throws Exception {

        // setup
        String orderReference = "QR-SHOP-6b3ac19c-eee8-4732-a043-4a34bff16eca";
        LinkIDServiceClient client = getLinkIDServiceClient();

        // operate
        LinkIDPaymentStatus linkIDPaymentStatus = client.getPaymentStatus( orderReference );

        // verify
        assertNotNull( linkIDPaymentStatus );
        assertEquals( linkIDPaymentStatus.getOrderReference(), orderReference );
        assertNotNull( linkIDPaymentStatus.getUserId() );
    }

    //    @Test
    public void testLTQRPush()
            throws Exception {

        // setup
        LinkIDPaymentContext linkIDPaymentContext = new LinkIDPaymentContext.Builder( new LinkIDPaymentAmount( 10, LinkIDCurrency.EUR ) )   //
                .orderReference( UUID.randomUUID().toString() ).paymentAddBrowser( LinkIDPaymentAddBrowser.NOT_ALLOWED ).build();
        DateTime expiryDateTime = new DateTime();
        expiryDateTime = expiryDateTime.plusMonths( 2 );

        LinkIDLTQRContent ltqrContent = new LinkIDLTQRContent.Builder().authenticationMessage( "LTQR Test" )
                                                                       .finishedMessage( "LTQR Test finished" )
                                                                       .paymentContext( linkIDPaymentContext )
                                                                       .expiryDate( expiryDateTime.toDate() )
                                                                       .build();

        LinkIDServiceClient client = getLinkIDServiceClient();

        // operate
        LinkIDLTQRSession linkIDLTQRSession = client.ltqrPush( ltqrContent, null, LinkIDLTQRLockType.NEVER );

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
    public void testLTQRInfo()
            throws Exception {

        // setup
        List<String> ltqrReferences = Lists.newLinkedList();
        ltqrReferences.add( "856eed32-2119-4f94-b705-f177079e1b9e" );
        LinkIDServiceClient client = getLinkIDServiceClient();

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
        String mandateReference = "dfc816ae-b2b6-4af6-9260-72c94b9a684d";
        LinkIDPaymentContext linkIDPaymentContext = new LinkIDPaymentContext.Builder(
                new LinkIDPaymentAmount( 1, "urn:linkid:wallet:coin:coffee" ) ).description( "Test description" ).build();
        LinkIDServiceClient client = getLinkIDServiceClient();

        // operate
        String orderReference = client.mandatePayment( mandateReference, linkIDPaymentContext, Locale.ENGLISH );

        // verify
        assertNotNull( orderReference );
    }

    //    @Test
    public void testCapture()
            throws Exception {

        // Setup
        String orderReference = "foo";
        LinkIDServiceClient client = getLinkIDServiceClient();

        // operate
        client.paymentCapture( orderReference );
    }

    //    @Test
    public void testGetLocalization()
            throws Exception {

        // Setup
        List<String> keys = Lists.newLinkedList();
        keys.add( "urn:linkid:wallet:coin:coffee" );
        keys.add( "urn:linkid:wallet:leaseplan" );

        LinkIDServiceClient client = getLinkIDServiceClient();

        // operate
        List<LinkIDLocalization> localizations = client.getLocalization( keys );

        // verify
        assertEquals( 4, localizations.size() );
        assertEquals( 4, localizations.get( 0 ).getValues().size() );
    }

    // Auth

    private LinkIDServiceClient getLinkIDServiceClient() {

        return new LinkIDServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );
    }

    private WSSecurityUsernameTokenCallback getUsernameTokenCallback() {

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
