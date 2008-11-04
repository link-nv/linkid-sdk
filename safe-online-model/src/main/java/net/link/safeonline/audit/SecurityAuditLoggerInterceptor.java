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

import net.link.safeonline.authentication.exception.SafeOnlineSecurityException;


/**
 * EJB3 security audit logger interceptor.
 * 
 * @author wvdhaute
 * 
 */
public class SecurityAuditLoggerInterceptor {

    @EJB
    private SecurityAuditLogger securityAuditLogger;


    @AroundInvoke
    public Object interceptor(InvocationContext context)
            throws Exception {

        try {
            return context.proceed();
        }

        catch (SafeOnlineSecurityException e) {
            this.securityAuditLogger.addSecurityAudit(e.getSecurityThreat(), e.getTargetPrincipal(), e.getMessage());
            throw e;
        }
    }
}
