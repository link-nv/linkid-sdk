/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.bean;

import java.util.LinkedList;
import java.util.List;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import net.link.safeonline.audit.AuditBackend;
import net.link.safeonline.audit.AuditConstants;
import net.link.safeonline.audit.AuditMessage;
import net.link.safeonline.audit.dao.AccessAuditDAO;
import net.link.safeonline.audit.dao.AuditAuditDAO;
import net.link.safeonline.audit.dao.AuditContextDAO;
import net.link.safeonline.audit.dao.ResourceAuditDAO;
import net.link.safeonline.audit.dao.SecurityAuditDAO;
import net.link.safeonline.audit.exception.AuditContextNotFoundException;
import net.link.safeonline.entity.audit.AuditContextEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = AuditConstants.AUDIT_BACKEND_QUEUE_NAME),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class AuditBackendProcessorBean implements MessageListener {

    private static final Log LOG = LogFactory.getLog(AuditBackendProcessorBean.class);

    @EJB
    private AuditAuditDAO    auditAuditDAO;

    @EJB
    private AuditContextDAO  auditContextDAO;

    @EJB
    private ResourceAuditDAO resourceAuditDAO;

    @EJB
    private SecurityAuditDAO securityAuditDAO;

    @EJB
    private AccessAuditDAO   accessAuditDAO;


    public void onMessage(Message message) {

        try {
            AuditMessage auditMessage = new AuditMessage(message);
            long auditContextId = auditMessage.getAuditContextId();
            LOG.debug("processing audit context: " + auditContextId);

            for (AuditBackend auditBackend : getAuditBackends()) {
                try {
                    auditBackend.process(auditContextId);
                } catch (Exception e) {
                    addAuditAudit(e, auditContextId);
                }
            }

            if (requiresSanitation(auditContextId)) {
                sanitize(auditContextId);
            }
        }

        catch (NamingException e) {
            throw new EJBException("JNDI error: " + e.getMessage(), e);
        } catch (JMSException e) {
            throw new EJBException("JMS error: " + e.getMessage(), e);
        }
    }

    private void sanitize(long auditContextId) {

        LOG.debug("sanitizing audit context: " + auditContextId);
        this.accessAuditDAO.cleanup(auditContextId);

        try {
            this.auditContextDAO.removeAuditContext(auditContextId);
        } catch (AuditContextNotFoundException e) {
            throw new EJBException("audit context not found: " + auditContextId, e);
        }
    }

    private boolean requiresSanitation(long auditContextId) {

        if (this.resourceAuditDAO.hasRecords(auditContextId)) {
            LOG.debug("has resource audit records");
            return false;
        }
        if (this.securityAuditDAO.hasRecords(auditContextId)) {
            LOG.debug("has security audit records");
            return false;
        }
        if (this.auditAuditDAO.hasRecords(auditContextId)) {
            LOG.debug("has audit audit records");
            return false;
        }
        if (this.accessAuditDAO.hasErrorRecords(auditContextId)) {
            LOG.debug("has access audit error records");
            return false;
        }

        return true;
    }

    private void addAuditAudit(Exception exception, long auditContextId) {

        String message = exception.getMessage();
        
        try {
            AuditContextEntity auditContext = this.auditContextDAO.getAuditContext(auditContextId);
            this.auditAuditDAO.addAuditAudit(auditContext, message);
        }

        catch (AuditContextNotFoundException e) {
            this.auditAuditDAO
                    .addAuditAudit("audit backend error for audit context " + auditContextId + ": " + message);
        }
    }

    public List<AuditBackend> getAuditBackends() throws NamingException {

        List<AuditBackend> auditBackends = new LinkedList<AuditBackend>();

        InitialContext initialContext = new InitialContext();
        Context context = (Context) initialContext.lookup(AuditBackend.JNDI_CONTEXT);
        NamingEnumeration<NameClassPair> result = initialContext.list(AuditBackend.JNDI_CONTEXT);
        
        while (result.hasMore()) {
            NameClassPair nameClassPair = result.next();
            String objectName = nameClassPair.getName();
            LOG.debug(objectName + ":" + nameClassPair.getClassName());
            
            Object object = context.lookup(objectName);
            if (!(object instanceof AuditBackend)) {
                String message = "object \"" + AuditBackend.JNDI_CONTEXT + "/" + objectName + "\" is not a "
                        + AuditBackend.class.getName() + "; it is " + (object == null? "null": "a " + object.getClass().getName());
                LOG.error(message);
                throw new IllegalStateException(message);
            }
            
            AuditBackend auditBackend = (AuditBackend) PortableRemoteObject.narrow(object, AuditBackend.class);
            auditBackends.add(auditBackend);
        }
        
        return auditBackends;
    }
}
