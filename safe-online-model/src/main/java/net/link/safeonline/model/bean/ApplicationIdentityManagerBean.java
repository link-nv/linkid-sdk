/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationIdentityDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.model.ApplicationIdentityManager;


@Stateless
public class ApplicationIdentityManagerBean implements ApplicationIdentityManager {

    private static final Log       LOG = LogFactory.getLog(ApplicationIdentityManagerBean.class);

    @EJB
    private ApplicationDAO         applicationDAO;

    @EJB
    private ApplicationIdentityDAO applicationIdentityDAO;

    @EJB
    private AttributeTypeDAO       attributeTypeDAO;


    public void updateApplicationIdentity(String applicationId,
            List<IdentityAttributeTypeDO> newApplicationIdentityAttributes) throws ApplicationNotFoundException,
            ApplicationIdentityNotFoundException, AttributeTypeNotFoundException {

        LOG.debug("update application identity for application: " + applicationId);

        ApplicationEntity application = this.applicationDAO.getApplication(applicationId);
        long currentIdentityVersion = application.getCurrentApplicationIdentity();
        ApplicationIdentityEntity applicationIdentity = this.applicationIdentityDAO.getApplicationIdentity(application,
                currentIdentityVersion);
        List<AttributeTypeEntity> currentAttributeTypes = applicationIdentity.getAttributeTypes();
        if (null == currentAttributeTypes) {
            currentAttributeTypes = new LinkedList<AttributeTypeEntity>();
        }

        List<AttributeTypeEntity> newAttributeTypes = new LinkedList<AttributeTypeEntity>();
        for (IdentityAttributeTypeDO newAttribute : newApplicationIdentityAttributes) {
            LOG.debug("new identity attribute: " + newAttribute);
            AttributeTypeEntity newAttributeType = this.attributeTypeDAO.getAttributeType(newAttribute.getName());
            newAttributeTypes.add(newAttributeType);
        }

        boolean requireNewIdentity = CollectionUtils.isProperSubCollection(currentAttributeTypes, newAttributeTypes);

        LOG.debug("require new identity: " + requireNewIdentity);
        if (true == requireNewIdentity) {
            long newIdentityVersion = currentIdentityVersion + 1;
            LOG.debug("new identity version: " + newIdentityVersion);
            applicationIdentity = this.applicationIdentityDAO.addApplicationIdentity(application, newIdentityVersion);
            for (IdentityAttributeTypeDO attribute : newApplicationIdentityAttributes) {
                AttributeTypeEntity attributeType = this.attributeTypeDAO.getAttributeType(attribute.getName());
                boolean required = attribute.isRequired();
                boolean dataMining = attribute.isDataMining();
                this.applicationIdentityDAO.addApplicationIdentityAttribute(applicationIdentity, attributeType,
                        required, dataMining);
            }
            LOG.debug("setting new identity version on application: " + newIdentityVersion);
            application.setCurrentApplicationIdentity(newIdentityVersion);
            return;
        }

        /*
         * Else we still need to update the current application identity.
         */

        /*
         * First construct a map for fast lookup.
         */
        Map<String, IdentityAttributeTypeDO> newIdentityAttributesMap = new HashMap<String, IdentityAttributeTypeDO>();
        for (IdentityAttributeTypeDO newIdentityAttribute : newApplicationIdentityAttributes) {
            newIdentityAttributesMap.put(newIdentityAttribute.getName(), newIdentityAttribute);
        }

        List<ApplicationIdentityAttributeEntity> toRemove = new LinkedList<ApplicationIdentityAttributeEntity>();

        for (ApplicationIdentityAttributeEntity applicationIdentityAttribute : applicationIdentity.getAttributes()) {
            IdentityAttributeTypeDO newIdentityAttribute = newIdentityAttributesMap.get(applicationIdentityAttribute
                    .getAttributeTypeName());
            if (null != newIdentityAttribute) {
                /*
                 * In this case just update the existing identity attribute.
                 */
                applicationIdentityAttribute.setRequired(newIdentityAttribute.isRequired());
            } else {
                toRemove.add(applicationIdentityAttribute);
                /*
                 * Don't remove from the list while iterating over the list.
                 */
            }
        }

        for (ApplicationIdentityAttributeEntity toRemoveEntity : toRemove) {
            this.applicationIdentityDAO.removeApplicationIdentityAttribute(toRemoveEntity);
        }

        LOG.debug("changing current identity version: " + currentIdentityVersion);
    }
}
