/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.net.InetAddress;
import java.net.SocketException;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import net.link.safeonline.Task;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.audit.ResourceAuditLogger;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.entity.audit.ResourceLevelType;
import net.link.safeonline.entity.audit.ResourceNameType;
import net.link.safeonline.entity.audit.SecurityThreatType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.jboss.annotation.ejb.LocalBinding;


/**
 * Clock Drift Detector Task. Detects local clock drift using an NTP server. Since WS-Security and SAML depends on timestamps to prevent
 * replay attacks it's important to have a proper clock drift detection mechanism.
 * 
 * @author fcorneli
 * 
 */
@Stateless
@Configurable
@Interceptors( { AuditContextManager.class, ConfigurationInterceptor.class })
@Local(Task.class)
@LocalBinding(jndiBinding = ClockDriftDetectorTaskBean.JNDI_BINDING)
public class ClockDriftDetectorTaskBean implements Task {

    private static final Log    LOG                     = LogFactory.getLog(ClockDriftDetectorTaskBean.class);

    public static final String  JNDI_BINDING            = Task.JNDI_PREFIX + "ClockDriftDetectorTaskBean/local";

    @EJB(mappedName = SecurityAuditLogger.JNDI_BINDING)
    private SecurityAuditLogger securityAuditLogger;

    @EJB(mappedName = ResourceAuditLogger.JNDI_BINDING)
    private ResourceAuditLogger resourceAuditLogger;

    public static final String  CONFIG_GROUP            = "Clock Drift Detector";

    @Configurable(group = CONFIG_GROUP, name = "NTP Timeout (ms)")
    private Integer             timeout                 = 10 * 1000;

    public static final String  DEFAULT_NTP_SERVER_NAME = "0.pool.ntp.org";

    @Configurable(group = CONFIG_GROUP, name = "NTP Server Name")
    private String              ntpServerName           = DEFAULT_NTP_SERVER_NAME;

    @Configurable(group = CONFIG_GROUP, name = "Maximum clock offset (ms)")
    private Integer             maxClockOffset          = 1000;


    public String getName() {

        return "Clock Drift Detector Task";
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void perform()
            throws Exception {

        LOG.debug("perform task");
        TimeInfo timeInfo;
        try {
            NTPUDPClient client = new NTPUDPClient();
            client.setDefaultTimeout(timeout);
            client.open();
            InetAddress ntpServerAddress = InetAddress.getByName(ntpServerName);
            LOG.debug("NTP server: " + ntpServerAddress);
            timeInfo = client.getTime(ntpServerAddress);
            client.close();
            timeInfo.computeDetails();
            Long offset = timeInfo.getOffset();
            LOG.debug("clock offset (ms): " + offset);
            if (Math.abs(offset) > maxClockOffset) {
                LOG.warn("maximum clock offset reached");
                securityAuditLogger.addSecurityAudit(SecurityThreatType.DISRUPTION, "Maximum clock offset reached of " + offset
                        + " milliseconds against NTP Server: " + ntpServerAddress);
            }
        } catch (SocketException e) {
            resourceAuditLogger.addResourceAudit(ResourceNameType.NTP, ResourceLevelType.RESOURCE_UNAVAILABLE, ntpServerName,
                    "Error contacting NTP server");
        }

    }
}
