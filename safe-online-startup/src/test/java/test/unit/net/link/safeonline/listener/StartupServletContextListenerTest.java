/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.listener;

import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.expect;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import junit.framework.TestCase;
import net.link.safeonline.Startable;
import net.link.safeonline.listener.StartupServletContextListener;
import net.link.safeonline.test.util.JndiTestUtils;

import org.easymock.IMocksControl;


public class StartupServletContextListenerTest extends TestCase {

    private JndiTestUtils                 jndiTestUtils;

    private StartupServletContextListener testedInstance;


    @Override
    protected void setUp() throws Exception {

        super.setUp();

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();

        this.testedInstance = new StartupServletContextListener();
    }

    @Override
    protected void tearDown() throws Exception {

        this.jndiTestUtils.tearDown();

        super.tearDown();
    }

    public void testPostStartAccordingToPriorities() throws Exception {

        // setup
        IMocksControl mocksControl = createStrictControl();
        mocksControl.checkOrder(true);
        Startable mockDontCareStartable = mocksControl.createMock(Startable.class);
        Startable mockBootstrapStartable = mocksControl.createMock(Startable.class);
        ServletContext mockServletContext = mocksControl.createMock(ServletContext.class);
        ServletContextEvent servletContextEvent = new ServletContextEvent(mockServletContext);
        String testJndiPrefix = "test/jndi/prefix";

        this.jndiTestUtils.bindComponent(testJndiPrefix + "/dontcare", mockDontCareStartable);
        this.jndiTestUtils.bindComponent(testJndiPrefix + "/bootstrap", mockBootstrapStartable);

        // stubs
        expect(mockServletContext.getInitParameter("StartableJndiPrefix")).andStubReturn(testJndiPrefix);
        expect(mockBootstrapStartable.getPriority()).andStubReturn(Startable.PRIORITY_BOOTSTRAP);
        expect(mockDontCareStartable.getPriority()).andStubReturn(Startable.PRIORITY_DONT_CARE);

        // expectations
        mockBootstrapStartable.postStart();
        mockDontCareStartable.postStart();

        mockDontCareStartable.preStop();
        mockBootstrapStartable.preStop();

        // prepare
        mocksControl.replay();

        // operate
        this.testedInstance.contextInitialized(servletContextEvent);
        this.testedInstance.contextDestroyed(servletContextEvent);

        // verify
        mocksControl.verify();
    }
}
