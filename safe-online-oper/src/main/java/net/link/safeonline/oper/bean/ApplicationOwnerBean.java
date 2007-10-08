/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.bean;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;

import net.link.safeonline.authentication.exception.ExistingApplicationOwnerException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.oper.ApplicationOwner;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.service.SubjectService;

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
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX
		+ "ApplicationOwnerBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class ApplicationOwnerBean implements ApplicationOwner {

	private static final Log LOG = LogFactory
			.getLog(ApplicationOwnerBean.class);

	@EJB
	private ApplicationService applicationService;

	@EJB
	private SubjectService subjectService;

	@SuppressWarnings("unused")
	@DataModel("applicationOwnerList")
	private List<ApplicationOwnerWrapper> applicationOwnerList;

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
			this.facesMessages.addToControlFromResourceBundle("login",
					FacesMessage.SEVERITY_ERROR, "errorSubjectNotFound");
			return null;
		} catch (ExistingApplicationOwnerException e) {
			String msg = "application owner already exists";
			LOG.debug(msg);
			this.facesMessages.addToControlFromResourceBundle("name",
					FacesMessage.SEVERITY_ERROR,
					"errorApplicationOwnerAlreadyExists");
			return null;
		}
		return "success";
	}

	@Factory("applicationOwnerList")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void applicationOwnerListFactory() {
		LOG.debug("application owner list factory");
		List<ApplicationOwnerEntity> applicationOwnerEntityList = this.applicationService
				.listApplicationOwners();
		this.applicationOwnerList = new LinkedList<ApplicationOwnerWrapper>();
		for (ApplicationOwnerEntity applicationOwnerEntity : applicationOwnerEntityList) {
			this.applicationOwnerList.add(new ApplicationOwnerWrapper(
					applicationOwnerEntity));
		}
	}

	@Remove
	@Destroy
	public void destroyCallback() {
	}

	public class ApplicationOwnerWrapper {

		private String adminName;

		private ApplicationOwnerEntity entity;

		public ApplicationOwnerWrapper(ApplicationOwnerEntity entity) {
			this.entity = entity;
			this.adminName = subjectService.getSubjectLogin(this.entity
					.getAdmin().getUserId());
		}

		public String getAdminName() {
			return this.adminName;
		}

		public ApplicationOwnerEntity getEntity() {
			return this.entity;
		}
	}
}
