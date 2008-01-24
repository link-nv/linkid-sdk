/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 * <h2>{@link DriverProfileEntity} - Links the {@link ProfileDataEntity}s that
 * belong to a certain driver in a certain execution.</h2>
 *
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Entity
@NamedQuery(name = DriverProfileEntity.findByExecution, query = "SELECT p"
		+ "    FROM DriverProfileEntity p"
		+ "    WHERE p.driverName = :driverName AND p.execution = :execution")
public class DriverProfileEntity {

	public static final String findByExecution = "DriverProfileEntity.findByExecution";

	@Id
	@SuppressWarnings("unused")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private String driverName;

	@ManyToOne
	@SuppressWarnings("unused")
	private ExecutionEntity execution;

	@OneToMany()
	private Set<ProfileDataEntity> profileData;

	@OneToMany()
	private Set<DriverExceptionEntity> profileError;

	public DriverProfileEntity() {

		this.profileData = new HashSet<ProfileDataEntity>();
		this.profileError = new HashSet<DriverExceptionEntity>();
	}

	public DriverProfileEntity(String driverName, ExecutionEntity execution) {

		this.driverName = driverName;
		this.execution = execution;

		this.profileData = new HashSet<ProfileDataEntity>();
		this.profileError = new HashSet<DriverExceptionEntity>();
	}

	/**
	 * @return The name of the driver that this profile applies to.
	 */
	public String getDriverName() {

		return this.driverName;
	}

	/**
	 * @return The data collected in this driver's profile.
	 */
	public Set<ProfileDataEntity> getProfileData() {

		return this.profileData;
	}

	/**
	 * @return The problems collected in this driver's profile.
	 */
	public Set<DriverExceptionEntity> getProfileError() {

		return this.profileError;
	}

	/**
	 * Register profiling data of a single driver execution.
	 */
	public void register(ProfileDataEntity data) {

		this.profileData.add(data);
	}

	/**
	 * Register a problem that occurred during a single driver execution.
	 */
	public void register(DriverExceptionEntity error) {

		this.profileError.add(error);
	}
}
