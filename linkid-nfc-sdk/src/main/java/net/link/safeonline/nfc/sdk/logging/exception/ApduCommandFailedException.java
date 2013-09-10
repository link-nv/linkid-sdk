/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.nfc.sdk.logging.exception;

import javax.smartcardio.CommandAPDU;


/**
 * <h2>{@link ApduCommandFailedException}</h2>
 * <p/>
 * <p>
 * [description / usage].
 * </p>
 * <p/>
 * <p>
 * <i>Nov 9, 2009</i>
 * </p>
 *
 * @author wvdhaute
 */
public class ApduCommandFailedException extends Exception {

    private CommandAPDU commandAPDU;

    public ApduCommandFailedException(CommandAPDU commandApdu) {

        commandAPDU = commandApdu;
    }

    public CommandAPDU getCommandAPDU() {

        return commandAPDU;
    }
}
