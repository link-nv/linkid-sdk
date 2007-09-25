/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.bean;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.interceptor.Interceptors;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import net.link.safeonline.audit.AuditConstants;
import net.link.safeonline.audit.AuditMessage;
import net.link.safeonline.audit.dao.AccessAuditDAO;
import net.link.safeonline.audit.dao.AuditAuditDAO;
import net.link.safeonline.audit.dao.ResourceAuditDAO;
import net.link.safeonline.audit.dao.SecurityAuditDAO;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.entity.audit.AccessAuditEntity;
import net.link.safeonline.entity.audit.AuditAuditEntity;
import net.link.safeonline.entity.audit.OperationStateType;
import net.link.safeonline.entity.audit.ResourceAuditEntity;
import net.link.safeonline.entity.audit.SecurityAuditEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.net.SyslogAppender;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = AuditConstants.AUDIT_TOPIC_NAME) })
@Interceptors(ConfigurationInterceptor.class)
@Configurable(group = "Audit syslog configuration")
public class AuditSyslogBean implements MessageListener {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(AuditSyslogBean.class);

	Logger logger = Logger.getLogger(AuditSyslogBean.class);

	@Configurable(name = "Hostname", group = "Audit syslog")
	private String syslogHost = "127.0.0.1";

	@Configurable(name = "Threshold", group = "Audit syslog")
	private String threshold = "INFO";

	@EJB
	private SecurityAuditDAO securityAuditDAO;

	@EJB
	private ResourceAuditDAO resourceAuditDAO;

	@EJB
	private AccessAuditDAO accessAuditDAO;

	@EJB
	private AuditAuditDAO auditAuditDAO;

	@PostConstruct
	public void init() {
		LOG.debug("init audit syslog bean ( " + this.syslogHost + " )");

		SyslogAppender syslogAppender = new SyslogAppender();
		syslogAppender.setSyslogHost(this.syslogHost);
		syslogAppender.setFacility("LOCAL0");
		syslogAppender.setThreshold(Level.toLevel(this.threshold));
		syslogAppender.setLayout(new SimpleLayout());

		logger.addAppender(syslogAppender);
	}

	public void onMessage(Message msg) {
		try {
			LOG.debug("syslog MDB : onMessage");
			AuditMessage auditMessage = new AuditMessage(msg);
			Long auditContextId = auditMessage.getAuditContextId();

			LOG.debug("syslog MDB : retrieved audit message (id="
					+ auditContextId + ")");

			logSecurityAudits(auditContextId);
			logResourceAudits(auditContextId);
			logAccessAudits(auditContextId);
			logAuditAudits(auditContextId);

		} catch (JMSException e) {
			throw new EJBException(e.getMessage());
		}

	}

	private void logSecurityAudits(Long auditContextId) {
		List<SecurityAuditEntity> securityAuditEntries = this.securityAuditDAO
				.listRecords(auditContextId);
		for (SecurityAuditEntity e : securityAuditEntries) {
			logger.error("Security audit context " + e.getAuditContext()
					+ " : principal=" + e.getTargetPrincipal() + " message="
					+ e.getMessage());
		}
	}

	private void logResourceAudits(Long auditContextId) {
		List<ResourceAuditEntity> resourceAuditEntries = this.resourceAuditDAO
				.listRecords(auditContextId);
		for (ResourceAuditEntity e : resourceAuditEntries) {
			logger.error("Resource audit context " + e.getAuditContext()
					+ " : resource=" + e.getResourceName() + " type="
					+ e.getResourceLevel() + " source="
					+ e.getSourceComponent() + " message=" + e.getMessage());
		}
	}

	private void logAccessAudits(Long auditContextId) {
		List<AccessAuditEntity> accessAuditEntries = this.accessAuditDAO
				.listRecords(auditContextId);
		for (AccessAuditEntity e : accessAuditEntries) {
			if (e.getOperationState() != OperationStateType.BEGIN
					&& e.getOperationState() != OperationStateType.NORMAL_END) {
				logger.error("Access audit context " + e.getAuditContext()
						+ " : principal=" + e.getPrincipal() + " operation="
						+ e.getOperation() + "operationState="
						+ e.getOperationState());
			}
		}
	}

	private void logAuditAudits(Long auditContextId) {
		List<AuditAuditEntity> auditAuditEntries = this.auditAuditDAO
				.listRecords(auditContextId);
		for (AuditAuditEntity e : auditAuditEntries) {
			logger.error("Audit audit context " + e.getAuditContext()
					+ " message=" + e.getMessage());
		}
	}
}
