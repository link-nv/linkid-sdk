package test.unit.net.link.safeonline.sdk.ws;

import java.util.Arrays;
import java.util.List;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.api.payment.PaymentTransactionDO;
import net.link.safeonline.sdk.api.ws.data.client.DataClient;
import net.link.safeonline.sdk.api.ws.idmapping.NameIdentifierMappingClient;
import net.link.safeonline.sdk.api.ws.reporting.ReportingServiceClient;
import net.link.safeonline.sdk.ws.data.DataClientImpl;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClientImpl;
import net.link.safeonline.sdk.ws.reporting.ReportingServiceClientImpl;
import net.link.util.logging.Logger;
import net.link.util.ws.security.username.AbstractWSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 02/04/14
 * Time: 13:15
 */
public class WSClientTest {

    private static final Logger logger = Logger.get( WSClientTest.class );

    //    @Test
    public void testData()
            throws Exception {

        String userId = "2b35dbab-2ba2-403b-8c36-a8399c3af3d5";

        DataClient client = new DataClientImpl( "http://192.168.5.14:8080/linkid-ws-username", null, getUsernameTokenCallback() );
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

        NameIdentifierMappingClient client = new NameIdentifierMappingClientImpl( "http://192.168.5.14:8080/linkid-ws-username", null,
                getUsernameTokenCallback() );
        String userId = client.getUserId( "profile.email.address", "wim.vandenhaute@gmail.com" );
    }

    //    @Test
    public void testReporting()
            throws Exception {

        ReportingServiceClient client = new ReportingServiceClientImpl( "http://192.168.5.14:8080/linkid-ws-username", null, getUsernameTokenCallback() );

        List<String> orderReferences = Arrays.asList( "842a53ebe15247c1992d73a8f6db4b66" );

        List<PaymentTransactionDO> txns = client.getPaymentReportForOrderReferences( orderReferences );
        logger.inf( "# txns = %d", txns.size() );
    }

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
