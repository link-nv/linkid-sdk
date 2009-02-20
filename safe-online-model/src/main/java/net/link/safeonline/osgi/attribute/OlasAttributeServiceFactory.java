package net.link.safeonline.osgi.attribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;


public class OlasAttributeServiceFactory implements ServiceFactory {

    private static final Log LOG          = LogFactory.getLog(OlasAttributeServiceFactory.class);

    private int              usageCounter = 0;


    public Object getService(Bundle bundle, ServiceRegistration registration) {

        LOG.debug("Create object of OlasAttributeService for " + bundle.getSymbolicName());
        usageCounter++;
        LOG.debug("Number of bundles using service " + usageCounter);
        return new OlasAttributeServiceImpl();
    }

    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {

        LOG.debug("Release object of OlasAttributeService for " + bundle.getSymbolicName());
        usageCounter--;
        LOG.debug("Number of bundles using service " + usageCounter);
    }

}
