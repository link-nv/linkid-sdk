/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.RegisteredDeviceEntity.QUERY_LIST_SUBJECT;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

@Entity
@Table(name = "registeredDevices")
@NamedQueries( { @NamedQuery(name = QUERY_LIST_SUBJECT, query = "SELECT regDevice "
		+ "FROM RegisteredDeviceEntity AS regDevice "
		+ "WHERE regDevice.subject = :subject") })
public class RegisteredDeviceEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_LIST_SUBJECT = "reg.dev.sub";

	private SubjectEntity subject;

	private String id;

	private String humanReadableId;

	private DeviceEntity device;

	public RegisteredDeviceEntity(SubjectEntity subject, String id,
			String humanReadableId, DeviceEntity device) {
		this.subject = subject;
		this.id = id;
		this.humanReadableId = humanReadableId;
		this.device = device;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "subject", insertable = false, updatable = false)
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

	public String getHumanReadableId() {
		return this.humanReadableId;
	}

	public void setHumanReadableId(String humanReadableId) {
		this.humanReadableId = humanReadableId;
	}

	@ManyToOne(optional = false)
	public DeviceEntity getDevice() {
		return this.device;
	}

	public void setDevice(DeviceEntity device) {
		this.device = device;
	}

	public interface QueryInterface {
		@QueryMethod(QUERY_LIST_SUBJECT)
		List<RegisteredDeviceEntity> listRegisteredDevices(
				@QueryParam("subject")
				SubjectEntity subject);
	}

}
