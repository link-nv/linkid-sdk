/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi;

import net.link.safeonline.authentication.exception.SafeOnlineResourceException;
import net.link.safeonline.entity.audit.ResourceLevelType;
import net.link.safeonline.entity.audit.ResourceNameType;

import org.osgi.framework.ServiceReference;


/**
 * <h2>{@link OSGIServiceImpl}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Dec 10, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class OSGIServiceImpl implements OSGIService {

    ServiceReference[] serviceReferences;

    String             serviceName;

    ServiceReference   selectedServiceReference;


    public OSGIServiceImpl(ServiceReference[] serviceReferences, String serviceName) {

        this.serviceReferences = serviceReferences;
        this.serviceName = serviceName;

    }

    /**
     * {@inheritDoc}
     */
    public Object getService()
            throws SafeOnlineResourceException {

        if (null == serviceReferences)
            throw new SafeOnlineResourceException(ResourceNameType.OSGI, ResourceLevelType.RESOURCE_UNAVAILABLE, serviceName);

        for (ServiceReference serviceReference : serviceReferences) {
            Object service = serviceReference.getBundle().getBundleContext().getService(serviceReference);
            if (service.getClass().getName().equals(serviceName)) {
                selectedServiceReference = serviceReference;
                return service;
            }
        }

        throw new SafeOnlineResourceException(ResourceNameType.OSGI, ResourceLevelType.RESOURCE_UNAVAILABLE, serviceName);

    }

    /**
     * {@inheritDoc}
     */
    public void ungetService() {

        selectedServiceReference.getBundle().getBundleContext().ungetService(selectedServiceReference);
    }
}
