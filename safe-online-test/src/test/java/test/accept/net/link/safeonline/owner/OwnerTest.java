/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.accept.net.link.safeonline.owner;

import junit.framework.TestCase;
import net.link.safeonline.webapp.AcceptanceTestManager;
import net.link.safeonline.webapp.PageUtils;
import net.link.safeonline.webapp.WebappConstants;
import net.link.safeonline.webapp.owner.OwnerOverview;


public class OwnerTest extends TestCase {

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

    public void testOwnerLogonLogout() throws Exception {

        this.acceptanceTestManager.setContext("Testing owner webapp admin login logout");

        OwnerOverview ownerOverview = PageUtils.loginOwnerWithPassword(this.acceptanceTestManager,
                WebappConstants.OWNER_ADMIN, "secret");
        ownerOverview.logout();
    }
}
