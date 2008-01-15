/*
 *   Copyright 2008, Maarten Billemont
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package net.link.safeonline.performance.service.bean;

import javax.ejb.Stateless;

import net.link.safeonline.performance.DriverException;
import net.link.safeonline.performance.entity.DriverExceptionEntity;
import net.link.safeonline.performance.service.DriverExceptionService;
import net.link.safeonline.util.performance.ProfileData;

import org.jboss.annotation.ejb.LocalBinding;

/**
 * <h2>{@link DriverExceptionServiceBean} - [in short] (TODO).</h2>
 * <p>
 * [description / usage].
 * </p>
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Stateless
@LocalBinding(jndiBinding = DriverExceptionService.BINDING)
public class DriverExceptionServiceBean extends ProfilingServiceBean implements DriverExceptionService {

	/**
	 * {@inheritDoc}
	 */
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
		persist(exceptionEntity);

		return exceptionEntity;
	}
}
