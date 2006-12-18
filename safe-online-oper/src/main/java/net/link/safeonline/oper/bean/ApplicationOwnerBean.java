/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ExistingApplicationOwnerException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.oper.ApplicationOwner;
import net.link.safeonline.oper.OperatorConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("applicationOwner")
@LocalBinding(jndiBinding = "SafeOnline/oper/ApplicationOwnerBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class ApplicationOwnerBean implements ApplicationOwner {

	private static final Log LOG = LogFactory
			.getLog(ApplicationOwnerBean.class);

	@EJB
	private ApplicationService applicationService;

	@SuppressWarnings("unused")
	@DataModel("applicationOwnerList")
	private List<ApplicationOwnerEntity> applicationOwnerList;

	@In(create = true)
	FacesMessages facesMessages;

	private String login;

	private String name;

	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String add() {
		LOG.debug("add");
		try {
			this.applicationService.registerApplicationOwner(this.name,
					this.login);
		} catch (SubjectNotFoundException e) {
			String msg = "subject not found";
			LOG.debug(msg);
			this.facesMessages.add("login", msg);
			return null;
		} catch (ApplicationNotFoundException e) {
			String msg = "application not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (ExistingApplicationOwnerException e) {
			String msg = "application owner already exists";
			LOG.debug(msg);
			this.facesMessages.add("name", msg);
			return null;
		}
		return "success";
	}

	@Factory("applicationOwnerList")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void applicationOwnerListFactory() {
		LOG.debug("application owner list factory");
		this.applicationOwnerList = this.applicationService
				.getApplicationOwners();
	}

	@Remove
	@Destroy
	public void destroyCallback() {
	}
}
