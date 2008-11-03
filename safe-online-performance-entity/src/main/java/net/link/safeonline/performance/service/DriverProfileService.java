/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.service;

import javax.ejb.Local;

import net.link.safeonline.performance.entity.DriverExceptionEntity;
import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.service.bean.DriverProfileServiceBean;


/**
 * <h2>{@link DriverProfileServiceBean}<br>
 * <sub>Service bean for {@link DriverProfileEntity}.</sub></h2>
 * 
 * <p>
 * Create and access {@link DriverProfileEntity}s. Register {@link ProfileDataEntity}s or {@link DriverExceptionEntity}s in them.
 * </p>
 * 
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Local
public interface DriverProfileService {

    public static final String JNDI_BINDING = "SafeOnline/DriverProfileService";


    /**
     * Add a new driver profile to the database.
     */
    public DriverProfileEntity addProfile(String driverName, ExecutionEntity execution);

    /**
     * Retrieve the driver profile of the given execution and for the driver with the given name.
     */
    public DriverProfileEntity getProfile(String driverName, ExecutionEntity execution);
}
