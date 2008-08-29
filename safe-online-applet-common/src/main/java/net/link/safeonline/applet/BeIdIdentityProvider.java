/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.applet;

import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.shared.statement.IdentityProvider;


/**
 * Identity provider based on BeID PKCS11 component.
 * 
 * @author fcorneli
 * 
 */
public class BeIdIdentityProvider implements IdentityProvider {

    private final SmartCard smartCard;


    public BeIdIdentityProvider(SmartCard smartCard) {

        this.smartCard = smartCard;
    }

    public String getGivenName() {

        return this.smartCard.getGivenName();
    }

    public String getSurname() {

        return this.smartCard.getSurname();
    }
}
