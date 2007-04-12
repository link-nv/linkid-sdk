/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.lawyer.bean;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;

import net.link.safeonline.demo.lawyer.LawyerConstants;
import net.link.safeonline.demo.lawyer.LawyerStatus;
import net.link.safeonline.demo.lawyer.LawyerStatusManager;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("lawyerStatusManager")
@LocalBinding(jndiBinding = "SafeOnlineLawyerDemo/LawyerStatusManagerBean/local")
@SecurityDomain(LawyerConstants.SECURITY_DOMAIN)
public class LawyerStatusManagerBean extends AbstractLawyerDataClientBean
		implements LawyerStatusManager {

	@Logger
	private Log log;

	@In(create = true)
	FacesMessages facesMessages;

	@Resource
	private SessionContext sessionContext;

	@Factory("lawyerStatus")
	@RolesAllowed( { LawyerConstants.USER_ROLE, LawyerConstants.ADMIN_ROLE })
	public LawyerStatus lawyerStatusFactory() {
		log.debug("lawyerStatusFactory");
		String subjectLogin = this.sessionContext.getCallerPrincipal()
				.getName();
		LawyerStatus lawyerStatus = getLawyerStatus(subjectLogin);
		if (null == lawyerStatus) {
			lawyerStatus = new LawyerStatus();
		}
		return lawyerStatus;
	}
}
