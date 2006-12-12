/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.SubjectManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class IdentityServiceBean implements IdentityService {

	private static final Log LOG = LogFactory.getLog(IdentityServiceBean.class);

	@EJB
	private SubjectManager subjectManager;

	@EJB
	private HistoryDAO historyDAO;

	@RolesAllowed(SafeOnlineConstants.USER_ROLE)
	public String getName() {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		String name = subject.getName();
		LOG.debug("get name of " + subject.getLogin() + ": " + name);
		return name;
	}

	@RolesAllowed(SafeOnlineConstants.USER_ROLE)
	public void saveName(String name) {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		LOG.debug("save name " + name + " for entity with login "
				+ subject.getLogin());
		subject.setName(name);
	}

	@RolesAllowed(SafeOnlineConstants.USER_ROLE)
	public List<HistoryEntity> getHistory() {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		List<HistoryEntity> result = this.historyDAO.getHistory(subject);
		return result;
	}
}
