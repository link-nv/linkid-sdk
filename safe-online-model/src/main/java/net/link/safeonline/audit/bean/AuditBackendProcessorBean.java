/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.bean;

import java.util.LinkedList;
import java.util.List;

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
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import net.link.safeonline.audit.AuditBackend;
import net.link.safeonline.audit.AuditConstants;
import net.link.safeonline.audit.AuditMessage;
import net.link.safeonline.audit.dao.AuditAuditDAO;
import net.link.safeonline.audit.dao.AuditContextDAO;
import net.link.safeonline.audit.exception.AuditContextNotFoundException;
import net.link.safeonline.entity.audit.AuditContextEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = AuditConstants.AUDIT_BACKEND_QUEUE_NAME) })
public class AuditBackendProcessorBean implements MessageListener {

	private static final Log LOG = LogFactory
			.getLog(AuditBackendProcessorBean.class);

	@Resource(mappedName = AuditConstants.CONNECTION_FACTORY_NAME)
	private ConnectionFactory factory;

	@Resource(mappedName = AuditConstants.AUDIT_SANITIZER_QUEUE_NAME)
	private Queue auditQueue;

	@EJB
	private AuditAuditDAO auditAuditDAO;

	@EJB
	private AuditContextDAO auditContextDAO;

	public void onMessage(Message message) {
		AuditMessage auditMessage;
		try {
			auditMessage = new AuditMessage(message);
		} catch (JMSException e) {
			throw new EJBException("JMS error: " + e.getMessage(), e);
		}
		long auditContextId = auditMessage.getAuditContextId();
		LOG.debug("processing audit context: " + auditContextId);

		List<AuditBackend> auditBackends;
		try {
			auditBackends = getAuditBackends();
		} catch (NamingException e) {
			throw new EJBException("JNDI error: " + e.getMessage(), e);
		}
		for (AuditBackend auditBackend : auditBackends) {
			try {
				auditBackend.process(auditContextId);
			} catch (Exception e) {
				addAuditAudit(auditBackend, e, auditContextId);
			}
		}

		sanitizeForwarding(auditMessage);
	}

	private void addAuditAudit(AuditBackend auditBackend, Exception exception,
			long auditContextId) {
		String message = exception.getMessage();
		AuditContextEntity auditContext;
		try {
			auditContext = this.auditContextDAO.getAuditContext(auditContextId);
		} catch (AuditContextNotFoundException e) {
			this.auditAuditDAO
					.addAuditAudit("audit backend error for audit context "
							+ auditContextId + ": " + message);
			return;
		}
		this.auditAuditDAO.addAuditAudit(auditContext, message);
	}

	public List<AuditBackend> getAuditBackends() throws NamingException {
		InitialContext initialContext = new InitialContext();
		Context context = (Context) initialContext
				.lookup(AuditBackend.JNDI_CONTEXT);
		NamingEnumeration<NameClassPair> result = initialContext
				.list(AuditBackend.JNDI_CONTEXT);
		List<AuditBackend> auditBackends = new LinkedList<AuditBackend>();
		while (result.hasMore()) {
			NameClassPair nameClassPair = result.next();
			String objectName = nameClassPair.getName();
			LOG.debug(objectName + ":" + nameClassPair.getClassName());
			Object object = context.lookup(objectName);
			if (!(object instanceof AuditBackend)) {
				String message = "object \"" + AuditBackend.JNDI_CONTEXT + "/"
						+ objectName + "\" is not a "
						+ AuditBackend.class.getName();
				LOG.error(message);
				throw new IllegalStateException(message);
			}
			AuditBackend startable = (AuditBackend) PortableRemoteObject
					.narrow(object, AuditBackend.class);
			auditBackends.add(startable);
		}
		return auditBackends;
	}

	public void sanitizeForwarding(AuditMessage auditMessage) {
		try {
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
