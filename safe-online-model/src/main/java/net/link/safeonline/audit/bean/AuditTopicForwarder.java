/* SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.bean;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import net.link.safeonline.audit.AuditConstants;
import net.link.safeonline.audit.AuditMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * MDB that forwards the audit contexts from the audit topic to the audit queue,
 * where they will be sanitized.
 * 
 * @author wvdhaute
 * 
 */

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = AuditConstants.AUDIT_TOPIC_NAME) })
public class AuditTopicForwarder implements MessageListener {

	private static final Log LOG = LogFactory.getLog(AuditTopicForwarder.class);

	@Resource(mappedName = AuditConstants.CONNECTION_FACTORY_NAME)
	private ConnectionFactory factory;

	@Resource(mappedName = AuditConstants.AUDIT_QUEUE_NAME)
	private Queue auditQueue;

	public void onMessage(Message msg) {
		LOG.debug("onMessage");
		try {
			AuditMessage auditMessage = new AuditMessage(msg);
			Connection connection = this.factory.createConnection();
			try {
				Session session = connection.createSession(true, 0);
				try {
					MessageProducer producer = session
							.createProducer(this.auditQueue);
					try {
						Message message = auditMessage.getJMSMessage(session);
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
		} catch (JMSException e) {
			throw new EJBException();
		}
	}
}
