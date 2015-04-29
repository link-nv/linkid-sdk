package test.unit.net.link.safeonline.sdk.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import net.link.safeonline.sdk.api.parking.LinkIDParkingSession;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentOrder;
import net.link.safeonline.sdk.api.reporting.LinkIDReportWalletFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletReportTransaction;
import net.link.safeonline.sdk.api.ws.auth.LinkIDAuthServiceClient;
import net.link.safeonline.sdk.api.ws.configuration.LinkIDConfigurationServiceClient;
import net.link.safeonline.sdk.api.ws.configuration.LinkIDTheme;
import net.link.safeonline.sdk.api.ws.configuration.LinkIDThemes;
import net.link.safeonline.sdk.api.ws.configuration.LinkIDThemesException;
import net.link.safeonline.sdk.api.ws.data.client.LinkIDDataClient;
import net.link.safeonline.sdk.api.ws.idmapping.LinkIDNameIdentifierMappingClient;
import net.link.safeonline.sdk.api.ws.ltqr.LinkIDLTQRServiceClient;
import net.link.safeonline.sdk.api.ws.payment.LinkIDPaymentServiceClient;
import net.link.safeonline.sdk.api.ws.payment.LinkIDPaymentStatus;
import net.link.safeonline.sdk.api.ws.reporting.LinkIDReportingServiceClient;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletAddCreditException;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletEnrollException;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletRemoveCreditException;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletRemoveException;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletServiceClient;
import net.link.safeonline.sdk.ws.auth.LinkIDAuthServiceClientImpl;
import net.link.safeonline.sdk.ws.configuration.LinkIDConfigurationServiceClientImpl;
import net.link.safeonline.sdk.ws.data.LinkIDDataClientImpl;
import net.link.safeonline.sdk.ws.idmapping.LinkIDNameIdentifierMappingClientImpl;
import net.link.safeonline.sdk.ws.ltqr.LinkIDLTQRServiceClientImpl;
import net.link.safeonline.sdk.ws.payment.LinkIDPaymentServiceClientImpl;
import net.link.safeonline.sdk.ws.reporting.LinkIDReportingServiceClientImpl;
import net.link.safeonline.sdk.ws.wallet.LinkIDWalletServiceClientImpl;
import net.link.util.common.ApplicationMode;
import net.link.util.logging.Logger;
import net.link.util.ws.security.username.AbstractWSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;


/**
 * Created by wvdhaute
 * Date: 02/04/14
 * Time: 13:15
 */
public class LinkIDWSClientTest {

    private static final Logger logger = Logger.get( LinkIDWSClientTest.class );

    private String wsLocation;

    @Before
    public void setUp()
            throws Exception {

        // DEBUG so ssl validation is skipped for local self signed ssl cert, obv do not do this in production, nor even against demo.linkid.be for that matter.
        System.setProperty( ApplicationMode.PROPERTY, ApplicationMode.DEBUG.name() );
        this.wsLocation = "https://192.168.5.14:8443/linkid-ws-username";
        //        this.wsLocation = "https://demo.linkid.be/linkid-ws-username";
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
    public void testIdMapping()
            throws Exception {

        LinkIDNameIdentifierMappingClient client = new LinkIDNameIdentifierMappingClientImpl( wsLocation, null, getUsernameTokenCallback() );
        String userId = client.getUserId( "profile.email.address", "wim.vandenhaute@gmail.com" );
    }

    //    @Test
    public void testReportingPayment()
            throws Exception {

        LinkIDReportingServiceClient client = new LinkIDReportingServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );

        List<String> orderReferences = Arrays.asList( "ac321fe96156489299b77a007f41509b", "9fdc23058f0449479d091072759d4646",
                "6dba05fe1ff04e96b92995a661ba77d0" );

        List<LinkIDPaymentOrder> linkIDPaymentOrders = client.getPaymentReportForOrderReferences( orderReferences );
        logger.inf( "# orders = %d", linkIDPaymentOrders.size() );

        for (LinkIDPaymentOrder linkIDPaymentOrder : linkIDPaymentOrders) {
            logger.inf( "Order: %s", linkIDPaymentOrder );
        }
    }

    //    @Test
    public void testReportingParking()
            throws Exception {

        LinkIDReportingServiceClient client = new LinkIDReportingServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );

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

        LinkIDReportingServiceClient client = new LinkIDReportingServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );

        String walletOrganizationId = "f508212c-9189-4402-ab76-6e26110697b4";
        Date startDate = DateTime.now().minusYears( 1 ).toDate();
        String applicationName = "test-shop";
        String walletId = "ff52177f-8f80-4640-9e86-558f6b1b24c3";
        String userId = "e4269366-ddfb-43dc-838d-01569a8c4c22";

        //        List<LinkIDWalletReportTransaction> transactions = client.getWalletReport( walletOrganizationId, new LinkIDReportDateFilter( startDate, null ) );
        //        List<LinkIDWalletReportTransaction> transactions = client.getWalletReport( walletOrganizationId, new LinkIDReportApplicationFilter( applicationName ) );
        List<LinkIDWalletReportTransaction> transactions = client.getWalletReport( walletOrganizationId, new LinkIDReportWalletFilter( walletId, userId ) );
        logger.inf( "# txns = %d", transactions.size() );

        for (LinkIDWalletReportTransaction transaction : transactions) {
            logger.inf( "transaction: %s (wallet: %s)", transaction, transaction.getWalletId() );
        }
    }

    //    @Test
    public void testWalletEnrollment()
            throws Exception {

        // setup
        LinkIDWalletServiceClient client = new LinkIDWalletServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );
        String userId = "9e4d2818-d9d4-454c-9b1d-1f067a1f7469";
        String walletOrganizationId = "60d3113d-7229-4387-a271-792d905ca4ed";

        // operate
        try {
            String walletId = client.enroll( userId, walletOrganizationId, 500, LinkIDCurrency.EUR );
            logger.inf( "Enrolled wallet: %s", walletId );
        }
        catch (LinkIDWalletEnrollException e) {
            logger.err( "Enroll error: %s", e.getErrorCode() );
            fail();
        }
    }

    //    @Test
    public void testWalletAddCredit()
            throws Exception {

        // setup
        LinkIDWalletServiceClient client = new LinkIDWalletServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );
        String userId = "9e4d2818-d9d4-454c-9b1d-1f067a1f7469";
        String walletId = "588ccf3c-04d8-4837-9285-9077b026699f";

        // operate
        try {
            client.addCredit( userId, walletId, 100, LinkIDCurrency.EUR );
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
        LinkIDWalletServiceClient client = new LinkIDWalletServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );
        String userId = "9e4d2818-d9d4-454c-9b1d-1f067a1f7469";
        String walletId = "588ccf3c-04d8-4837-9285-9077b026699f";

        // operate
        try {
            client.removeCredit( userId, walletId, 100, LinkIDCurrency.EUR );
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
        LinkIDWalletServiceClient client = new LinkIDWalletServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );
        String userId = "9e4d2818-d9d4-454c-9b1d-1f067a1f7469";
        String walletId = "8fb05095-6210-40a1-87e6-08f3b1f3a982";

        // operate
        try {
            client.remove( userId, walletId );
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
        LinkIDConfigurationServiceClient client = new LinkIDConfigurationServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );
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
    public void testCancelAuthentication()
            throws Exception {

        // setup
        LinkIDAuthServiceClient<AuthnRequest, Response> client = new LinkIDAuthServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );
        String sessionId = "WpowEE";

        // operate
        client.cancel( sessionId );
    }

    //    @Test
    public void testPushLTQR()
            throws Exception {

        // setup
        LinkIDLTQRServiceClient client = new LinkIDLTQRServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );
        LinkIDPaymentContext paymentContext = new LinkIDPaymentContext( 5, LinkIDCurrency.EUR );

        // operate
        client.push( null, null, paymentContext, false, null, null, null, null, null, null );
    }

    //    @Test
    public void testGetPaymentStatus()
            throws Exception {
        // setup
        String orderReference = "7d545bcb56f84fc8945f0cd537ca6694";
        LinkIDPaymentServiceClient client = new LinkIDPaymentServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );

        // operate
        LinkIDPaymentStatus linkIDPaymentStatus = client.getStatus( orderReference );

        // verify
        assertNotNull( linkIDPaymentStatus );
        assertEquals( linkIDPaymentStatus.getOrderReference(), orderReference );
        assertNotNull( linkIDPaymentStatus.getUserId() );
    }

    // Auth

    private WSSecurityUsernameTokenCallback getUsernameTokenCallback() {

        return new AbstractWSSecurityUsernameTokenCallback() {
            @Override
            public String getUsername() {

                return "example-mobile";
                //                return "test-shop";
            }

            @Override
            public String getPassword() {

                return "6E6C1CB7-965C-48A0-B2B0-6B65674BE19F";
                //                return "5E017416-23B2-47E1-A9E0-43EE3C75A1B0";
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
