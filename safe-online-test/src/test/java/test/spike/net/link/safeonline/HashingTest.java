package test.spike.net.link.safeonline;

import java.security.MessageDigest;
import java.security.Security;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

import org.junit.Test;


public class HashingTest {

    @Test
    public void testMac() throws Exception {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        String inputString = "www.java2s.com";

        KeyGenerator keyGen = KeyGenerator.getInstance("HmacMD5");
        SecretKey secretKey = keyGen.generateKey();

        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        mac.init(secretKey);

        byte[] byteData = inputString.getBytes("UTF8");

        byte[] macBytes = mac.doFinal(byteData);

        String macAsString = new sun.misc.BASE64Encoder().encode(macBytes);

        System.out.println("Authentication code is: " + macAsString);
    }

    @Test
    public void testMessageDigest() throws Exception {

        String text = "testmessage";

        byte[] plainText = text.getBytes("UTF8");

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-512",
                new org.bouncycastle.jce.provider.BouncyCastleProvider());

        System.out.println("\n" + messageDigest.getProvider().getInfo());

        messageDigest.update(plainText);
        String digestAsString = new sun.misc.BASE64Encoder().encode(messageDigest.digest());

        System.out.println("\nDigest: ");
        System.out.println(digestAsString);

    }

}
