/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.service.bean;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.performance.DriverException;
import net.link.safeonline.performance.entity.DriverExceptionEntity;
import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.service.DriverExceptionService;
import net.link.safeonline.util.performance.ProfileData;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link DriverExceptionServiceBean}<br>
 * <sub>Service bean for {@link DriverExceptionEntity}.</sub></h2>
 * 
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 * 
 * @see DriverExceptionService
 * @author mbillemo
 */
@Stateless
@LocalBinding(jndiBinding = DriverExceptionService.JNDI_BINDING)
public class DriverExceptionServiceBean extends AbstractProfilingServiceBean implements DriverExceptionService {

    /**
     * {@inheritDoc}
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public DriverExceptionEntity addException(DriverProfileEntity profile, DriverException exception) {

        // Dig for the root cause.
        Throwable cause = exception;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        // Format this cause into a message.
        int errorSourceLine = -1;
        String errorSourceClass = null;
        StackTraceElement errorSource = null;

        if (cause.getStackTrace().length > 0) {
            errorSource = cause.getStackTrace()[0];
            errorSourceClass = ProfileData.compressSignature(errorSource.getClassName());
            errorSourceLine = errorSource.getLineNumber();
        }

        String errorClass = ProfileData.compressSignature(cause.getClass().getName());
        String message = String.format("%s: %s (%s:%d)", errorClass, cause.getMessage(), errorSourceClass, errorSourceLine);

        // Create the exception entity.
        DriverExceptionEntity exceptionEntity = new DriverExceptionEntity(profile, exception.getOccurredTime(), message);
        this.em.persist(exceptionEntity);

        return exceptionEntity;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<DriverExceptionEntity> getAllProfileErrors(DriverProfileEntity profile) {

        return this.em.createNamedQuery(DriverExceptionEntity.getByProfile).setParameter("profile", profile).getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<DriverExceptionEntity> getProfileErrors(DriverProfileEntity profile, int dataPoints) {

        // Find the driver profile's profile data.
        Long dataDuration = (Long) this.em.createNamedQuery(DriverExceptionEntity.getExecutionDuration).setParameter("profile", profile)
                                          .getSingleResult();
        Long dataStart = (Long) this.em.createNamedQuery(DriverExceptionEntity.getExecutionStart).setParameter("profile", profile)
                                       .getSingleResult();

        // Bail out of there are no errors for this profile.
        if (dataDuration == null || dataStart == null || dataDuration + dataStart == 0)
            return new ArrayList<DriverExceptionEntity>();

        int period = (int) Math.ceil((double) dataDuration / dataPoints);

        List<DriverExceptionEntity> pointData = new ArrayList<DriverExceptionEntity>();
        for (long point = 0; point * period < dataDuration; ++point) {
            pointData.addAll(this.em.createNamedQuery(DriverExceptionEntity.createAverage).setParameter("profile", profile).setParameter(
                    "start", dataStart + point * period).setParameter("stop", dataStart + (point + 1) * period).getResultList());
        }

        return pointData;
    }
}
