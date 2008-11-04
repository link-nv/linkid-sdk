/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Configurable
@LocalBinding(jndiBinding = SamlAuthorityService.JNDI_BINDING)
@Interceptors(ConfigurationInterceptor.class)
public class SamlAuthorityServiceBean implements SamlAuthorityService {

    public static final String CONFIG_GROUP           = "SAML Authority";

    @Configurable(group = CONFIG_GROUP, name = "Issuer Name")
    private String             issuerName             = "OLAS";

    @Configurable(group = CONFIG_GROUP, name = "Authentication Assertion Validity (sec)")
    private Integer            authnAssertionValidity = 60 * 10;


    public String getIssuerName() {

        return this.issuerName;
    }

    public int getAuthnAssertionValidity() {

        return this.authnAssertionValidity;
    }
}
