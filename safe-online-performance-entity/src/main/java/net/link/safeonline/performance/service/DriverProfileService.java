/*
 *   Copyright 2008, Maarten Billemont
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package net.link.safeonline.performance.service;

import javax.ejb.Local;

import net.link.safeonline.performance.entity.DriverExceptionEntity;
import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;

/**
 * <h2>{@link DriverProfileService} - [in short] (TODO).</h2>
 * <p>
 * [description / usage].
 * </p>
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Local
public interface DriverProfileService {

	public static final String BINDING = "SafeOnline/DriverProfileService";

	/**
	 * Add a new driver profile to the database.
	 */
	public DriverProfileEntity addProfile(String driverName,
			ExecutionEntity execution);

	/**
	 * Retrieve the driver profile of the given execution and for the driver
	 * with the given name.
	 */
	public DriverProfileEntity getProfile(String driverName,
			ExecutionEntity execution);

	/**
	 * Register new profiled data with the given driver profile.
	 */
	public void register(DriverProfileEntity driverProfile,
			ProfileDataEntity data);

	/**
	 * Register a new problem with the given driver profile.
	 */
	public void register(DriverProfileEntity driverProfile,
			DriverExceptionEntity exception);

}
