/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import net.link.safeonline.audit.AuditContextPolicyContextHandler;
import net.link.safeonline.audit.ResourceAuditLogger;
import net.link.safeonline.audit.dao.AuditAuditDAO;
import net.link.safeonline.audit.dao.AuditContextDAO;
import net.link.safeonline.audit.dao.ResourceAuditDAO;
import net.link.safeonline.audit.exception.AuditContextNotFoundException;
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


    public void addResourceAudit(ResourceNameType resourceName, ResourceLevelType resourceLevel, String sourceComponent, String message) {

        try {
            Long auditContextId = (Long) PolicyContext.getContext(AuditContextPolicyContextHandler.AUDIT_CONTEXT_KEY);
            if (null == auditContextId) {
                this.auditAuditDAO.addAuditAudit("no audit context available");
                return;
            }

            try {
                AuditContextEntity auditContext = this.auditContextDAO.getAuditContext(auditContextId);

                this.resourceAuditDAO.addResourceAudit(auditContext, resourceName, resourceLevel, sourceComponent, message);
            }

            catch (AuditContextNotFoundException e) {
                this.auditAuditDAO.addAuditAudit("audit context not found: " + auditContextId);
            }
        }

        catch (PolicyContextException e) {
            this.auditAuditDAO.addAuditAudit("audit context policy context error: " + e.getMessage());
        }
    }

}
