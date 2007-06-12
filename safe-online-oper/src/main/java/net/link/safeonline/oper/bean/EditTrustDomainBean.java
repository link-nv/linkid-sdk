/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.oper.EditTrustDomain;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.service.PkiService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("editTrustDomain")
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX
		+ "EditTrustDomainBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class EditTrustDomainBean implements EditTrustDomain {

	private static final Log LOG = LogFactory.getLog(EditTrustDomainBean.class);

	private boolean performOcspCheck;

	private long ocspCacheTimeOutMillis;

	@In(create = true)
	FacesMessages facesMessages;

	@In(value = "selectedTrustDomain")
	private TrustDomainEntity selectedTrustDomain;

	@EJB
	private PkiService pkiService;

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	@End
	public String save() {
		LOG.debug("saving " + this.selectedTrustDomain);
		this.selectedTrustDomain.setPerformOcspCheck(this.performOcspCheck);
		this.selectedTrustDomain.setOcspCacheTimeOutMillis(this.ocspCacheTimeOutMillis);
		try {
			this.pkiService.saveTrustDomain(this.selectedTrustDomain);
		} catch (TrustDomainNotFoundException e) {
			String msg = "trust domain not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}
		return "success";
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		LOG.debug("destroy");
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public boolean isPerformOcspCheck() {
		return this.performOcspCheck;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setPerformOcspCheck(boolean performOcspCheck) {
		this.performOcspCheck = performOcspCheck;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public long getOcspCacheTimeOutMillis() {
		return this.ocspCacheTimeOutMillis;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setOcspCacheTimeOutMillis(long ocspCacheTimeOutMillis) {
		this.ocspCacheTimeOutMillis = ocspCacheTimeOutMillis;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	@Begin
	public String edit() {
		LOG.debug("view selected trust domain: " + this.selectedTrustDomain);
		this.performOcspCheck = this.selectedTrustDomain.isPerformOcspCheck();
		this.ocspCacheTimeOutMillis = this.selectedTrustDomain.getOcspCacheTimeOutMillis();
		return "edit";
	}

	@End
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String cancel() {
		LOG.debug("cancel");
		return "canceled";
	}
}
