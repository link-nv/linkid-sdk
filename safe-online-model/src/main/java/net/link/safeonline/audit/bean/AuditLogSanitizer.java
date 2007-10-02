/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.bean;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import net.link.safeonline.audit.AuditConstants;
import net.link.safeonline.audit.AuditMessage;
import net.link.safeonline.audit.dao.AccessAuditDAO;
import net.link.safeonline.audit.dao.AuditAuditDAO;
import net.link.safeonline.audit.dao.AuditContextDAO;
import net.link.safeonline.audit.dao.ResourceAuditDAO;
import net.link.safeonline.audit.dao.SecurityAuditDAO;
import net.link.safeonline.audit.exception.AuditContextNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Audit log sanitization. Fetches audit contexts from the audit queue and
 * cleans up the ones containing no relevant information.
 * 
 * @author wvdhaute
 * 
 */

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = AuditConstants.AUDIT_SANITIZER_QUEUE_NAME) })
public class AuditLogSanitizer implements MessageListener {

	private static final Log LOG = LogFactory.getLog(AuditLogSanitizer.class);

	@EJB
	private AuditContextDAO auditContextDAO;

	@EJB
	private AuditAuditDAO auditAuditDAO;

	@EJB
	private AccessAuditDAO accessAuditDAO;

	@EJB
	private ResourceAuditDAO resourceAuditDAO;

	@EJB
	private SecurityAuditDAO securityAuditDAO;

	public void onMessage(Message msg) {
		AuditMessage auditMessage;
		try {
			auditMessage = new AuditMessage(msg);
		} catch (JMSException e) {
			throw new EJBException(
					"audit message JMS error: " + e.getMessage(), e);
		}
		Long auditContextId = auditMessage.getAuditContextId();
		LOG.debug("audit context: " + auditContextId);
		if (requiresSanitation(auditContextId)) {
			flush(auditContextId);
		}
	}

	private boolean requiresSanitation(long auditContextId) {
		if (this.resourceAuditDAO.hasRecords(auditContextId)) {
			return false;
		}
		if (this.securityAuditDAO.hasRecords(auditContextId)) {
			return false;
		}
		if (this.auditAuditDAO.hasRecords(auditContextId)) {
			return false;
		}
		if (this.accessAuditDAO.hasErrorRecords(auditContextId)) {
			return false;
		}
		return true;
	}

	private void flush(long auditContextId) {
		LOG.debug("sanitizing audit context: " + auditContextId);
		this.accessAuditDAO.cleanup(auditContextId);
		try {
			this.auditContextDAO.removeAuditContext(auditContextId);
		} catch (AuditContextNotFoundException e) {
			throw new EJBException(
					"audit context not found: " + auditContextId, e);
		}
	}
}
