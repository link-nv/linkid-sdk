/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.entity.ConfigItemEntity;
import net.link.safeonline.model.ConfigurationManager;
import net.link.safeonline.model.bean.SamlAuthorityConfigurationProviderBean;

@Stateless
public class SamlAuthorityServiceBean implements SamlAuthorityService {

	@EJB
	private ConfigurationManager configurationManager;

	public String getIssuerName() {
		ConfigItemEntity issuerNameConfigItem = this.configurationManager
				.findConfigItem(SamlAuthorityConfigurationProviderBean.ISSUER_NAME_CONFIG_PARAM);
		if (null == issuerNameConfigItem) {
			throw new EJBException(
					"SAML authority Issuer Name configuration parameter not present");
		}
		String issuerName = issuerNameConfigItem.getValue();
		return issuerName;
	}
}
