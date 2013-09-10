/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.nfc.sdk.pcsc;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import net.link.safeonline.nfc.sdk.card.CardType;


/**
 * <h2>{@link Pcsc}</h2>
 * <p/>
 * <p>
 * [description / usage].
 * </p>
 * <p/>
 * <p>
 * <i>Nov 6, 2009</i>
 * </p>
 *
 * @author wvdhaute
 */
public interface Pcsc {

    /**
     * Checks if a card reader is connected.
     */
    public boolean isCardReaderPresent()
            throws CardException;

    /**
     * Wait for a card reader to connect.
     */
    public void waitForCardReader()
            throws CardException;

    /**
     * Wait until a card is detected, returns {@link CardType} of connected card.
     */
    public CardType waitForCard()
            throws CardException, InterruptedException;

    /**
     * Disconnect the card.
     */
    public void disconnect()
            throws CardException;

    /**
     * Returns {@link CardType} of connected card.
     */
    public CardType getCardType()
            throws CardException;

    /**
     * Returns {@link CardChannel} to the connected card.
     */
    public CardChannel getCardChannel();
}
