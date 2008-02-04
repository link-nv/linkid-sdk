/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.service.bean;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.performance.entity.AgentTimeEntity;
import net.link.safeonline.performance.entity.MeasurementEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.service.ProfileDataService;
import net.link.safeonline.util.performance.ProfileData;

import org.jboss.annotation.ejb.LocalBinding;

/**
 * <h2>{@link ProfileDataServiceBean} - Service bean for
 * {@link ProfileDataEntity}.</h2>
 *
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 *
 * @see ProfileDataService
 * @author mbillemo
 */
@Stateless
@LocalBinding(jndiBinding = ProfileDataService.BINDING)
public class ProfileDataServiceBean extends ProfilingServiceBean implements
		ProfileDataService {

	/**
	 * {@inheritDoc}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ProfileDataEntity addData(ProfileData data, AgentTimeEntity agentTime) {

		Set<MeasurementEntity> measurements = new HashSet<MeasurementEntity>();
		for (Map.Entry<String, Long> measurement : data.getMeasurements()
				.entrySet()) {
			MeasurementEntity measurementEntity = new MeasurementEntity(
					measurement.getKey(), measurement.getValue());
			this.em.persist(measurementEntity);

			measurements.add(measurementEntity);
		}

		ProfileDataEntity dataEntity = new ProfileDataEntity(agentTime
				.getStart(), measurements);
		this.em.persist(dataEntity);

		return dataEntity;
	}
}
