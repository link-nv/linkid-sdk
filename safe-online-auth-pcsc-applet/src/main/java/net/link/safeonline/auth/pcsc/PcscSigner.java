/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.pcsc;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

import net.link.safeonline.shared.Signer;


public class PcscSigner extends Pcsc implements Signer {

    private final PcscSignerLogger logger;


    public PcscSigner(CardChannel cardChannel, PcscSignerLogger logger) {

        super(cardChannel);
        if (null == logger) {
            this.logger = new NullPcscSignerLogger();
        } else {
            this.logger = logger;
        }
    }

    public X509Certificate getCertificate() {

        try {
            return this.getAuthenticationCertificate();
        } catch (Exception e) {
            this.logger.log("getCert error: " + e.getMessage());
            throw new RuntimeException("getCert error");
        }
    }

    public byte[] sign(byte[] data) {

        PinDialog pinDialog = new PinDialog();
        String pin = pinDialog.getPin();
        if (null == pin) {
            this.logger.log("PIN canceled");
            throw new RuntimeException("PIN canceled");
        }
        byte[] signatureValue;
        try {
            this.logger.log("signing...");
            signatureValue = super.sign(data, pin);
        } catch (CardException e) {
            this.logger.log("card error: " + e.getMessage());
            throw new RuntimeException("card error: " + e.getMessage(), e);
        } catch (IOException e) {
            this.logger.log("IO error: " + e.getMessage());
            throw new RuntimeException("IO error: " + e.getMessage(), e);
        } catch (Exception e) {
            this.logger.log("sign error: " + e.getMessage());
            throw new RuntimeException("sign error: " + e.getMessage(), e);
        }
        return signatureValue;
    }
}
