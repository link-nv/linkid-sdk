package net.link.safeonline.demo.model;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;


@Local
public interface WSSecurityConfiguration extends SafeOnlineService, WSSecurityConfigurationService {

    public static final String JNDI_BINDING = DemoConstants.DEMO_JNDI_PREFIX + "WSSecurityConfigurationBean/local";
}
