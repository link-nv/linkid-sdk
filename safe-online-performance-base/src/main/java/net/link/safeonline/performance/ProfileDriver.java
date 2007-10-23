/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance;

import net.link.safeonline.util.jacc.ProfileData;

/**
 * 
 * 
 * @author mbillemo
 */
public abstract class ProfileDriver {

	protected abstract void prepare() throws DriverException;

	protected abstract ProfileData run() throws DriverException;
}
