/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sc.pcsc.common;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

import net.link.safeonline.applet.PinDialog;
import net.link.safeonline.sc.pcsc.auth.AuthenticationMessages;
import net.link.safeonline.sc.pcsc.auth.AuthenticationMessages.KEY;
import net.link.safeonline.shared.Signer;


public class PcscSigner extends Pcsc implements Signer {

    private final PcscSignerLogger logger;

    private AuthenticationMessages messages;


    public PcscSigner(CardChannel cardChannel, PcscSignerLogger logger, AuthenticationMessages messages) {

        super(cardChannel);
        if (null == logger) {
            this.logger = new NullPcscSignerLogger();
        } else {
            this.logger = logger;
        }

        this.messages = messages;
    }

    public X509Certificate getCertificate() {

        try {
            return getAuthenticationCertificate();
        } catch (Exception e) {
            logger.log("getCert error: " + e.getMessage());
            throw new RuntimeException("getCert error");
        }
    }

    public byte[] sign(byte[] data) {

        PinDialog pinDialog = new PinDialog(messages.getString(KEY.ENTER_PIN));
        String pin = pinDialog.getPin();
        if (null == pin) {
            logger.log("PIN canceled");
            throw new RuntimeException("PIN canceled");
        }
        byte[] signatureValue;
        try {
            logger.log("signing...");
            signatureValue = super.sign(data, pin);
        } catch (CardException e) {
            logger.log("card error: " + e.getMessage());
            throw new RuntimeException("card error: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.log("IO error: " + e.getMessage());
            throw new RuntimeException("IO error: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.log("sign error: " + e.getMessage());
            throw new RuntimeException("sign error: " + e.getMessage(), e);
        }
        return signatureValue;
    }
}
