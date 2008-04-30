/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.notification;

import static net.link.safeonline.entity.notification.EndpointReferenceEntity.QUERY_LIST_ALL;
import static net.link.safeonline.entity.notification.EndpointReferenceEntity.QUERY_WHERE_ADDRESS_APPLICATION;
import static net.link.safeonline.entity.notification.EndpointReferenceEntity.QUERY_WHERE_ADDRESS_DEVICE;
import static net.link.safeonline.entity.notification.EndpointReferenceEntity.QUERY_WHERE_APPLICATION;
import static net.link.safeonline.entity.notification.EndpointReferenceEntity.QUERY_WHERE_DEVICE;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.EqualsBuilder;
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
		@NamedQuery(name = QUERY_WHERE_ADDRESS_DEVICE, query = "SELECT epr "
				+ "FROM EndpointReferenceEntity AS epr "
				+ "WHERE epr.address = :address AND epr.device = :device"),
		@NamedQuery(name = QUERY_WHERE_ADDRESS_APPLICATION, query = "SELECT epr "
				+ "FROM EndpointReferenceEntity AS epr "
				+ "WHERE epr.address = :address AND "
				+ "epr.application = :application"),
		@NamedQuery(name = QUERY_WHERE_APPLICATION, query = "SELECT epr "
				+ "FROM EndpointReferenceEntity AS epr "
				+ "WHERE epr.application = :application"),
		@NamedQuery(name = QUERY_WHERE_DEVICE, query = "SELECT epr "
				+ "FROM EndpointReferenceEntity AS epr "
				+ "WHERE epr.device = :device") })
public class EndpointReferenceEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_LIST_ALL = "epr.list.all";

	public static final String QUERY_WHERE_ADDRESS_DEVICE = "epr.add.dev";

	public static final String QUERY_WHERE_ADDRESS_APPLICATION = "epr.add.app";

	public static final String QUERY_WHERE_DEVICE = "epr.dev";

	public static final String QUERY_WHERE_APPLICATION = "epr.app";

	private long id;

	private String address;

	private ApplicationEntity application;

	private DeviceEntity device;

	public EndpointReferenceEntity() {
		// empty
	}

	public EndpointReferenceEntity(String address, ApplicationEntity application) {
		this.address = address;
		this.application = application;
	}

	public EndpointReferenceEntity(String address, DeviceEntity device) {
		this.address = address;
		this.device = device;
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
	public DeviceEntity getDevice() {
		return this.device;
	}

	public void setDevice(DeviceEntity device) {
		this.device = device;
	}

	@Transient
	public String getName() {
		if (null != this.application)
			return this.application.getName();
		return this.device.getName();
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("address", this.address);
		if (null != this.application)
			builder.append("application", this.application.getName());
		if (null != this.device)
			builder.append("device", this.device.getName());
		return builder.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (false == obj instanceof EndpointReferenceEntity) {
			return false;
		}
		EndpointReferenceEntity rhs = (EndpointReferenceEntity) obj;
		EqualsBuilder builder = new EqualsBuilder();
		builder.append(this.address, rhs.address);
		if (null != this.application)
			builder.append(this.application, rhs.application);
		if (null != this.device)
			builder.append(this.device, rhs.device);
		return builder.isEquals();
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(this.address);
		if (null != this.application)
			builder.append(this.application);
		if (null != this.device)
			builder.append(this.device);
		return builder.toHashCode();
	}

	public interface QueryInterface {
		@QueryMethod(value = QUERY_LIST_ALL)
		List<EndpointReferenceEntity> listEndpoints();

		@QueryMethod(value = QUERY_WHERE_ADDRESS_DEVICE, nullable = true)
		EndpointReferenceEntity find(@QueryParam("address") String address,
				@QueryParam("device") DeviceEntity device);

		@QueryMethod(value = QUERY_WHERE_ADDRESS_APPLICATION, nullable = true)
		EndpointReferenceEntity find(@QueryParam("address") String address,
				@QueryParam("application") ApplicationEntity application);

		@QueryMethod(value = QUERY_WHERE_DEVICE)
		List<EndpointReferenceEntity> listEndpoints(
				@QueryParam("device") DeviceEntity device);

		@QueryMethod(value = QUERY_WHERE_APPLICATION)
		List<EndpointReferenceEntity> listEndpoints(
				@QueryParam("application") ApplicationEntity application);

	}
}
