/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.bean;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import net.link.safeonline.audit.AuditConstants;
import net.link.safeonline.audit.AuditContextFinalizer;
import net.link.safeonline.audit.AuditMessage;
import net.link.safeonline.audit.dao.AuditAuditDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementation of the audit context finalizer component. Important here is that this component runs within its own transaction.
 * 
 * @author fcorneli
 */
@Stateless
public class AuditContextFinalizerBean implements AuditContextFinalizer {

    private static final Log  LOG = LogFactory.getLog(AuditContextFinalizerBean.class);

    @Resource(mappedName = AuditConstants.CONNECTION_FACTORY_NAME)
    private ConnectionFactory factory;

    @Resource(mappedName = AuditConstants.AUDIT_BACKEND_QUEUE_NAME)
    private Queue             auditBackendQueue;

    @EJB
    private AuditAuditDAO     auditAuditDAO;


    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void finalizeAuditContext(Long auditContextId) {

        LOG.debug("finalizing audit context: " + auditContextId);
        try {
            Connection connection = this.factory.createConnection();
            try {
                Session session = connection.createSession(true, 0);
                try {
                    AuditMessage auditMessage = new AuditMessage(auditContextId);
                    Message message = auditMessage.getJMSMessage(session);

                    MessageProducer producer = session.createProducer(this.auditBackendQueue);
                    try {
                        producer.send(message);
                    } finally {
                        producer.close();
                    }
                } finally {
                    session.close();
                }
            } finally {
                connection.close();
            }
        }

        catch (JMSException e) {
            this.auditAuditDAO.addAuditAudit("unable to publish audit context " + auditContextId + " - reason: " + e.getMessage()
                    + " - errorCode: " + e.getErrorCode());
        }
    }
}
