/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.service;

import javax.ejb.Local;

import net.link.safeonline.performance.DriverException;
import net.link.safeonline.performance.entity.DriverExceptionEntity;
import net.link.safeonline.performance.service.bean.DriverExceptionServiceBean;

/**
 * <h2>{@link DriverExceptionServiceBean} - Service bean for
 * {@link DriverExceptionEntity}.</h2>
 *
 * <p>
 * Create {@link DriverExceptionEntity}s from {@link DriverException}s
 * </p>
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Local
public interface DriverExceptionService {

	public static final String BINDING = "SafeOnline/DriverExceptionService";

	/**
	 * Add the given problem to the database.
	 */
	public DriverExceptionEntity addException(DriverException exception);

}
