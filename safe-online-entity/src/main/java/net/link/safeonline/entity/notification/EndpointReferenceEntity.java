/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.notification;

import static net.link.safeonline.entity.notification.EndpointReferenceEntity.QUERY_WHERE_ADDRESS_APPLICATION;
import static net.link.safeonline.entity.notification.EndpointReferenceEntity.QUERY_WHERE_ADDRESS_DEVICE;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

/**
 * Entity representing a W3CEndpointReference.
 * 
 * @author wvdhaute
 * 
 */
@Entity
@Table(name = "endpoint_ref")
@NamedQueries( {
		@NamedQuery(name = QUERY_WHERE_ADDRESS_DEVICE, query = "SELECT epr "
				+ "FROM EndpointReferenceEntity AS epr "
				+ "WHERE epr.address = :address AND epr.device = :device"),
		@NamedQuery(name = QUERY_WHERE_ADDRESS_APPLICATION, query = "SELECT epr "
				+ "FROM EndpointReferenceEntity AS epr "
				+ "WHERE epr.address = :address AND "
				+ "epr.application = :application") })
public class EndpointReferenceEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_ADDRESS_DEVICE = "epr.add.dev";

	public static final String QUERY_WHERE_ADDRESS_APPLICATION = "epr.add.app";

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

	@Column(nullable = true)
	public ApplicationEntity getApplication() {
		return this.application;
	}

	public void setApplication(ApplicationEntity application) {
		this.application = application;
	}

	@Column(nullable = true)
	public DeviceEntity getDevice() {
		return this.device;
	}

	public void setDevice(DeviceEntity device) {
		this.device = device;
	}

	public interface QueryInterface {
		@QueryMethod(value = QUERY_WHERE_ADDRESS_DEVICE, nullable = true)
		EndpointReferenceEntity find(@QueryParam("address")
		String address, @QueryParam("device")
		DeviceEntity device);

		@QueryMethod(value = QUERY_WHERE_ADDRESS_APPLICATION, nullable = true)
		EndpointReferenceEntity find(@QueryParam("address")
		String address, @QueryParam("application")
		ApplicationEntity application);

	}
}
