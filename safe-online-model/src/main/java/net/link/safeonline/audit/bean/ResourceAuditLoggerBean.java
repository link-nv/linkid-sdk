/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.audit.AuditContextPolicyContextHandler;
import net.link.safeonline.audit.ResourceAuditLogger;
import net.link.safeonline.audit.dao.AuditAuditDAO;
import net.link.safeonline.audit.dao.AuditContextDAO;
import net.link.safeonline.audit.dao.ResourceAuditDAO;
import net.link.safeonline.audit.exception.AuditContextNotFoundException;
import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.entity.audit.ResourceLevelType;
import net.link.safeonline.entity.audit.ResourceNameType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = ResourceAuditLogger.JNDI_BINDING)
@Interceptors(AuditContextManager.class)
public class ResourceAuditLoggerBean implements ResourceAuditLogger {

    private static final Log LOG = LogFactory.getLog(ResourceAuditLoggerBean.class);

    @EJB(mappedName = AuditAuditDAO.JNDI_BINDING)
    private AuditAuditDAO    auditAuditDAO;

    @EJB(mappedName = AuditContextDAO.JNDI_BINDING)
    private AuditContextDAO  auditContextDAO;

    @EJB(mappedName = ResourceAuditDAO.JNDI_BINDING)
    private ResourceAuditDAO resourceAuditDAO;


    public void addResourceAudit(ResourceNameType resourceName, ResourceLevelType resourceLevel, String sourceComponent, String message) {

        try {
            LOG.debug("addResourceAudit");
            Long auditContextId = (Long) PolicyContext.getContext(AuditContextPolicyContextHandler.AUDIT_CONTEXT_KEY);
            LOG.debug(" - context: " + auditContextId);
            if (null == auditContextId) {
                LOG.debug(" - no audit context available");
                auditAuditDAO.addAuditAudit("no audit context available");
                return;
            }

            try {
                LOG.debug(" - get audit context");
                AuditContextEntity auditContext = auditContextDAO.getAuditContext(auditContextId);

                LOG.debug(" - add resource audit event with audit context: " + auditContext);
                resourceAuditDAO.addResourceAudit(auditContext, resourceName, resourceLevel, sourceComponent, message);
            }

            catch (AuditContextNotFoundException e) {
                e.printStackTrace();
                auditAuditDAO.addAuditAudit("audit context not found: " + auditContextId);
            }
        }

        catch (PolicyContextException e) {
            e.printStackTrace();
            auditAuditDAO.addAuditAudit("audit context policy context error: " + e.getMessage());
        }
    }

}
