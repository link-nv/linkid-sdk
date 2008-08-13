/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.config.model.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.dao.ConfigItemDAO;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.config.model.ConfigurationManager;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.config.ConfigItemEntity;


@Stateless
@Configurable
@Interceptors( { ConfigurationInterceptor.class })
public class ConfigurationManagerBean implements ConfigurationManager {

    public static final String WS_SECURITY_MAX_TIMESTAMP_OFFSET = "Maximum WS-Security Timestamp Offset (ms)";

    @EJB
    private ConfigItemDAO      configItemDAO;

    @EJB
    private ApplicationDAO     applicationDAO;


    public ConfigItemEntity findConfigItem(String name) {

        return this.configItemDAO.findConfigItem(name);
    }


    /**
     * Maximum Offset between the WS-Security Created time and the time indicated by the local clock.
     */
    @Configurable(group = "Security", name = WS_SECURITY_MAX_TIMESTAMP_OFFSET)
    private Long maxWsSecurityTimestampOffset = 1000 * 60 * 5L;


    public long getMaximumWsSecurityTimestampOffset() {

        return this.maxWsSecurityTimestampOffset;
    }

    public boolean skipMessageIntegrityCheck(String applicationName) throws ApplicationNotFoundException {

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        boolean skipMessageIntegrityCheck = application.isSkipMessageIntegrityCheck();
        return skipMessageIntegrityCheck;
    }
}
