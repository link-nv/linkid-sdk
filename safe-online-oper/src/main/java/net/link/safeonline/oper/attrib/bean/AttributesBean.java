/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.attrib.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.AttributeTypeDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.attrib.Attributes;
import net.link.safeonline.service.AttributeTypeService;

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
@Name("attributes")
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX + "AttributesBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class AttributesBean implements Attributes {

    private static final Log          LOG                = LogFactory.getLog(AttributesBean.class);

    @In(create = true)
    FacesMessages                     facesMessages;

    @EJB
    private AttributeTypeService      attributeTypeService;

    private String                    pluginConfiguration;

    @SuppressWarnings("unused")
    @DataModel("attributeTypeList")
    private List<AttributeTypeEntity> attributeTypeList;

    @DataModelSelection("attributeTypeList")
    @Out(value = "selectedAttributeType", required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private AttributeTypeEntity       selectedAttributeType;

    public static final String        NEW_ATTRIBUTE_TYPE = "newAttributeType";


    @Factory("attributeTypeList")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void attributeTypeListFactory() {

        LOG.debug("attributeTypeListFactory");
        this.attributeTypeList = this.attributeTypeService.listAttributeTypes();
    }

    @Remove
    @Destroy
    public void destroyCallback() {

        LOG.debug("destroy");
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String view() {

        LOG.debug("view: " + this.selectedAttributeType.getName());

        this.pluginConfiguration = this.selectedAttributeType.getPluginConfiguration();

        return "view";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String remove() {

        LOG.debug("remove: " + this.selectedAttributeType.getName());
        return "remove";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String removeConfirm() throws AttributeTypeDescriptionNotFoundException, PermissionDeniedException,
            AttributeTypeNotFoundException {

        LOG.debug("confirm remove: " + this.selectedAttributeType.getName());
        this.attributeTypeService.remove(this.selectedAttributeType);
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getPluginConfiguration() {

        return this.pluginConfiguration;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setPluginConfiguration(String pluginConfiguration) {

        this.pluginConfiguration = pluginConfiguration;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String savePlugin() throws AttributeTypeNotFoundException {

        this.attributeTypeService.savePluginConfiguration(this.selectedAttributeType.getName(),
                this.pluginConfiguration);
        return null;
    }

}
