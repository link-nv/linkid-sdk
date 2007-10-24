/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.interceptor;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import net.link.safeonline.util.jacc.ProfileData;
import net.link.safeonline.util.jacc.ProfilingPolicyContextHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 
 * @author mbillemo
 */
public class ProfileInterceptor {

	private static final Log LOG = LogFactory.getLog(ProfileInterceptor.class);

	@AroundInvoke
	public Object aroundInvoke(InvocationContext context) throws Exception {

		ProfileData profileData = ProfilingPolicyContextHandler
				.getProfileData();

		if (null == profileData)
			return context.proceed();
		if (profileData.isLocked())
			return context.proceed();

		LOG.debug("Profiler Intercepting: " + context.getMethod());

		profileData.lock();

		// Make the call that needs profiling.
		long startTime = System.currentTimeMillis();
		Object result = context.proceed();
		long duration = System.currentTimeMillis() - startTime;

		profileData.unlock();

		// Record the stats for the call and release the lock.
		profileData.addMeasurement(context.getMethod().toGenericString(),
				duration);

		return result;
	}
}
