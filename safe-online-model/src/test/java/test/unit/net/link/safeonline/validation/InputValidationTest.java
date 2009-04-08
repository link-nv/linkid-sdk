/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.validation;

import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Map;

import javax.interceptor.InvocationContext;

import net.link.safeonline.validation.InputValidation;
import net.link.safeonline.validation.annotation.ValidatorAnnotation;
import net.link.safeonline.validation.validator.Validator;
import net.link.safeonline.validation.validator.ValidatorResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;


public class InputValidationTest {

    private InputValidation testedInstance;


    @Before
    public void setUp()
            throws Exception {

        testedInstance = new InputValidation();
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @ValidatorAnnotation(TestValidator.class)
    public @interface TestAnnotation {

    }

    public static class TestValidator implements Validator<Annotation> {

        private static final Log LOG     = LogFactory.getLog(TestValidator.class);

        private static boolean   invoked = false;


        public static boolean isInvoked() {

            return invoked;
        }

        public void validate(Object value, @SuppressWarnings("unused") int parameterIdx,
                             @SuppressWarnings("unused") Annotation parameterAnnotation, @SuppressWarnings("unused") ValidatorResult result) {

            LOG.debug("validate: " + value);
            invoked = true;
        }
    }


    public void method(@SuppressWarnings("unused") @TestAnnotation String param) {

        // empty
    }

    private Method getLocalMethod(String methodName) {

        Class<?> clazz = InputValidationTest.class;
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName))
                return method;
        }
        throw new IllegalArgumentException("no method found with name: " + methodName);
    }

    @Test
    public void testInvoke()
            throws Exception {

        // setup
        Method method = getLocalMethod("method");
        InvocationContext invocationContext = new TestInvocationContext(method, "Hello World");

        // operate
        testedInstance.inputValidationInterceptor(invocationContext);

        // verify
        assertTrue(TestValidator.isInvoked());
    }


    private static class TestInvocationContext implements InvocationContext {

        private final Method   method;

        private final Object[] parameters;


        public TestInvocationContext(Method method, Object... parameters) {

            this.method = method;
            this.parameters = parameters;
        }

        public Map<String, Object> getContextData() {

            return null;
        }

        public Method getMethod() {

            return method;
        }

        public Object[] getParameters() {

            return parameters;
        }

        public Object getTarget() {

            return null;
        }

        public Object proceed()
                throws Exception {

            return null;
        }

        public void setParameters(@SuppressWarnings("unused") Object[] aobj) {

            // empty
        }
    }
}
