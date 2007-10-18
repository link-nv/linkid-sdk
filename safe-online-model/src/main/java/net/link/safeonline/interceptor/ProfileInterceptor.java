/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.interceptor;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import net.link.safeonline.util.jacc.BasicPolicyHandler;
import net.link.safeonline.util.jacc.ProfileData;

/**
 * 
 * 
 * @author mbillemo
 */
public class ProfileInterceptor {

	private static final ProfileData profileData = new ProfileData();

	// Create a policy handler that will manage our profiled data.
	static {
		BasicPolicyHandler<ProfileData> handler = new BasicPolicyHandler<ProfileData>();
		handler.put(ProfileData.KEY, profileData);
	}

	@AroundInvoke
	public Object aroundInvoke(InvocationContext context) throws Exception {

		// Lock the context to prevent internal calls from being intercepted.
		if (isLocked(context))
			return context.proceed();
		lock(context);

		// Make the call that needs profiling.
		long startTime = System.currentTimeMillis();
		Object result = context.proceed();
		long duration = System.currentTimeMillis() - startTime;

		// Record the stats for the call and release the lock.
		profileData.put(context.getMethod().toGenericString(), duration);
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
