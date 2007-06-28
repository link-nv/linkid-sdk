/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.mandate.bean;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;

import net.link.safeonline.demo.mandate.AuthorizationService;
import net.link.safeonline.demo.mandate.MandateConstants;
import net.link.safeonline.demo.mandate.entity.UserEntity;

@Stateless
@LocalBinding(jndiBinding = AuthorizationService.JNDI_BINDING)
public class AuthorizationServiceBean implements AuthorizationService {

	private static final Log LOG = LogFactory
			.getLog(AuthorizationServiceBean.class);

	@PersistenceContext(unitName = MandateConstants.ENTITY_MANAGER_NAME)
	private EntityManager entityManager;

	public boolean isAdmin(String username) {
		LOG.debug("isAdmin: " + username);

		UserEntity user = this.entityManager.find(UserEntity.class, username);
		if (null == user) {
			return false;
		}

		return user.isAdmin();
	}

	public void bootstrap() {
		LOG.debug("bootstrapping...");
		UserEntity defaultAdminUser = this.entityManager.find(UserEntity.class,
				AuthorizationService.DEFAULT_ADMIN_USER);
		if (null == defaultAdminUser) {
			LOG.debug("adding default admin user: "
					+ AuthorizationService.DEFAULT_ADMIN_USER);
			defaultAdminUser = new UserEntity(
					AuthorizationService.DEFAULT_ADMIN_USER);
			this.entityManager.persist(defaultAdminUser);
		}
		if (false == defaultAdminUser.isAdmin()) {
			LOG.debug("resetting default admin user to admin privilege: "
					+ defaultAdminUser.getName());
			defaultAdminUser.setAdmin(true);
		}
	}
}
