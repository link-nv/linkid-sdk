/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.link.safeonline.util.filter.ProfileStats;

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

	protected abstract Map<ProfileStats, Number> run() throws Exception;

	public List<Map<ProfileStats, Number>> execute() {

		List<Map<ProfileStats, Number>> iterations = new ArrayList<Map<ProfileStats, Number>>();

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
