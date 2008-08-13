/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.device;

import static net.link.safeonline.entity.device.DeviceSubjectEntity.QUERY_REGISTRATION;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Entity representing a subject from a remote device.
 * 
 * Id equals the device mapping id returned by OLAS.
 * 
 * Registrations is the list of actual registered devices for this 'device subject'. These are regular OLAS subjects but
 * without the standard Login attribute.
 * 
 * @author wvdhaute
 * 
 */
@Entity
@Table(name = "device_subject")
@NamedQueries( { @NamedQuery(name = QUERY_REGISTRATION, query = "SELECT deviceSubject "
        + "FROM DeviceSubjectEntity AS deviceSubject " + "JOIN deviceSubject.registrations registration "
        + "WHERE registration = :registration") })
public class DeviceSubjectEntity implements Serializable {

    private static final long   serialVersionUID   = 1L;

    public static final String  QUERY_REGISTRATION = "devsub.reg";

    private String              id;

    private List<SubjectEntity> registrations;


    public DeviceSubjectEntity() {

        // empty
    }

    public DeviceSubjectEntity(String id) {

        this.id = id;
        this.registrations = new LinkedList<SubjectEntity>();
    }

    @Id
    public String getId() {

        return this.id;
    }

    public void setId(String id) {

        this.id = id;
    }

    @OneToMany
    public List<SubjectEntity> getRegistrations() {

        return this.registrations;
    }

    public void setRegistrations(List<SubjectEntity> registrations) {

        this.registrations = registrations;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (null == obj) {
            return false;
        }
        if (false == (obj instanceof DeviceSubjectEntity)) {
            return false;
        }
        DeviceSubjectEntity rhs = (DeviceSubjectEntity) obj;
        return new EqualsBuilder().append(this.id, rhs.id).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(this.id).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("id", this.id).toString();
    }


    public interface QueryInterface {

        @QueryMethod(value = QUERY_REGISTRATION, nullable = true)
        DeviceSubjectEntity findSubject(@QueryParam("registration") SubjectEntity registration);
    }

}
