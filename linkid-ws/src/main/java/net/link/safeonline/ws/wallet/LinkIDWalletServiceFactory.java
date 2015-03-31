/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.wallet;

import java.net.URL;
import javax.xml.namespace.QName;
import net.lin_k.safe_online.wallet.WalletService;


public class LinkIDWalletServiceFactory {

    private LinkIDWalletServiceFactory() {

        // empty
    }

    public static WalletService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource( "safe-online-wallet.wsdl" );
        if (null == wsdlUrl)
            throw new RuntimeException( "SafeOnline Wallet WSDL not found" );

        return new WalletService( wsdlUrl, new QName( "urn:net:lin-k:safe-online:wallet", "WalletService" ) );
    }
}
