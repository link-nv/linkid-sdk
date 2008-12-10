/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.notification;

import static net.link.safeonline.entity.notification.EndpointReferenceEntity.QUERY_LIST_ALL;
import static net.link.safeonline.entity.notification.EndpointReferenceEntity.QUERY_WHERE_ADDRESS_APPLICATION;
import static net.link.safeonline.entity.notification.EndpointReferenceEntity.QUERY_WHERE_ADDRESS_NODE;
import static net.link.safeonline.entity.notification.EndpointReferenceEntity.QUERY_WHERE_APPLICATION;
import static net.link.safeonline.entity.notification.EndpointReferenceEntity.QUERY_WHERE_NODE;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Entity representing a W3CEndpointReference.
 * 
 * @author wvdhaute
 * 
 */
@Entity
@Table(name = "endpoint_ref")
@NamedQueries( {
        @NamedQuery(name = QUERY_LIST_ALL, query = "FROM EndpointReferenceEntity epr"),
        @NamedQuery(name = QUERY_WHERE_ADDRESS_NODE, query = "SELECT epr FROM EndpointReferenceEntity AS epr "
                + "WHERE epr.address = :address AND epr.node = :node"),
        @NamedQuery(name = QUERY_WHERE_ADDRESS_APPLICATION, query = "SELECT epr "
                + "FROM EndpointReferenceEntity AS epr WHERE epr.address = :address AND " + "epr.application = :application"),
        @NamedQuery(name = QUERY_WHERE_APPLICATION, query = "SELECT epr FROM EndpointReferenceEntity AS epr "
                + "WHERE epr.application = :application"),
        @NamedQuery(name = QUERY_WHERE_NODE, query = "SELECT epr FROM EndpointReferenceEntity AS epr " + "WHERE epr.node = :node") })
public class EndpointReferenceEntity implements Serializable {

    private static final long                           serialVersionUID                = 1L;

    public static final String                          QUERY_LIST_ALL                  = "epr.list.all";

    public static final String                          QUERY_WHERE_ADDRESS_NODE        = "epr.add.node";

    public static final String                          QUERY_WHERE_ADDRESS_APPLICATION = "epr.add.app";

    public static final String                          QUERY_WHERE_NODE                = "epr.node";

    public static final String                          QUERY_WHERE_APPLICATION         = "epr.app";

    private long                                        id;

    private String                                      address;

    private ApplicationEntity                           application;

    private NodeEntity                                  node;

    private Set<NotificationProducerSubscriptionEntity> subscriptions;


    public EndpointReferenceEntity() {

        // empty
    }

    public EndpointReferenceEntity(String address, ApplicationEntity application) {

        this.address = address;
        this.application = application;
        this.subscriptions = new HashSet<NotificationProducerSubscriptionEntity>();
    }

    public EndpointReferenceEntity(String address, NodeEntity node) {

        this.address = address;
        this.node = node;
        this.subscriptions = new HashSet<NotificationProducerSubscriptionEntity>();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {

        return this.id;
    }

    public void setId(long id) {

        this.id = id;
    }

    public String getAddress() {

        return this.address;
    }

    public void setAddress(String address) {

        this.address = address;
    }

    @ManyToOne
    @JoinColumn(nullable = true)
    public ApplicationEntity getApplication() {

        return this.application;
    }

    public void setApplication(ApplicationEntity application) {

        this.application = application;
    }

    @ManyToOne
    @JoinColumn(nullable = true)
    public NodeEntity getNode() {

        return this.node;
    }

    public void setNode(NodeEntity node) {

        this.node = node;
    }

    @ManyToMany(mappedBy = NotificationProducerSubscriptionEntity.CONSUMERS_COLUMN_NAME)
    public Set<NotificationProducerSubscriptionEntity> getSubscriptions() {

        return this.subscriptions;
    }

    public void setSubscriptions(Set<NotificationProducerSubscriptionEntity> subscriptions) {

        this.subscriptions = subscriptions;
    }

    @Transient
    public String getName() {

        if (null != this.application)
            return this.application.getName();
        return this.node.getName();
    }

    @Override
    public String toString() {

        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("address", this.address);
        if (null != this.application) {
            builder.append("application", this.application.getName());
        }
        if (null != this.node) {
            builder.append("node", this.node.getName());
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (null == obj)
            return false;
        if (this == obj)
            return true;
        if (false == obj instanceof EndpointReferenceEntity)
            return false;
        final EndpointReferenceEntity rhs = (EndpointReferenceEntity) obj;
        return this.id == rhs.id;
    }

    @Override
    public int hashCode() {

        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(this.address);
        if (null != this.application) {
            builder.append(this.application);
        }
        if (null != this.node) {
            builder.append(this.node);
        }
        return builder.toHashCode();
    }


    public interface QueryInterface {

        @QueryMethod(value = QUERY_LIST_ALL)
        List<EndpointReferenceEntity> listEndpoints();

        @QueryMethod(value = QUERY_WHERE_ADDRESS_NODE, nullable = true)
        EndpointReferenceEntity find(@QueryParam("address") String address, @QueryParam("node") NodeEntity node);

        @QueryMethod(value = QUERY_WHERE_ADDRESS_APPLICATION, nullable = true)
        EndpointReferenceEntity find(@QueryParam("address") String address, @QueryParam("application") ApplicationEntity application);

        @QueryMethod(value = QUERY_WHERE_NODE)
        List<EndpointReferenceEntity> listEndpoints(@QueryParam("node") NodeEntity node);

        @QueryMethod(value = QUERY_WHERE_APPLICATION)
        List<EndpointReferenceEntity> listEndpoints(@QueryParam("application") ApplicationEntity application);

    }
}
