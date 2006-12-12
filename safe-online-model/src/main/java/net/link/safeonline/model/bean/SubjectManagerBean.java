/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.SubjectManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

/**
 * Implementation of subject manager interface. This component really must live
 * within the SafeOnline core security domain since it depends on the caller
 * principal to retrieve its data.
 * 
 * @author fcorneli
 * 
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class SubjectManagerBean implements SubjectManager {

	private static final Log LOG = LogFactory.getLog(SubjectManagerBean.class);

	@Resource
	private SessionContext context;

	@EJB
	private SubjectDAO subjectDAO;

	@RolesAllowed(SafeOnlineConstants.USER_ROLE)
	public SubjectEntity getCallerSubject() {
		Principal principal = this.context.getCallerPrincipal();
		String login = principal.getName();
		SubjectEntity subject;
		try {
			subject = this.subjectDAO.getSubject(login);
		} catch (SubjectNotFoundException e) {
			String msg = "subject not found for called principal: " + login;
			LOG.fatal(msg, e);
			throw new RuntimeException(msg, e);
		}
		LOG.debug("get caller subject: " + subject.getLogin());
		return subject;
	}
}
