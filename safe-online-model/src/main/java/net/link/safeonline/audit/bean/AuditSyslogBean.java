/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.bean;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.audit.AuditBackend;
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
import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@LocalBinding(jndiBinding = AuditBackend.JNDI_PREFIX + "AuditSyslogBean")
@Interceptors(ConfigurationInterceptor.class)
@Configurable(group = "Audit syslog configuration")
public class AuditSyslogBean implements AuditBackend {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(AuditSyslogBean.class);

	Logger logger = Logger.getLogger("syslog");

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
		syslogAppender.setFacility("LOCAL1");
		syslogAppender.setThreshold(Level.toLevel(this.threshold));
		syslogAppender.setLayout(new SimpleLayout());

		logger.addAppender(syslogAppender);
	}

	private void logSecurityAudits(Long auditContextId) {
		List<SecurityAuditEntity> securityAuditEntries = this.securityAuditDAO
				.listRecords(auditContextId);
		for (SecurityAuditEntity e : securityAuditEntries) {
			String msg = "Security audit context "
					+ e.getAuditContext().getId() + " : principal="
					+ e.getTargetPrincipal() + " message=" + e.getMessage();
			LOG.debug(msg);
			logger.error(msg);
		}
	}

	private void logResourceAudits(Long auditContextId) {
		List<ResourceAuditEntity> resourceAuditEntries = this.resourceAuditDAO
				.listRecords(auditContextId);
		for (ResourceAuditEntity e : resourceAuditEntries) {
			String msg = "Resource audit context "
					+ e.getAuditContext().getId() + " : resource="
					+ e.getResourceName() + " type=" + e.getResourceLevel()
					+ " source=" + e.getSourceComponent() + " message="
					+ e.getMessage();
			LOG.debug(msg);
			logger.error(msg);
		}
	}

	private void logAccessAudits(Long auditContextId) {
		List<AccessAuditEntity> accessAuditEntries = this.accessAuditDAO
				.listRecords(auditContextId);
		for (AccessAuditEntity e : accessAuditEntries) {
			if (e.getOperationState() != OperationStateType.BEGIN
					&& e.getOperationState() != OperationStateType.NORMAL_END) {
				String msg = "Access audit context "
						+ e.getAuditContext().getId() + " : principal="
						+ e.getPrincipal() + " operation=" + e.getOperation()
						+ "operationState=" + e.getOperationState();
				LOG.debug(msg);
				logger.error(msg);
			}
		}
	}

	private void logAuditAudits(Long auditContextId) {
		List<AuditAuditEntity> auditAuditEntries = this.auditAuditDAO
				.listRecords(auditContextId);
		for (AuditAuditEntity e : auditAuditEntries) {
			String msg = "Audit audit context " + e.getAuditContext().getId()
					+ " message=" + e.getMessage();
			LOG.debug(msg);
			logger.error(msg);
		}
	}

	public void process(long auditContextId) {
		logSecurityAudits(auditContextId);
		logResourceAudits(auditContextId);
		logAccessAudits(auditContextId);
		logAuditAudits(auditContextId);
	}
}
