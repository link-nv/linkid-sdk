package test.net.link.safeonline.sms;

import static org.junit.Assert.assertEquals;
import net.link.safeonline.sms.SMS;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestSMS {

    Logger LOG = LoggerFactory.getLogger(TestSMS.class);


    @Test
    public void testEncoding() {

        SMS sms = new SMS("+32 477 657 223", "hellohello");
        assertEquals(new String(Hex.encodeHex(sms.getEncoded())), "0001000b912374677522f300000ae8329bfd4697d9ec37");
    }

}
