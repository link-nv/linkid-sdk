/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.nfc.sdk.pcsc;

import java.util.List;
import javax.smartcardio.*;
import net.link.safeonline.nfc.sdk.card.CardType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <h2>{@link PcscImpl}</h2>
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
public class PcscImpl implements Pcsc {

    private static final Logger LOG = LoggerFactory.getLogger( PcscImpl.class );

    // private static final byte[] GET_FIRMWARE_COMMAND = new byte[] { (byte) 0xFF, (byte) 0x00, (byte) 0x48, (byte) 0x00, (byte) 0x00 };

    private final CardTerminals cardTerminals;

    private Card card;

    private CardChannel cardChannel;

    public PcscImpl() {

        TerminalFactory factory = TerminalFactory.getDefault();
        cardTerminals = factory.terminals();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isCardReaderPresent()
            throws CardException {

        try {
            List<CardTerminal> cardTerminalList = cardTerminals.list();
            return false == cardTerminalList.isEmpty();
        }
        catch (CardException e) {
            LOG.error( "Failed to list card terminals", e );
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void waitForCardReader()
            throws CardException {

        while (false == isCardReaderPresent()) {
            try {
                Thread.sleep( 1000 );
            }
            catch (InterruptedException e) {
                throw new RuntimeException( e );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public CardType waitForCard()
            throws CardException, InterruptedException {

        CardType cardType = getCardType();
        if (null != cardType) {
            return cardType;
        }

        LOG.debug( "Waiting for a card to connect..." );
        while (true) {
            try {
                cardTerminals.waitForChange();
            }
            catch (CardException e) {
                LOG.error( "Failed to wait for card change", e );
                return null;
            }

            cardType = getCardType();
            if (null != cardType) {
                return cardType;
            }

            LOG.debug( "no cards found, ima sleep a bit before trying again" );
            Thread.sleep( 2000 );
        }
    }

    /**
     * {@inheritDoc}
     */
    public CardType getCardType()
            throws CardException {

        List<CardTerminal> cardTerminalList;
        try {
            cardTerminalList = cardTerminals.list();
        }
        catch (CardException e) {
            LOG.error( "Failed to list card terminals", e );
            throw e;
        }

        for (CardTerminal tCardTerminal : cardTerminalList) {
            try {
                Thread.sleep( 2000 );
            }
            catch (InterruptedException e) {
                throw new RuntimeException( e );
            }

            LOG.debug( "handling cardterminal: " + tCardTerminal.getName() );
            try {
                Thread.sleep( 2000 );
            }
            catch (InterruptedException e) {
                throw new RuntimeException( e );
            }

            if (tCardTerminal.isCardPresent()) {
                try {
                    Thread.sleep( 2000 );
                }
                catch (InterruptedException e) {
                    throw new RuntimeException( e );
                }
                LOG.debug( "card present" );
                try {
                    Card tCard = tCardTerminal.connect( "*" );
                    ATR atr = tCard.getATR();
                    LOG.debug( "atr: " + PcscUtil.byte2String( atr.getBytes() ) );

                    CardType cardType = CardType.getCardType( atr.getBytes() );
                    if (null != cardType) {
                        card = tCard;
                        cardChannel = tCard.getBasicChannel();
                    }
                    return cardType;
                }
                catch (CardException e) {
                    LOG.error( "failed to establish connection to card", e );
                    throw e;
                }
            }
        }
        LOG.debug( "no card found" );
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void disconnect()
            throws CardException {

        if (null != card) {
            LOG.debug( "disconnecting card..." );
            card.disconnect( true );
        }
    }

    /**
     * {@inheritDoc}
     */
    public CardChannel getCardChannel() {

        return cardChannel;
    }
}
