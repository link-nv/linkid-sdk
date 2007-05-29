/*
 * SafeOnline project.
 * 
 * Copyright 2005-2007 Frank Cornelis H.S.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import net.link.safeonline.validation.annotation.ValidatorAnnotation;
import net.link.safeonline.validation.validator.Validator;
import net.link.safeonline.validation.validator.ValidatorCatalog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Basic input validation interceptor for EJB3.
 * 
 * @author cornelis
 * 
 */
public class InputValidation {

	private static final Log LOG = LogFactory.getLog(InputValidation.class);

	@SuppressWarnings("unchecked")
	@AroundInvoke
	public Object inputValidationInterceptor(InvocationContext context)
			throws Exception {
		Method method = context.getMethod();
		LOG.debug("input validation on " + method.getName());
		InputValidationValidatorResult validatorResult = new InputValidationValidatorResult();
		Annotation[][] allParameterAnnotations = method
				.getParameterAnnotations();
		Object[] parameters = context.getParameters();
		for (int parameterIdx = 0; parameterIdx < allParameterAnnotations.length; parameterIdx++) {
			Annotation[] parameterAnnotations = allParameterAnnotations[parameterIdx];
			for (Annotation parameterAnnotation : parameterAnnotations) {
				ValidatorAnnotation validatorClassAnnotation = parameterAnnotation
						.annotationType().getAnnotation(ValidatorAnnotation.class);
				if (null == validatorClassAnnotation) {
					continue;
				}
				Class<? extends Validator> validatorClass = validatorClassAnnotation
						.value();
				Validator validator = ValidatorCatalog
						.getInstance(validatorClass);
				Object parameter = parameters[parameterIdx];
				validator.validate(parameter, parameterIdx,
						parameterAnnotation, validatorResult);
			}
		}
		if (validatorResult.isEmpty()) {
			return context.proceed();
		}
		throw new IllegalArgumentException(validatorResult.toString());
	}
}
