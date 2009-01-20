/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.NodeMappingEntity.DELETE_ALL_SUBJECT;
import static net.link.safeonline.entity.NodeMappingEntity.QUERY_LIST_NODE;
import static net.link.safeonline.entity.NodeMappingEntity.QUERY_LIST_SUBJECT;
import static net.link.safeonline.entity.NodeMappingEntity.QUERY_SUBJECT_NODE;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;
import net.link.safeonline.jpa.annotation.UpdateMethod;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


@Entity
@Table(name = "nodeMappings", uniqueConstraints = @UniqueConstraint(columnNames = { "subject", "node" }))
@NamedQueries( {
        @NamedQuery(name = QUERY_LIST_SUBJECT, query = "SELECT n " + "FROM NodeMappingEntity AS n " + "WHERE n.subject = :subject"),
        @NamedQuery(name = QUERY_LIST_NODE, query = "SELECT n " + "FROM NodeMappingEntity AS n " + "WHERE n.node = :node"),
        @NamedQuery(name = QUERY_SUBJECT_NODE, query = "SELECT n " + "FROM NodeMappingEntity AS n "
                + "WHERE n.subject = :subject AND n.node = :node"),
        @NamedQuery(name = DELETE_ALL_SUBJECT, query = "DELETE FROM NodeMappingEntity AS n " + "WHERE n.subject = :subject") })
public class NodeMappingEntity implements Serializable {

    private static final long  serialVersionUID   = 1L;

    public static final String QUERY_LIST_SUBJECT = "node.map.sub";

    public static final String QUERY_LIST_NODE    = "node.map.node";

    public static final String QUERY_SUBJECT_NODE = "node.map.subnode";

    public static final String DELETE_ALL_SUBJECT = "node.map.del.sub";

    private SubjectEntity      subject;

    private NodeEntity         node;

    private String             id;


    public NodeMappingEntity() {

        // empty
    }

    public NodeMappingEntity(SubjectEntity subject, String id, NodeEntity node) {

        this.subject = subject;
        this.id = id;
        this.node = node;
    }

    @ManyToOne
    @JoinColumn(name = "subject", nullable = false)
    public SubjectEntity getSubject() {

        return subject;
    }

    public void setSubject(SubjectEntity subject) {

        this.subject = subject;
    }

    @Id
    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name = "node", nullable = false)
    public NodeEntity getNode() {

        return node;
    }

    public void setNode(NodeEntity node) {

        this.node = node;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (false == obj instanceof NodeMappingEntity)
            return false;
        NodeMappingEntity rhs = (NodeMappingEntity) obj;
        return new EqualsBuilder().append(id, rhs.id).isEquals();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("id", id).append("subject", subject.getUserId())
                                                                    .append("node", node.getName()).toString();
    }


    public interface QueryInterface {

        @QueryMethod(value = QUERY_SUBJECT_NODE, nullable = true)
        NodeMappingEntity findNodeMapping(@QueryParam("subject") SubjectEntity subject, @QueryParam("node") NodeEntity node);

        @QueryMethod(QUERY_LIST_SUBJECT)
        List<NodeMappingEntity> listNodeMappings(@QueryParam("subject") SubjectEntity subject);

        @UpdateMethod(DELETE_ALL_SUBJECT)
        void deleteAll(@QueryParam("subject") SubjectEntity subject);

        @QueryMethod(QUERY_LIST_NODE)
        List<NodeMappingEntity> listNodeMappings(@QueryParam("node") NodeEntity node);
    }
}
