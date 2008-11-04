/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationPoolNotFoundException;
import net.link.safeonline.authentication.exception.ExistingApplicationPoolException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ApplicationPoolService;
import net.link.safeonline.authentication.service.ApplicationPoolServiceRemote;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationPoolDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;


/**
 * Implementation of application pool service interface.
 * 
 * @author wvdhaute
 * 
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@LocalBinding(jndiBinding = ApplicationPoolService.JNDI_BINDING)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class ApplicationPoolServiceBean implements ApplicationPoolService, ApplicationPoolServiceRemote {

    private static final Log   LOG = LogFactory.getLog(ApplicationPoolServiceBean.class);

    @EJB
    private ApplicationPoolDAO applicationPoolDAO;

    @EJB
    private ApplicationDAO     applicationDAO;


    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<ApplicationPoolEntity> listApplicationPools() {

        return this.applicationPoolDAO.listApplicationPools();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public ApplicationPoolEntity addApplicationPool(String name, Long ssoTimeout, List<String> applicationNameList)
                                                                                                                   throws ExistingApplicationPoolException,
                                                                                                                   ApplicationNotFoundException {

        LOG.debug("add application pool: " + name);
        checkExistingApplicationPool(name);

        List<ApplicationEntity> applicationList = new LinkedList<ApplicationEntity>();
        for (String applicationName : applicationNameList) {
            ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
            applicationList.add(application);
        }

        ApplicationPoolEntity applicationPool = this.applicationPoolDAO.addApplicationPool(name, ssoTimeout);
        applicationPool.setApplications(applicationList);
        return applicationPool;
    }

    private void checkExistingApplicationPool(String name) throws ExistingApplicationPoolException {

        ApplicationPoolEntity existingApplicationPool = this.applicationPoolDAO.findApplicationPool(name);
        if (null != existingApplicationPool)
            throw new ExistingApplicationPoolException();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void removeApplicationPool(String name) throws ApplicationPoolNotFoundException, PermissionDeniedException {

        LOG.debug("remove application pool: " + name);
        ApplicationPoolEntity applicationPool = this.applicationPoolDAO.getApplicationPool(name);

        /*
         * TODO: Remove the application pools from the applications
         */
        this.applicationPoolDAO.removeApplicationPool(applicationPool);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public ApplicationPoolEntity getApplicationPool(String applicationPoolName) throws ApplicationPoolNotFoundException {

        return this.applicationPoolDAO.getApplicationPool(applicationPoolName);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void setSsoTimeout(String applicationPoolName, Long ssoTimeout) throws ApplicationPoolNotFoundException {

        LOG.debug("set sso timeout for application pool " + applicationPoolName + " to " + ssoTimeout);
        ApplicationPoolEntity applicationPool = this.applicationPoolDAO.getApplicationPool(applicationPoolName);
        applicationPool.setSsoTimeout(ssoTimeout);

    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateApplicationList(String applicationPoolName, List<String> applicationNameList)
                                                                                                   throws ApplicationPoolNotFoundException,
                                                                                                   ApplicationNotFoundException {

        LOG.debug("update application list for application pool: " + applicationPoolName);
        ApplicationPoolEntity applicationPool = this.applicationPoolDAO.getApplicationPool(applicationPoolName);
        List<ApplicationEntity> applicationList = new LinkedList<ApplicationEntity>();
        for (String applicationName : applicationNameList) {
            ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
            applicationList.add(application);
        }
        applicationPool.setApplications(applicationList);
    }
}
