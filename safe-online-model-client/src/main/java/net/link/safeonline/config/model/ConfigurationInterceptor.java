/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.config.model;

import java.lang.reflect.Method;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ConfigurationInterceptor {

    private static final Log     LOG = LogFactory.getLog(ConfigurationInterceptor.class);

    @EJB(mappedName = ConfigurationManager.JNDI_BINDING)
    private ConfigurationManager configurationManager;


    @AroundInvoke
    public Object invoke(InvocationContext invocationContext)
            throws Exception {

        return configure(invocationContext);
    }

    @PostConstruct
    public void initializeConfiguration(InvocationContext invocationContext)
            throws Exception {

        LOG.debug("@PostConstruct configuration");
        configure(invocationContext);
    }

    private String getMethodName(InvocationContext invocationContext) {

        Method method = invocationContext.getMethod();
        if (null == method)
            return "lifecycle callback method";
        return method.getName();
    }

    private Object configure(InvocationContext invocationContext)
            throws Exception {

        Object target = invocationContext.getTarget();
        LOG.debug("Configuring: " + target.getClass().getName() + " (method: " + getMethodName(invocationContext) + ")");

        this.configurationManager.configure(target);

        return invocationContext.proceed();
    }
}
