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

import net.link.safeonline.audit.dao.AuditAuditDAO;
import net.link.safeonline.audit.dao.AuditContextDAO;
import net.link.safeonline.audit.exception.ExistingAuditContextException;
import net.link.safeonline.audit.exception.MissingAuditContextException;
import net.link.safeonline.entity.audit.AuditContextEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * EJB3 Interceptor that manages the audit context. Also publishes the finalized audit context IDs to the audit topic
 * 
 * @author fcorneli
 * 
 */
public class AuditContextManager {

    private static final Log      LOG = LogFactory.getLog(AuditContextManager.class);

    @EJB(mappedName = AuditContextFinalizer.JNDI_BINDING)
    private AuditContextFinalizer auditContextFinalizer;

    @EJB(mappedName = AuditContextDAO.JNDI_BINDING)
    private AuditContextDAO       auditContextDAO;

    @EJB(mappedName = AuditAuditDAO.JNDI_BINDING)
    private AuditAuditDAO         auditAuditDAO;


    /**
     * Create audit contexts for our calls.
     */
    @AroundInvoke
    public Object interceptor(InvocationContext context)
            throws Exception {

        try {
            initAuditContext();

            return context.proceed();
        }

        finally {
            cleanupAuditContext();
        }
    }

    /**
     * Unlock the audit context for this call.
     * 
     * If this call is the last in the call stack, the audit context is removed from the thread and is finalized (see
     * {@link AuditContextFinalizer#finalizeAuditContext(Long)}).
     */
    private void cleanupAuditContext() {

        LOG.debug("cleanup audit context");
        try {
            Long auditContextId = AuditContextPolicyContextHandler.getAuditContextId();
            boolean isMainEntry = AuditContextPolicyContextHandler.unlockAuditContext();

            if (isMainEntry) {
                auditContextFinalizer.finalizeAuditContext(auditContextId);
            }
        }

        catch (MissingAuditContextException e) {
            auditAuditDAO.addAuditAudit("missing audit context");
        }
    }

    /**
     * Lock the audit context for this call.
     * 
     * If no audit context is set for the current thread; first create one and assign it to the thread.
     */
    private void initAuditContext() {

        boolean hasAuditContext = AuditContextPolicyContextHandler.lockAuditContext();
        if (hasAuditContext)
            return;

        /* No audit context is set yet; create a new one and assign it to the thread. */
        long newAuditContextId = createNewAuditContext();
        LOG.debug("Created new audit context; ID: " + newAuditContextId);

        try {
            AuditContextPolicyContextHandler.setAndLockAuditContextId(newAuditContextId);
        } catch (ExistingAuditContextException e) {
            auditAuditDAO.addAuditAudit("Couldn't set audit context on thread: already in use; ID: " + e.getAuditContextId());
        }
    }

    /**
     * Create a new {@link AuditContextEntity}.
     * 
     * @return The {@link AuditContextEntity}'s id.
     */
    private long createNewAuditContext() {

        return auditContextDAO.createAuditContext().getId();
    }
}
