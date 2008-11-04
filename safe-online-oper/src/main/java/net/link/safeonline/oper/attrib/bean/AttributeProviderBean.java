package net.link.safeonline.oper.attrib.bean;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.model.SelectItem;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeProviderNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingAttributeProviderException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.AttributeProviderManagerService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.AttributeProviderEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.attrib.AttributeProvider;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;


@Stateful
@Name("attributeProvider")
@LocalBinding(jndiBinding = AttributeProvider.JNDI_BINDING)
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class AttributeProviderBean implements AttributeProvider {

    public static final String              ATTRIBUTE_PROVIDERS_NAME = "attributeProviders";

    @Logger
    private Log                             log;

    @EJB
    private AttributeProviderManagerService attributeProviderManagerService;

    @EJB
    private ApplicationService              applicationService;

    @SuppressWarnings("unused")
    @DataModel(ATTRIBUTE_PROVIDERS_NAME)
    private List<AttributeProviderEntity>   attributeProviders;

    @DataModelSelection(ATTRIBUTE_PROVIDERS_NAME)
    private AttributeProviderEntity         selectedAttributeProvider;

    @In(value = "selectedAttributeType")
    private AttributeTypeEntity             selectedAttributeType;

    @In(create = true)
    FacesMessages                           facesMessages;

    private String                          application;


    @Factory(ATTRIBUTE_PROVIDERS_NAME)
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void attributeProvidersFactory()
            throws AttributeTypeNotFoundException {

        String attributeName = this.selectedAttributeType.getName();
        this.attributeProviders = this.attributeProviderManagerService.getAttributeProviders(attributeName);
    }

    @Remove
    @Destroy
    public void destroyCallback() {

        // empty
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String removeProvider()
            throws AttributeTypeNotFoundException, AttributeProviderNotFoundException {

        this.log.debug("removing attribute provider #0", this.selectedAttributeProvider);
        this.attributeProviderManagerService.removeAttributeProvider(this.selectedAttributeProvider);
        attributeProvidersFactory();
        return "provider-removed";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String add()
            throws ExistingAttributeProviderException, ApplicationNotFoundException, AttributeTypeNotFoundException,
            PermissionDeniedException {

        this.log.debug("add application provider: " + this.application);
        this.attributeProviderManagerService.addAttributeProvider(this.application, this.selectedAttributeType.getName());
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public List<SelectItem> getApplicationList() {

        List<ApplicationEntity> applications = this.applicationService.listApplications();
        List<SelectItem> applicationList = new LinkedList<SelectItem>();
        for (ApplicationEntity currentApplication : applications) {
            applicationList.add(new SelectItem(currentApplication.getName()));
        }
        return applicationList;
    }

    public String getApplication() {

        return this.application;
    }

    public void setApplication(String application) {

        this.application = application;
    }
}
