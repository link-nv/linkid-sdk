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
import net.link.safeonline.model.ConfigurationInterceptor;

@Stateless
@Configurable
@Interceptors(ConfigurationInterceptor.class)
public class SamlAuthorityServiceBean implements SamlAuthorityService {

	@Configurable(group = "SAML Authority", name = "Issuer Name")
	private String issuerName = "safe-online";

	public String getIssuerName() {
		return this.issuerName;
	}
}
