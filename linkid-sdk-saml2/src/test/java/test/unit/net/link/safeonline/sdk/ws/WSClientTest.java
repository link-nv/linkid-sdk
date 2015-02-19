package test.unit.net.link.safeonline.sdk.ws;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.api.payment.Currency;
import net.link.safeonline.sdk.api.payment.PaymentTransactionDO;
import net.link.safeonline.sdk.api.ws.data.client.DataClient;
import net.link.safeonline.sdk.api.ws.idmapping.NameIdentifierMappingClient;
import net.link.safeonline.sdk.api.ws.reporting.ReportingServiceClient;
import net.link.safeonline.sdk.api.ws.wallet.WalletAddCreditException;
import net.link.safeonline.sdk.api.ws.wallet.WalletAddCreditResult;
import net.link.safeonline.sdk.api.ws.wallet.WalletEnrollException;
import net.link.safeonline.sdk.api.ws.wallet.WalletEnrollResult;
import net.link.safeonline.sdk.api.ws.wallet.WalletServiceClient;
import net.link.safeonline.sdk.ws.data.DataClientImpl;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClientImpl;
import net.link.safeonline.sdk.ws.reporting.ReportingServiceClientImpl;
import net.link.safeonline.sdk.ws.wallet.WalletServiceClientImpl;
import net.link.util.common.ApplicationMode;
import net.link.util.logging.Logger;
import net.link.util.ws.security.username.AbstractWSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;


/**
 * Created by wvdhaute
 * Date: 02/04/14
 * Time: 13:15
 */
public class WSClientTest {

    private static final Logger logger = Logger.get( WSClientTest.class );

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

        DataClient client = new DataClientImpl( wsLocation, null, getUsernameTokenCallback() );
        List attributes = client.getAttributes( userId, "profile.givenName" );

        // set
        client.setAttributeValue( userId, attributes );

        AttributeSDK<String> attribute = (AttributeSDK<String>) attributes.get( 0 );

        // remove
        client.removeAttribute( userId, "profile.givenName", attribute.getId() );

        client.setAttributeValue( userId, attributes );
    }

    //    @Test
    public void testIdMapping()
            throws Exception {

        NameIdentifierMappingClient client = new NameIdentifierMappingClientImpl( wsLocation, null, getUsernameTokenCallback() );
        String userId = client.getUserId( "profile.email.address", "wim.vandenhaute@gmail.com" );
    }

    //    @Test
    public void testReporting()
            throws Exception {

        ReportingServiceClient client = new ReportingServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );

        List<String> orderReferences = Arrays.asList( "842a53ebe15247c1992d73a8f6db4b66" );

        List<PaymentTransactionDO> txns = client.getPaymentReportForOrderReferences( orderReferences );
        logger.inf( "# txns = %d", txns.size() );
    }

    //    @Test
    public void testWalletEnrollment()
            throws Exception {

        // setup
        WalletServiceClient client = new WalletServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );
        List<String> userIds = Arrays.asList( "9e4d2818-d9d4-454c-9b1d-1f067a1f7469" );
        String walletId = "60d3113d-7229-4387-a271-792d905ca4ed";

        // operate
        try {
            WalletEnrollResult result = client.enroll( userIds, walletId, 5, Currency.EUR );
            logger.dbg( "# not known: %d", result.getUnknownUsers().size() );
            logger.dbg( "# already enrolled: %d", result.getAlreadyEnrolledUsers().size() );
        }
        catch (WalletEnrollException e) {
            logger.err( "Enroll error: %s", e.getErrorCode() );
            fail();
        }
    }

    //    @Test
    public void testWalletAddCredit()
            throws Exception {

        // setup
        WalletServiceClient client = new WalletServiceClientImpl( wsLocation, null, getUsernameTokenCallback() );
        List<String> userIds = Arrays.asList( "9e4d2818-d9d4-454c-9b1d-1f067a1f7469" );
        String walletId = "60d3113d-7229-4387-a271-792d905ca4ed";

        // operate
        try {
            WalletAddCreditResult result = client.addCredit( userIds, walletId, 5, Currency.EUR );
            logger.dbg( "# not known: %d", result.getUnknownUsers().size() );
            logger.dbg( "# not enrolled: %d", result.getNotEnrolledUsers().size() );
        }
        catch (WalletAddCreditException e) {
            logger.err( "Add credit error: %s", e.getErrorCode() );
            fail();
        }
    }

    // Auth

    private WSSecurityUsernameTokenCallback getUsernameTokenCallback() {

        return new AbstractWSSecurityUsernameTokenCallback() {
            @Override
            public String getUsername() {

                return "example-mobile";
            }

            @Override
            public String getPassword() {

                return "6E6C1CB7-965C-48A0-B2B0-6B65674BE19F";
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
