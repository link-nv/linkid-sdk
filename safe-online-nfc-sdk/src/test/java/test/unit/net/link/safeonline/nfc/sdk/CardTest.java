/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.nfc.sdk;

import net.link.safeonline.nfc.sdk.card.*;
import net.link.safeonline.nfc.sdk.card.MiFare1K.KeyType;
import net.link.safeonline.nfc.sdk.card.MiFare1K.ValueBlockOperationType;
import net.link.safeonline.nfc.sdk.pcsc.Pcsc;
import net.link.safeonline.nfc.sdk.pcsc.PcscImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;


public class CardTest {

    private static final Log LOG = LogFactory.getLog( CardTest.class );

//    @Test
    public void testGetATR()
            throws Exception {

        Pcsc pcsc = new PcscImpl();

        LOG.debug( "Connect card reader if needed" );
        pcsc.waitForCardReader();

        LOG.debug( "Connect card to card reader" );
        CardType cardType = pcsc.waitForCard();

        LOG.debug( "cardtype = " + cardType.name() );
        if (cardType.equals( CardType.MIFARE_1K )) {

            MiFare1K miFare1K = new MiFare1K( pcsc.getCardChannel() );

            // read ATS
            byte[] ats = miFare1K.getATS();
            for (byte atsByte : ats)
                LOG.debug( "ATS: " + Integer.toHexString( atsByte & 0xff ) );

            // read ATS String
            LOG.debug( "ATS String: " + miFare1K.getATSString() );

            // load authentication key
            byte[] key = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
            byte blockNumber = (byte) 0x04;
            byte keyLocation = (byte) 0x00;
            miFare1K.loadKey( key, keyLocation, false );
            LOG.debug( "loaded key" );

            // authenticate
            miFare1K.authenticate( keyLocation, blockNumber, KeyType.TYPE_A );
            LOG.debug( "authenticated" );

            // read data block
            byte[] data = miFare1K.readData( blockNumber, 16 );
            for (byte dataByte : data)
                LOG.debug( "data: " + Integer.toHexString( dataByte & 0xff ) );

            // write data block
            miFare1K.writeData( blockNumber, new byte[] {
                    (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08,
                    (byte) 0x09, (byte) 0x0A, (byte) 0x0B, (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F
            } );
            LOG.debug( "wrote new data" );

            // read new data
            byte[] newData = miFare1K.readData( blockNumber, 16 );
            for (byte dataByte : newData)
                LOG.debug( "new data: " + Integer.toHexString( dataByte & 0xff ) );

            // store "1" in block 0x05
            byte valueBlock = (byte) 0x05;
            miFare1K.valueBlockOperation( valueBlock, ValueBlockOperationType.STORE, 1 );
            LOG.debug( "store value block" );

            // read out value block
            LOG.debug( "value block: " + miFare1K.readValueBlock( valueBlock ) );

            // copy value block 0x05 to 0x06
            miFare1K.restoreValueBlock( valueBlock, (byte) 0x06 );

            // increment value block 0x05 by "5"
            miFare1K.valueBlockOperation( valueBlock, ValueBlockOperationType.INCREMENT, 5 );

            // read out value block
            LOG.debug( "value block: " + miFare1K.readValueBlock( valueBlock ) );

            // decrement value block 0x05 by "2"
            miFare1K.valueBlockOperation( valueBlock, ValueBlockOperationType.DECREMENT, 2 );

            // read out value block
            LOG.debug( "value block: " + miFare1K.readValueBlock( valueBlock ) );
        } else if (cardType.equals( CardType.TOPAZ )) {

            Topaz topaz = new Topaz( pcsc.getCardChannel() );

            // read UID using RID command
            byte[] readUid = topaz.readID();
            for (byte uidByte : readUid)
                LOG.debug( "RID: " + Integer.toHexString( uidByte & 0xff ) );

            // read UID direct from memory
            byte[] uid = topaz.getUID();
            for (byte uidByte : uid)
                LOG.debug( "UID direct: " + Integer.toHexString( uidByte & 0xff ) );

            // read data @ block 1, byte 5
            byte origData = topaz.readMemory( 1, 5 );
            LOG.debug( "origData: " + Integer.toHexString( origData & 0xff ) );

            // update data @ block 1, byte 5
            topaz.writeMemory( 1, 5, (byte) 0x22 );
            LOG.debug( "update data" );

            // read new data @ block 1, byte 5
            byte newData = topaz.readMemory( 1, 5 );
            LOG.debug( "newData: " + Integer.toHexString( newData & 0xff ) );

            topaz.dumpMemory();
        }

        pcsc.disconnect();
    }
}
