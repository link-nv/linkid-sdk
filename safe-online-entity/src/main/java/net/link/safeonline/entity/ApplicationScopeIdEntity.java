/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.ApplicationScopeIdEntity.DELETE_ALL_WHERE_APPLICATION;
import static net.link.safeonline.entity.ApplicationScopeIdEntity.DELETE_ALL_WHERE_SUBJECT;
import static net.link.safeonline.entity.ApplicationScopeIdEntity.QUERY_WHERE_SUBJECT_APPLICATION;

import java.io.Serializable;

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
@Table(name = "app_scope_id", uniqueConstraints = @UniqueConstraint(columnNames = { "application", "subject" }))
@NamedQueries( {
        @NamedQuery(name = QUERY_WHERE_SUBJECT_APPLICATION, query = "SELECT applicationScopeId "
                + "FROM ApplicationScopeIdEntity AS applicationScopeId "
                + "WHERE applicationScopeId.subject = :subject AND applicationScopeId.application = :application"),
        @NamedQuery(name = DELETE_ALL_WHERE_SUBJECT, query = "DELETE FROM ApplicationScopeIdEntity AS applicationScopeId "
                + "WHERE applicationScopeId.subject = :subject"),
        @NamedQuery(name = DELETE_ALL_WHERE_APPLICATION, query = "DELETE FROM ApplicationScopeIdEntity AS applicationScopeId "
                + "WHERE applicationScopeId.application = :application") })
public class ApplicationScopeIdEntity implements Serializable {

    private static final long  serialVersionUID                = 1L;

    public static final String QUERY_WHERE_SUBJECT_APPLICATION = "appscope.subapp";

    public static final String DELETE_ALL_WHERE_SUBJECT        = "appscope.del.sub";

    public static final String DELETE_ALL_WHERE_APPLICATION    = "appscope.del.app";

    private SubjectEntity      subject;

    private ApplicationEntity  application;

    private String             id;


    public ApplicationScopeIdEntity() {

        // empty
    }

    public ApplicationScopeIdEntity(SubjectEntity subject, String id, ApplicationEntity application) {

        this.subject = subject;
        this.id = id;
        this.application = application;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "application", updatable = false)
    public ApplicationEntity getApplication() {

        return this.application;
    }

    public void setApplication(ApplicationEntity application) {

        this.application = application;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject", updatable = false)
    public SubjectEntity getSubject() {

        return this.subject;
    }

    public void setSubject(SubjectEntity subject) {

        this.subject = subject;
    }

    @Id
    public String getId() {

        return this.id;
    }

    public void setId(String id) {

        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (false == obj instanceof ApplicationScopeIdEntity) {
            return false;
        }
        ApplicationScopeIdEntity rhs = (ApplicationScopeIdEntity) obj;
        return new EqualsBuilder().append(this.id, rhs.id).isEquals();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("subject", this.subject).append(
                "application", this.application).append("id", this.id).toString();
    }


    public interface QueryInterface {

        @QueryMethod(value = QUERY_WHERE_SUBJECT_APPLICATION, nullable = true)
        ApplicationScopeIdEntity findApplicationScopeId(@QueryParam("subject") SubjectEntity subject,
                @QueryParam("application") ApplicationEntity application);

        @UpdateMethod(value = DELETE_ALL_WHERE_SUBJECT)
        void deleteAll(@QueryParam("subject") SubjectEntity subject);

        @UpdateMethod(value = DELETE_ALL_WHERE_APPLICATION)
        void deleteAll(@QueryParam("application") ApplicationEntity application);
    }

}
