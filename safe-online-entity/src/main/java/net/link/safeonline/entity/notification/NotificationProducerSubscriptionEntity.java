/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.notification;

import static net.link.safeonline.entity.notification.NotificationProducerSubscriptionEntity.QUERY_LIST_ALL;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import net.link.safeonline.jpa.annotation.QueryMethod;

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
@NamedQueries( { @NamedQuery(name = QUERY_LIST_ALL, query = "FROM NotificationProducerSubscriptionEntity s") })
public class NotificationProducerSubscriptionEntity implements Serializable {

    private static final long            serialVersionUID      = 1L;

    public static final String           CONSUMERS_COLUMN_NAME = "consumers";

    public static final String           QUERY_LIST_ALL        = "not.sub.list.all";

    private String                       topic;

    private Set<EndpointReferenceEntity> consumers;


    public NotificationProducerSubscriptionEntity() {

        // empty
    }

    public NotificationProducerSubscriptionEntity(String topic) {

        this.topic = topic;
        consumers = new HashSet<EndpointReferenceEntity>();
    }

    @Id
    public String getTopic() {

        return topic;
    }

    public void setTopic(String topic) {

        this.topic = topic;
    }

    @ManyToMany(fetch = FetchType.EAGER)
    public Set<EndpointReferenceEntity> getConsumers() {

        return consumers;
    }

    public void setConsumers(Set<EndpointReferenceEntity> consumers) {

        this.consumers = consumers;
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("topic", topic).toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (null == obj)
            return false;
        if (this == obj)
            return true;
        if (false == obj instanceof NotificationProducerSubscriptionEntity)
            return false;
        NotificationProducerSubscriptionEntity rhs = (NotificationProducerSubscriptionEntity) obj;
        return new EqualsBuilder().append(topic, rhs.topic).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(topic).toHashCode();
    }


    public interface QueryInterface {

        @QueryMethod(value = QUERY_LIST_ALL)
        List<NotificationProducerSubscriptionEntity> listTopics();
    }
}
