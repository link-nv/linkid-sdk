/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit;

import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import net.link.safeonline.authentication.exception.SafeOnlineResourceException;


/**
 * EJB3 resource audit logger interceptor.
 * 
 * @author wvdhaute
 * 
 */
public class ResourceAuditLoggerInterceptor {

    @EJB(mappedName = ResourceAuditLogger.JNDI_BINDING)
    private ResourceAuditLogger resourceAuditLogger;


    @AroundInvoke
    public Object interceptor(InvocationContext context)
            throws Exception {

        try {
            return context.proceed();
        }

        catch (SafeOnlineResourceException e) {
            this.resourceAuditLogger.addResourceAudit(e.getResourceName(), e.getResourceLevel(), e.getSourceComponent(), e.getMessage());
            throw e;
        }
    }
}
