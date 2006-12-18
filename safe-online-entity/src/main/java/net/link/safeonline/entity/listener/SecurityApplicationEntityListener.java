/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.listener;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Set;

import javax.ejb.EJBException;
import javax.persistence.PreUpdate;
import javax.security.auth.Subject;

import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.SubjectEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;

/**
 * Implementation of application ownership security constraint.
 * 
 * @author fcorneli
 * 
 */
public class SecurityApplicationEntityListener {

	private static final Log LOG = LogFactory
			.getLog(SecurityApplicationEntityListener.class);

	private boolean isCallerInRole(Subject subject, String role) {
		Set<Group> groups = subject.getPrincipals(Group.class);
		if (null == groups) {
			return false;
		}
		SimplePrincipal rolePrincipal = new SimplePrincipal(role);
		for (Group group : groups) {
			while (!"Roles".equals(group.getName())) {
				continue;
			}
			if (group.isMember(rolePrincipal)) {
				return true;
			}
		}
		return false;
	}

	@PreUpdate
	public void preUpdateCallback(ApplicationEntity application) {
		LOG.debug("pre update callback on application: "
				+ application.getName());

		Principal principal = SecurityAssociation.getPrincipal();
		Subject subject = SecurityAssociation.getSubject();
		if (null == subject) {
			String msg = "subject is null";
			LOG.error(msg);
			throw new EJBException(msg);
		}
		if (null == principal) {
			String msg = "principal is null";
			LOG.error(msg);
			throw new EJBException(msg);
		}

		boolean isOperator = isCallerInRole(subject, "operator");
		if (isOperator) {
			return;
		}

		boolean isOwner = isCallerInRole(subject, "owner");
		if (!isOwner) {
			String msg = "caller has no owner role";
			LOG.error(msg);
			throw new EJBException(msg);
		}

		String login = principal.getName();
		ApplicationOwnerEntity applicationOwner = application
				.getApplicationOwner();
		SubjectEntity adminSubject = applicationOwner.getAdmin();
		if (login.equals(adminSubject.getLogin())) {
			return;
		}
		String msg = "only application owner admin can change the application";
		LOG.error(msg);
		throw new EJBException(msg);
	}
}
