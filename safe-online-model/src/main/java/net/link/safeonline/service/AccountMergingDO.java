/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.link.safeonline.authentication.service.AttributeDO;
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

	private List<SubscriptionEntity> preservedSubscriptions;

	private List<SubscriptionEntity> importedSubscriptions;

	private List<AttributeDO> preservedAttributes;

	private List<AttributeDO> importedAttributes;

	private List<AttributeDO> mergedAttributes;

	private Map<AttributeDO, AttributeDO> choosableAttributes;

	public AccountMergingDO(SubjectEntity sourceSubject) {
		this.sourceSubject = sourceSubject;
	}

	public SubjectEntity getSourceSubject() {
		return this.sourceSubject;
	}

	public List<SubscriptionEntity> getPreservedSubscriptions() {
		return this.preservedSubscriptions;
	}

	public void setPreservedSubscriptions(
			List<SubscriptionEntity> preservedSubscriptions) {
		this.preservedSubscriptions = preservedSubscriptions;
	}

	public List<SubscriptionEntity> getImportedSubscriptions() {
		return this.importedSubscriptions;
	}

	public void addImportedSubscription(
			SubscriptionEntity importedSubscriptionEntity) {
		if (null == this.importedSubscriptions)
			this.importedSubscriptions = new LinkedList<SubscriptionEntity>();
		this.importedSubscriptions.add(importedSubscriptionEntity);
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

	public Map<AttributeDO, AttributeDO> getChoosableAttributes() {
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

	public void addChoosableAttributes(List<AttributeDO> sourceAttributes,
			List<AttributeDO> targetAttributes) {
		if (null == this.choosableAttributes)
			this.choosableAttributes = new HashMap<AttributeDO, AttributeDO>();
		Iterator<AttributeDO> sourceIt = sourceAttributes.iterator();
		Iterator<AttributeDO> targetIt = targetAttributes.iterator();
		while (sourceIt.hasNext() && targetIt.hasNext()) {
			this.choosableAttributes.put(sourceIt.next(), targetIt.next());
		}
	}

	public void log() {
		for (SubscriptionEntity subscription : this.preservedSubscriptions) {
			LOG.debug("preserved subscription: "
					+ subscription.getApplication().getName());
		}
		for (SubscriptionEntity subscription : this.importedSubscriptions) {
			LOG.debug("imported subscription: "
					+ subscription.getApplication().getName());
		}
		for (AttributeDO attribute : this.preservedAttributes) {
			LOG.debug("preserved attribute: " + attribute.getName() + " ("
					+ attribute.getType() + ")");
			LOG.debug("  * " + attribute.getIndex() + ": "
					+ attribute.getValue());
		}
		for (AttributeDO attribute : this.importedAttributes) {
			LOG.debug("imported attribute: " + attribute.getName() + " ("
					+ attribute.getType() + ")");
			LOG.debug("  * " + attribute.getIndex() + ": "
					+ attribute.getValue());

		}
		for (AttributeDO attribute : this.mergedAttributes) {
			LOG.debug("merged attribute: " + attribute.getName() + " ("
					+ attribute.getType() + ")");
			LOG.debug("  * " + attribute.getIndex() + ": "
					+ attribute.getValue());

		}
		for (Entry<AttributeDO, AttributeDO> choosableAttribute : this.choosableAttributes
				.entrySet()) {
			AttributeDO sourceAttribute = choosableAttribute.getKey();
			AttributeDO targetAttribute = choosableAttribute.getValue();
			LOG.debug("choosable attribute: " + sourceAttribute.getName()
					+ " (" + sourceAttribute.getType() + ")");
			LOG.debug("  * source: " + sourceAttribute.getIndex() + ": "
					+ sourceAttribute.getValue());
			LOG.debug("  * target: " + targetAttribute.getIndex() + ": "
					+ targetAttribute.getValue());
		}

	}
}
