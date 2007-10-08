/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.pkix.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;

import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.pkix.TrustDomain;
import net.link.safeonline.pkix.exception.ExistingTrustDomainException;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.service.PkiService;

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
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX
		+ "TrustDomainBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class TrustDomainBean implements TrustDomain {

	private static final Log LOG = LogFactory.getLog(TrustDomainBean.class);

	@In(required = false)
	private TrustDomainEntity newTrustDomain;

	@In(create = true)
	FacesMessages facesMessages;

	@DataModel
	@SuppressWarnings("unused")
	private List<TrustDomainEntity> trustDomainList;

	@EJB
	private PkiService pkiService;

	@DataModelSelection("trustDomainList")
	@Out(value = "selectedTrustDomain", required = false, scope = ScopeType.SESSION)
	@In(required = false)
	private TrustDomainEntity selectedTrustDomain;

	@Factory("trustDomainList")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void trustDomainListFactory() {
		LOG.debug("application list factory");
		this.trustDomainList = this.pkiService.listTrustDomains();
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String add() {
		LOG.debug("add trust domain");
		try {
			/*
			 * this.pkiService.addTrustDomain(this.name, this.performOcspCheck,
			 * this.ocspCacheTimeOutMillis);
			 */
			String name = this.newTrustDomain.getName();
			boolean performOcspCheck = this.newTrustDomain.isPerformOcspCheck();
			long ocspCacheTimeOutMillis = this.newTrustDomain
					.getOcspCacheTimeOutMillis();
			this.pkiService.addTrustDomain(name, performOcspCheck,
					ocspCacheTimeOutMillis);
		} catch (ExistingTrustDomainException e) {
			String msg = "existing trust domain";
			LOG.debug(msg);
			this.facesMessages.addToControlFromResourceBundle("name",
					FacesMessage.SEVERITY_ERROR,
					"errorTrustDomainAlreadyExists");
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
	public String view() {
		LOG.debug("view selected trust domain: " + this.selectedTrustDomain);
		return "view";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String removeTrustDomain() {
		LOG.debug("remove trust domain: " + this.selectedTrustDomain);
		try {
			this.pkiService.removeTrustDomain(this.selectedTrustDomain
					.getName());
		} catch (TrustDomainNotFoundException e) {
			String msg = "trust domain not found";
			LOG.debug(msg);
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorTrustDomainNotFound");
			return null;
		}
		trustDomainListFactory();
		return "removed";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String clearOcspCache() {
		LOG.debug("Clearing OCSP cache for all trust domains");
		this.pkiService.clearOcspCache();
		return "clear-cache";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String clearOcspCachePerTrustDomain() {
		LOG.debug("Clearing OCSP cache for trust domain: "
				+ this.selectedTrustDomain.getName());
		this.pkiService.clearOcspCachePerTrustDomain(this.selectedTrustDomain);
		return "clear-cache";
	}

	@Factory
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public TrustDomainEntity getNewTrustDomain() {
		return new TrustDomainEntity();
	}
}
