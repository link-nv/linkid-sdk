/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AttributeTypeDefinitionException;
import net.link.safeonline.authentication.exception.AttributeTypeDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingAttributeTypeException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationIdentityDAO;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeProviderDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.AttributeTypeServiceRemote;
import net.link.safeonline.util.Filter;
import net.link.safeonline.util.FilterUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;


@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class AttributeTypeServiceBean implements AttributeTypeService, AttributeTypeServiceRemote {

    private static final Log       LOG = LogFactory.getLog(AttributeTypeServiceBean.class);

    @EJB
    private AttributeTypeDAO       attributeTypeDAO;

    @EJB
    private AttributeProviderDAO   attributeProviderDAO;

    @EJB
    private ApplicationIdentityDAO applicationIdentityDAO;

    @EJB
    private AttributeDAO           attributeDAO;

    @EJB
    private DeviceDAO              deviceDAO;


    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<AttributeTypeEntity> listAttributeTypes() {

        List<AttributeTypeEntity> attributeTypes = this.attributeTypeDAO.listAttributeTypes();
        return attributeTypes;
    }

    @RolesAllowed(SafeOnlineRoles.GLOBAL_OPERATOR_ROLE)
    public void add(AttributeTypeEntity attributeType) throws ExistingAttributeTypeException,
            AttributeTypeNotFoundException, AttributeTypeDefinitionException {

        LOG.debug("add: " + attributeType);
        String name = attributeType.getName();
        checkExistingAttributeType(name);
        checkCompoundedMembers(attributeType);
        this.attributeTypeDAO.addAttributeType(attributeType);
        markCompoundMembers(attributeType);
    }

    private void markCompoundMembers(AttributeTypeEntity attributeType) throws AttributeTypeNotFoundException {

        for (CompoundedAttributeTypeMemberEntity member : attributeType.getMembers()) {
            String memberName = member.getMember().getName();
            /*
             * Make sure to first load an attached member attribute type.
             */
            AttributeTypeEntity memberAttributeType = this.attributeTypeDAO.getAttributeType(memberName);
            memberAttributeType.setCompoundMember(true);
        }
    }

    private void checkCompoundedMembers(AttributeTypeEntity attributeType) throws AttributeTypeNotFoundException,
            AttributeTypeDefinitionException {

        for (CompoundedAttributeTypeMemberEntity member : attributeType.getMembers()) {
            String memberName = member.getMember().getName();
            AttributeTypeEntity memberAttributeType = this.attributeTypeDAO.getAttributeType(memberName);
            if (memberAttributeType.isCompounded())
                /*
                 * We don't allow compounded of compounded attributes.
                 */
                throw new AttributeTypeDefinitionException();
            if (memberAttributeType.isCompoundMember())
                /*
                 * For the moment we don't allow an attribute to be a member of more than one compounded attribute.
                 */
                throw new AttributeTypeDefinitionException();
        }
    }

    private void checkExistingAttributeType(String name) throws ExistingAttributeTypeException {

        AttributeTypeEntity existingAttributeType = this.attributeTypeDAO.findAttributeType(name);
        if (null != existingAttributeType)
            throw new ExistingAttributeTypeException();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<AttributeTypeDescriptionEntity> listDescriptions(String attributeTypeName)
            throws AttributeTypeNotFoundException {

        AttributeTypeEntity attributeType = this.attributeTypeDAO.getAttributeType(attributeTypeName);
        List<AttributeTypeDescriptionEntity> descriptions = this.attributeTypeDAO.listDescriptions(attributeType);
        return descriptions;
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void addDescription(AttributeTypeDescriptionEntity newAttributeTypeDescription)
            throws AttributeTypeNotFoundException {

        String attributeTypeName = newAttributeTypeDescription.getAttributeTypeName();
        AttributeTypeEntity attributeType = this.attributeTypeDAO.getAttributeType(attributeTypeName);
        this.attributeTypeDAO.addAttributeTypeDescription(attributeType, newAttributeTypeDescription);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeDescription(AttributeTypeDescriptionEntity attributeTypeDescription)
            throws AttributeTypeDescriptionNotFoundException {

        AttributeTypeDescriptionEntity attachedEntity = this.attributeTypeDAO.getDescription(attributeTypeDescription
                .getPk());
        this.attributeTypeDAO.removeDescription(attachedEntity);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void saveDescription(AttributeTypeDescriptionEntity attributeTypeDescription) {

        this.attributeTypeDAO.saveDescription(attributeTypeDescription);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<AttributeTypeEntity> listAvailableMemberAttributeTypes() {

        List<AttributeTypeEntity> attributeTypes = this.attributeTypeDAO.listAttributeTypes();
        List<AttributeTypeEntity> availableMemberAttributeTypes = FilterUtil.filter(attributeTypes,
                new AvailableMemberAttributeTypeFilter());
        return availableMemberAttributeTypes;
    }


    static class AvailableMemberAttributeTypeFilter implements Filter<AttributeTypeEntity> {

        public boolean isAllowed(AttributeTypeEntity element) {

            if (element.isCompounded())
                return false;
            if (element.isCompoundMember())
                return false;
            if (false == element.isMultivalued())
                return false;
            return true;
        }
    }


    @RolesAllowed(SafeOnlineRoles.GLOBAL_OPERATOR_ROLE)
    public void remove(AttributeTypeEntity attributeType) throws AttributeTypeDescriptionNotFoundException,
            PermissionDeniedException, AttributeTypeNotFoundException {

        LOG.debug("remove attribute type: " + attributeType.getName());

        // attribute type cannot be in any application identity
        checkApplicationIdentities(attributeType);

        // attribute type cannot be a compounded member
        checkCompounded(attributeType);

        // attribute type cannot be used by a device
        checkDevices(attributeType);

        // remove attribute provider entities
        this.attributeProviderDAO.removeAttributeProviders(attributeType);

        // remove all its descriptions
        List<AttributeTypeDescriptionEntity> descriptions = this.attributeTypeDAO.listDescriptions(attributeType);
        for (AttributeTypeDescriptionEntity description : descriptions) {
            removeDescription(description);
        }

        // remove attributes of this type
        this.attributeDAO.removeAttributes(attributeType);

        if (attributeType.isCompounded()) {
            this.attributeTypeDAO.removeMemberEntries(attributeType);
            unmarkCompoundMembers(attributeType);
        }

        this.attributeTypeDAO.removeAttributeType(attributeType.getName());
    }

    private void checkApplicationIdentities(AttributeTypeEntity attributeType) throws PermissionDeniedException {

        List<ApplicationIdentityEntity> applicationIdentities = this.applicationIdentityDAO.listApplicationIdentities();
        for (ApplicationIdentityEntity applicationIdentity : applicationIdentities) {
            if (applicationIdentity.getAttributeTypes().contains(attributeType)) {
                throw new PermissionDeniedException("Attribute type still exists in application identity: "
                        + applicationIdentity.getApplication().getName(), "errorPermissionAttributeInIdentity",
                        applicationIdentity.getApplication().getName());
            }
        }
    }

    private void checkCompounded(AttributeTypeEntity attributeType) throws PermissionDeniedException {

        if (attributeType.isCompoundMember()) {
            throw new PermissionDeniedException("Cannot remove a compound member attribute type",
                    "errorPermissionCompoundMember");
        }
    }

    private void checkDevices(AttributeTypeEntity attributeType) throws PermissionDeniedException {

        List<DeviceEntity> devices = this.deviceDAO.listDevices();
        for (DeviceEntity device : devices) {
            if (device.getAttributeType().equals(attributeType)) {
                throw new PermissionDeniedException("Attribute type exist in device: " + device.getName(),
                        "errorPermissionAttributeInDevice", device.getName());
            }
        }
    }

    private void unmarkCompoundMembers(AttributeTypeEntity attributeType) throws AttributeTypeNotFoundException {

        for (CompoundedAttributeTypeMemberEntity member : attributeType.getMembers()) {
            String memberName = member.getMember().getName();
            /*
             * Make sure to first load an attached member attribute type.
             */
            AttributeTypeEntity memberAttributeType = this.attributeTypeDAO.getAttributeType(memberName);
            memberAttributeType.setCompoundMember(false);
        }
    }

    @RolesAllowed(SafeOnlineRoles.GLOBAL_OPERATOR_ROLE)
    public void savePluginConfiguration(String attributeTypeName, String pluginConfiguration)
            throws AttributeTypeNotFoundException {

        LOG.debug("set plugin configuration: " + pluginConfiguration + " for attribute type " + attributeTypeName);
        AttributeTypeEntity attributeType = this.attributeTypeDAO.getAttributeType(attributeTypeName);
        attributeType.setPluginConfiguration(pluginConfiguration);
    }
}
