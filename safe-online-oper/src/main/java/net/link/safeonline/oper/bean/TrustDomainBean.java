/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.authentication.exception.ExistingTrustDomainException;
import net.link.safeonline.authentication.exception.TrustDomainNotFoundException;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.TrustDomain;
import net.link.safeonline.service.PkiService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("trustDomain")
@LocalBinding(jndiBinding = "SafeOnline/oper/TrustDomainBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class TrustDomainBean implements TrustDomain {

	private static final Log LOG = LogFactory.getLog(TrustDomainBean.class);

	private String name;

	@In(create = true)
	FacesMessages facesMessages;

	@DataModel
	@SuppressWarnings("unused")
	private List<TrustDomainEntity> trustDomainList;

	@EJB
	private PkiService pkiService;

	@DataModelSelection("trustDomainList")
	@Out(value = "selectedTrustDomain", required = false, scope = ScopeType.SESSION)
	private TrustDomainEntity selectedtrustDomain;

	@Factory("trustDomainList")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void trustDomainListFactory() {
		LOG.debug("application list factory");
		this.trustDomainList = this.pkiService.getTrustDomains();
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String add() {
		LOG.debug("add trust domain: " + this.name);
		try {
			this.pkiService.addTrustDomain(this.name);
		} catch (ExistingTrustDomainException e) {
			String msg = "existing trust domain";
			LOG.debug(msg);
			this.facesMessages.add("name", msg);
			return null;
		}
		return "success";
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		// empty
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String getName() {
		return this.name;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setName(String name) {
		this.name = name;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String view() {
		LOG.debug("view selected trust domain: " + this.selectedtrustDomain);
		return "view";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String removeTrustDomain() {
		LOG.debug("remove trust domain: " + this.selectedtrustDomain);
		try {
			this.pkiService.removeTrustDomain(this.selectedtrustDomain
					.getName());
		} catch (TrustDomainNotFoundException e) {
			String msg = "trust domain not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}
		trustDomainListFactory();
		return "removed";
	}
}
