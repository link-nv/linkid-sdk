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
import junit.framework.TestCase;
import net.link.safeonline.validation.annotation.NonEmptyString;
import net.link.safeonline.validation.validator.NonEmptyStringValidator;
import net.link.safeonline.validation.validator.ValidatorResult;

import org.easymock.EasyMock;


public class NonEmptyStringValidatorTest extends TestCase {

    private NonEmptyStringValidator testedInstance;

    private ValidatorResult         mockValidatorResult;

    private NonEmptyString          nonEmptyStringSampleAnnotation;


    @SuppressWarnings("unused")
    private void sampleFunc(@NonEmptyString("sample") String parameter) {

        // empty
    }

    @Override
    protected void setUp() throws Exception {

        super.setUp();

        this.testedInstance = new NonEmptyStringValidator();

        this.mockValidatorResult = createMock(ValidatorResult.class);

        this.nonEmptyStringSampleAnnotation = (NonEmptyString) NonEmptyStringValidatorTest.class.getDeclaredMethod("sampleFunc",
                String.class).getParameterAnnotations()[0][0];
    }

    public void testNullStringIsInvalid() throws Exception {

        // setup
        String sampleString = null;

        // expectations
        this.mockValidatorResult.addResult(EasyMock.matches(".*sample.*"));

        // prepare
        replay(this.mockValidatorResult);

        // operate
        this.testedInstance.validate(sampleString, 1, this.nonEmptyStringSampleAnnotation, this.mockValidatorResult);

        // verify
        verify(this.mockValidatorResult);
    }

    public void testEmptyStringIsInvalid() throws Exception {

        // setup
        String sampleString = "";

        // expectations
        this.mockValidatorResult.addResult(EasyMock.matches(".*sample.*"));

        // prepare
        replay(this.mockValidatorResult);

        // operate
        this.testedInstance.validate(sampleString, 1, this.nonEmptyStringSampleAnnotation, this.mockValidatorResult);

        // verify
        verify(this.mockValidatorResult);
    }
}
