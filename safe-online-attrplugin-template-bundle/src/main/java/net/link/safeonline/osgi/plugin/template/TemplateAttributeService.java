/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.plugin.template;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.link.safeonline.osgi.OlasAttributeService;
import net.link.safeonline.osgi.exception.AttributeNotFoundException;
import net.link.safeonline.osgi.exception.AttributeTypeNotFoundException;
import net.link.safeonline.osgi.exception.AttributeUnavailableException;
import net.link.safeonline.osgi.exception.SubjectNotFoundException;
import net.link.safeonline.osgi.plugin.PluginAttributeService;

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
            List<Map<String, Object>> values = new LinkedList<Map<String, Object>>();
            values.add(createCompoundAttribute());
            values.add(createCompoundAttribute());
            return values;
        } else if (attributeName.equals(testStringAttributeName)) {
            List<String> values = new LinkedList<String>();
            values.add("test-string-1-" + UUID.randomUUID().toString());
            values.add("test-string-2-" + UUID.randomUUID().toString());
            values.add("test-string-3-" + UUID.randomUUID().toString());
            return values;
        } else if (attributeName.equals(testBooleanAttributeName)) {
            List<Boolean> values = new LinkedList<Boolean>();
            values.add(true);
            values.add(false);
            values.add(true);
            return values;
        } else if (attributeName.equals(testDateAttributeName)) {
            List<Date> values = new LinkedList<Date>();
            values.add(new Date());
            values.add(new Date());
            values.add(new Date());
            return values;
        } else if (attributeName.equals(testDoubleAttributeName)) {
            List<Double> values = new LinkedList<Double>();
            values.add(values.size() + 0.5);
            values.add(values.size() + 0.5);
            values.add(values.size() + 0.5);
            return values;
        } else if (attributeName.equals(testIntegerAttributeName)) {
            List<Integer> values = new LinkedList<Integer>();
            values.add(values.size());
            values.add(values.size());
            values.add(values.size());
            return values;
        } else {
            // Beware: this code makes an endless loop by getting the external
            // attribute from OLAS which will again redirect to this plugin ...
            ServiceReference serviceReference = bundleContext.getServiceReference(OlasAttributeService.class.getName());
            if (null != serviceReference) {
                OlasAttributeService attributeService = (OlasAttributeService) bundleContext.getService(serviceReference);
                Object value = attributeService.getAttribute(userId, attributeName);
                bundleContext.ungetService(serviceReference);
                return value;
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
