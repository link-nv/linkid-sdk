package test.unit.net.link.safeonline.sdk.ws;

import java.util.List;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.api.ws.data.client.DataClient;
import net.link.safeonline.sdk.api.ws.idmapping.client.NameIdentifierMappingClient;
import net.link.safeonline.sdk.ws.data.DataClientImpl;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClientImpl;
import net.link.util.ws.security.username.AbstractWSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 02/04/14
 * Time: 13:15
 */
public class WSClientTest {

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

    private WSSecurityUsernameTokenCallback getUsernameTokenCallback() {

        return new AbstractWSSecurityUsernameTokenCallback() {
            @Override
            public String getUsername() {

                return "demo-test";
            }

            @Override
            public String getPassword() {

                return "08427E9F-6355-4DE4-B992-B1AB93CEE9D4";
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
