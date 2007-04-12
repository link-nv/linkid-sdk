/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.lawyer.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateful;

import net.link.safeonline.demo.lawyer.LawyerConstants;
import net.link.safeonline.demo.lawyer.LawyerSearch;
import net.link.safeonline.demo.lawyer.LawyerStatus;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("lawyerSearch")
@LocalBinding(jndiBinding = "SafeOnlineLawyerDemo/LawyerSearchBean/local")
@SecurityDomain(LawyerConstants.SECURITY_DOMAIN)
public class LawyerSearchBean extends AbstractLawyerDataClientBean implements
		LawyerSearch {

	@Logger
	private Log log;

	@In(create = true)
	FacesMessages facesMessages;

	@In("name")
	@Out(scope = ScopeType.SESSION)
	private String name;

	@SuppressWarnings("unused")
	@Out(value = "lawyerEditableStatus", required = false, scope = ScopeType.SESSION)
	private LawyerStatus lawyerStatus;

	@RolesAllowed(LawyerConstants.ADMIN_ROLE)
	public String search() {
		log.debug("search: " + this.name);
		LawyerStatus lawyerStatus = getLawyerStatus(this.name);
		if (null == lawyerStatus) {
			return null;
		}
		this.lawyerStatus = lawyerStatus;
		return "success";
	}
}
