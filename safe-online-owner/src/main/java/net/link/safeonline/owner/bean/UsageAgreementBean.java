/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.owner.bean;

import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementTextEntity;
import net.link.safeonline.owner.OwnerConstants;
import net.link.safeonline.owner.UsageAgreement;

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

@Stateful
@Name("ownerUsageAgreement")
@LocalBinding(jndiBinding = OwnerConstants.JNDI_PREFIX
		+ "UsageAgreementBean/local")
@SecurityDomain(OwnerConstants.SAFE_ONLINE_OWNER_SECURITY_DOMAIN)
public class UsageAgreementBean implements UsageAgreement {

	private static final Log LOG = LogFactory.getLog(UsageAgreementBean.class);

	@EJB
	private ApplicationService applicationService;

	@In(value = "selectedUsageAgreement", required = true)
	@Out(value = "selectedUsageAgreement", required = true, scope = ScopeType.SESSION)
	private UsageAgreementEntity usageAgreement;

	@SuppressWarnings("unused")
	@DataModel(value = "usageAgreementTextList")
	private Set<UsageAgreementTextEntity> usageAgreementTextList;

	@DataModelSelection(value = "usageAgreementTextList")
	@Out(value = "selectedUsageAgreementText", required = false, scope = ScopeType.PAGE)
	private UsageAgreementTextEntity selectedUsageAgreementText;

	@Factory(value = "usageAgreementTextList")
	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public void usageAgreementTextListFactory() {
		this.usageAgreementTextList = this.usageAgreement
				.getUsageAgreementTexts();
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String viewText() {
		LOG.debug("view usage agreement text: language="
				+ this.selectedUsageAgreementText.getLanguage());
		return "viewtext";
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String getText() {
		String usageAgreementText = this.selectedUsageAgreementText.getText();
		usageAgreementText = usageAgreementText.replaceAll("\n", "<br>");
		usageAgreementText = usageAgreementText.replaceAll("\r", "<br>");
		return usageAgreementText;
	}

	@Remove
	@Destroy
	public void destroyCallback() {
	}

}
