package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import static net.link.safeonline.entity.AllowedDeviceEntity.QUERY_WHERE_APPLICATION;
import static net.link.safeonline.entity.AllowedDeviceEntity.DELETE_WHERE_APPLICATION;

@Entity
@Table(name = "alloweddevices", uniqueConstraints = @UniqueConstraint(columnNames = {
		"application", "device" }))
@NamedQueries( {
		@NamedQuery(name = QUERY_WHERE_APPLICATION, query = "SELECT allowedDevice "
				+ "FROM AllowedDeviceEntity AS allowedDevice "
				+ "WHERE allowedDevice.application = :application"),
		@NamedQuery(name = DELETE_WHERE_APPLICATION, query = "DELETE FROM AllowedDeviceEntity "
				+ "AS allowedDevice WHERE allowedDevice.application = :application") })
public class AllowedDeviceEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_APPLICATION = "alloweddevice.app";

	public static final String DELETE_WHERE_APPLICATION = "alloweddevice.del";

	private long id;

	private ApplicationEntity application;

	private DeviceEntity device;

	private int weight;

	public AllowedDeviceEntity() {
		// empty
	}

	public AllowedDeviceEntity(ApplicationEntity application,
			DeviceEntity device, int weight) {
		this.application = application;
		this.device = device;
		this.weight = weight;
	}

	@ManyToOne
	@JoinColumn(name = "application", nullable = false)
	public ApplicationEntity getApplication() {
		return this.application;
	}

	public void setApplication(ApplicationEntity application) {
		this.application = application;
	}

	@ManyToOne
	@JoinColumn(name = "device", nullable = false)
	public DeviceEntity getDevice() {
		return this.device;
	}

	public void setDevice(DeviceEntity device) {
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

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public static Query createQueryListAllowedDevicesByApplication(
			EntityManager entityManager, ApplicationEntity application) {
		Query query = entityManager.createNamedQuery(QUERY_WHERE_APPLICATION);
		query.setParameter("application", application);
		return query;
	}

	public static Query createQueryDeleteByApplication(
			EntityManager entityManager, ApplicationEntity application) {
		Query query = entityManager.createNamedQuery(DELETE_WHERE_APPLICATION);
		query.setParameter("application", application);
		return query;
	}

}
