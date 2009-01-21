/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.service.WSAuthenticationService;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Configurable
@LocalBinding(jndiBinding = WSAuthenticationService.JNDI_BINDING)
@Interceptors(ConfigurationInterceptor.class)
public class WSAuthenticationServiceBean implements WSAuthenticationService {

    public static final String CONFIG_GROUP          = "WS Authentication";

    @Configurable(group = CONFIG_GROUP, name = "WS Authentication Timeout (sec)")
    private Integer            authenticationTimeout = 60 * 10;


    public int getAuthenticationTimeout() {

        return authenticationTimeout;
    }
}
