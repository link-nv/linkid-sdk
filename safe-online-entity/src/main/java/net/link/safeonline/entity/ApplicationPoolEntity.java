/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.ApplicationPoolEntity.QUERY_WHERE_ALL;
import static net.link.safeonline.entity.ApplicationPoolEntity.QUERY_WHERE_APP1_APP2;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Entity
@Table(name = "application_pool")
@NamedQueries( {
        @NamedQuery(name = QUERY_WHERE_ALL, query = "FROM ApplicationPoolEntity"),
        @NamedQuery(name = QUERY_WHERE_APP1_APP2, query = "SELECT pool FROM ApplicationPoolEntity pool "
                + "WHERE :application1 MEMBER OF pool.applications AND :application2 MEMBER OF pool.applications") })
public class ApplicationPoolEntity implements Serializable {

    public static final String      QUERY_WHERE_ALL       = "app.pool.all";

    public static final String      QUERY_WHERE_APP1_APP2 = "app.pool.app1.app2";

    private static final long       serialVersionUID      = 1L;

    private String                  name;

    private long                    ssoTimeout;

    private List<ApplicationEntity> applications;


    public ApplicationPoolEntity() {

        // empty
    }

    public ApplicationPoolEntity(String name, long ssoTimeout) {

        this.name = name;
        this.ssoTimeout = ssoTimeout;
        this.applications = new LinkedList<ApplicationEntity>();
    }

    @Id
    public String getName() {

        return this.name;
    }

    public void setName(String name) {

        this.name = name;
    }

    /**
     * Single Sign-On Timeout for this Application Pool
     */
    public long getSsoTimeout() {

        return this.ssoTimeout;
    }

    public void setSsoTimeout(long ssoTimeout) {

        this.ssoTimeout = ssoTimeout;
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @OrderBy("name")
    public List<ApplicationEntity> getApplications() {

        return this.applications;
    }

    public void setApplications(List<ApplicationEntity> applications) {

        this.applications = applications;
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(this.name).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ApplicationPoolEntity rhs = (ApplicationPoolEntity) obj;
        return new EqualsBuilder().append(this.name, rhs.name).isEquals();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append(this.name).toString();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_WHERE_ALL)
        List<ApplicationPoolEntity> listApplicationPools();

        @QueryMethod(QUERY_WHERE_APP1_APP2)
        List<ApplicationPoolEntity> listCommonApplicationPools(
                @QueryParam("application1") ApplicationEntity application1,
                @QueryParam("application2") ApplicationEntity application2);
    }
}
