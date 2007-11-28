/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.authentication.service.AuthenticationDevice;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used to store the resulting data of account merging and to transfer data
 * between service and user web application.
 * 
 * @author wvdhaute
 * 
 */
public class AccountMergingDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(AccountMergingDO.class);

	private SubjectEntity sourceSubject;

	private Set<AuthenticationDevice> neededProvenDevices;

	private List<SubscriptionEntity> preservedSubscriptions;

	private List<SubscriptionDO> importedSubscriptions;

	private List<AttributeDO> preservedAttributes;

	private List<AttributeDO> importedAttributes;

	private List<AttributeDO> mergedAttributes;

	private List<AttributeDO> mergedAttributesToAdd;

	private List<ChoosableAttributeDO> choosableAttributes;

	public AccountMergingDO(SubjectEntity sourceSubject) {
		this.sourceSubject = sourceSubject;
	}

	public SubjectEntity getSourceSubject() {
		return this.sourceSubject;
	}

	public Set<AuthenticationDevice> getNeededProvenDevices() {
		return this.neededProvenDevices;
	}

	public void addNeededProvenDevices(Set<AuthenticationDevice> devices) {
		if (null == this.neededProvenDevices)
			this.neededProvenDevices = new HashSet<AuthenticationDevice>();
		this.neededProvenDevices.addAll(devices);
	}

	public List<SubscriptionEntity> getPreservedSubscriptions() {
		return this.preservedSubscriptions;
	}

	public void setPreservedSubscriptions(
			List<SubscriptionEntity> preservedSubscriptions) {
		this.preservedSubscriptions = preservedSubscriptions;
	}

	public List<SubscriptionDO> getImportedSubscriptions() {
		return this.importedSubscriptions;
	}

	public void addImportedSubscription(SubscriptionDO importedSubscriptionDO) {
		if (null == this.importedSubscriptions)
			this.importedSubscriptions = new LinkedList<SubscriptionDO>();
		this.importedSubscriptions.add(importedSubscriptionDO);
	}

	public List<AttributeDO> getPreservedAttributes() {
		return this.preservedAttributes;
	}

	public List<AttributeDO> getImportedAttributes() {
		return this.importedAttributes;
	}

	public List<AttributeDO> getMergedAttributes() {
		return this.mergedAttributes;
	}

	public List<AttributeDO> getMergedAttributesToAdd() {
		return this.mergedAttributesToAdd;
	}

	public List<AttributeDO> getVisiblePreservedAttributes() {
		return getVisibleAttributes(this.preservedAttributes);
	}

	public List<AttributeDO> getVisibleImportedAttributes() {
		return getVisibleAttributes(this.importedAttributes);
	}

	public List<AttributeDO> getVisibleMergedAttributes() {
		return getVisibleAttributes(this.mergedAttributes);
	}

	private List<AttributeDO> getVisibleAttributes(List<AttributeDO> attributes) {
		List<AttributeDO> visibleAttributes = new LinkedList<AttributeDO>();
		for (AttributeDO attribute : attributes) {
			if (attribute.isUserVisible())
				visibleAttributes.add(attribute);
		}
		return visibleAttributes;
	}

	public List<ChoosableAttributeDO> getChoosableAttributes() {
		return this.choosableAttributes;
	}

	public void addPreservedAttributes(List<AttributeDO> attributes) {
		if (null == this.preservedAttributes)
			this.preservedAttributes = new LinkedList<AttributeDO>();
		this.preservedAttributes.addAll(attributes);
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

	public void addChoosableAttributes(List<AttributeDO> targetAttributes,
			List<AttributeDO> sourceAttributes) {
		if (null == this.choosableAttributes)
			this.choosableAttributes = new LinkedList<ChoosableAttributeDO>();
		Iterator<AttributeDO> targetIt = targetAttributes.iterator();
		Iterator<AttributeDO> sourceIt = sourceAttributes.iterator();
		while (sourceIt.hasNext() && targetIt.hasNext()) {
			ChoosableAttributeDO choosableAttributeDO = new ChoosableAttributeDO(
					targetIt.next(), sourceIt.next());
			this.choosableAttributes.add(choosableAttributeDO);
		}
	}

	public void log() {
		LOG.debug("Preserved subscriptions:");
		for (SubscriptionEntity subscription : this.preservedSubscriptions) {
			LOG.debug("  * " + subscription.getApplication().getName());
		}
		LOG.debug("Imported subscriptions:");
		if (null != this.importedSubscriptions) {
			for (SubscriptionDO subscription : this.importedSubscriptions) {
				LOG.debug("  * "
						+ subscription.getSubscription().getApplication()
								.getName());
			}
		}
		LOG.debug("Needed proven devices:");
		if (null != this.neededProvenDevices) {
			for (AuthenticationDevice device : this.neededProvenDevices) {
				LOG.debug("  * " + device.getDeviceName());
			}
		}
		LOG.debug("Preserved attributes:");
		for (AttributeDO attribute : this.preservedAttributes) {
			LOG.debug("  * " + attribute.getIndex() + ": "
					+ attribute.getType() + ": " + attribute.getValue());
		}
		LOG.debug("Imported attributes:");
		for (AttributeDO attribute : this.importedAttributes) {
			LOG.debug("  * " + attribute.getIndex() + ": "
					+ attribute.getType() + ": " + attribute.getValue());
		}
		LOG.debug("Merged attributes:");
		for (AttributeDO attribute : this.mergedAttributes) {
			LOG.debug("  * " + attribute.getIndex() + ": "
					+ attribute.getType() + ": " + attribute.getValue());
		}
		LOG.debug("To-be-added merged attributes:");
		for (AttributeDO attribute : this.mergedAttributesToAdd) {
			LOG.debug("  * " + attribute.getIndex() + ": "
					+ attribute.getType() + ": " + attribute.getValue());
		}

		LOG.debug("Choosable attributes:");
		if (null != this.choosableAttributes) {
			for (ChoosableAttributeDO choosableAttribute : this.choosableAttributes) {
				LOG.debug("  * "
						+ choosableAttribute.getSourceAttribute().getIndex()
						+ ": "
						+ choosableAttribute.getSourceAttribute().getType());
				LOG.debug("    * source: "
						+ choosableAttribute.getSourceAttribute().getIndex()
						+ ": "
						+ choosableAttribute.getSourceAttribute().getValue()
						+ " (selected:" + choosableAttribute.isSourceSelected()
						+ ")");
				LOG.debug("    * target: "
						+ choosableAttribute.getTargetAttribute().getIndex()
						+ ": "
						+ choosableAttribute.getTargetAttribute().getValue());
			}
		}

	}
}
