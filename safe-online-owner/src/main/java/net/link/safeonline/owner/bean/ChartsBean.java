/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.owner.bean;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.owner.Charts;
import net.link.safeonline.owner.OwnerConstants;
import net.link.safeonline.service.StatisticService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("chart")
@LocalBinding(jndiBinding = OwnerConstants.JNDI_PREFIX + "ChartsBean/local")
@SecurityDomain(OwnerConstants.SAFE_ONLINE_OWNER_SECURITY_DOMAIN)
public class ChartsBean implements Charts {

	private static final Log LOG = LogFactory.getLog(ChartsBean.class);

	@EJB
	private StatisticService statisticService;

	@In(create = true)
	FacesMessages facesMessages;

	@In(value = "selectedApplication")
	private ApplicationEntity selectedApplication;

	@SuppressWarnings("unused")
	@Out(value = "chartURL", required = false)
	private String chartURL;

	public static final String STAT_LIST_NAME = "statList";

	@SuppressWarnings("unused")
	@DataModel(STAT_LIST_NAME)
	private List<StatisticEntity> statList;

	@DataModelSelection(STAT_LIST_NAME)
	@Out(value = "selectedStat", required = false)
	private StatisticEntity selectedStat;

	@Remove
	@Destroy
	public void destroyCallback() {
	}

	@Factory(STAT_LIST_NAME)
	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public void statListFactory() {
		LOG.debug("selectedApplication: " + this.selectedApplication);
		try {
			this.statList = this.statisticService
					.getStatistics(this.selectedApplication);
		} catch (PermissionDeniedException e) {
			LOG.error("permission denied: " + e.getMessage());
			this.facesMessages.add("permission denied");
			this.statList = new LinkedList<StatisticEntity>();
		}
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String viewStat() {
		this.chartURL = "view.chart?chartname=" + this.selectedStat.getName()
				+ "&domain=" + this.selectedStat.getDomain()
				+ "&applicationname=" + this.selectedApplication.getName();
		return "viewstat";
	}
}
