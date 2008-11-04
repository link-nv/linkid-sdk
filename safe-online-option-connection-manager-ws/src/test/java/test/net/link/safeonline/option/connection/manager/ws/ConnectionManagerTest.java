package test.net.link.safeonline.option.connection.manager.ws;

import java.net.URL;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

import net.link.safeonline.option.connection.manager.ws.ConnectionManagerConstants;
import net.link.safeonline.option.connection.manager.ws.generated.ConnectionManager;
import net.link.safeonline.option.connection.manager.ws.generated.ConnectionManagerService;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ConnectionManagerTest {

    public static final String URL = "http://localhost:8123/connection-manager";


    @Test
    public void testConnectionManagerWebService()
            throws Exception {

        Endpoint.publish(URL, new ConnectionManagerImpl());

        ConnectionManagerService service = new ConnectionManagerService(new URL(URL + "?wsdl"), new QName(
                ConnectionManagerConstants.NAMESPACE, ConnectionManagerConstants.LOCALPART));
        ConnectionManager port = service.getConnectionManagerPort();
        assertEquals(ConnectionManagerImpl.IMEI, port.getIMEI());
        assertEquals(ConnectionManagerImpl.IMEI, port.getIMEI());
    }


    @WebService(portName = "ConnectionManagerPort", targetNamespace = "http://ws.manager.connection.option.safeonline.link.net/", serviceName = "ConnectionManagerService")
    public class ConnectionManagerImpl implements ConnectionManager {

        public static final String IMEI = "123";


        public String getIMEI() {

            return IMEI;
        }

    }

}
