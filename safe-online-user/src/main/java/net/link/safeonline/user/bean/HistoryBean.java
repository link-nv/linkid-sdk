/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.user.History;
import net.link.safeonline.user.UserConstants;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Name;

@Stateless
@Name("history")
@LocalBinding(jndiBinding = UserConstants.JNDI_PREFIX + "HistoryBean/local")
@SecurityDomain(UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN)
public class HistoryBean implements History {

	@EJB
	private IdentityService identityService;

	@RolesAllowed(UserConstants.USER_ROLE)
	public List<HistoryEntity> getList() {
		List<HistoryEntity> result = this.identityService.getHistory();
		return result;
	}
}
