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
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionPK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.attrib.AttributeDescription;
import net.link.safeonline.service.AttributeTypeService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.faces.FacesMessages;


@Stateful
@Name("attributeDesc")
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX + "AttributeDescriptionBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class AttributeDescriptionBean implements AttributeDescription {

    private static final Log                     LOG                                  = LogFactory
                                                                                              .getLog(AttributeDescriptionBean.class);

    public static final String                   ATTRIBUTE_TYPE_DESCRIPTION_NAME      = "attributeTypeDescriptions";

    @EJB
    private AttributeTypeService                 attributeTypeService;

    @In(value = "selectedAttributeType", required = true)
    private AttributeTypeEntity                  selectedAttributeType;

    @In(create = true)
    FacesMessages                                facesMessages;

    public static final String                   NEW_ATTRIBUTE_TYPE_DESCIPTION_NAME   = "newAttributeTypeDescription";

    @In(value = NEW_ATTRIBUTE_TYPE_DESCIPTION_NAME, required = false)
    private AttributeTypeDescriptionEntity       newAttributeTypeDescription;

    @SuppressWarnings("unused")
    @DataModel(ATTRIBUTE_TYPE_DESCRIPTION_NAME)
    private List<AttributeTypeDescriptionEntity> attributeTypeDescriptions;

    public static final String                   EDIT_ATTRIBUTE_TYPE_DESCRIPTION_NAME = "editAttributeTypeDescription";

    @DataModelSelection(ATTRIBUTE_TYPE_DESCRIPTION_NAME)
    @Out(value = EDIT_ATTRIBUTE_TYPE_DESCRIPTION_NAME, required = false, scope = ScopeType.CONVERSATION)
    @In(value = EDIT_ATTRIBUTE_TYPE_DESCRIPTION_NAME, required = false)
    private AttributeTypeDescriptionEntity       selectedAttributeTypeDescription;


    @Factory(ATTRIBUTE_TYPE_DESCRIPTION_NAME)
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void attributeTypeDescriptionsFactory() throws AttributeTypeNotFoundException {

        String attributeTypeName = this.selectedAttributeType.getName();
        LOG.debug("attrib type descr factory: " + attributeTypeName);
        this.attributeTypeDescriptions = this.attributeTypeService.listDescriptions(attributeTypeName);
    }

    @Remove
    @Destroy
    public void destroyCallback() {

    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Begin
    public String edit() {

        LOG.debug("edit attribute type description: " + this.selectedAttributeTypeDescription.getLanguage());
        return "edit";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(NEW_ATTRIBUTE_TYPE_DESCIPTION_NAME)
    public AttributeTypeDescriptionEntity newAttributeTypeDescriptionFactory() {

        AttributeTypeDescriptionEntity tempNewAttributeTypeDescription = new AttributeTypeDescriptionEntity();
        return tempNewAttributeTypeDescription;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String add() throws AttributeTypeNotFoundException {

        LOG.debug("add: " + this.newAttributeTypeDescription);
        String attributeTypeName = this.selectedAttributeType.getName();
        String language = this.newAttributeTypeDescription.getLanguage();
        AttributeTypeDescriptionPK pk = new AttributeTypeDescriptionPK(attributeTypeName, language);
        this.newAttributeTypeDescription.setPk(pk);
        this.attributeTypeService.addDescription(this.newAttributeTypeDescription);
        return "success";
    }

    @End
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String removeDescription() throws AttributeTypeDescriptionNotFoundException, AttributeTypeNotFoundException {

        LOG.debug("remove: " + this.selectedAttributeTypeDescription);
        this.attributeTypeService.removeDescription(this.selectedAttributeTypeDescription);
        attributeTypeDescriptionsFactory();
        return "removed";
    }

    @End
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String save() {

        LOG.debug("save: " + this.selectedAttributeTypeDescription);
        this.attributeTypeService.saveDescription(this.selectedAttributeTypeDescription);
        return "saved";
    }

    @End
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String cancelEdit() {

        return "cancel";
    }
}
