/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.attrib.bean;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.model.SelectItem;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.AttributeTypeDefinitionException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingAttributeTypeException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.service.NodeService;
import net.link.safeonline.ctrl.Convertor;
import net.link.safeonline.ctrl.ConvertorUtil;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.ctrl.error.annotation.Error;
import net.link.safeonline.ctrl.error.annotation.ErrorHandling;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.attrib.AddAttribute;
import net.link.safeonline.osgi.OSGIStartable;
import net.link.safeonline.service.AttributeTypeService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;


@Stateful
@Name("addAttribute")
@Scope(ScopeType.CONVERSATION)
@LocalBinding(jndiBinding = AddAttribute.JNDI_BINDING)
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class AddAttributeBean implements AddAttribute {

    private static final String       singleValuedType = "singleValued";
    private static final String       multiValuedType  = "multiValued";
    private static final String       compoundType     = "compounded";

    private static final String       olasAttribute    = "olas";
    private static final String       pluginAttribute  = "external";

    @Logger
    private Log                       log;

    @EJB(mappedName = AttributeTypeService.JNDI_BINDING)
    private AttributeTypeService      attributeTypeService;

    @EJB(mappedName = NodeService.JNDI_BINDING)
    private NodeService               nodeService;

    @EJB(mappedName = OSGIStartable.JNDI_BINDING)
    private OSGIStartable             osgiStartable;

    @In(create = true)
    FacesMessages                     facesMessages;

    private String                    category;

    private String                    name;

    private String                    node;

    private String                    locationOption;

    private Long                      cacheTimeout     = new Long(0);

    private String                    plugin;

    private String                    pluginConfiguration;

    private String                    type;

    private boolean                   userVisible;

    private boolean                   userEditable;

    private List<AttributeTypeEntity> sourceMemberAttributes;

    private List<AttributeTypeEntity> selectedMemberAttributes;

    @SuppressWarnings("unused")
    @DataModel
    private List<AttributeTypeEntity> attributeTypeList;


    @Remove
    @Destroy
    public void destroyCallback() {

    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getCategory() {

        return this.category;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getName() {

        return this.name;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String next() {

        this.log.debug("next: name #0, category #1", this.name, this.category);
        return this.category;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setCategory(String category) {

        this.category = category;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setName(String name) {

        this.name = name;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getNode() {

        return this.node;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setNode(String node) {

        this.node = node;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getPlugin() {

        return this.plugin;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setPlugin(String plugin) {

        this.plugin = plugin;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getPluginConfiguration() {

        return this.pluginConfiguration;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setPluginConfiguration(String pluginConfiguration) {

        this.pluginConfiguration = pluginConfiguration;
    }

    @Factory("types")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public List<SelectItem> typesFactory() {

        List<SelectItem> types = new LinkedList<SelectItem>();
        types.add(new SelectItem(singleValuedType, "Single-valued"));
        types.add(new SelectItem(multiValuedType, "Multi-valued"));
        types.add(new SelectItem(compoundType, "Compounded"));
        return types;
    }

    @Factory("datatypes")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public List<SelectItem> datatypesFactory() {

        List<SelectItem> datatypes = new LinkedList<SelectItem>();
        for (DatatypeType currentType : DatatypeType.values()) {
            if (false == currentType.isPrimitive()) {
                continue;
            }
            datatypes.add(new SelectItem(currentType.name(), currentType.getFriendlyName()));

        }
        return datatypes;
    }

    @Factory("locations")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public List<SelectItem> locationTypesFactory() {

        List<SelectItem> locations = new LinkedList<SelectItem>();
        locations.add(new SelectItem(olasAttribute, "OLAS"));
        locations.add(new SelectItem(pluginAttribute, "External"));
        return locations;
    }

    @Factory("attributeNodes")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public List<SelectItem> nodeFactory() {

        List<NodeEntity> nodeList = this.nodeService.listNodes();
        List<SelectItem> nodes = ConvertorUtil.convert(nodeList, new OlasEntitySelectItemConvertor());
        return nodes;
    }

    @Factory("attributePlugins")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public List<SelectItem> pluginFactory() {

        List<SelectItem> plugins = new LinkedList<SelectItem>();
        Object[] pluginServices = this.osgiStartable.getPluginServices();
        if (null == pluginServices)
            return plugins;
        for (Object pluginService : pluginServices) {
            plugins.add(new SelectItem(pluginService.getClass().getName()));
        }
        return plugins;
    }


    static class OlasEntitySelectItemConvertor implements Convertor<NodeEntity, SelectItem> {

        public SelectItem convert(NodeEntity input) {

            SelectItem output = new SelectItem(input.getName());
            return output;
        }
    }


    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getType() {

        return this.type;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setType(String type) {

        this.type = type;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String typeNext() {

        this.log.debug("type next");
        return "next";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public boolean isUserVisible() {

        return this.userVisible;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setUserVisible(boolean userVisible) {

        this.userVisible = userVisible;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public boolean isUserEditable() {

        return this.userEditable;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setUserEditable(boolean userEditable) {

        this.userEditable = userEditable;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public List<AttributeTypeEntity> getSourceMemberAttributes() {

        this.sourceMemberAttributes = this.attributeTypeService.listAvailableMemberAttributeTypes();
        if (null != this.selectedMemberAttributes) {
            this.sourceMemberAttributes.removeAll(this.selectedMemberAttributes);
        }
        return this.sourceMemberAttributes;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setSourceMemberAttributes(List<AttributeTypeEntity> sourceMemberAttributes) {

        this.sourceMemberAttributes = sourceMemberAttributes;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public List<AttributeTypeEntity> getTargetMemberAttributes() {

        return this.selectedMemberAttributes;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setTargetMemberAttributes(List<AttributeTypeEntity> targetMemberAttributes) {

        this.selectedMemberAttributes = targetMemberAttributes;
        for (AttributeTypeEntity targetMemberAttribute : this.selectedMemberAttributes) {
            this.log.debug("set target: " + targetMemberAttribute.getName());
        }
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @End
    @ErrorHandling( {
            @Error(exceptionClass = NodeNotFoundException.class, messageId = "errorNodeNotFound", fieldId = "location"),
            @Error(exceptionClass = ExistingAttributeTypeException.class, messageId = "errorAttributeTypeAlreadyExists", fieldId = "name"),
            @Error(exceptionClass = AttributeTypeNotFoundException.class, messageId = "errorAttributeTypeMemberNotFound", fieldId = "name"),
            @Error(exceptionClass = AttributeTypeDefinitionException.class, messageId = "errorAttributeTypeMemberIllegal", fieldId = "name") })
    public String add()
            throws NodeNotFoundException, ExistingAttributeTypeException, AttributeTypeNotFoundException, AttributeTypeDefinitionException {

        this.log.debug("add");

        AttributeTypeEntity attributeType = new AttributeTypeEntity();
        attributeType.setName(this.name);
        if (compoundType.equals(this.category)) {
            attributeType.setMultivalued(true);
            attributeType.setType(DatatypeType.COMPOUNDED);
        } else {
            if (multiValuedType.equals(this.category)) {
                attributeType.setMultivalued(true);
            }
            attributeType.setType(DatatypeType.valueOf(this.type));
        }
        attributeType.setUserEditable(this.userEditable);
        attributeType.setUserVisible(this.userVisible);

        if (this.locationOption.equals(olasAttribute)) {
            NodeEntity olasNode = this.nodeService.getNode(this.node);
            attributeType.setLocation(olasNode);
        } else {
            attributeType.setPluginName(this.plugin);
            attributeType.setPluginConfiguration(this.pluginConfiguration);
        }
        attributeType.setAttributeCacheTimeoutMillis(this.cacheTimeout);

        if (null != this.memberAccessControlAttributes) {
            int memberSequence = 0;
            for (MemberAccessControl memberAccessControlAttribute : this.memberAccessControlAttributes) {
                this.log.debug("selected member attribute: " + memberAccessControlAttribute.getName());
                attributeType.addMember(memberAccessControlAttribute.getAttributeType(), memberSequence,
                        memberAccessControlAttribute.isRequired());
                memberSequence += 1;
            }
        }

        this.attributeTypeService.add(attributeType);

        /*
         * Reload the attribute type list here.
         */
        this.attributeTypeList = this.attributeTypeService.listAttributeTypes();

        return "success";
    }

    @End
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String cancel() {

        return "canceled";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String membersNext() {

        this.log.debug("members next");
        memberAccessControlAttributesFactory();
        return "next";
    }


    public static final String        MEMBER_ACCESS_CONTROL_ATTRIBUTES = "memberAccessControlAttributes";

    @DataModel("memberAccessControlAttributes")
    private List<MemberAccessControl> memberAccessControlAttributes;


    public static class MemberAccessControl {

        private boolean                   required;

        private final AttributeTypeEntity attributeType;


        public MemberAccessControl(AttributeTypeEntity attributeType) {

            this.attributeType = attributeType;
        }

        public String getName() {

            return this.attributeType.getName();
        }

        public boolean isRequired() {

            return this.required;
        }

        public void setRequired(boolean required) {

            this.required = required;
        }

        public AttributeTypeEntity getAttributeType() {

            return this.attributeType;
        }
    }


    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(MEMBER_ACCESS_CONTROL_ATTRIBUTES)
    public void memberAccessControlAttributesFactory() {

        this.log.debug("memberAccessControlAttributesFactory");
        this.memberAccessControlAttributes = new LinkedList<MemberAccessControl>();
        if (null == this.selectedMemberAttributes)
            return;
        for (AttributeTypeEntity selectedMemberAttribute : this.selectedMemberAttributes) {
            this.memberAccessControlAttributes.add(new MemberAccessControl(selectedMemberAttribute));
        }
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String membersAccessControlNext() {

        this.log.debug("members access control next");
        return "next";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getLocationOption() {

        return this.locationOption;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setLocationOption(String locationOption) {

        this.locationOption = locationOption;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String acNext() {

        this.log.debug("ac next");
        return "next";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String locationNext() {

        this.log.debug("location next: " + this.locationOption);
        pluginFactory();
        return this.locationOption;
    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public Long getCacheTimeout() {

        return this.cacheTimeout;
    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setCacheTimeout(Long cacheTimeout) {

        this.cacheTimeout = cacheTimeout;
    }

}
