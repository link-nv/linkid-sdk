package test.net.link.safeonline.sms;

import junit.framework.TestCase;
import net.link.safeonline.sms.SMS;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class TestSMS extends TestCase {

    Log LOG = LogFactory.getLog(TestSMS.class);


    public void testEncoding() {

        SMS sms = new SMS("+32 477 657 223", "hellohello");
        assertEquals(new String(Hex.encodeHex(sms.getEncoded())), "0001000b912374677522f300000ae8329bfd4697d9ec37");
    }

}
