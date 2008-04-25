/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.notification;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * WS-Notification producer subscription.
 * 
 * @author wvdhaute
 * 
 */
@Entity
@Table(name = "prod_subscr")
public class NotificationProducerSubscriptionEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private String topic;

	private Set<EndpointReferenceEntity> consumers;

	public NotificationProducerSubscriptionEntity() {
		// empty
	}

	public NotificationProducerSubscriptionEntity(String topic) {
		this.topic = topic;
		this.consumers = new HashSet<EndpointReferenceEntity>();
	}

	@Id
	public String getTopic() {
		return this.topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	@OneToMany(fetch = FetchType.EAGER)
	public Set<EndpointReferenceEntity> getConsumers() {
		return this.consumers;
	}

	public void setConsumers(Set<EndpointReferenceEntity> consumers) {
		this.consumers = consumers;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("topic", this.topic).toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (false == obj instanceof NotificationProducerSubscriptionEntity) {
			return false;
		}
		NotificationProducerSubscriptionEntity rhs = (NotificationProducerSubscriptionEntity) obj;
		return new EqualsBuilder().append(this.topic, rhs.topic).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.topic).toHashCode();
	}
}