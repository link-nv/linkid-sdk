/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.pcsc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class Pcsc {

    public static final String       BEID_ATR_11    = "3b9813400aa503010101ad1311";

    public static final String       BEID_ATR_10    = "3b9894400aa503010101ad1310";

    public static final String       BEID_ATR_100   = "3b989440ffa503010101ad1310";

    public static final byte[]       MF             = new byte[] { 0x3F, 0x00 };

    /**
     * 3F00 = MF, DF01 = DF(ID), 4031 = EF(ID#RN)
     */
    public static final byte[]       EF_ID_RN       = new byte[] { 0x3F, 0x00, (byte) 0xDF, 0x01, 0x40, 0x31 };

    /**
     * 3F00 = MF, DF01 = DF(ID), 4032 = EF(SGN#RN)
     */
    public static final byte[]       EF_SGN_RN      = new byte[] { 0x3F, 0x00, (byte) 0xDF, 0x01, 0x40, 0x32 };

    /**
     * 3F00 = MF, DF01 = DF(ID), 4033 = EF(ID#Address)
     */
    public static final byte[]       EF_ID_ADDRESS  = new byte[] { 0x3F, 0x00, (byte) 0xDF, 0x01, 0x40, 0x33 };

    /**
     * 3F00 = MF, DF01 = DF(ID), 4034 = EF(SGN#Address)
     */
    public static final byte[]       EF_SGN_ADDRESS = new byte[] { 0x3F, 0x00, (byte) 0xDF, 0x01, 0x40, 0x34 };

    /**
     * 3F00 = MF, DF01 = DF(ID), 4035 = EF(ID#Photo)
     */
    public static final byte[]       EF_ID_PHOTO    = new byte[] { 0x3F, 0x00, (byte) 0xDF, 0x01, 0x40, 0x35 };

    /**
     * 3F00 = MF, 2F00 = EF(DIR)
     */
    public static final byte[]       EF_DIR         = new byte[] { 0x3F, 0x00, 0x2F, 0x00 };

    /**
     * 3F00 = MF, DF00 = DF(BELPIC), 5038 = EF(Cert#2)
     */
    public static final byte[]       EF_CERT_2      = new byte[] { 0x3F, 0x00, (byte) 0xDF, 0x00, 0x50, 0x38 };

    /**
     * 3F00 = MF, DF00 = DF(BELPIC), 5039 = EF(Cert#3)
     */
    public static final byte[]       EF_CERT_3      = new byte[] { 0x3F, 0x00, (byte) 0xDF, 0x00, 0x50, 0x39 };

    /**
     * 3F00 = MF, DF00 = DF(BELPIC), 503A = EF(Cert#4)
     */
    public static final byte[]       EF_CERT_4      = new byte[] { 0x3F, 0x00, (byte) 0xDF, 0x00, 0x50, 0x3A };

    /**
     * 3F00 = MF, DF00 = DF(BELPIC), 503B = EF(Cert#6)
     */
    public static final byte[]       EF_CERT_6      = new byte[] { 0x3F, 0x00, (byte) 0xDF, 0x00, 0x50, 0x3B };

    /**
     * 3F00 = MF, DF00 = DF(BELPIC), 503C = EF(Cert#8)
     */
    public static final byte[]       EF_CERT_8      = new byte[] { 0x3F, 0x00, (byte) 0xDF, 0x00, 0x50, 0x3C };

    private static final Log         LOG            = LogFactory.getLog(Pcsc.class);

    private final CardChannel        cardChannel;

    private final CertificateFactory certificateFactory;

    private X509Certificate          authenticationCertificate;

    private X509Certificate          signingCertificate;

    private X509Certificate          caCertificate;

    private X509Certificate          rootCertificate;

    private X509Certificate          nationalRegisterCertificate;

    private BufferedImage            photo;


    public Pcsc(CardChannel cardChannel) {

        this.cardChannel = cardChannel;
        try {
            certificateFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new RuntimeException("Certificate Factory error: " + e.getMessage(), e);
        }
    }

    public ResponseAPDU transmit(CommandAPDU commandApdu)
            throws CardException {

        ResponseAPDU responseApdu = cardChannel.transmit(commandApdu);
        return responseApdu;
    }

    public byte[] readFile(byte[] fullAid)
            throws CardException {

        CommandAPDU selectFileApdu = new CommandAPDU(0x00, 0xA4, 0x08, 0x0C, fullAid);
        ResponseAPDU responseApdu = cardChannel.transmit(selectFileApdu);
        checkResponseSW(responseApdu);

        int offset = 0;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data;
        do {
            CommandAPDU readBinaryApdu = new CommandAPDU(0x00, 0xB0, offset >> 8, offset & 0xFF, 0xFF);
            responseApdu = cardChannel.transmit(readBinaryApdu);
            checkResponseSW(responseApdu);
            data = responseApdu.getData();
            LOG.debug("reading offset: " + offset + " # bytes: " + data.length);
            offset += data.length;
            try {
                buffer.write(data);
            } catch (IOException e) {
                throw new RuntimeException("IO error: " + e.getMessage(), e);
            }
        } while (0xFF == data.length);
        return buffer.toByteArray();
    }

    public IdentityFile getIdentityFile()
            throws CardException {

        byte[] data = readFile(EF_ID_RN);
        IdentityFile identifyFile = parseTagLengthValueData(data, IdentityFile.class);
        return identifyFile;
    }

    public AddressFile getAddressFile()
            throws CardException {

        byte[] data = readFile(EF_ID_ADDRESS);
        AddressFile addressFile = parseTagLengthValueData(data, AddressFile.class);
        return addressFile;
    }

    public X509Certificate getAuthenticationCertificate()
            throws CardException, CertificateException {

        if (null == authenticationCertificate) {
            authenticationCertificate = getCertificate(EF_CERT_2);
        }
        return authenticationCertificate;
    }

    public X509Certificate getSigningCertificate()
            throws CardException, CertificateException {

        if (null == signingCertificate) {
            signingCertificate = getCertificate(EF_CERT_3);
        }
        return signingCertificate;
    }

    public X509Certificate getCACertificate()
            throws CardException, CertificateException {

        if (null == caCertificate) {
            caCertificate = getCertificate(EF_CERT_4);
        }
        return caCertificate;
    }

    public X509Certificate getRootCertificate()
            throws CardException, CertificateException {

        if (null == rootCertificate) {
            rootCertificate = getCertificate(EF_CERT_6);
        }
        return rootCertificate;
    }

    public X509Certificate getNationalRegisterCertificate()
            throws CardException, CertificateException {

        if (null == nationalRegisterCertificate) {
            nationalRegisterCertificate = getCertificate(EF_CERT_8);
        }
        return nationalRegisterCertificate;
    }

    public BufferedImage getPhoto()
            throws CardException, IOException {

        if (null == photo) {
            byte[] data = readFile(EF_ID_PHOTO);
            photo = ImageIO.read(new ByteArrayInputStream(data));
        }
        return photo;
    }

    public boolean verifyRnSignature()
            throws CardException, CertificateException, InvalidKeyException, SignatureException {

        byte[] signatureData = readFile(EF_SGN_RN);
        LOG.debug("RN signature size: " + signatureData.length);

        byte[] photoData = readFile(EF_ID_PHOTO);
        MessageDigest hash;
        try {
            hash = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA1 algo not available");
        }
        byte[] photoDigest = hash.digest(photoData);
        IdentityFile identityFile = getIdentityFile();
        if (false == Arrays.equals(photoDigest, identityFile.hashPhoto))
            return false;

        Signature signature;
        try {
            signature = Signature.getInstance("SHA1withRSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA1withRSA algo nog available");
        }
        X509Certificate nrCertificate = getNationalRegisterCertificate();
        signature.initVerify(nrCertificate);
        byte[] idData = readFile(EF_ID_RN);
        signature.update(idData);
        boolean result = signature.verify(signatureData);
        return result;
    }

    public boolean verifyAddressSignature()
            throws CardException, CertificateException, InvalidKeyException, SignatureException {

        byte[] signatureData = readFile(EF_SGN_ADDRESS);
        Signature signature;
        try {
            signature = Signature.getInstance("SHA1withRSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA1withRSA algo nog available");
        }
        X509Certificate nrCertificate = getNationalRegisterCertificate();
        signature.initVerify(nrCertificate);
        byte[] addressData = readFile(EF_ID_ADDRESS);
        signature.update(addressData);
        byte[] idSignData = readFile(EF_SGN_RN);
        signature.update(idSignData);
        boolean result = signature.verify(signatureData);
        return result;
    }

    public byte[] sign(byte[] data, String pin)
            throws CardException, IOException {

        MessageDigest hash;
        try {
            hash = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA1 digest algo not available");
        }
        byte[] digest = hash.digest(data);
        LOG.debug("digest size: " + digest.length);

        // 4 = size of following data block, 80 = algo tag, 0x01 = RSA PKCS#1,
        // 0x84 = key tag, 0x82 = authn key
        CommandAPDU setApdu = new CommandAPDU(0x00, 0x22, 0x41, 0xB6, new byte[] { 0x04, (byte) 0x80, 0x01, (byte) 0x84, (byte) 0x82 });
        ResponseAPDU responseApdu = cardChannel.transmit(setApdu);
        checkResponseSW(responseApdu);

        /*
         * Convert PIN to BCD
         */
        ByteArrayOutputStream pinOutput = new ByteArrayOutputStream();
        for (int idx = 0; idx < pin.length(); idx += 2) {
            int bcd = Integer.parseInt(pin.substring(idx, idx + 2), 16);
            pinOutput.write(bcd);
        }
        byte[] pinBcd = pinOutput.toByteArray();
        byte[] verifyData = new byte[] { (byte) (0x20 | pin.length()), (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF };
        System.arraycopy(pinBcd, 0, verifyData, 1, pin.length() / 2);

        CommandAPDU verifyApdu = new CommandAPDU(0x00, 0x20, 0x00, 0x01, verifyData);
        responseApdu = cardChannel.transmit(verifyApdu);
        checkResponseSW(responseApdu);

        byte[] ALGORITHM_IDENTIFIER_SHA1 = new byte[] { 0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2B, 0x0E, 0x03, 0x02, 0x1A, 0x05, 0x00, 0x04,
                0x14 };
        ByteArrayOutputStream signData = new ByteArrayOutputStream();
        signData.write(ALGORITHM_IDENTIFIER_SHA1);
        signData.write(digest);
        byte[] dsData = signData.toByteArray();
        CommandAPDU computeDigitalSignatureApdu = new CommandAPDU(0x00, 0x2A, 0x9E, 0x9A, dsData);
        responseApdu = cardChannel.transmit(computeDigitalSignatureApdu);
        checkResponseSW(responseApdu);

        byte[] signature = responseApdu.getData();
        LOG.debug("signature size: " + signature.length);
        return signature;
    }

    private X509Certificate getCertificate(byte[] fullAid)
            throws CardException, CertificateException {

        byte[] data = readFile(fullAid);
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(data));
        return certificate;
    }

    private void checkResponseSW(ResponseAPDU responseApdu) {

        if (0x90 != responseApdu.getSW1()) {
            LOG.debug("SW1-SW2: " + Integer.toHexString(responseApdu.getSW1()) + "-" + Integer.toHexString(responseApdu.getSW2()));
            throw new RuntimeException("SW1: " + Integer.toHexString(responseApdu.getSW1()));
        }
        if (0x00 != responseApdu.getSW2()) {
            LOG.debug("SW1-SW2: " + Integer.toHexString(responseApdu.getSW1()) + "-" + Integer.toHexString(responseApdu.getSW2()));
            throw new RuntimeException("SW2: " + Integer.toHexString(responseApdu.getSW2()));
        }
    }

    /**
     * TLV: Tag Length Value structure.
     * 
     * @param <T>
     * @param data
     * @param type
     * @return
     */
    private <T> T parseTagLengthValueData(byte[] data, Class<T> type) {

        int idx = 0;
        T result;
        try {
            result = type.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("could not instantiate class: " + type.getName());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("illegal access error on: " + type.getName());
        }
        while (idx < data.length - 1) {
            /*
             * data.length - 1 : we expect at least 2 more bytes, else we stop.
             */
            int tag = data[idx];
            idx++;
            LOG.debug("tag: " + tag);

            int length = data[idx];
            idx++;
            LOG.debug("length: " + length);
            byte[] entry = Arrays.copyOfRange(data, idx, idx + length);
            idx += length;

            Field field = findTaggedField(type, tag);
            if (null != field) {
                setValue(field, entry, result);
            }
        }
        return result;
    }

    private void setValue(Field field, byte[] value, Object object) {

        field.setAccessible(true);
        Object fieldValue;
        try {
            Convert convertAnnotation = field.getAnnotation(Convert.class);
            if (null != convertAnnotation) {
                Class<? extends Convertor<?>> convertorClass = convertAnnotation.value();
                LOG.debug("using convertor: " + convertorClass.getName());
                Convertor<?> convertor;
                try {
                    convertor = convertorClass.newInstance();
                } catch (InstantiationException e) {
                    throw new RuntimeException("could not instantiate convertor: " + convertorClass.getName());
                }
                try {
                    fieldValue = convertor.convert(value);
                } catch (ConvertorException e) {
                    throw new RuntimeException("convert error on field \"" + field.getName() + "\": " + e.getMessage());
                }
            } else if (String.class.equals(field.getType())) {
                fieldValue = new String(value, Charset.forName("UTF-8"));
            } else {
                fieldValue = value;
            }
            LOG.debug("field value type: " + fieldValue.getClass().getName());
            field.set(object, fieldValue);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("field type incorrect: " + field.getType().getName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("cannot access field: " + field.getName());
        }
    }

    private Field findTaggedField(Class<?> type, int tag) {

        for (Field field : type.getDeclaredFields()) {
            Tag tagAnnotation = field.getAnnotation(Tag.class);
            if (null == tagAnnotation) {
                continue;
            }
            int tagValue = tagAnnotation.value();
            if (tag == tagValue)
                return field;
        }
        return null;
    }
}
