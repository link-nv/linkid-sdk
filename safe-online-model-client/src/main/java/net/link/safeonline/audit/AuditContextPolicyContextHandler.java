/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit;

import javax.security.jacc.PolicyContextHandler;

import net.link.safeonline.audit.exception.ExistingAuditContextException;
import net.link.safeonline.audit.exception.MissingAuditContextException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * JACC policy context handler for audit context information.
 * 
 * @author fcorneli
 * 
 */
public class AuditContextPolicyContextHandler implements PolicyContextHandler {

    private static final Log                     LOG               = LogFactory.getLog(AuditContextPolicyContextHandler.class);

    public static final String                   AUDIT_CONTEXT_KEY = "net.link.safeonline.audit.context";

    private static ThreadLocal<AuditContextInfo> auditContextInfos = new ThreadLocal<AuditContextInfo>();


    public static final class AuditContextInfo {

        private final long auditContextId;

        private long       counter;


        public long getAuditContextId() {

            return auditContextId;
        }

        public AuditContextInfo(long auditContextId) {

            this.auditContextId = auditContextId;
        }

        public void lock() {

            counter++;
        }

        /**
         * @return <code>true</code>: Audit stack is completely unlocked (last entry was unlocked).
         */
        public boolean unlock() {

            counter--;
            return 0 == counter;
        }
    }


    public Object getContext(String key, @SuppressWarnings("unused") Object data) {

        if (!supports(key))
            return null;

        AuditContextInfo auditContextInfo = auditContextInfos.get();
        if (null == auditContextInfo)
            return null;

        return auditContextInfo.getAuditContextId();
    }

    public String[] getKeys() {

        return new String[] { AUDIT_CONTEXT_KEY };
    }

    public boolean supports(String key) {

        return key.equalsIgnoreCase(AUDIT_CONTEXT_KEY);
    }

    /**
     * Sets the audit context Id for the current thread.
     * 
     * @param auditContextId
     *            The ID of the audit context to assign to this thread.
     * @throws ExistingAuditContextException
     */
    public static synchronized void setAndLockAuditContextId(long auditContextId)
            throws ExistingAuditContextException {

        AuditContextInfo previousAuditContextInfo = auditContextInfos.get();
        if (null != previousAuditContextInfo) {
            long previousAuditContextId = previousAuditContextInfo.getAuditContextId();
            LOG.fatal("Can't set audit context to ID: " + auditContextId + "; It's already set to ID: " + previousAuditContextId);
            throw new ExistingAuditContextException(previousAuditContextId);
        }

        AuditContextInfo auditContextInfo = new AuditContextInfo(auditContextId);
        auditContextInfos.set(auditContextInfo);
        auditContextInfo.lock();
    }

    /**
     * Returns the audit context Id for the current thread
     * 
     * @return auditContextId The ID of the audit context to assign to this thread.
     * @throws MissingAuditContextException
     */
    public static synchronized Long getAuditContextId()
            throws MissingAuditContextException {

        AuditContextInfo auditContextInfo = auditContextInfos.get();
        if (null == auditContextInfo) {
            LOG.fatal("Audit context was requested but it isn't set.");
            throw new MissingAuditContextException();
        }

        return auditContextInfo.getAuditContextId();
    }

    /**
     * Locks the audit context associated with the current thread.
     * 
     * @return <code>false</code>: The current thread has no audit context.
     */
    public static synchronized boolean lockAuditContext() {

        AuditContextInfo auditContextInfo = auditContextInfos.get();
        if (null == auditContextInfo)
            return false;

        auditContextInfo.lock();
        return true;
    }

    /**
     * Unlocks the audit context for the current call.
     * 
     * When the entire stack of audit contexts has been unlocked (the last call was completed) the audit context is removed from the thread.
     * 
     * @return <code>true</code>: The entire audit stack was unlocked (last entry was unlocked).
     * @throws MissingAuditContextException
     */
    public static synchronized boolean unlockAuditContext()
            throws MissingAuditContextException {

        AuditContextInfo auditContextInfo = auditContextInfos.get();
        if (null == auditContextInfo) {
            LOG.fatal("missing audit context");
            throw new MissingAuditContextException();
        }

        boolean stackUnlocked = auditContextInfo.unlock();
        if (stackUnlocked) {
            auditContextInfos.remove();
        }

        return stackUnlocked;
    }
}
