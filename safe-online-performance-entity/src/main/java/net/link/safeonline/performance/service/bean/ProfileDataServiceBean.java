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

		ProfileDataEntity dataEntity = new ProfileDataEntity(profile, agentTime);
		this.em.persist(dataEntity);

		for (Map.Entry<String, Long> measurement : data.getMeasurements()
				.entrySet())
			this.em.persist(new MeasurementEntity(dataEntity, measurement
					.getKey(), measurement.getValue()));

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

			ProfileDataEntity profileDataEntity = new ProfileDataEntity(
					profileData.get(0).getProfile(), profileData.get(0)
							.getScenarioTiming());
			pointData.add(profileDataEntity);

			for (String measurement : durations.keySet())
				profileDataEntity.getMeasurements().add(
						new MeasurementEntity(profileDataEntity, measurement,
								durations.get(measurement)
										/ counts.get(measurement)));

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

			ScenarioTimingEntity timing = (ScenarioTimingEntity) this.em
					.createNamedQuery(ProfileDataEntity.getScenarioTiming)
					.setParameter("profile", profile).setParameter("start",
							dataStart + point * period).setParameter("stop",
							dataStart + (point + 1) * period).getSingleResult();

			ProfileDataEntity profileDataEntity = new ProfileDataEntity(
					profile, timing);
			pointData.add(profileDataEntity);

			List<MeasurementEntity> measurements = this.em.createNamedQuery(
					ProfileDataEntity.createAverage).setParameter("profile",
					profile).setParameter("start", dataStart + point * period)
					.setParameter("stop", dataStart + (point + 1) * period)
					.getResultList();
			for (MeasurementEntity measurement : measurements) {
				measurement.setProfileData(profileDataEntity);
				profileDataEntity.getMeasurements().add(measurement);
			}

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
