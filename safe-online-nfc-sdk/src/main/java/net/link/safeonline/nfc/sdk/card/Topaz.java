/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.nfc.sdk.card;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import net.link.safeonline.nfc.sdk.logging.exception.ApduCommandFailedException;
import net.link.safeonline.nfc.sdk.pcsc.PcscUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <h2>{@link Topaz}</h2>
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
public class Topaz {

    private static final Logger LOG = LoggerFactory.getLogger( Topaz.class );

    private static final byte[] READ_ID_MEMORY_COMMAND = new byte[] {
            (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Pseudo APDU
            (byte) 0x04, // length of pseudo APDU data
            (byte) 0xD4, (byte) 0x40, (byte) 0x01, // data exchange command
            (byte) 0x78, // read ID command
    };

    private static final byte[] READ_ALL_MEMORY_COMMAND = new byte[] {
            (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Pseudo APDU
            (byte) 0x04, // length of pseudo APDU data
            (byte) 0xD4, (byte) 0x40, (byte) 0x01, // data exchange command
            (byte) 0x00, // read all command
    };

    private static final byte[] READ_MEMORY_COMMAND  = new byte[] {
            (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Pseudo APDU
            (byte) 0x05, // length of pseudo APDU data
            (byte) 0xD4, (byte) 0x40, (byte) 0x01, // data exchange command
            (byte) 0x01, // read command
            (byte) 0x00 // memory address
    };
    private static final byte[] WRITE_MEMORY_COMMAND = new byte[] {
            (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Pseudo APDU
            (byte) 0x06, // length of pseudo APDU data
            (byte) 0xD4, (byte) 0x40, (byte) 0x01, // data exchange command
            (byte) 0x53, // update command
            (byte) 0x00, // memory address
            (byte) 0x00 // data
    };

    private CardChannel cardChannel;

    public Topaz(CardChannel cardChannel) {

        this.cardChannel = cardChannel;
    }

    /**
     * Returns UID bytes, big endian.
     */
    public byte[] getUID()
            throws CardException, ApduCommandFailedException {

        byte[] uid = new byte[7];

        uid[0] = readMemory( 0, 6 );
        uid[1] = readMemory( 0, 5 );
        uid[2] = readMemory( 0, 4 );
        uid[3] = readMemory( 0, 3 );
        uid[4] = readMemory( 0, 2 );
        uid[5] = readMemory( 0, 1 );
        uid[6] = readMemory( 0, 0 );

        return uid;
    }

    /**
     * Returns UID bytes as string, big endian.
     */
    public String getUIDString()
            throws CardException, ApduCommandFailedException {

        return PcscUtil.byte2String( getUID() );
    }

    /**
     * Read memory address @ specified block.
     */
    public byte readMemory(int blockNr, int byteNr)
            throws CardException, ApduCommandFailedException {

        if (blockNr > 12 || blockNr < 0) {
            throw new IllegalArgumentException( "Block number must be in range [0..12]" );
        }

        if (byteNr > 7 || byteNr < 0) {
            throw new IllegalArgumentException( "Byte number must be in range [0..7]" );
        }

        int memoryAddress = blockNr * 8 + byteNr;

        byte[] getMemoryBytes = READ_MEMORY_COMMAND.clone();
        getMemoryBytes[getMemoryBytes.length - 1] = new Integer( memoryAddress ).byteValue();

        byte[] response = PcscUtil.transmit( cardChannel, getMemoryBytes );
        return response[3]; // first 3 bytes are equal to the data exchange command of the pseudo apdu command ( D5 41 00 )
    }

    /**
     * Update memory @ specified address.
     */
    public void writeMemory(int blockNr, int byteNr, byte data)
            throws CardException, ApduCommandFailedException {

        if (blockNr > 12 || blockNr < 0) {
            throw new IllegalArgumentException( "Block number must be in range [0..12]" );
        }

        if (byteNr > 7 || byteNr < 0) {
            throw new IllegalArgumentException( "Byte number must be in range [0..7]" );
        }

        int memoryAddress = blockNr * 8 + byteNr;

        byte[] writeMemoryBytes = WRITE_MEMORY_COMMAND.clone();
        writeMemoryBytes[writeMemoryBytes.length - 2] = new Integer( memoryAddress ).byteValue();
        writeMemoryBytes[writeMemoryBytes.length - 1] = data;

        PcscUtil.transmit( cardChannel, writeMemoryBytes );
    }

    /**
     * Read out all memory
     */
    public byte[] readAll()
            throws CardException, ApduCommandFailedException {

        byte[] result = PcscUtil.transmit( cardChannel, READ_ALL_MEMORY_COMMAND );
        byte[] rall = new byte[result.length - 5]; // first 5 bytes returned are: 3 bytes data exchange format, HR0, HR1
        for (int i = 0; i < rall.length; i++) {
            rall[i] = result[5 + i];
        }
        return rall;
    }

    /**
     * RID command, returns 4 LSB's of 7 bytes UID
     */
    public byte[] readID()
            throws CardException, ApduCommandFailedException {

        byte[] result = PcscUtil.transmit( cardChannel, READ_ID_MEMORY_COMMAND );
        byte[] rid = new byte[4]; // first 5 bytes returned are: 3 bytes data exchange format, HR0, HR1
        rid[0] = result[5];
        rid[1] = result[6];
        rid[2] = result[7];
        rid[3] = result[8];
        return rid;
    }

    /**
     * Dumps memory of card to log.
     */
    public void dumpMemory()
            throws CardException, ApduCommandFailedException {

        byte[] bytes = readAll();
        LOG.debug( "Topaz Memory Dump" );
        LOG.debug( String.format( "---------------------------------------------------------------------------------" ) );
        LOG.debug( String.format( "| BLock # | Byte-0 | Byte-1 | Byte-2 | Byte-3 | Byte-4 | Byte-5 | Byte-6 | Byte-7" ) );
        LOG.debug( String.format( "---------------------------------------------------------------------------------" ) );
        LOG.debug( String.format( "|     0   |   %2x   |   %2x   |   %2x   |   %2x   |   %2x   |   %2x   |   %2x   |   %2x  ", bytes[0],
                bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7] ) );
        for (int i = 8; i < bytes.length;) {
            LOG.debug( String.format( "|    %2d   |   %2x   |   %2x   |   %2x   |   %2x   |   %2x   |   %2x   |   %2x   |   %2x  ", i / 8,
                    bytes[i + 0], bytes[i + 1], bytes[i + 2], bytes[i + 3], bytes[i + 4], bytes[i + 5], bytes[i + 6], bytes[i + 7] ) );
            i += 8;
        }
    }
}
