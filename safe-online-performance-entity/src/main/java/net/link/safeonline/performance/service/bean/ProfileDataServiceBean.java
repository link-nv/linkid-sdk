/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.service.bean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.Query;

import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.MeasurementEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;
import net.link.safeonline.performance.service.ProfileDataService;
import net.link.safeonline.util.performance.ProfileData;

import org.jboss.annotation.ejb.LocalBinding;

/**
 * <h2>{@link ProfileDataServiceBean}<br>
 * <sub>Service bean for {@link ProfileDataEntity}.</sub></h2>
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
	public ProfileDataEntity addData(DriverProfileEntity profile,
			ProfileData data, ScenarioTimingEntity agentTime) {

		Set<MeasurementEntity> measurements = new HashSet<MeasurementEntity>();
		for (Map.Entry<String, Long> measurement : data.getMeasurements()
				.entrySet()) {
			MeasurementEntity measurementEntity = new MeasurementEntity(
					measurement.getKey(), measurement.getValue());
			this.em.persist(measurementEntity);

			measurements.add(measurementEntity);
		}

		ProfileDataEntity dataEntity = new ProfileDataEntity(profile,
				agentTime, measurements);
		this.em.persist(dataEntity);

		return dataEntity;
	}

	@SuppressWarnings("unchecked")
	public Set<ProfileDataEntity> getProfileData_All(
			DriverProfileEntity profile,
			@SuppressWarnings("unused") int dataPoints) {

		return new HashSet<ProfileDataEntity>(this.em.createNamedQuery(
				ProfileDataEntity.getByProfile)
				.setParameter("profile", profile).getResultList());
	}

	@SuppressWarnings("unchecked")
	public Set<ProfileDataEntity> getProfileData_JavaPager(
			DriverProfileEntity profile, int dataPoints) {

		// Calculate how many ProfileDataEntities to use for one averaging.
		double dataCount = (Long) this.em.createNamedQuery(
				ProfileDataEntity.countByProfile).setParameter("profile",
				profile).getSingleResult();
		int period = (int) Math.ceil(dataCount / dataPoints);

		// Fetch the ProfileDataEntities in pages of 'period'.
		Query profileDataQuery = this.em.createNamedQuery(
				ProfileDataEntity.getByProfile)
				.setParameter("profile", profile).setMaxResults(period);

		// Average each page into one new ProfileDataEntity.
		List<ProfileDataEntity> profileData;
		Set<ProfileDataEntity> pointData = new HashSet<ProfileDataEntity>();
		for (int point = 0; (profileData = profileDataQuery.setFirstResult(
				point * period).getResultList()) != null; ++point) {
			if (profileData.isEmpty())
				break;

			Map<String, Long> durations = new HashMap<String, Long>();
			Map<String, Integer> counts = new HashMap<String, Integer>();
			for (ProfileDataEntity d : profileData)
				for (MeasurementEntity m : d.getMeasurements())
					if (!ProfileData.REQUEST_START_TIME.equals(m
							.getMeasurement())) {
						if (!durations.containsKey(m.getMeasurement())) {
							durations.put(m.getMeasurement(), 0l);
							counts.put(m.getMeasurement(), 0);
						}

						durations.put(m.getMeasurement(), durations.get(m
								.getMeasurement())
								+ m.getDuration());
						counts.put(m.getMeasurement(), counts.get(m
								.getMeasurement()) + 1);
					} else if (!durations.containsKey(m.getMeasurement())) {
						durations.put(m.getMeasurement(), m.getDuration());
						counts.put(m.getMeasurement(), 1);
					}

			Set<MeasurementEntity> measurements = new HashSet<MeasurementEntity>();
			for (String measurement : durations.keySet())
				measurements.add(new MeasurementEntity(measurement, durations
						.get(measurement)
						/ counts.get(measurement)));

			pointData.add(new ProfileDataEntity(
					profileData.get(0).getProfile(), profileData.get(0)
							.getScenarioTiming(), measurements));
		}

		return pointData;
	}

	@SuppressWarnings("unchecked")
	public Set<ProfileDataEntity> getProfileData_SQLPager(
			DriverProfileEntity profile, int dataPoints) {

		// Find the driver profile's profile data.
		long dataDuration = (Long) this.em.createNamedQuery(
				ProfileDataEntity.getExecutionDuration).setParameter("profile",
				profile).getSingleResult();
		long dataStart = (Long) this.em.createNamedQuery(
				ProfileDataEntity.getExecutionStart).setParameter("profile",
				profile).getSingleResult();
		int period = (int) Math.ceil((double) dataDuration / dataPoints);
		System.err.println("period = dataDuration (" + dataDuration
				+ ") / dataPoints (" + dataPoints + ") = " + period);

		Set<ProfileDataEntity> pointData = new HashSet<ProfileDataEntity>();
		for (long point = 0; point * period < dataDuration; ++point) {

			List<MeasurementEntity> measurements = this.em.createNamedQuery(
					ProfileDataEntity.createAverage).setParameter("profile",
					profile).setParameter("start", dataStart + point * period)
					.setParameter("stop", dataStart + (point + 1) * period)
					.getResultList();

			ScenarioTimingEntity timing = measurements.get(
					measurements.size() / 2).getProfileData()
					.getScenarioTiming();

			pointData.add(new ProfileDataEntity(profile, timing,
					new HashSet<MeasurementEntity>(measurements)));
		}

		return pointData;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Set<ProfileDataEntity> getProfileData(DriverProfileEntity profile,
			int dataPoints) {

		return getProfileData_All(profile, dataPoints);
	}
}
