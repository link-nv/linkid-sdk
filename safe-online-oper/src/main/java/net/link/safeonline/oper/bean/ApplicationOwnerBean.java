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
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.authentication.exception.ExistingApplicationAdminException;
import net.link.safeonline.authentication.exception.ExistingApplicationOwnerException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.ctrl.error.annotation.Error;
import net.link.safeonline.ctrl.error.annotation.ErrorHandling;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.oper.ApplicationOwner;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.service.SubjectService;

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
@Name("applicationOwner")
@LocalBinding(jndiBinding = ApplicationOwner.JNDI_BINDING)
@Interceptors(ErrorMessageInterceptor.class)
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class ApplicationOwnerBean implements ApplicationOwner {

    private static final Log              LOG                         = LogFactory.getLog(ApplicationOwnerBean.class);

    private static final String           APPLICATION_OWNER_LIST_NAME = "applicationOwnerList";

    private static final String           APPLICATION_LIST_NAME       = "applicationList";

    @EJB(mappedName = ApplicationService.JNDI_BINDING)
    private ApplicationService            applicationService;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    protected SubjectService              subjectService;

    @DataModel(APPLICATION_OWNER_LIST_NAME)
    private List<ApplicationOwnerWrapper> applicationOwnerList;

    @DataModelSelection(APPLICATION_OWNER_LIST_NAME)
    @Out(value = "selectedApplicationOwner", required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private ApplicationOwnerWrapper       selectedApplicationOwner;

    @SuppressWarnings("unused")
    @DataModel(APPLICATION_LIST_NAME)
    private List<ApplicationEntity>       applicationList;

    @DataModelSelection(APPLICATION_LIST_NAME)
    @Out(value = "selectedApplication", required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private ApplicationEntity             selectedApplication;

    @In(create = true)
    FacesMessages                         facesMessages;

    private String                        login;

    private String                        name;


    public String getLogin() {

        return login;
    }

    public void setLogin(String login) {

        this.login = login;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @ErrorHandling( {
            @Error(exceptionClass = SubjectNotFoundException.class, messageId = "errorSubjectNotFound", fieldId = "login"),
            @Error(exceptionClass = ExistingApplicationOwnerException.class, messageId = "errorApplicationOwnerAlreadyExists", fieldId = "name"),
            @Error(exceptionClass = ExistingApplicationAdminException.class, messageId = "errorApplicationAdminAlreadyExists", fieldId = "login") })
    public String add()
            throws SubjectNotFoundException, ExistingApplicationOwnerException, ExistingApplicationAdminException {

        LOG.debug("add");
        applicationService.registerApplicationOwner(name, login);
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String remove()
            throws SubscriptionNotFoundException, SubjectNotFoundException, ApplicationOwnerNotFoundException, PermissionDeniedException {

        LOG.debug("remove");
        applicationService.removeApplicationOwner(selectedApplicationOwner.getEntity().getName(),
                selectedApplicationOwner.getAdminName());

        applicationOwnerListFactory();
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String view() {

        LOG.debug("view owner: " + selectedApplicationOwner.getAdminName());
        return "view-owner";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String viewapp() {

        LOG.debug("view owner app: " + selectedApplication.getName());
        return "view-app";
    }

    @Factory(APPLICATION_OWNER_LIST_NAME)
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void applicationOwnerListFactory() {

        LOG.debug("application owner list factory");
        List<ApplicationOwnerEntity> applicationOwnerEntityList = applicationService.listApplicationOwners();
        applicationOwnerList = new LinkedList<ApplicationOwnerWrapper>();
        for (ApplicationOwnerEntity applicationOwnerEntity : applicationOwnerEntityList) {
            applicationOwnerList.add(new ApplicationOwnerWrapper(applicationOwnerEntity));
        }
    }

    @Factory(APPLICATION_LIST_NAME)
    public void applicationListFactory() {

        if (null == selectedApplicationOwner)
            return;
        LOG.debug("application list factory for owner=" + selectedApplicationOwner.getEntity().getName());
        applicationList = selectedApplicationOwner.getEntity().getApplications();
    }

    @Remove
    @Destroy
    public void destroyCallback() {

    }


    public class ApplicationOwnerWrapper {

        private String                 adminName;

        private ApplicationOwnerEntity entity;


        public ApplicationOwnerWrapper(ApplicationOwnerEntity entity) {

            this.entity = entity;
            adminName = subjectService.getSubjectLogin(this.entity.getAdmin().getUserId());
        }

        public String getAdminName() {

            return adminName;
        }

        public ApplicationOwnerEntity getEntity() {

            return entity;
        }
    }
}
