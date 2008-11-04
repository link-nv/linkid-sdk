/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import net.link.safeonline.audit.AuditContextPolicyContextHandler;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.audit.dao.AuditAuditDAO;
import net.link.safeonline.audit.dao.AuditContextDAO;
import net.link.safeonline.audit.dao.SecurityAuditDAO;
import net.link.safeonline.audit.exception.AuditContextNotFoundException;
import net.link.safeonline.authentication.exception.SafeOnlineSecurityException;
import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = SecurityAuditLogger.JNDI_BINDING)
public class SecurityAuditLoggerBean implements SecurityAuditLogger {

    @EJB
    private AuditAuditDAO    auditAuditDAO;

    @EJB
    private AuditContextDAO  auditContextDAO;

    @EJB
    private SecurityAuditDAO securityAuditDAO;


    @AroundInvoke
    public Object interceptor(InvocationContext context) throws Exception {

        try {
            return context.proceed();
        }

        catch (SafeOnlineSecurityException e) {
            addSecurityAudit(e.getSecurityThreat(), e.getTargetPrincipal(), e.getMessage());
            throw e;
        }
    }

    public void addSecurityAudit(SecurityThreatType securityThreat, String targetPrincipal, String message) {

        try {
            Long auditContextId = (Long) PolicyContext.getContext(AuditContextPolicyContextHandler.AUDIT_CONTEXT_KEY);
            if (null == auditContextId) {
                this.auditAuditDAO.addAuditAudit("no audit context available");
                return;
            }

            try {
                AuditContextEntity auditContext = this.auditContextDAO.getAuditContext(auditContextId);

                this.securityAuditDAO.addSecurityAudit(auditContext, securityThreat, targetPrincipal, message);
            }

            catch (AuditContextNotFoundException e) {
                this.auditAuditDAO.addAuditAudit("audit context not found: " + auditContextId);
            }
        }

        catch (PolicyContextException e) {
            this.auditAuditDAO.addAuditAudit("audit context policy context error: " + e.getMessage());
        }
    }

    public void addSecurityAudit(SecurityThreatType securityThreat, String message) {

        addSecurityAudit(securityThreat, null, message);
    }
}
