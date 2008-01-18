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
package net.link.safeonline.performance.service.bean;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;

import net.link.safeonline.performance.entity.MeasurementEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.service.ProfileDataService;
import net.link.safeonline.util.performance.ProfileData;

import org.jboss.annotation.ejb.LocalBinding;

/**
 * <h2>{@link ProfileDataServiceBean} - [in short] (TODO).</h2>
 * <p>
 * [description / usage].
 * </p>
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Stateless
@LocalBinding(jndiBinding = ProfileDataService.BINDING)
public class ProfileDataServiceBean extends ProfilingServiceBean implements
		ProfileDataService {

	/**
	 * {@inheritDoc}
	 */
	public ProfileDataEntity addData(ProfileData data) {

		Set<MeasurementEntity> measurements = new HashSet<MeasurementEntity>();
		for (Map.Entry<String, Long> measurement : data.getMeasurements()
				.entrySet()) {
			MeasurementEntity measurementEntity = new MeasurementEntity(
					measurement.getKey(), measurement.getValue());
			this.em.persist(measurementEntity);

			measurements.add(measurementEntity);
		}

		ProfileDataEntity dataEntity = new ProfileDataEntity(measurements);
		this.em.persist(dataEntity);

		return dataEntity;
	}
}
