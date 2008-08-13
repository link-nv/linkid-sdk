/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit;

import java.lang.reflect.Method;
import java.security.Principal;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import net.link.safeonline.audit.dao.AccessAuditDAO;
import net.link.safeonline.audit.dao.AuditAuditDAO;
import net.link.safeonline.audit.dao.AuditContextDAO;
import net.link.safeonline.audit.dao.SecurityAuditDAO;
import net.link.safeonline.audit.exception.AuditContextNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SafeOnlineException;
import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.entity.audit.OperationStateType;
import net.link.safeonline.entity.audit.SecurityThreatType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * EJB3 access audit logger.
 *
 * @author fcorneli
 *
 */
public class AccessAuditLogger {

    private static final Log LOG = LogFactory.getLog(AccessAuditLogger.class);

    @Resource
    private SessionContext   sessionContext;

    @EJB
    private AccessAuditDAO   accessAuditDAO;

    @EJB
    private AuditContextDAO  auditContextDAO;

    @EJB
    private AuditAuditDAO    auditAuditDAO;

    @EJB
    private SecurityAuditDAO securityAuditDAO;


    @AroundInvoke
    public Object interceptor(InvocationContext context) throws Exception {

        auditAccessBegin(context);

        Object result;
        try {
            result = context.proceed();
            auditAccessEnd(context);
            return result;
        } catch (PermissionDeniedException e) {
            auditAccess(context, OperationStateType.BUSINESS_EXCEPTION_END);
            auditSecurity(e.getMessage());
            throw e;
        } catch (SafeOnlineException e) {
            auditAccess(context, OperationStateType.BUSINESS_EXCEPTION_END);
            throw e;
        } catch (Exception e) {
            auditAccess(context, OperationStateType.SYSTEM_EXCEPTION_END);
            throw e;
        }
    }

    private void auditAccessBegin(InvocationContext context) {

        auditAccess(context, OperationStateType.BEGIN);
    }

    private void auditSecurity(String message) {

        String principalName = getCallerPrincipalName();
        LOG.debug("security audit: " + message + " for principal " + principalName);

        AuditContextEntity auditContext = findAuditContext();
        if (null != auditContext) {
            this.securityAuditDAO.addSecurityAudit(auditContext, SecurityThreatType.DECEPTION, principalName, message);
        }
    }

    private void auditAccess(InvocationContext context, OperationStateType operationState) {

        Method method = context.getMethod();
        String methodName = method.getName();
        String principalName = getCallerPrincipalName();

        LOG.debug("access audit: " + methodName + " as " + principalName + " at " + operationState);

        AuditContextEntity auditContext = findAuditContext();
        if (null != auditContext) {
            this.accessAuditDAO.addAccessAudit(auditContext, methodName, operationState, principalName);
        }
    }

    private AuditContextEntity findAuditContext() {

        Long auditContextId;
        try {
            auditContextId = (Long) PolicyContext.getContext(AuditContextPolicyContextHandler.AUDIT_CONTEXT_KEY);
        } catch (PolicyContextException e) {
            this.auditAuditDAO.addAuditAudit("audit context policy context error: " + e.getMessage());
            return null;
        }
        if (null == auditContextId) {
            this.auditAuditDAO.addAuditAudit("no audit context available");
            return null;
        }

        try {
            AuditContextEntity auditContext = this.auditContextDAO.getAuditContext(auditContextId);
            return auditContext;
        } catch (AuditContextNotFoundException e) {
            this.auditAuditDAO.addAuditAudit("audit context not found: " + auditContextId);
            return null;
        }
    }

    private void auditAccessEnd(InvocationContext context) {

        auditAccess(context, OperationStateType.NORMAL_END);
    }

    private String getCallerPrincipalName() {

        Principal callerPrincipal;
        try {
            callerPrincipal = this.sessionContext.getCallerPrincipal();
        } catch (IllegalStateException e) {
            /*
             * Under JBoss we get an IllegalStateException instead of a null principal if there is no identifiable
             * caller principal.
             */
            LOG.debug("getCallerPrincipal throws IllegalStateException");
            return null;
        }
        if (null == callerPrincipal)
            return null;
        String name = callerPrincipal.getName();
        return name;
    }
}
