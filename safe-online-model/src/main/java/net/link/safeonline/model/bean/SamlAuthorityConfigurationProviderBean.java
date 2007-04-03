/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.util.Map;
import java.util.TreeMap;

import javax.ejb.Local;
import javax.ejb.Stateless;

import org.jboss.annotation.ejb.LocalBinding;

import net.link.safeonline.ConfigurationProvider;

@Stateless
@Local(ConfigurationProvider.class)
@LocalBinding(jndiBinding = ConfigurationProvider.JNDI_PREFIX + "/"
		+ "SamlAuthorityConfigurationProviderBean")
public class SamlAuthorityConfigurationProviderBean implements
		ConfigurationProvider {

	public static final String GROUP_NAME = "SAML Authority";

	public static final String ISSUER_NAME_CONFIG_PARAM = "Issuer Name";

	public static final String ISSUER_NAME_DEFAULT = "safe-online";

	public Map<String, String> getConfigurationParameters() {
		Map<String, String> configurationParameters = new TreeMap<String, String>();
		configurationParameters.put(ISSUER_NAME_CONFIG_PARAM,
				ISSUER_NAME_DEFAULT);
		return configurationParameters;
	}

	public String getGroupName() {
		return GROUP_NAME;
	}
}
