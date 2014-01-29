package test.unit.net.link.safeonline.ws.haws;

import junit.framework.TestCase;
import net.lin_k.safe_online.haws.HawsService;
import net.link.safeonline.ws.haws.HawsServiceFactory;


public class HawserviceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        HawsService result = HawsServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
