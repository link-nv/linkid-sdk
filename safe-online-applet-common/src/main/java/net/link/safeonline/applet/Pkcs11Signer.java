/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.applet;

import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.shared.JceSigner;


/**
 * PKCS#11 signer implementation.
 * 
 * @author fcorneli
 * 
 */
public class Pkcs11Signer extends JceSigner {

    public Pkcs11Signer(SmartCard smartCard) {

        super(smartCard.getAuthenticationPrivateKey(), smartCard.getAuthenticationCertificate());
    }
}
