/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.bean;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.audit.AuditBackend;
import net.link.safeonline.audit.TinySyslogAppender;
import net.link.safeonline.audit.TinySyslogAppender.Facility;
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
import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@LocalBinding(jndiBinding = AuditBackend.JNDI_PREFIX + "AuditSyslogBean")
@Interceptors(ConfigurationInterceptor.class)
public class AuditSyslogBean implements AuditBackend {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(AuditSyslogBean.class);

	private static final String CONFIG_GROUP = "Audit syslog";

	Logger logger = Logger.getLogger("syslog");

	@Configurable(name = "Hostname", group = CONFIG_GROUP)
	private String syslogHost = "127.0.0.1";

	@Configurable(name = "Threshold", group = CONFIG_GROUP)
	private String threshold = "INFO";

	@Configurable(name = "Facility", group = CONFIG_GROUP)
	private String facility = "LOCAL0";

	@EJB
	private SecurityAuditDAO securityAuditDAO;

	@EJB
	private ResourceAuditDAO resourceAuditDAO;

	@EJB
	private AccessAuditDAO accessAuditDAO;

	@EJB
	private AuditAuditDAO auditAuditDAO;

	private TinySyslogAppender syslogAppender;

	@PostConstruct
	public void init() {
		LOG.debug("init audit syslog bean ( " + this.syslogHost + " )");

		this.syslogAppender = new TinySyslogAppender(Facility
				.valueOf(this.facility), this.syslogHost);
		this.syslogAppender.setThreshold(Level.toLevel(this.threshold));
		this.syslogAppender.setLayout(new SimpleLayout());

		this.logger.addAppender(this.syslogAppender);
	}

	@PreDestroy
	public void close() {
		this.syslogAppender.close();
	}

	private void logSecurityAudits(Long auditContextId) {
		List<SecurityAuditEntity> securityAuditEntries = this.securityAuditDAO
				.listRecords(auditContextId);
		for (SecurityAuditEntity e : securityAuditEntries) {
			String msg = "Security audit context "
					+ e.getAuditContext().getId() + " : principal="
					+ e.getTargetPrincipal() + " message=" + e.getMessage();
			LOG.debug(msg);
			this.logger.error(msg);
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
			this.logger.error(msg);
		}
	}

	private void logAccessAudits(Long auditContextId) {
		List<AccessAuditEntity> accessAuditEntries = this.accessAuditDAO
				.listRecords(auditContextId);
		for (AccessAuditEntity e : accessAuditEntries)
			if (e.getOperationState() != OperationStateType.BEGIN
					&& e.getOperationState() != OperationStateType.NORMAL_END) {
				String msg = "Access audit context "
						+ e.getAuditContext().getId() + " : principal="
						+ e.getPrincipal() + " operation=" + e.getOperation()
						+ "operationState=" + e.getOperationState();
				LOG.debug(msg);
				this.logger.error(msg);
			}
	}

	private void logAuditAudits(Long auditContextId) {
		List<AuditAuditEntity> auditAuditEntries = this.auditAuditDAO
				.listRecords(auditContextId);
		for (AuditAuditEntity e : auditAuditEntries) {
			String msg = "Audit audit context " + e.getAuditContext().getId()
					+ " message=" + e.getMessage();
			LOG.debug(msg);
			this.logger.error(msg);
		}
	}

	public void process(long auditContextId) {
		if (0 == this.syslogHost.length())
			LOG.debug("skipping syslog");
		logSecurityAudits(auditContextId);
		logResourceAudits(auditContextId);
		logAccessAudits(auditContextId);
		logAuditAudits(auditContextId);
	}
}
