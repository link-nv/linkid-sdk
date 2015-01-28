/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.wallet;

import junit.framework.TestCase;
import net.lin_k.safe_online.wallet.WalletService;
import net.link.safeonline.ws.wallet.WalletServiceFactory;


public class WalletServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        WalletService result = WalletServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
