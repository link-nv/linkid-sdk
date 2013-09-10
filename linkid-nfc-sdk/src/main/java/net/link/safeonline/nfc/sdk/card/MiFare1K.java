/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.nfc.sdk.card;

import java.util.Arrays;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import net.link.safeonline.nfc.sdk.logging.exception.ApduCommandFailedException;
import net.link.safeonline.nfc.sdk.pcsc.PcscUtil;


/**
 * <h2>{@link MiFare1K}</h2>
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
public class MiFare1K {

    private static final byte[] GET_ATS_COMMAND = new byte[] { (byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x04 };

    private static final byte[] LOAD_KEY_COMMAND = new byte[] {
            (byte) 0xFF, // class
            (byte) 0x82, // INS
            (byte) 0x00, // key structure ( 0x00 : volatile )
            (byte) 0x00, // key location ( [0x00..0x1F] )
            (byte) 0x06, // LC
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 // key
    };

    private static final byte[] AUTHENTICATE_COMMAND = new byte[] {
            (byte) 0xFF, // class
            (byte) 0x86, // INS
            (byte) 0x00, // P1
            (byte) 0x00, // P2
            (byte) 0x05, // LC
            (byte) 0x01, // version
            (byte) 0x00, //
            (byte) 0x00, // block number
            (byte) 0x00, // key type
            (byte) 0x00 // key number
    };

    private static final byte[] READ_DATA_COMMAND = new byte[] {
            (byte) 0xFF, // class
            (byte) 0xB0, // INS
            (byte) 0x00, // P1
            (byte) 0x00, // block number
            (byte) 0x00 // Number of bytes to read
    };

    private static final byte[] WRITE_DATA_COMMAND = new byte[] {
            (byte) 0xFF, // class
            (byte) 0xD6, // INS
            (byte) 0x00, // P1
            (byte) 0x00, // block number
            (byte) 0x00 // Number of bytes to write
            // ... 1 to 16 bytes
    };

    private static final byte[] VALUE_BLOCK_OPERATION_COMMAND = new byte[] {
            (byte) 0xFF, // class
            (byte) 0xD7, // INS
            (byte) 0x00, // P1
            (byte) 0x00, // block number
            (byte) 0x05, // LC
            (byte) 0x00, // value block operation
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 // value ( MSB..LSB )
    };

    private static final byte[] READ_VALUE_BLOCK_COMMAND = new byte[] {
            (byte) 0xFF, // class
            (byte) 0xB1, // INS
            (byte) 0x00, // P1
            (byte) 0x00, // block number
            (byte) 0x04
    };

    private static final byte[] RESTORE_VALUE_BLOCK_COMMAND = new byte[] {
            (byte) 0xFF, // class
            (byte) 0xD7, // INS
            (byte) 0x00, // P1
            (byte) 0x00, // source block number
            (byte) 0x02, // Lc
            (byte) 0x03, //
            (byte) 0x00, // target block number
    };

    private CardChannel cardChannel;


    public enum KeyType {

        TYPE_A,
        TYPE_B;
    }


    public enum ValueBlockOperationType {

        STORE( (byte) 0x00 ),
        INCREMENT( (byte) 0x01 ),
        DECREMENT( (byte) 0x02 );

        private final byte operationByte;

        private ValueBlockOperationType(byte operationByte) {

            this.operationByte = operationByte;
        }

        public byte getOperationByte() {

            return operationByte;
        }
    }

    public MiFare1K(CardChannel cardChannel) {

        this.cardChannel = cardChannel;
    }

    /**
     * Returns ATS for this card.
     */
    public byte[] getATS()
            throws CardException, ApduCommandFailedException {

        return PcscUtil.transmit( cardChannel, GET_ATS_COMMAND );
    }

    /**
     * Returns card's ATS in {@link String} format.
     */
    public String getATSString()
            throws CardException, ApduCommandFailedException {

        return PcscUtil.byte2String( getATS() );
    }

    /**
     * Load authentication key into reader
     */
    public void loadKey(byte[] key, byte keyLocation, boolean persistent)
            throws CardException, ApduCommandFailedException {

        if (key.length != 6)
            throw new IllegalArgumentException( "Key length must be 6 bytes" );

        checkKeyLocation( keyLocation );

        byte[] loadKeyBytes = LOAD_KEY_COMMAND.clone();
        loadKeyBytes[2] = persistent? (byte) 0x01: (byte) 0x00;
        loadKeyBytes[3] = keyLocation;
        loadKeyBytes[5] = key[0];
        loadKeyBytes[6] = key[1];
        loadKeyBytes[7] = key[2];
        loadKeyBytes[8] = key[3];
        loadKeyBytes[9] = key[4];
        loadKeyBytes[10] = key[5];

        PcscUtil.transmit( cardChannel, loadKeyBytes );
    }

    /**
     * Authenticate certain data block
     */
    public void authenticate(byte keyLocation, byte block, KeyType keyType)
            throws CardException, ApduCommandFailedException {

        checkKeyLocation( keyLocation );
        checkBlock( block );

        byte[] authenticateBytes = AUTHENTICATE_COMMAND.clone();
        authenticateBytes[7] = block;
        authenticateBytes[8] = keyType.equals( KeyType.TYPE_A )? (byte) 0x60: (byte) 0x61;
        authenticateBytes[9] = keyLocation;

        PcscUtil.transmit( cardChannel, authenticateBytes );
    }

    /**
     * Read specified number of bytes from specified "data block".
     */
    public byte[] readData(byte block, int numberOfBytes)
            throws CardException, ApduCommandFailedException {

        checkBlock( block );

        if (numberOfBytes > 16 || numberOfBytes < 1)
            throw new IllegalArgumentException( "Number of bytes must be in [1..16] range" );

        byte[] readDataBytes = READ_DATA_COMMAND.clone();
        readDataBytes[3] = block;
        readDataBytes[4] = (byte) numberOfBytes;

        return PcscUtil.transmit( cardChannel, readDataBytes );
    }

    /**
     * Write data to specified "data block".
     */
    public void writeData(byte block, byte[] data)
            throws CardException, ApduCommandFailedException {

        checkBlock( block );

        if (null == data || data.length > 16)
            throw new IllegalArgumentException( "Number of bytes must be in [1..16] range" );

        byte[] writeDataBytes = Arrays.copyOf( WRITE_DATA_COMMAND, WRITE_DATA_COMMAND.length + data.length );
        writeDataBytes[WRITE_DATA_COMMAND.length - 2] = block;
        writeDataBytes[WRITE_DATA_COMMAND.length - 1] = (byte) data.length;
        for (int i = 0; i < data.length; i++)
            writeDataBytes[WRITE_DATA_COMMAND.length + i] = data[i];

        PcscUtil.transmit( cardChannel, writeDataBytes );
    }

    /**
     * Perform specified value block operation on specified block
     */
    public void valueBlockOperation(byte block, ValueBlockOperationType operation, int value)
            throws CardException, ApduCommandFailedException {

        checkBlock( block );

        byte[] valueBytes = PcscUtil.int2byte( value );

        byte[] valueBlockOperationBytes = VALUE_BLOCK_OPERATION_COMMAND.clone();
        valueBlockOperationBytes[3] = block;
        valueBlockOperationBytes[5] = operation.getOperationByte();
        valueBlockOperationBytes[6] = valueBytes[0];
        valueBlockOperationBytes[7] = valueBytes[1];
        valueBlockOperationBytes[8] = valueBytes[2];
        valueBlockOperationBytes[9] = valueBytes[3];

        PcscUtil.transmit( cardChannel, valueBlockOperationBytes );
    }

    /**
     * Read out value block of specified block
     */
    public int readValueBlock(byte block)
            throws CardException, ApduCommandFailedException {

        checkBlock( block );

        byte[] readValueBlockBytes = READ_VALUE_BLOCK_COMMAND.clone();
        readValueBlockBytes[3] = block;

        byte[] valueBytes = PcscUtil.transmit( cardChannel, readValueBlockBytes );
        return PcscUtil.byte2int( valueBytes );
    }

    /**
     * Restore. Copy value from source value block to target value block.
     */
    public void restoreValueBlock(byte sourceBlock, byte targetBlock)
            throws CardException, ApduCommandFailedException {

        checkBlock( sourceBlock );
        checkBlock( targetBlock );

        byte[] restoreValueBlockBytes = RESTORE_VALUE_BLOCK_COMMAND.clone();
        restoreValueBlockBytes[3] = sourceBlock;
        restoreValueBlockBytes[6] = targetBlock;

        PcscUtil.transmit( cardChannel, restoreValueBlockBytes );
    }

    private void checkBlock(byte block) {

        if (block > (byte) 0x3E || block < (byte) 0x00)
            throw new IllegalArgumentException( "Block must be in [0x00..0x3E] range" );
    }

    private void checkKeyLocation(byte keyLocation) {

        if (keyLocation > (byte) 0x1F || keyLocation < (byte) 0x00)
            throw new IllegalArgumentException( "Key location must be in [0x00..0x1F] range" );
    }
}
