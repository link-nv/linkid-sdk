/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.siemens.acceptance.attributepull;

import java.io.InputStream;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import net.link.safeonline.osgi.plugin.OlasAttributeService;
import net.link.safeonline.osgi.plugin.PluginAttributeService;
import net.link.safeonline.osgi.plugin.exception.AttributeNotFoundException;
import net.link.safeonline.osgi.plugin.exception.AttributeTypeNotFoundException;
import net.link.safeonline.osgi.plugin.exception.AttributeUnavailableException;
import net.link.safeonline.osgi.plugin.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.KeyStoreUtils;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


/**
 * <h2>{@link AcceptanceAttributeService}<br>
 * <sub>Acceptance test OSGi bundle</sub></h2>
 * 
 * <p>
 * This service fetches it's attribute value through a call to the localhost attribute web service. It can be used in the acceptance
 * scenario of Siemens.
 * </p>
 * 
 * <p>
 * <i>Nov 17, 2008</i>
 * </p>
 * 
 * @author dhouthoo
 */
public class AcceptanceAttributeService implements PluginAttributeService {

    private X509Certificate     certificate;
    private PrivateKeyEntry     privateKeyEntry;

    private static final String keystore       = "safe-online-node-keystore.jks";
    private static final String keystoreType   = "jks";
    private static final String password       = "secret";
    private static final String location       = "http://localhost:8080";

    private static final String compoundedName = "siemens:compounded";
    private static final String osgiName       = "siemens:compounded:osgi";
    private static final String wsName         = "siemens:compounded:ws";
    private static final String targetName     = "siemens:target";

    private BundleContext       bundleContext;


    public AcceptanceAttributeService(BundleContext bundleContext) {

        System.out.println("AcceptanceAttributeService constructor");
        InputStream keyStoreInputStream = this.getClass().getClassLoader().getResourceAsStream(keystore);
        this.privateKeyEntry = KeyStoreUtils.loadPrivateKeyEntry(keystoreType, keyStoreInputStream, password, password);
        this.certificate = (X509Certificate) this.privateKeyEntry.getCertificate();

        this.bundleContext = bundleContext;
    }

    @SuppressWarnings("unchecked")
    public Object getAttribute(String userId, String attributeName, String configuration)
            throws AttributeTypeNotFoundException, AttributeNotFoundException, AttributeUnavailableException, SubjectNotFoundException {

        System.out.println("get attribute " + attributeName + " for user " + userId + " (configuration=" + configuration + ")");

        // fetch the through the OSGi attribute service
        String osgiValue = null;
        ServiceReference serviceReference = this.bundleContext.getServiceReference(OlasAttributeService.class.getName());
        if (null != serviceReference) {
            OlasAttributeService attributeService = (OlasAttributeService) this.bundleContext.getService(serviceReference);
            osgiValue = (String) attributeService.getAttribute(userId, targetName);
            System.out.println("osgi: found attribute: " + osgiValue);
        }

        // fetch the through the attribute web service

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String wsValue = null;
        try {
            // Thread.currentThread().setContextClassLoader(new MyClassLoader(this.getClass().getClassLoader()));
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            // Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());

            System.out.println("ws: fetch attribute through ws");
            AttributeClient attributeClient = new AttributeClientImpl(location, this.certificate, this.privateKeyEntry.getPrivateKey());
            HashMap<String, Object> attributes = new HashMap<String, Object>();
            attributes.put(targetName, (Object) null);
            System.out.println("ws: get attribute values");
            attributeClient.getAttributeValues(userId, attributes);
            wsValue = (String) attributes.get(targetName);
            System.out.println("ws: found attribute: " + wsValue);

        } catch (Exception e) {
            // empty
            System.out.println("ws: exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }

        if (attributeName.equals(compoundedName)) {
            Map<String, Object> result = new HashMap<String, Object>();
            result.put(wsName, wsValue);
            result.put(osgiName, osgiValue);
            Map<String, Object>[] resultArray = new Map[1];
            resultArray[0] = result;
            return resultArray;
        } else if (attributeName.equals(wsName))
            return wsValue;
        else if (attributeName.equals(osgiName))
            return osgiValue;
        return null;
    }
}
