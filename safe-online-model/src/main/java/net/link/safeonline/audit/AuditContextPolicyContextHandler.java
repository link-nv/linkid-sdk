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

    private static final Log                     LOG               = LogFactory
                                                                           .getLog(AuditContextPolicyContextHandler.class);

    public static final String                   AUDIT_CONTEXT_KEY = "net.link.safeonline.audit.context";

    private static ThreadLocal<AuditContextInfo> auditContextInfos = new ThreadLocal<AuditContextInfo>();


    public static final class AuditContextInfo {

        private final long auditContextId;

        private long       counter;


        public long getAuditContextId() {

            return this.auditContextId;
        }

        public AuditContextInfo(long auditContextId) {

            this.auditContextId = auditContextId;
            this.counter = 1;
        }

        public void lock() {

            this.counter++;
        }

        public boolean unlock() {

            this.counter--;
            return 0 == this.counter;
        }
    }


    public Object getContext(String key, @SuppressWarnings("unused") Object data) {

        if (false == key.equalsIgnoreCase(AUDIT_CONTEXT_KEY))
            return null;
        AuditContextInfo auditContextInfo = auditContextInfos.get();
        if (null == auditContextInfo)
            return null;
        return auditContextInfo.getAuditContextId();
    }

    public String[] getKeys() {

        String[] keys = { AUDIT_CONTEXT_KEY };
        return keys;
    }

    public boolean supports(String key) {

        return key.equalsIgnoreCase(AUDIT_CONTEXT_KEY);
    }

    /**
     * Sets the audit context Id for the current thread.
     *
     * @param auditContextId
     * @throws ExistingAuditContextException
     */
    public static synchronized void setAuditContextId(long auditContextId) throws ExistingAuditContextException {

        AuditContextInfo previousAuditContextInfo = auditContextInfos.get();
        if (null != previousAuditContextInfo) {
            long previousAuditContextId = previousAuditContextInfo.getAuditContextId();
            LOG.fatal("previous audit context found: " + previousAuditContextId);
            throw new ExistingAuditContextException(previousAuditContextId);
        }
        AuditContextInfo auditContextInfo = new AuditContextInfo(auditContextId);
        auditContextInfos.set(auditContextInfo);
    }

    /**
     * Returns the audit context Id for the current thread
     *
     * @return auditContextId
     * @throws MissingAuditContextException
     */
    public static synchronized Long getAuditContextId() throws MissingAuditContextException {

        AuditContextInfo auditContextInfo = auditContextInfos.get();
        if (null == auditContextInfo) {
            LOG.fatal("missing audit context");
            throw new MissingAuditContextException();
        }
        return auditContextInfo.getAuditContextId();
    }

    /**
     * Locks the audit context associated with the current thread. If the current thread has no audit context this
     * method will return <code>false</code>.
     *
     */
    public static synchronized boolean lockAuditContext() {

        AuditContextInfo auditContextInfo = auditContextInfos.get();
        if (null != auditContextInfo) {
            auditContextInfo.lock();
            return true;
        }
        return false;
    }

    /**
     * Removes the audit context for the current thread.
     *
     * @throws MissingAuditContextException
     */
    public static synchronized boolean removeAuditContext() throws MissingAuditContextException {

        AuditContextInfo auditContextInfo = auditContextInfos.get();
        if (null == auditContextInfo) {
            LOG.fatal("missing audit context");
            throw new MissingAuditContextException();
        }
        boolean isMainEntry = auditContextInfo.unlock();
        if (isMainEntry) {
            auditContextInfos.remove();
        }
        return isMainEntry;
    }
}
