/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.pcsc;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

import net.link.safeonline.shared.statement.IdentityProvider;


public class PcscIdentityProvider extends Pcsc implements IdentityProvider {

    public PcscIdentityProvider(CardChannel cardChannel) {

        super(cardChannel);
    }

    public String getGivenName() {

        IdentityFile identityFile;
        try {
            identityFile = getIdentityFile();
        } catch (CardException e) {
            throw new RuntimeException("error");
        }
        return identityFile.getName();
    }

    public String getSurname() {

        IdentityFile identityFile;
        try {
            identityFile = getIdentityFile();
        } catch (CardException e) {
            throw new RuntimeException("error");
        }
        return identityFile.getFirstName();
    }
}
