/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.service.bean;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.performance.DriverException;
import net.link.safeonline.performance.entity.DriverExceptionEntity;
import net.link.safeonline.performance.service.DriverExceptionService;
import net.link.safeonline.util.performance.ProfileData;

import org.jboss.annotation.ejb.LocalBinding;

/**
 * <h2>{@link DriverExceptionServiceBean} - Service bean for
 * {@link DriverExceptionEntity}.</h2>
 *
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 *
 * @see DriverExceptionService
 * @author mbillemo
 */
@Stateless
@LocalBinding(jndiBinding = DriverExceptionService.BINDING)
public class DriverExceptionServiceBean extends ProfilingServiceBean implements
		DriverExceptionService {

	/**
	 * {@inheritDoc}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public DriverExceptionEntity addException(DriverException exception) {

		// Dig for the root cause.
		Throwable cause = exception;
		while (cause.getCause() != null)
			cause = cause.getCause();

		// Format this cause into a message.
		int errorSourceLine = -1;
		String errorSourceClass = null;
		StackTraceElement errorSource = null;

		if (cause.getStackTrace().length > 0) {
			errorSource = cause.getStackTrace()[0];
			errorSourceClass = ProfileData.compressSignature(errorSource
					.getClassName());
			errorSourceLine = errorSource.getLineNumber();
		}

		String errorClass = ProfileData.compressSignature(cause.getClass()
				.getName());
		String message = String.format("%s: %s (%s:%d)", errorClass, cause
				.getMessage(), errorSourceClass, errorSourceLine);

		// Create the exception entity.
		DriverExceptionEntity exceptionEntity = new DriverExceptionEntity(
				exception.getOccurredTime(), message);
		this.em.persist(exceptionEntity);

		return exceptionEntity;
	}
}
