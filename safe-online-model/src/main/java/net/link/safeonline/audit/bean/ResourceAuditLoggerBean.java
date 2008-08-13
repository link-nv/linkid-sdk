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
import net.link.safeonline.audit.ResourceAuditLogger;
import net.link.safeonline.audit.dao.AuditAuditDAO;
import net.link.safeonline.audit.dao.AuditContextDAO;
import net.link.safeonline.audit.dao.ResourceAuditDAO;
import net.link.safeonline.audit.exception.AuditContextNotFoundException;
import net.link.safeonline.authentication.exception.SafeOnlineResourceException;
import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.entity.audit.ResourceLevelType;
import net.link.safeonline.entity.audit.ResourceNameType;


@Stateless
public class ResourceAuditLoggerBean implements ResourceAuditLogger {

    @EJB
    private AuditAuditDAO    auditAuditDAO;

    @EJB
    private AuditContextDAO  auditContextDAO;

    @EJB
    private ResourceAuditDAO resourceAuditDAO;


    @AroundInvoke
    public Object interceptor(InvocationContext context) throws Exception {

        Object result;
        try {
            result = context.proceed();
            return result;
        } catch (SafeOnlineResourceException e) {
            addResourceAudit(e.getResourceName(), e.getResourceLevel(), e.getSourceComponent(), e.getMessage());
            throw e;
        }
    }

    public void addResourceAudit(ResourceNameType resourceName, ResourceLevelType resourceLevel,
            String sourceComponent, String message) {

        Long auditContextId;
        try {
            auditContextId = (Long) PolicyContext.getContext(AuditContextPolicyContextHandler.AUDIT_CONTEXT_KEY);
        } catch (PolicyContextException e) {
            this.auditAuditDAO.addAuditAudit("audit context policy context error: " + e.getMessage());
            return;
        }
        if (null == auditContextId) {
            this.auditAuditDAO.addAuditAudit("no audit context available");
            return;
        }

        AuditContextEntity auditContext;
        try {
            auditContext = this.auditContextDAO.getAuditContext(auditContextId);
        } catch (AuditContextNotFoundException e) {
            this.auditAuditDAO.addAuditAudit("audit context not found: " + auditContextId);
            return;
        }

        this.resourceAuditDAO.addResourceAudit(auditContext, resourceName, resourceLevel, sourceComponent, message);
    }

}
