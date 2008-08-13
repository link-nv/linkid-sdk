/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.interceptor;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import net.link.safeonline.util.performance.ProfileData;
import net.link.safeonline.util.performance.ProfilingPolicyContextHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * EJB3 Interceptor for profiling method invocations.
 *
 * @author mbillemo
 */
public class ProfileInterceptor {

    private static final Log LOG = LogFactory.getLog(ProfileInterceptor.class);


    @AroundInvoke
    public Object aroundInvoke(InvocationContext context) throws Exception {

        ProfileData profileData = ProfilingPolicyContextHandler.getProfileData();

        if (null == profileData || profileData.isLocked())
            return context.proceed();

        LOG.debug("Profiler Intercepting: " + context.getMethod());

        profileData.lock();

        // Make the call that needs profiling.
        long startTime = System.currentTimeMillis();
        try {
            Object result = context.proceed();
            return result;
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Record the stats for the call and release the lock.
            profileData.unlock();
            profileData.addMeasurement(context.getMethod(), duration);
        }
    }
}
