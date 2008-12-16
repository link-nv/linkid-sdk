package test.unit.net.link.safeonline.encap.webapp;

import static org.easymock.EasyMock.createMock;
import junit.framework.TestCase;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.model.encap.EncapDeviceService;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;


public class EnablePageTest extends TestCase {

    private EncapDeviceService   mockEncapDeviceService;

    private SamlAuthorityService mockSamlAuthorityService;

    private HelpdeskManager      mockHelpdeskManager;

    private WicketTester         wicket;

    private JndiTestUtils        jndiTestUtils;


    @Override
    @Before
    public void setUp()
            throws Exception {

        super.setUp();

        WicketUtil.setUnitTesting(true);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();

        mockEncapDeviceService = createMock(EncapDeviceService.class);
        mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        mockHelpdeskManager = createMock(HelpdeskManager.class);

        wicket = new WicketTester(new EncapTestApplication());
        wicket.processRequestCycle();

    }

    @Override
    @After
    public void tearDown()
            throws Exception {

        jndiTestUtils.tearDown();
    }
}
