/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi;

import static net.link.safeonline.osgi.OSGIConstants.SMS_SERVICE_GROUP_NAME;
import static net.link.safeonline.osgi.OSGIConstants.SMS_SERVICE_IMPL_NAME;

import java.io.Serializable;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.config.model.ConfigurationManager;
import net.link.safeonline.osgi.attribute.OlasAttributeServiceFactory;
import net.link.safeonline.osgi.configuration.OlasConfigurationServiceFactory;
import net.link.safeonline.osgi.log.OlasLogServiceFactory;
import net.link.safeonline.osgi.plugin.PluginAttributeService;
import net.link.safeonline.osgi.sms.SmsService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;


/**
 * <h2>{@link OSGIHostActivator}<br>
 * <sub>Sets up service tracking</sub></h2>
 * 
 * <p>
 * This activator sets up service trackers and reacts appropriately when service implementations come and go
 * </p>
 * 
 * <p>
 * <i>Aug 19, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class OSGIHostActivator implements BundleActivator, ServiceListener, Serializable {

    private static final long   serialVersionUID                     = 1L;

    public static final String  JNDI_BINDING                         = SafeOnlineService.JNDI_PREFIX + "OSGI/HostActivator";

    private static final Log    LOG                                  = LogFactory.getLog(OSGIHostActivator.class);

    private ServiceTracker      pluginAttributeServiceTracker        = null;

    private ServiceTracker      smsServiceTracker                    = null;

    private ServiceRegistration olasAttributeServiceRegistration     = null;
    private ServiceRegistration olasConfigurationServiceRegistration = null;
    private ServiceRegistration olasLogServiceRegistration           = null;


    /**
     * {@inheritDoc}
     */
    public void start(BundleContext context) {

        // Register itself as service listener
        context.addServiceListener(this);

        // Initialize a service tracker for the plugin attribute service
        pluginAttributeServiceTracker = new ServiceTracker(context, PluginAttributeService.class.getName(), null);
        pluginAttributeServiceTracker.open();

        // Initialize a service tracker for the sms service
        smsServiceTracker = new ServiceTracker(context, SmsService.class.getName(), null);
        smsServiceTracker.open();

        // Initialize olas attribute service
        OlasAttributeServiceFactory attribtueServiceFactory = new OlasAttributeServiceFactory();
        olasAttributeServiceRegistration = context.registerService(OlasAttributeService.class.getName(), attribtueServiceFactory, null);

        // Initialize olas configuration service
        OlasConfigurationServiceFactory configurationServiceFactory = new OlasConfigurationServiceFactory();
        olasConfigurationServiceRegistration = context.registerService(OlasConfigurationService.class.getName(),
                configurationServiceFactory, null);

        // Initialize olas log service
        OlasLogServiceFactory logServiceFactory = new OlasLogServiceFactory();
        olasLogServiceRegistration = context.registerService(LogService.class.getName(), logServiceFactory, null);

    }

    /**
     * {@inheritDoc}
     */
    public void stop(BundleContext context) {

        pluginAttributeServiceTracker.close();
        smsServiceTracker.close();
        olasAttributeServiceRegistration.unregister();
        olasConfigurationServiceRegistration.unregister();
        olasLogServiceRegistration.unregister();

    }

    /**
     * {@inheritDoc}
     */
    public void serviceChanged(ServiceEvent event) {

        // TODO: handle modified events, so configuration does not change necessarily

        if (event.getType() == ServiceEvent.REGISTERED) {
            Object service = event.getServiceReference().getBundle().getBundleContext().getService(event.getServiceReference());
            if (service instanceof SmsService) {
                LOG.debug("registered sms service: " + ((SmsService) service).getClass().getName());
                addSmsService(service.getClass().getName());
            }
            event.getServiceReference().getBundle().getBundleContext().ungetService(event.getServiceReference());
        } else if (event.getType() == ServiceEvent.UNREGISTERING) {
            Object service = event.getServiceReference().getBundle().getBundleContext().getService(event.getServiceReference());
            if (service instanceof SmsService) {
                LOG.debug("unregistering sms service: " + ((SmsService) service).getClass().getName());
                removeSmsService(service.getClass().getName());
            }
            event.getServiceReference().getBundle().getBundleContext().ungetService(event.getServiceReference());
        } else {
            Object service = event.getServiceReference().getBundle().getBundleContext().getService(event.getServiceReference());
            LOG.debug("event of type: " + event.getType() + " logged ( " + ServiceEvent.MODIFIED + " )");
            LOG.debug("service = " + service.getClass().getName());
        }

    }

    private void addSmsService(String serviceName) {

        ConfigurationManager configurationManager = EjbUtils.getEJB(ConfigurationManager.JNDI_BINDING, ConfigurationManager.class);
        configurationManager.addConfigurationValue(SMS_SERVICE_GROUP_NAME, SMS_SERVICE_IMPL_NAME, true, serviceName);
    }

    private void removeSmsService(String serviceName) {

        ConfigurationManager configurationManager = EjbUtils.getEJB(ConfigurationManager.JNDI_BINDING, ConfigurationManager.class);
        configurationManager.removeConfigurationValue(SMS_SERVICE_GROUP_NAME, SMS_SERVICE_IMPL_NAME, serviceName);
    }

    public ServiceReference[] getPluginServiceReferences() {

        return pluginAttributeServiceTracker.getServiceReferences();
    }

    public ServiceReference[] getSmsServiceReferences() {

        return smsServiceTracker.getServiceReferences();
    }

    public Object[] getPluginServices() {

        return pluginAttributeServiceTracker.getServices();
    }

    public Object[] getSmsServices() {

        return smsServiceTracker.getServices();
    }

}
