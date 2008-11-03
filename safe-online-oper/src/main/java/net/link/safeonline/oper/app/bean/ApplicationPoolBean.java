/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.app.bean;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationPoolNotFoundException;
import net.link.safeonline.authentication.exception.ExistingApplicationPoolException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ApplicationPoolService;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.app.ApplicationPool;
import net.link.safeonline.oper.app.ApplicationPoolSelection;

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
import org.jboss.seam.faces.FacesMessages;


@Stateful
@Name("operApplicationPool")
@LocalBinding(jndiBinding = ApplicationPool.JNDI_BINDING)
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class ApplicationPoolBean implements ApplicationPool {

    private static final Log               LOG                                         = LogFactory.getLog(ApplicationPoolBean.class);

    private static final String            OPER_APPLICATION_POOL_LIST_NAME             = "operApplicationPoolList";

    private static final String            OPER_APPLICATION_POOL_APPLICATION_LIST_NAME = "operApplicationPoolApplicationList";

    private static final String            OPER_APPLICATION_POOL_ELEMENTS_NAME         = "operApplicationPoolElements";

    @EJB
    private ApplicationPoolService         applicationPoolService;

    @EJB
    private ApplicationService             applicationService;

    @In(create = true)
    FacesMessages                          facesMessages;

    private String                         name;

    private Long                           ssoTimeout;

    @SuppressWarnings("unused")
    @DataModel(OPER_APPLICATION_POOL_LIST_NAME)
    private List<ApplicationPoolEntity>    operApplicationPoolList;

    @DataModelSelection(OPER_APPLICATION_POOL_LIST_NAME)
    @Out(value = "selectedApplicationPool", required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private ApplicationPoolEntity          selectedApplicationPool;

    @DataModel(OPER_APPLICATION_POOL_APPLICATION_LIST_NAME)
    private List<ApplicationPoolSelection> operApplicationPoolApplicationList;

    @SuppressWarnings("unused")
    @DataModel(OPER_APPLICATION_POOL_ELEMENTS_NAME)
    private List<ApplicationPoolSelection> operApplicationPoolElements;


    /**
     * {@inheritDoc}
     */
    @Remove
    @Destroy
    public void destroyCallback() {

        LOG.debug("destroy");
        this.name = null;
    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(OPER_APPLICATION_POOL_LIST_NAME)
    public void applicationPoolListFactory() {

        LOG.debug("application pool list factory");
        this.operApplicationPoolList = this.applicationPoolService.listApplicationPools();
    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(OPER_APPLICATION_POOL_APPLICATION_LIST_NAME)
    public void applicationPoolApplicationListFactory() {

        LOG.debug("application pool application list factory");
        this.operApplicationPoolApplicationList = new LinkedList<ApplicationPoolSelection>();
        List<ApplicationEntity> applications = this.applicationService.listApplications();
        for (ApplicationEntity application : applications) {
            boolean included = false;
            if (null != this.selectedApplicationPool) {
                if (this.selectedApplicationPool.getApplications().contains(application)) {
                    included = true;
                }
            }
            this.operApplicationPoolApplicationList.add(new ApplicationPoolSelection(application.getName(), included));
        }
    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(OPER_APPLICATION_POOL_ELEMENTS_NAME)
    public void applicationPoolElementsFactory() {

        LOG.debug("application pool list elements factory");
        if (null == this.selectedApplicationPool)
            return;
        this.operApplicationPoolElements = new LinkedList<ApplicationPoolSelection>();
        for (ApplicationEntity application : this.selectedApplicationPool.getApplications()) {
            this.operApplicationPoolElements.add(new ApplicationPoolSelection(application.getName(), true));
        }
    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public List<SelectItem> getApplicationList() {

        List<ApplicationEntity> applications = this.applicationService.listApplications();
        List<SelectItem> applicationList = new LinkedList<SelectItem>();
        for (ApplicationEntity currentApplication : applications) {
            applicationList.add(new SelectItem(currentApplication.getName()));
        }
        return applicationList;
    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getName() {

        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public Long getSsoTimeout() {

        return this.ssoTimeout;
    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setName(String name) {

        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setSsoTimeout(Long ssoTimeout) {

        this.ssoTimeout = ssoTimeout;
    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String remove() throws ApplicationPoolNotFoundException {

        String applicationPoolName = this.selectedApplicationPool.getName();
        LOG.debug("remove application pool: " + applicationPoolName);
        try {
            this.applicationPoolService.removeApplicationPool(applicationPoolName);
        } catch (PermissionDeniedException e) {
            LOG.debug("permission denied to remove: " + applicationPoolName);
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, e.getResourceMessage(), e.getResourceArgs());
            return null;
        }
        applicationPoolListFactory();
        return "success";

    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String save() throws ApplicationPoolNotFoundException, ApplicationNotFoundException {

        String applicationPoolName = this.selectedApplicationPool.getName();
        LOG.debug("save application pool: " + applicationPoolName);

        this.applicationPoolService.setSsoTimeout(applicationPoolName, this.ssoTimeout);

        List<String> applicationList = new LinkedList<String>();
        for (ApplicationPoolSelection application : this.operApplicationPoolApplicationList) {
            if (application.isIncluded()) {
                applicationList.add(application.getName());
            }
        }
        this.applicationPoolService.updateApplicationList(applicationPoolName, applicationList);

        this.selectedApplicationPool = null;

        applicationPoolListFactory();
        return "success";
    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String view() {

        /*
         * To set the selected application pool.
         */
        LOG.debug("view application pool: " + this.selectedApplicationPool.getName());
        return "view";
    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String add() throws ApplicationPoolNotFoundException, ApplicationNotFoundException {

        LOG.debug("add application pool: " + this.name);

        List<String> applicationList = new LinkedList<String>();
        for (ApplicationPoolSelection application : this.operApplicationPoolApplicationList) {
            if (application.isIncluded()) {
                applicationList.add(application.getName());
            }
        }

        try {
            this.applicationPoolService.addApplicationPool(this.name, this.ssoTimeout, applicationList);

        } catch (ExistingApplicationPoolException e) {
            LOG.debug("application pool already exists: " + this.name);
            this.facesMessages.addToControlFromResourceBundle("name", FacesMessage.SEVERITY_ERROR, "errorApplicationPoolAlreadyExists",
                    this.name);
            return null;
        }

        this.selectedApplicationPool = null;

        applicationPoolListFactory();
        return "success";
    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String edit() {

        /*
         * To set the selected application.
         */
        LOG.debug("edit application pool: " + this.selectedApplicationPool.getName());

        this.ssoTimeout = this.selectedApplicationPool.getSsoTimeout();

        return "edit";
    }
}
