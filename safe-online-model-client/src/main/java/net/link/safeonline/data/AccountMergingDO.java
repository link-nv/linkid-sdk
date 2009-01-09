/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.data;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Used to store the resulting data of account merging and to transfer data between service and user web application.
 * 
 * @author wvdhaute
 * 
 */
public class AccountMergingDO implements Serializable {

    private static final long          serialVersionUID = 1L;

    private static final Log           LOG              = LogFactory.getLog(AccountMergingDO.class);

    private SubjectEntity              sourceSubject;

    private List<DeviceEntity>         neededProvenDevices;

    private List<SubscriptionEntity>   preservedSubscriptions;

    private List<SubscriptionDO>       importedSubscriptions;

    private List<AttributeDO>          preservedAttributes;

    private List<AttributeDO>          importedAttributes;

    private List<AttributeDO>          mergedAttributes;

    private List<AttributeDO>          mergedAttributesToAdd;

    private List<ChoosableAttributeDO> choosableAttributes;


    public AccountMergingDO(SubjectEntity sourceSubject) {

        this.sourceSubject = sourceSubject;
    }

    public SubjectEntity getSourceSubject() {

        return sourceSubject;
    }

    public List<DeviceEntity> getNeededProvenDevices() {

        return neededProvenDevices;
    }

    public void addNeededProvenDevices(List<DeviceEntity> devices) {

        if (null == neededProvenDevices) {
            neededProvenDevices = new LinkedList<DeviceEntity>();
        }
        neededProvenDevices.addAll(devices);
    }

    public List<SubscriptionEntity> getPreservedSubscriptions() {

        return preservedSubscriptions;
    }

    public void setPreservedSubscriptions(List<SubscriptionEntity> preservedSubscriptions) {

        this.preservedSubscriptions = preservedSubscriptions;
    }

    public List<SubscriptionDO> getImportedSubscriptions() {

        return importedSubscriptions;
    }

    public void addImportedSubscription(SubscriptionDO importedSubscriptionDO) {

        if (null == importedSubscriptions) {
            importedSubscriptions = new LinkedList<SubscriptionDO>();
        }
        importedSubscriptions.add(importedSubscriptionDO);
    }

    public List<AttributeDO> getPreservedAttributes() {

        return preservedAttributes;
    }

    public List<AttributeDO> getImportedAttributes() {

        return importedAttributes;
    }

    public List<AttributeDO> getMergedAttributes() {

        return mergedAttributes;
    }

    public List<AttributeDO> getMergedAttributesToAdd() {

        return mergedAttributesToAdd;
    }

    public List<AttributeDO> getVisiblePreservedAttributes() {

        return getVisibleAttributes(preservedAttributes);
    }

    public List<AttributeDO> getVisibleImportedAttributes() {

        return getVisibleAttributes(importedAttributes);
    }

    public List<AttributeDO> getVisibleMergedAttributes() {

        return getVisibleAttributes(mergedAttributes);
    }

    private List<AttributeDO> getVisibleAttributes(List<AttributeDO> attributes) {

        List<AttributeDO> visibleAttributes = new LinkedList<AttributeDO>();
        for (AttributeDO attribute : attributes) {
            if (attribute.isUserVisible()) {
                visibleAttributes.add(attribute);
            }
        }
        return visibleAttributes;
    }

    public List<ChoosableAttributeDO> getChoosableAttributes() {

        return choosableAttributes;
    }

    public void addPreservedAttributes(List<AttributeDO> attributes) {

        if (null == preservedAttributes) {
            preservedAttributes = new LinkedList<AttributeDO>();
        }
        preservedAttributes.addAll(attributes);
    }

    public void setImportedAttributes(List<AttributeDO> importedAttributes) {

        this.importedAttributes = importedAttributes;
    }

    public void setMergedAttributes(List<AttributeDO> mergedAttributes) {

        this.mergedAttributes = mergedAttributes;
    }

    public void setMergedAttributesToAdd(List<AttributeDO> mergedAttributesToAdd) {

        this.mergedAttributesToAdd = mergedAttributesToAdd;
    }

    public void addChoosableAttributes(List<AttributeDO> targetAttributes, List<AttributeDO> sourceAttributes) {

        if (null == choosableAttributes) {
            choosableAttributes = new LinkedList<ChoosableAttributeDO>();
        }
        Iterator<AttributeDO> targetIt = targetAttributes.iterator();
        Iterator<AttributeDO> sourceIt = sourceAttributes.iterator();
        while (sourceIt.hasNext() && targetIt.hasNext()) {
            ChoosableAttributeDO choosableAttributeDO = new ChoosableAttributeDO(targetIt.next(), sourceIt.next());
            choosableAttributes.add(choosableAttributeDO);
        }
    }

    public void log() {

        LOG.debug("Preserved subscriptions:");
        for (SubscriptionEntity subscription : preservedSubscriptions) {
            LOG.debug("  * " + subscription.getApplication().getName());
        }
        LOG.debug("Imported subscriptions:");
        if (null != importedSubscriptions) {
            for (SubscriptionDO subscription : importedSubscriptions) {
                LOG.debug("  * " + subscription.getSubscription().getApplication().getName());
            }
        }
        LOG.debug("Needed proven devices:");
        if (null != neededProvenDevices) {
            for (DeviceEntity device : neededProvenDevices) {
                LOG.debug("  * " + device.getName());
            }
        }
        LOG.debug("Preserved attributes:");
        for (AttributeDO attribute : preservedAttributes) {
            LOG.debug("  * " + attribute.getIndex() + ": " + attribute.getType() + ": " + attribute.getValue());
        }
        LOG.debug("Imported attributes:");
        for (AttributeDO attribute : importedAttributes) {
            LOG.debug("  * " + attribute.getIndex() + ": " + attribute.getType() + ": " + attribute.getValue());
        }
        LOG.debug("Merged attributes:");
        for (AttributeDO attribute : mergedAttributes) {
            LOG.debug("  * " + attribute.getIndex() + ": " + attribute.getType() + ": " + attribute.getValue());
        }
        LOG.debug("To-be-added merged attributes:");
        for (AttributeDO attribute : mergedAttributesToAdd) {
            LOG.debug("  * " + attribute.getIndex() + ": " + attribute.getType() + ": " + attribute.getValue());
        }

        LOG.debug("Choosable attributes:");
        if (null != choosableAttributes) {
            for (ChoosableAttributeDO choosableAttribute : choosableAttributes) {
                LOG.debug("  * " + choosableAttribute.getSourceAttribute().getIndex() + ": "
                        + choosableAttribute.getSourceAttribute().getType());
                LOG.debug("    * source: " + choosableAttribute.getSourceAttribute().getIndex() + ": "
                        + choosableAttribute.getSourceAttribute().getValue() + " (selected:" + choosableAttribute.isSourceSelected() + ")");
                LOG.debug("    * target: " + choosableAttribute.getTargetAttribute().getIndex() + ": "
                        + choosableAttribute.getTargetAttribute().getValue());
            }
        }

    }
}
