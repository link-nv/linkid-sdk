/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.accept.net.link.safeonline.user;

import junit.framework.TestCase;
import net.link.safeonline.webapp.AcceptanceTestManager;


/**
 * Acceptance test for user web application.
 * 
 * @author fcorneli
 * 
 */
public class UserTest extends TestCase {

    private AcceptanceTestManager acceptanceTestManager;


    @Override
    public void setUp()
            throws Exception {

        super.setUp();
        acceptanceTestManager = new AcceptanceTestManager();
        acceptanceTestManager.setUp();
    }

    @Override
    protected void tearDown()
            throws Exception {

        acceptanceTestManager.tearDown();
        super.tearDown();
    }

}
