/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sms.clickatell.osgi;

import net.link.safeonline.osgi.OlasConfigurationService;
import net.link.safeonline.osgi.sms.SmsService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.impl.LogServiceLoggerFactory;

public class ClickatellSmsServiceActivator implements BundleActivator {

	ServiceRegistration clickatellSmsServiceRegistration;

	static OlasConfigurationService configuration;
	static ServiceReference configurationServiceReference;

	public static final String CONFIG_GROUP_NAME = "Clickatell";
	public static final String CONFIG_USERNAME = "Username";
	public static final String CONFIG_PASSWORD = "Password";
	public static final String CONFIG_API_ID = "API Id";
	public static final String CONFIG_URL = "URL";
	public static final String CONFIG_DEFAULT_USERNAME = "test";
	public static final String CONFIG_DEFAULT_PASSWORD = "test";
	public static final String CONFIG_DEFAULT_API_ID = "1234";
	public static final String CONFIG_DEFAULT_URL = "http://api.clickatell.com/soap/webservice.php";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {

		LogServiceLoggerFactory.open(context);

		configurationServiceReference = context
				.getServiceReference(OlasConfigurationService.class.getName());
		configuration = (OlasConfigurationService) context
				.getService(configurationServiceReference);
		configuration.initConfigurationValue(CONFIG_GROUP_NAME,
				CONFIG_USERNAME, CONFIG_DEFAULT_USERNAME);
		configuration.initConfigurationValue(CONFIG_GROUP_NAME,
				CONFIG_PASSWORD, CONFIG_DEFAULT_PASSWORD);
		configuration.initConfigurationValue(CONFIG_GROUP_NAME, CONFIG_API_ID,
				CONFIG_DEFAULT_API_ID);

		ClickatellSmsServiceFactory smsServiceFactory = new ClickatellSmsServiceFactory();
		clickatellSmsServiceRegistration = context.registerService(
				SmsService.class.getName(), smsServiceFactory, null);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		context.ungetService(configurationServiceReference);
		clickatellSmsServiceRegistration.unregister();
		LogServiceLoggerFactory.close();
	}
}
