/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance;

import java.util.ArrayList;
import java.util.List;

import net.link.safeonline.util.jacc.ProfileData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 
 * @author mbillemo
 */
public abstract class ProfileDriver {

	private static final Log LOG = LogFactory.getLog(ProfileDriver.class);

	protected abstract void prepare() throws Exception;

	protected abstract ProfileData run() throws Exception;

	public List<ProfileData> execute() {

		List<ProfileData> iterations = new ArrayList<ProfileData>();

		try {
			prepare();
			iterations.add(run());
		} catch (Exception e) {
			LOG.error(e);
			e.printStackTrace();
		}

		return iterations;
	}
}
