/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.jboss.annotation.security.SecurityDomain;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.ApplicationOwnerDAO;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.ApplicationOwnerManager;
import net.link.safeonline.model.SubjectManager;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class ApplicationOwnerManagerBean implements ApplicationOwnerManager {

	@EJB
	private SubjectManager subjectManager;

	@EJB
	private ApplicationOwnerDAO applicationOwnerDAO;

	@RolesAllowed(SafeOnlineConstants.OWNER_ROLE)
	public ApplicationOwnerEntity getCallerApplicationOwner() {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		ApplicationOwnerEntity applicationOwner = this.applicationOwnerDAO
				.getApplicationOwner(subject);
		return applicationOwner;
	}
}
