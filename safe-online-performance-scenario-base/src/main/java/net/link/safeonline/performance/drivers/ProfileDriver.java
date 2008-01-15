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
import net.link.safeonline.performance.entity.DriverExceptionEntity;
import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.service.DriverExceptionService;
import net.link.safeonline.performance.service.DriverProfileService;
import net.link.safeonline.performance.service.ProfileDataService;
import net.link.safeonline.sdk.ws.MessageAccessor;
import net.link.safeonline.util.performance.ProfileData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract class of a service driver. This class manages the internals; such as
 * collecting profile data, exceptions and execution speed for iterations.<br>
 * <br>
 * Implementing drivers need to declare methods specific to their functionality
 * in which they should call {@link #loadDriver()} before performing any driver
 * logic and {@link #report(MessageAccessor)} once they have completed their
 * task; or {@link #report(Exception)} if an error occurred during the work they
 * were doing. <br>
 * <br>
 * The profiling data will be gathered by this class and can later be retrieved
 * by using {@link #getProfile()}.<br>
 *
 * @author mbillemo
 */
public abstract class ProfileDriver {

	private static final Log LOG = LogFactory.getLog(ProfileDriver.class);

	private ThreadLocal<DriverProfileService> driverProfileService = new ThreadLocal<DriverProfileService>() {
		@Override
		protected DriverProfileService initialValue() {

			return getService(DriverProfileService.class,
					DriverProfileService.BINDING);
		}
	};
	private ThreadLocal<ProfileDataService> profileDataService = new ThreadLocal<ProfileDataService>() {

		@Override
		protected ProfileDataService initialValue() {

			return getService(ProfileDataService.class,
					ProfileDataService.BINDING);
		}
	};
	private ThreadLocal<DriverExceptionService> driverExceptionService = new ThreadLocal<DriverExceptionService>() {

		@Override
		protected DriverExceptionService initialValue() {

			return getService(DriverExceptionService.class,
					DriverExceptionService.BINDING);
		}
	};

	private String host;
	private String title;
	private ExecutionEntity execution;

	private DriverProfileEntity profile;

	public ProfileDriver(String hostname, String title,
			ExecutionEntity execution) {

		this.host = hostname;
		this.title = title;
		this.execution = execution;

		this.profile = this.driverProfileService.get().addProfile(title,
				execution);
	}

	public String getHost() {

		return this.host;
	}

	public String getTitle() {

		return this.title;
	}

	public DriverProfileEntity getProfile() {

		try {
		return this.driverProfileService.get().getProfile(this.title,
				this.execution);
		} catch (IllegalStateException e) {
			return this.profile;
		}
	}

	protected void report(MessageAccessor service) {

		report(new ProfileData(service.getHeaders()));
	}

	protected void report(ProfileData profileData) {

		this.driverProfileService.get().register(getProfile(),
				this.profileDataService.get().addData(profileData));
	}

	protected DriverException report(Exception error) {

		LOG.warn(String.format("Failed driver request: %s", error));

		DriverException driverException;
		if (error instanceof DriverException)
			driverException = (DriverException) error;
		else
			driverException = new DriverException(error);

		DriverExceptionEntity exceptionEntity = this.driverExceptionService
				.get().addException(driverException);
		this.driverProfileService.get().register(getProfile(), exceptionEntity);

		return driverException;
	}

	<S> S getService(Class<S> service, String binding) {

		try {
			InitialContext initialContext = new InitialContext();
			return service.cast(initialContext.lookup(binding));
		} catch (NoInitialContextException e) {
			LOG.warn("Initial context not set up; "
					+ "assuming we're not running in an "
					+ "enterprise container");

			try {
				return service.cast(Class.forName(
						service.getName().replaceFirst("\\.([^\\.]*)$",
								".bean.$1Bean")).newInstance());
			} catch (InstantiationException ee) {
				LOG.error("Couldn't create service " + service + " at "
						+ binding, ee);
				throw new RuntimeException(ee);
			} catch (IllegalAccessException ee) {
				LOG.error("Couldn't access service " + service + " at "
						+ binding, ee);
				throw new RuntimeException(ee);
			} catch (ClassNotFoundException ee) {
				LOG.error("Couldn't find service "
						+ service.getName().replaceFirst("\\.([^\\.]*)$",
								".bean.$1Bean") + " at " + binding, ee);
				throw new RuntimeException(ee);
			}
		} catch (NamingException e) {
			LOG.error("Couldn't find service " + service + " at " + binding, e);
			throw new RuntimeException(e);
		}
	}
}
