/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.nfc.sdk.card;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <h2>{@link CardType}</h2>
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
public enum CardType {

    TOPAZ( new byte[] {
            (byte) 0x3B, (byte) 0x8F, (byte) 0x80, (byte) 0x01, (byte) 0x80, (byte) 0x4F, (byte) 0x0C, (byte) 0xA0, (byte) 0x00,
            (byte) 0x00, (byte) 0x03, (byte) 0x06, (byte) 0x03, (byte) 0xF0, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x9F
    } ),
    MIFARE_1K( new byte[] {
            (byte) 0x3B, (byte) 0x8F, (byte) 0x80, (byte) 0x01, (byte) 0x80, (byte) 0x4F, (byte) 0x0C, (byte) 0xA0, (byte) 0x00,
            (byte) 0x00, (byte) 0x03, (byte) 0x06, (byte) 0x03, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x6A
    } );

    private static final Logger LOG = LoggerFactory.getLogger( CardType.class );

    private final byte[] ATR;

    private CardType(byte[] ATR) {

        this.ATR = ATR;
    }

    public byte[] getATR() {

        return ATR;
    }

    public static CardType getCardType(byte[] atr) {

        for (CardType cardType : CardType.values()) {
            LOG.debug( "checking card type " + cardType.name() );
            if (cardType.getATR().length != atr.length) {
                continue;
            }

            if (Arrays.equals( atr, cardType.getATR() )) {
                LOG.debug( "card type is: " + cardType.name() );
                return cardType;
            }
        }

        return null;
    }
}
