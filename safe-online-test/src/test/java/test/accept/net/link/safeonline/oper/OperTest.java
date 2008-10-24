/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.accept.net.link.safeonline.oper;

import junit.framework.TestCase;
import net.link.safeonline.webapp.AcceptanceTestManager;
import net.link.safeonline.webapp.PageUtils;
import net.link.safeonline.webapp.WebappConstants;
import net.link.safeonline.webapp.oper.OperOverview;


public class OperTest extends TestCase {

    private AcceptanceTestManager acceptanceTestManager;


    @Override
    protected void setUp() throws Exception {

        super.setUp();
        this.acceptanceTestManager = new AcceptanceTestManager();
        this.acceptanceTestManager.setUp();
    }

    @Override
    protected void tearDown() throws Exception {

        this.acceptanceTestManager.tearDown();
        super.tearDown();
    }

    public void testAdminLogonLogout() throws Exception {

        this.acceptanceTestManager.setContext("Testing operator webapp admin login logout.");

        OperOverview operOverview = PageUtils.loginOperWithPassword(this.acceptanceTestManager, WebappConstants.OPER_ADMIN, "admin");
        operOverview.logout();
    }

    public void testAddApplication() throws Exception {

        this.acceptanceTestManager.setContext("Testing operator webapp add application.");

        OperOverview operOverview = PageUtils.loginOperWithPassword(this.acceptanceTestManager, WebappConstants.OPER_ADMIN, "admin");
        operOverview.logout();
    }
}
