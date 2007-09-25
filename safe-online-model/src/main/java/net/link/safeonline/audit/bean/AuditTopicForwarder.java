/* SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.bean;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
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
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.audit.AuditMessage;
import net.link.safeonline.audit.service.AuditService;

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
		@ActivationConfigProperty(propertyName = "destination", propertyValue = AuditConstants.auditTopic) })
public class AuditTopicForwarder implements MessageListener {

	private static final Log LOG = LogFactory.getLog(AuditTopicForwarder.class);

	@Resource(mappedName = "ConnectionFactory")
	private ConnectionFactory factory;

	@Resource(mappedName = AuditConstants.auditQueue)
	private Queue auditQueue;

	@EJB
	AuditService auditService;

	public void onMessage(Message msg) {

		LOG.debug("onMessage");

		try {
			AuditMessage auditMessage = new AuditMessage(msg);
			Connection connect = factory.createConnection();
			Session session = connect.createSession(true, 0);
			MessageProducer producer = session.createProducer(this.auditQueue);
			producer.send(auditMessage.getJMSMessage(session));
			session.close();
			connect.close();
		} catch (JMSException e) {
			throw new EJBException();
		}
	}
}
