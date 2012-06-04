/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.nfc.sdk.pcsc;

import javax.smartcardio.*;
import net.link.safeonline.nfc.sdk.logging.exception.ApduCommandFailedException;


/**
 * <h2>{@link PcscUtil}</h2>
 * <p/>
 * <p>
 * PCSC utility class for APDU commands.
 * </p>
 * <p/>
 * <p>
 * <i>Nov 10, 2009</i>
 * </p>
 *
 * @author wvdhaute
 */
public class PcscUtil {

    private static final int APDU_SUCCESS = 0x9000;

    public static byte[] transmit(CardChannel cardChannel, byte[] apduBytes)
            throws CardException, ApduCommandFailedException {

        CommandAPDU commandApdu = new CommandAPDU( apduBytes );
        ResponseAPDU responseApdu = cardChannel.transmit( commandApdu );
        if (APDU_SUCCESS != responseApdu.getSW())
            throw new ApduCommandFailedException( commandApdu );
        return responseApdu.getData();
    }

    public static byte[] int2byte(int i) {

        byte dest[] = new byte[4];
        dest[3] = (byte) (i & 0xff);
        dest[2] = (byte) (i >>> 8 & 0xff);
        dest[1] = (byte) (i >>> 16 & 0xff);
        dest[0] = (byte) (i >>> 24 & 0xff);
        return dest;
    }

    public static int byte2int(byte bytes[]) {

        return (bytes[0] & 0xff) << 24 | (bytes[1] & 0xff) << 16 | (bytes[2] & 0xff) << 8 | bytes[3] & 0xff;
    }

    public static String byte2String(byte bytes[]) {

        String byteString = "";
        for (byte b : bytes)
            byteString += Integer.toHexString( b & 0xff ) + " ";
        return byteString.trim();
    }
}
