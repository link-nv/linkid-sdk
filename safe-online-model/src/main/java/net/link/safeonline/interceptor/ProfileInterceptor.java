/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.interceptor;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.util.jacc.ProfileData;

/**
 * 
 * 
 * @author mbillemo
 */
public class ProfileInterceptor {

	private static final Log LOG = LogFactory.getLog(ProfileInterceptor.class);

	private ProfileData profileData;

	/**
	 * Create a new {@link ProfileInterceptor} instance.
	 */
	public ProfileInterceptor() {

		profileData = ProfileData.getProfileData();
	}

	@AroundInvoke
	public Object aroundInvoke(InvocationContext context) throws Exception {

		// Check to see whether profiling has been enabled.
		if (!profileData.isEnabled())
			return context.proceed();

		LOG.debug("Profiler Intercepting: " + context.getMethod());

		// Lock the context to prevent internal calls from being intercepted.
		if (isLocked(context))
			return context.proceed();
		lock(context);

		// Make the call that needs profiling.
		long startTime = System.currentTimeMillis();
		Object result = context.proceed();
		long duration = System.currentTimeMillis() - startTime;

		// Record the stats for the call and release the lock.
		profileData.add(context.getMethod().toGenericString(), duration);
		unlock(context);

		return result;
	}

	private boolean isLocked(InvocationContext context) {

		return context.getContextData().containsKey(ProfileData.KEY);
	}

	private void lock(InvocationContext context) {

		context.getContextData().put(ProfileData.KEY, new Object());
	}

	private void unlock(InvocationContext context) {

		context.getContextData().remove(ProfileData.KEY);
	}
}
