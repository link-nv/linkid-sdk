/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.service.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.NoResultException;

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
public class ProfileDataServiceBean extends AbstractProfilingServiceBean implements ProfileDataService {

    /**
     * {@inheritDoc}
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public ProfileDataEntity addData(DriverProfileEntity profile, ProfileData data, ScenarioTimingEntity agentTime) {

        ProfileDataEntity dataEntity = new ProfileDataEntity(profile, agentTime);
        this.em.persist(dataEntity);

        for (Map.Entry<String, Long> measurement : data.getMeasurements().entrySet()) {
            this.em.persist(new MeasurementEntity(dataEntity, measurement.getKey(), measurement.getValue()));
        }

        return dataEntity;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<ProfileDataEntity> getAllProfileData(DriverProfileEntity profile) {

        return this.em.createNamedQuery(ProfileDataEntity.getByProfile).setParameter("profile", profile)
                .getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<ProfileDataEntity> getProfileData(DriverProfileEntity profile, int dataPoints) {

        // Find the driver profile's profile data.
        Long dataDuration = (Long) this.em.createNamedQuery(ProfileDataEntity.getExecutionDuration).setParameter(
                "profile", profile).getSingleResult();
        Long dataStart = (Long) this.em.createNamedQuery(ProfileDataEntity.getExecutionStart).setParameter("profile",
                profile).getSingleResult();

        // Bail out of there is no data for this profile.
        if (dataDuration == null || dataStart == null || dataDuration + dataStart == 0) {
            LOG.warn("No data for profile: " + profile.getDriverClassName());
            return new ArrayList<ProfileDataEntity>();
        }

        int period = (int) Math.ceil((double) dataDuration / dataPoints);

        List<ProfileDataEntity> pointData = new ArrayList<ProfileDataEntity>((int) (dataDuration / period) + 1);
        for (long point = 0; point * period < dataDuration; ++point) {
            try {
                ScenarioTimingEntity timing = (ScenarioTimingEntity) this.em.createNamedQuery(
                        ProfileDataEntity.getScenarioTiming).setParameter("execution", profile.getExecution())
                        .setParameter("start", dataStart + point * period).setParameter("stop",
                                dataStart + (point + 1) * period).getSingleResult();

                ProfileDataEntity profileDataEntity = new ProfileDataEntity(profile, timing);
                pointData.add(profileDataEntity);

                List<MeasurementEntity> measurements = this.em.createNamedQuery(ProfileDataEntity.createAverage)
                        .setParameter("profile", profile).setParameter("start", dataStart + point * period)
                        .setParameter("stop", dataStart + (point + 1) * period).getResultList();
                for (MeasurementEntity measurement : measurements) {
                    measurement.setProfileData(profileDataEntity);
                    profileDataEntity.getMeasurements().add(measurement);
                }
            } catch (NoResultException e) {
            }
        }

        return pointData;
    }
}
