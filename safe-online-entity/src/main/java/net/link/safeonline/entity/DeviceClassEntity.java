/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.DeviceClassEntity.QUERY_LIST_ALL;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import net.link.safeonline.jpa.annotation.QueryMethod;

@Entity
@Table(name = "deviceClasses")
@NamedQueries( { @NamedQuery(name = QUERY_LIST_ALL, query = "FROM DeviceClassEntity dc") })
public class DeviceClassEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_LIST_ALL = "dc.all";

	private String name;

	private List<DeviceEntity> devices;

	private Map<String, DeviceClassDescriptionEntity> descriptions;

	public DeviceClassEntity() {
		// empty
	}

	public DeviceClassEntity(String name) {
		this.name = name;
	}

	@Id
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns list of devices of this device class.
	 * 
	 * @return
	 */
	@OneToMany
	public List<DeviceEntity> getDevices() {
		return this.devices;
	}

	public void setDevices(List<DeviceEntity> devices) {
		this.devices = devices;
	}

	/**
	 * Returns map of i18n device class descriptions.
	 * 
	 * @return
	 */
	@OneToMany(mappedBy = "deviceClass")
	@MapKey(name = "language")
	public Map<String, DeviceClassDescriptionEntity> getDescriptions() {
		return this.descriptions;
	}

	public void setDescriptions(
			Map<String, DeviceClassDescriptionEntity> descriptions) {
		this.descriptions = descriptions;
	}

	public interface QueryInterface {
		@QueryMethod(QUERY_LIST_ALL)
		List<DeviceClassEntity> listDeviceClasses();
	}

}
