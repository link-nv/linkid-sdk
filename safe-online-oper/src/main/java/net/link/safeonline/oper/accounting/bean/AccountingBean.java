/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.accounting.bean;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.accounting.Accounting;
import net.link.safeonline.service.StatisticService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


@Stateful
@Name("operAccounting")
@LocalBinding(jndiBinding = Accounting.JNDI_BINDING)
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class AccountingBean implements Accounting {

    public static final String      ACCOUNTING_APPLICATION_LIST_NAME = "accountingApplications";

    public static final String      ACCOUNTING_STAT_LIST_NAME        = "accountingStats";

    @Logger
    private Log                     log;

    @In(create = true)
    FacesMessages                   facesMessages;

    @EJB
    private ApplicationService      applicationService;

    @EJB
    private StatisticService        statisticService;

    @SuppressWarnings("unused")
    @DataModel(ACCOUNTING_APPLICATION_LIST_NAME)
    private List<ApplicationEntity> applicationList;

    @DataModelSelection(ACCOUNTING_APPLICATION_LIST_NAME)
    @Out(value = "selectedApplication", required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private ApplicationEntity       selectedApplication;

    @SuppressWarnings("unused")
    @DataModel(ACCOUNTING_STAT_LIST_NAME)
    private List<StatisticEntity>   statList;

    @DataModelSelection(ACCOUNTING_STAT_LIST_NAME)
    @Out(value = "selectedStat", required = false)
    private StatisticEntity         selectedStat;

    @SuppressWarnings("unused")
    @Out(value = "chartURL", required = false)
    private String                  chartURL;


    @Remove
    @Destroy
    public void destroyCallback() {

        this.log.debug("destroy: " + this);
    }

    @PostConstruct
    public void postConstructCallback() {

        this.log.debug("postConstruct: " + this);
    }

    @Factory(ACCOUNTING_APPLICATION_LIST_NAME)
    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void applicationListFactory() {

        this.log.debug("application list factory");
        this.applicationList = this.applicationService.listApplications();
    }

    @Factory(ACCOUNTING_STAT_LIST_NAME)
    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void statListFactory() throws PermissionDeniedException {

        this.log.debug("selectedApplication: " + this.selectedApplication);
        this.statList = this.statisticService.getStatistics(this.selectedApplication);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public String view() {

        this.log.debug("view accounting information for application: " + this.selectedApplication.getName());
        return "view";
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public String viewStat() {

        this.chartURL = "view.chart?chartname=" + this.selectedStat.getName() + "&domain=" + this.selectedStat.getDomain()
                + "&applicationname=" + this.selectedApplication.getName();
        return "viewstat";

    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public String export() throws IOException {

        DateTime dt = new DateTime();
        DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MM-yyyy_HHmmss");
        String filename = "accounting_" + this.selectedApplication.getName() + "_" + dt.toString(fmt) + ".xls";

        String exportURL = filename + "?applicationname=" + this.selectedApplication.getName();

        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();

        externalContext.redirect(exportURL);
        return null;
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public String exportStat() throws IOException {

        DateTime dt = new DateTime();
        DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MM-yyyy_HHmmss");
        String filename = "accounting_" + this.selectedApplication.getName() + "_" + this.selectedStat.getName() + "_" + dt.toString(fmt)
                + ".xls";

        String exportURL = filename + "?chartname=" + this.selectedStat.getName() + "&domain=" + this.selectedStat.getDomain()
                + "&applicationname=" + this.selectedApplication.getName();

        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();

        externalContext.redirect(exportURL);
        return null;
    }
}
