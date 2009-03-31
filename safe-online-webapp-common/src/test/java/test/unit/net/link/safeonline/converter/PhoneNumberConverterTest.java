package test.unit.net.link.safeonline.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import net.link.safeonline.custom.converter.PhoneNumberConverter;

import org.junit.Test;


public class PhoneNumberConverterTest {

    @Test
    public void testConversion() {

        String correctNumber = "+32494575697";
        String correctNumber2 = "0494575697";

        String number1 = "  +32 494  575   697  ";
        String number2 = "+32/494/575/697";
        String number3 = "+32.494.575.697";
        String number4 = "+32-494-575-697";
        String number5 = " 0494-575-697";
        String number6 = " (0494) 575 697";

        assertEquals(correctNumber, PhoneNumberConverter.convertNumber(number1));
        assertEquals(correctNumber, PhoneNumberConverter.convertNumber(number2));
        assertEquals(correctNumber, PhoneNumberConverter.convertNumber(number3));
        assertEquals(correctNumber, PhoneNumberConverter.convertNumber(number4));

        assertEquals(correctNumber2, PhoneNumberConverter.convertNumber(number5));
        assertEquals(correctNumber2, PhoneNumberConverter.convertNumber(number6));

        assertNull(PhoneNumberConverter.convertNumber("foo-bar"));
        assertNull(PhoneNumberConverter.convertNumber("++123456789"));
        assertNull(PhoneNumberConverter.convertNumber("!!@#$%^&**()_+"));
        assertNull(PhoneNumberConverter.convertNumber(null));
        assertNull(PhoneNumberConverter.convertNumber("I would rather tell you a story then give you my phone number"));
    }
}
