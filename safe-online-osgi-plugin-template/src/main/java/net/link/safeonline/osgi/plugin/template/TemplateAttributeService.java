/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.plugin.template;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.link.safeonline.osgi.plugin.OlasAttributeService;
import net.link.safeonline.osgi.plugin.PluginAttributeService;
import net.link.safeonline.osgi.plugin.exception.AttributeNotFoundException;
import net.link.safeonline.osgi.plugin.exception.AttributeTypeNotFoundException;
import net.link.safeonline.osgi.plugin.exception.AttributeUnavailableException;
import net.link.safeonline.osgi.plugin.exception.SubjectNotFoundException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


/**
 * <sub>Template attribute plugin service.</sub></h2>
 * 
 * <p>
 * The attribute plugin service implementation serves as an example to develop new attribute plugins. It shows how to access the OLAS
 * attribute service plugin to fetch OLAS attributes and shows how to construct the attribute view to return to OLAS for all datatypes.
 * </p>
 * 
 * <p>
 * <i>Aug 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class TemplateAttributeService implements PluginAttributeService {

    private static final String testStringAttributeName   = "urn:net:lin-k:safe-online:attribute:osgi:test:string";
    private static final String testDateAttributeName     = "urn:net:lin-k:safe-online:attribute:osgi:test:date";
    private static final String testBooleanAttributeName  = "urn:net:lin-k:safe-online:attribute:osgi:test:boolean";
    private static final String testDoubleAttributeName   = "urn:net:lin-k:safe-online:attribute:osgi:test:double";
    private static final String testIntegerAttributeName  = "urn:net:lin-k:safe-online:attribute:osgi:test:integer";
    private static final String testCompoundAttributeName = "urn:net:lin-k:safe-online:attribute:osgi:test:compound";

    private BundleContext       bundleContext;


    public TemplateAttributeService(BundleContext bundleContext) {

        this.bundleContext = bundleContext;
    }

    @SuppressWarnings("unchecked")
    public Object getAttribute(String userId, String attributeName, String configuration)
            throws AttributeTypeNotFoundException, AttributeNotFoundException, AttributeUnavailableException, SubjectNotFoundException {

        System.out.println("get attribute " + attributeName + " for user " + userId + " (configuration=" + configuration + ")");

        if (attributeName.equals(testCompoundAttributeName)) {
            Map<String, Object>[] values = new Map[2];
            values[0] = createCompoundAttribute();
            values[1] = createCompoundAttribute();
            return values;
        } else if (attributeName.equals(testStringAttributeName)) {
            String[] values = new String[3];
            values[0] = "test-string-" + UUID.randomUUID().toString();
            values[1] = "test-string-" + UUID.randomUUID().toString();
            values[2] = "test-string-" + UUID.randomUUID().toString();
            return values;
        } else if (attributeName.equals(testBooleanAttributeName)) {
            Boolean[] values = new Boolean[3];
            values[0] = true;
            values[0] = false;
            values[0] = true;
            return values;
        } else if (attributeName.equals(testDateAttributeName)) {
            Date[] values = new Date[3];
            values[0] = new Date();
            values[1] = new Date();
            values[2] = new Date();
            return values;
        } else if (attributeName.equals(testDoubleAttributeName)) {
            Double[] values = new Double[3];
            values[0] = 0.1;
            values[0] = 0.2;
            values[0] = 0.3;
            return values;
        } else if (attributeName.equals(testIntegerAttributeName)) {
            Integer[] values = new Integer[3];
            values[0] = 0;
            values[1] = 1;
            values[2] = 2;
            return values;
        } else {
            // Beware: this code makes an endless loop by getting the external
            // attribute from OLAS which will again redirect to this plugin ...
            ServiceReference serviceReference = this.bundleContext.getServiceReference(OlasAttributeService.class.getName());
            if (null != serviceReference) {
                OlasAttributeService attributeService = (OlasAttributeService) this.bundleContext.getService(serviceReference);
                return attributeService.getAttribute(userId, attributeName);
            }
        }
        System.err.println("OSGI service reference not found for: " + OlasAttributeService.class.getName());
        return null;
    }

    /**
     * Creates a compounded attribute, containing 1 member attribute for each attribute data type
     */
    private Map<String, Object> createCompoundAttribute() {

        Map<String, Object> compoundedAttribute = new HashMap<String, Object>();
        compoundedAttribute.put(testStringAttributeName, "test-string-" + UUID.randomUUID().toString());
        compoundedAttribute.put(testBooleanAttributeName, true);
        compoundedAttribute.put(testDateAttributeName, new Date());
        compoundedAttribute.put(testDoubleAttributeName, 0.5);
        compoundedAttribute.put(testIntegerAttributeName, 666);
        return compoundedAttribute;
    }
}
