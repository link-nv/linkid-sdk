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
import net.link.safeonline.audit.TinySyslogger;
import net.link.safeonline.audit.TinySyslogger.Facility;
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
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = AuditSyslogBean.JNDI_BINDING)
@Interceptors(ConfigurationInterceptor.class)
@Configurable
public class AuditSyslogBean implements AuditBackend {

    public static final String  JNDI_BINDING     = AuditBackend.JNDI_PREFIX + "AuditSyslogBean";

    private static final long   serialVersionUID = 1L;

    private static final Log    LOG              = LogFactory.getLog(AuditSyslogBean.class);

    private static final String CONFIG_GROUP     = "Audit syslog";

    @Configurable(name = "Hostname", group = CONFIG_GROUP)
    private String              syslogHost       = "127.0.0.1";

    @Configurable(name = "Facility", group = CONFIG_GROUP)
    private String              facility         = "LOCAL0";

    @EJB(mappedName = SecurityAuditDAO.JNDI_BINDING)
    private SecurityAuditDAO    securityAuditDAO;

    @EJB(mappedName = ResourceAuditDAO.JNDI_BINDING)
    private ResourceAuditDAO    resourceAuditDAO;

    @EJB(mappedName = AccessAuditDAO.JNDI_BINDING)
    private AccessAuditDAO      accessAuditDAO;

    @EJB(mappedName = AuditAuditDAO.JNDI_BINDING)
    private AuditAuditDAO       auditAuditDAO;

    private TinySyslogger       syslog;


    @PostConstruct
    public void init() {

        LOG.debug("init audit syslog bean ( " + syslogHost + " )");
        syslog = new TinySyslogger(Facility.valueOf(facility), syslogHost);
    }

    @PreDestroy
    public void close() {

        syslog.close();
    }

    private void logSecurityAudits(Long auditContextId) {

        List<SecurityAuditEntity> securityAuditEntries = securityAuditDAO.listRecords(auditContextId);
        for (SecurityAuditEntity e : securityAuditEntries) {
            String msg = String.format("Security audit context %d: principal='%s', message='%s'", e.getAuditContext().getId(),
                    e.getTargetPrincipal(), e.getMessage());

            LOG.debug(msg);
            syslog.log(msg);
        }
    }

    private void logResourceAudits(Long auditContextId) {

        List<ResourceAuditEntity> resourceAuditEntries = resourceAuditDAO.listRecords(auditContextId);
        for (ResourceAuditEntity e : resourceAuditEntries) {
            String msg = String.format("Resource audit context %d: resource='%s', type='%s', source='%s', message='%s'",
                    e.getAuditContext().getId(), e.getResourceName(), e.getResourceLevel(), e.getSourceComponent(), e.getMessage());

            LOG.debug(msg);
            syslog.log(msg);
        }
    }

    private void logAccessAudits(Long auditContextId) {

        List<AccessAuditEntity> accessAuditEntries = accessAuditDAO.listRecords(auditContextId);
        for (AccessAuditEntity e : accessAuditEntries)
            if (e.getOperationState() != OperationStateType.BEGIN && e.getOperationState() != OperationStateType.NORMAL_END) {
                String msg = String.format("Access audit context %d: principal='%s', operation='%s', operationState='%s'",
                        e.getAuditContext().getId(), e.getPrincipal(), e.getOperation(), e.getOperationState());

                LOG.debug(msg);
                syslog.log(msg);
            }
    }

    private void logAuditAudits(Long auditContextId) {

        List<AuditAuditEntity> auditAuditEntries = auditAuditDAO.listRecords(auditContextId);
        for (AuditAuditEntity e : auditAuditEntries) {
            String msg = String.format("Audit audit context %d: message='%s'", e.getAuditContext().getId(), e.getMessage());

            LOG.debug(msg);
            syslog.log(msg);
        }
    }

    public void process(long auditContextId) {

        if (syslogHost == null || syslogHost.length() == 0) {
            LOG.debug("skipping syslog");
            return;
        }

        logSecurityAudits(auditContextId);
        logResourceAudits(auditContextId);
        logAccessAudits(auditContextId);
        logAuditAudits(auditContextId);
    }
}
