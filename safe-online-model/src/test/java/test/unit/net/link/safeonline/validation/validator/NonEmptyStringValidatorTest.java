/*
 * SafeOnline project.
 *
 * Copyright 2005-2007 Frank Cornelis H.S.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.validation.validator;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import net.link.safeonline.validation.annotation.NonEmptyString;
import net.link.safeonline.validation.validator.NonEmptyStringValidator;
import net.link.safeonline.validation.validator.ValidatorResult;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;


public class NonEmptyStringValidatorTest {

    private NonEmptyStringValidator testedInstance;

    private ValidatorResult         mockValidatorResult;

    private NonEmptyString          nonEmptyStringSampleAnnotation;


    @SuppressWarnings("unused")
    public void sampleFunc(@NonEmptyString("sample") String parameter) {

        // empty
    }

    @Before
    public void setUp()
            throws Exception {

        testedInstance = new NonEmptyStringValidator();

        mockValidatorResult = createMock(ValidatorResult.class);

        nonEmptyStringSampleAnnotation = (NonEmptyString) NonEmptyStringValidatorTest.class.getDeclaredMethod("sampleFunc", String.class)
                                                                                           .getParameterAnnotations()[0][0];
    }

    @Test
    public void testNullStringIsInvalid()
            throws Exception {

        // setup
        String sampleString = null;

        // expectations
        mockValidatorResult.addResult(EasyMock.matches(".*sample.*"));

        // prepare
        replay(mockValidatorResult);

        // operate
        testedInstance.validate(sampleString, 1, nonEmptyStringSampleAnnotation, mockValidatorResult);

        // verify
        verify(mockValidatorResult);
    }

    @Test
    public void testEmptyStringIsInvalid()
            throws Exception {

        // setup
        String sampleString = "";

        // expectations
        mockValidatorResult.addResult(EasyMock.matches(".*sample.*"));

        // prepare
        replay(mockValidatorResult);

        // operate
        testedInstance.validate(sampleString, 1, nonEmptyStringSampleAnnotation, mockValidatorResult);

        // verify
        verify(mockValidatorResult);
    }
}
