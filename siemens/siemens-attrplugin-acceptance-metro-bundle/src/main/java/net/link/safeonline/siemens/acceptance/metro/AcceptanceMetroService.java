/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.siemens.acceptance.metro;

import net.link.safeonline.osgi.exception.AttributeNotFoundException;
import net.link.safeonline.osgi.exception.AttributeTypeNotFoundException;
import net.link.safeonline.osgi.exception.AttributeUnavailableException;
import net.link.safeonline.osgi.exception.SubjectNotFoundException;
import net.link.safeonline.osgi.plugin.PluginAttributeService;
import net.link.safeonline.siemens.acceptance.metro.wsclient.MetroClient;
import net.link.safeonline.siemens.acceptance.metro.wsclient.MetroClientImpl;

import org.osgi.framework.BundleContext;


/**
 * <h2>{@link AcceptanceMetroService}<br>
 * <sub>Acceptance test OSGi bundle for Metro WS</sub></h2>
 * 
 * <p>
 * This service calls a Metro WS.
 * </p>
 * 
 * <p>
 * <i>Jan 6, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class AcceptanceMetroService implements PluginAttributeService {

    private static final String defaultLocation    = "http://sebeco-dev-11:8080";

    private static final String metroAttributeName = "siemens:metro:string";


    public AcceptanceMetroService(@SuppressWarnings("unused") BundleContext bundleContext) {

        System.out.println("AcceptanceMetroService constructor");
    }

    @SuppressWarnings("unchecked")
    public Object getAttribute(String userId, String attributeName, String configuration)
            throws AttributeTypeNotFoundException, AttributeNotFoundException, AttributeUnavailableException, SubjectNotFoundException {

        System.out.println("get attribute " + attributeName + " for user " + userId + " (configuration=" + configuration + ")");

        // fetch value through the metro web service
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String wsValue = null;
        try {
            // Have to set the thread's context classloader to the bundle's classloader, jaxws loads in a resource for the Provider class
            // using Thread.currentThread.getContextClassLoader which returns the classloader of JBoss.
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

            String location = defaultLocation;
            if (null != configuration && configuration.length() > 0) {
                location = configuration;
            }

            MetroClient metroClient = new MetroClientImpl(location);
            wsValue = metroClient.getAttribute();
            System.out.println("ws: found attribute: " + wsValue);

        } catch (Exception e) {
            // empty
            System.out.println("ws: exception: " + e.getMessage());
            e.printStackTrace();
            throw new AttributeUnavailableException(e.getMessage());
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }

        if (attributeName.equals(metroAttributeName))
            return wsValue;
        return null;
    }
}
