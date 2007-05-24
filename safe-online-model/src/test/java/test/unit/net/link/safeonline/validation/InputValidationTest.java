/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.validation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Map;

import javax.interceptor.InvocationContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.validation.InputValidation;
import net.link.safeonline.validation.annotation.ValidatorClass;
import net.link.safeonline.validation.validator.Validator;
import net.link.safeonline.validation.validator.ValidatorResult;
import junit.framework.TestCase;

public class InputValidationTest extends TestCase {

	private InputValidation testedInstance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new InputValidation();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	@ValidatorClass(TestValidator.class)
	public @interface TestAnnotation {

	}

	public static class TestValidator implements Validator {

		private static final Log LOG = LogFactory.getLog(TestValidator.class);

		private static boolean invoked = false;

		public static boolean isInvoked() {
			return invoked;
		}

		public void validate(Object value, int parameterIdx,
				Annotation parameterAnnotation, ValidatorResult result) {
			LOG.debug("validate: " + value);
			invoked = true;
		}
	}

	public void method(@TestAnnotation
	String param) {
		// empty
	}

	private Method getLocalMethod(String methodName) {
		Class clazz = InputValidationTest.class;
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			if (method.getName().equals(methodName)) {
				return method;
			}
		}
		throw new IllegalArgumentException("no method found with name: "
				+ methodName);
	}

	public void testInvoke() throws Exception {
		// setup
		Method method = getLocalMethod("method");
		InvocationContext invocationContext = new TestInvocationContext(method,
				"Hello World");

		// operate
		this.testedInstance.inputValidationInterceptor(invocationContext);

		// verify
		assertTrue(TestValidator.isInvoked());
	}

	private static class TestInvocationContext implements InvocationContext {

		private final Method method;

		private final Object[] parameters;

		public TestInvocationContext(Method method, Object... parameters) {
			this.method = method;
			this.parameters = parameters;
		}

		public Map<String, Object> getContextData() {
			return null;
		}

		public Method getMethod() {
			return this.method;
		}

		public Object[] getParameters() {
			return this.parameters;
		}

		public Object getTarget() {
			return null;
		}

		public Object proceed() throws Exception {
			return null;
		}

		public void setParameters(Object[] aobj) {
		}
	}
}
