/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.wallet;

import junit.framework.TestCase;
import net.lin_k.safe_online.wallet._2.WalletService;
import net.link.safeonline.ws.wallet.LinkIDWalletServiceFactory;


public class LinkIDWalletServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        WalletService result = LinkIDWalletServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
