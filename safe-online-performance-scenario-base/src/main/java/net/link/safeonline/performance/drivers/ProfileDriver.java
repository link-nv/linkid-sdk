/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance.drivers;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

import net.link.safeonline.performance.DriverException;
import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;
import net.link.safeonline.performance.service.DriverExceptionService;
import net.link.safeonline.performance.service.DriverProfileService;
import net.link.safeonline.performance.service.ProfileDataService;
import net.link.safeonline.sdk.ws.MessageAccessor;
import net.link.safeonline.util.performance.ProfileData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link ProfileDriver}<br>
 * <sub>Takes care of the internals that all drivers require.</sub></h2>
 * 
 * <p>
 * Abstract class of a service driver. This class manages the internals; such as persisting profile data and exceptions for driver
 * executions.<br>
 * <br>
 * Implementing drivers need to declare methods specific to their functionality in which they should call {@link #report(MessageAccessor)}
 * once they have completed their task; or {@link #report(Throwable)} if an error occurred during the work they were doing. <br>
 * <br>
 * The profiling data will be gathered by this class and can later be retrieved by using {@link #getProfile()}.<br>
 * </p>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public abstract class ProfileDriver {

    private final Log                    LOG                    = LogFactory.getLog(getClass());

    private final DriverProfileService   driverProfileService   = getService(DriverProfileService.class);
    private final ProfileDataService     profileDataService     = getService(ProfileDataService.class);
    private final DriverExceptionService driverExceptionService = getService(DriverExceptionService.class);

    private String                       title;
    private ExecutionEntity              execution;
    private ScenarioTimingEntity         agentTime;
    private DriverProfileEntity          profile;


    public ProfileDriver(String title, ExecutionEntity execution, ScenarioTimingEntity agentTime) {

        this.title = title;
        this.execution = execution;
        this.agentTime = agentTime;

        profile = driverProfileService.getProfile(getClass().getCanonicalName(), execution);
    }

    /**
     * @return A description of what this driver does.
     */
    public abstract String getDescription();

    public String getHost() {

        return String.format("%s://%s", (execution.isSsl()? "https": "http"), execution.getHostname());
    }

    public String getTitle() {

        return title;
    }

    public DriverProfileEntity getProfile() {

        return profile;
    }

    protected void report(MessageAccessor service) {

        report(new ProfileData(service.getHeaders()));
    }

    protected void report(ProfileData profileData) {

        profileDataService.addData(profile, profileData, agentTime);
        agentTime.addOlasTime(profileData.getMeasurement(ProfileData.REQUEST_DELTA_TIME));
    }

    protected IllegalStateException report(Throwable error) {

        LOG.warn(String.format("Failed driver request: %s", error));

        DriverException driverException;
        if (error instanceof DriverException) {
            driverException = (DriverException) error;
        } else {
            driverException = new DriverException(error);
        }

        driverExceptionService.addException(profile, driverException);

        return new IllegalStateException(error);
    }

    // FIXME
    <S> S getService(Class<S> service) {

        try {
            String binding = (String) service.getField("JNDI_BINDING").get(null);
            try {
                InitialContext initialContext = new InitialContext();
                return service.cast(initialContext.lookup(binding));
            }

            catch (NoInitialContextException e) {
                String beanName = service.getName().replaceFirst("\\.([^\\.]*)$", ".bean.$1Bean");
                try {
                    return service.cast(Class.forName(beanName).newInstance());
                } catch (InstantiationException ee) {
                    LOG.error("Couldn't create service " + service + " at " + beanName, ee);
                    throw new RuntimeException(ee);
                } catch (IllegalAccessException ee) {
                    LOG.error("Couldn't access service " + service + " at " + beanName, ee);
                    throw new RuntimeException(ee);
                } catch (ClassNotFoundException ee) {
                    LOG.error("Couldn't find service " + service + " at " + beanName, ee);
                    throw new RuntimeException(ee);
                }
            } catch (NamingException e) {
                LOG.error("Couldn't find service " + service + " at " + binding, e);
                throw new RuntimeException(e);
            }
        }

        catch (IllegalArgumentException e) {
            LOG.error("JNDI_BINDING field for " + service + " not static?", e);
            throw e;
        } catch (SecurityException e) {
            LOG.error("No permission to access JNDI_BINDING field for " + service, e);
            throw e;
        } catch (IllegalAccessException e) {
            LOG.error("Access denied to JNDI_BINDING field for " + service, e);
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            LOG.error("JNDI_BINDING field for " + service + " not declared.", e);
            throw new RuntimeException(e);
        }
    }
}
