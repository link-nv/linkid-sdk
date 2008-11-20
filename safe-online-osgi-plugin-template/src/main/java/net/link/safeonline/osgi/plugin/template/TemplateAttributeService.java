/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.plugin.template;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.link.safeonline.osgi.plugin.Attribute;
import net.link.safeonline.osgi.plugin.DatatypeType;
import net.link.safeonline.osgi.plugin.OlasAttributeService;
import net.link.safeonline.osgi.plugin.PluginAttributeService;
import net.link.safeonline.osgi.plugin.exception.AttributeNotFoundException;
import net.link.safeonline.osgi.plugin.exception.AttributeTypeNotFoundException;
import net.link.safeonline.osgi.plugin.exception.AttributeUnavailableException;
import net.link.safeonline.osgi.plugin.exception.SubjectNotFoundException;
import net.link.safeonline.osgi.plugin.exception.UnsupportedDataTypeException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * <h2>{@link OSGIStartableBean}<br>
 * <sub>Template attribute plugin service.</sub></h2>
 * 
 * <p>
 * The attribute plugin service implementation serves as an example to develop
 * new attribute plugins. It shows how to access the OLAS attribute service
 * plugin to fetch OLAS attributes and shows how to construct the attribute view
 * to return to OLAS for all datatypes.
 * </p>
 * 
 * <p>
 * <i>Aug 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class TemplateAttributeService implements PluginAttributeService {

	private static final String testStringAttributeName = "urn:net:lin-k:safe-online:attribute:osgi:test:string";
	private static final String testDateAttributeName = "urn:net:lin-k:safe-online:attribute:osgi:test:date";
	private static final String testBooleanAttributeName = "urn:net:lin-k:safe-online:attribute:osgi:test:boolean";
	private static final String testDoubleAttributeName = "urn:net:lin-k:safe-online:attribute:osgi:test:double";
	private static final String testIntegerAttributeName = "urn:net:lin-k:safe-online:attribute:osgi:test:integer";
	private static final String testCompoundAttributeName = "urn:net:lin-k:safe-online:attribute:osgi:test:compound";

	private BundleContext bundleContext;

	public TemplateAttributeService(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	public List<Attribute> getAttribute(String userId, String attributeName,
			String configuration) throws UnsupportedDataTypeException,
			AttributeTypeNotFoundException, AttributeNotFoundException,
			AttributeUnavailableException, SubjectNotFoundException {
		System.out.println("get attribute " + attributeName + " for user "
				+ userId + " (configuration=" + configuration + ")");

		if (attributeName.equals(testCompoundAttributeName)) {
			return getCompoundAttribute();
		} else if (attributeName.equals(testStringAttributeName)) {
			List<Attribute> attribute = new LinkedList<Attribute>();
			attribute.add(createStringAttribute(0));
			attribute.add(createStringAttribute(1));
			attribute.add(createStringAttribute(2));
			return attribute;
		} else if (attributeName.equals(testBooleanAttributeName)) {
			List<Attribute> attribute = new LinkedList<Attribute>();
			attribute.add(createBooleanAttribute(0));
			return attribute;
		} else if (attributeName.equals(testDateAttributeName)) {
			List<Attribute> attribute = new LinkedList<Attribute>();
			attribute.add(createDateAttribute(0));
			attribute.add(createDateAttribute(1));
			return attribute;
		} else if (attributeName.equals(testDoubleAttributeName)) {
			List<Attribute> attribute = new LinkedList<Attribute>();
			attribute.add(createDoubleAttribute(0));
			attribute.add(createDoubleAttribute(1));
			return attribute;
		} else if (attributeName.equals(testIntegerAttributeName)) {
			List<Attribute> attribute = new LinkedList<Attribute>();
			attribute.add(createIntegerAttribute(0));
			attribute.add(createIntegerAttribute(1));
			return attribute;
		} else {
			// Beware: this code makes an endless loop by getting the external
			// attribute from OLAS which will again redirect to this plugin ...
			ServiceReference serviceReference = bundleContext
					.getServiceReference(OlasAttributeService.class.getName());
			if (null != serviceReference) {
				OlasAttributeService attributeService = (OlasAttributeService) bundleContext
						.getService(serviceReference);
				return attributeService.getAttribute(userId, attributeName);
			}
		}
		System.err.println("OSGI service reference not found for: "
				+ OlasAttributeService.class.getName());
		return null;
	}

	/**
	 * This method creates a compounded test attribute. This compounded
	 * attribute will contain 1 member attribute for each attribute data type.
	 */
	private List<Attribute> getCompoundAttribute() {
		List<Attribute> testAttribute = new LinkedList<Attribute>();

		testAttribute.addAll(createCompoundAttribute(0));
		testAttribute.addAll(createCompoundAttribute(1));

		return testAttribute;
	}

	/**
	 * Creates a compounded attribute, containing 1 member attribute for each
	 * attribute data type
	 */
	private List<Attribute> createCompoundAttribute(int index) {
		List<Attribute> attribute = new LinkedList<Attribute>();

		// create first compounded attribute + its member attributes
		Attribute compoundedAttribute1 = new Attribute(
				testCompoundAttributeName, DatatypeType.COMPOUNDED);
		compoundedAttribute1.setIndex(index);

		attribute.add(compoundedAttribute1);
		attribute.add(createStringAttribute(index));
		attribute.add(createBooleanAttribute(index));
		attribute.add(createDateAttribute(index));
		attribute.add(createDoubleAttribute(index));
		attribute.add(createIntegerAttribute(index));

		return attribute;
	}

	private Attribute createStringAttribute(int index) {
		Attribute stringAttribute = new Attribute(testStringAttributeName,
				DatatypeType.STRING);
		stringAttribute.setIndex(index);
		stringAttribute.setStringValue("string-value-" + index);
		return stringAttribute;
	}

	private Attribute createBooleanAttribute(int index) {

		Attribute booleanAttribute = new Attribute(testBooleanAttributeName,
				net.link.safeonline.osgi.plugin.DatatypeType.BOOLEAN);
		booleanAttribute.setIndex(index);
		booleanAttribute.setBooleanValue(true);
		return booleanAttribute;
	}

	private Attribute createDateAttribute(int index) {

		Attribute dateAttribute = new Attribute(testDateAttributeName,
				net.link.safeonline.osgi.plugin.DatatypeType.DATE);
		dateAttribute.setIndex(index);
		dateAttribute.setDateValue(new Date());
		return dateAttribute;
	}

	private Attribute createDoubleAttribute(int index) {

		Attribute doubleAttribute = new Attribute(testDoubleAttributeName,
				net.link.safeonline.osgi.plugin.DatatypeType.DOUBLE);
		doubleAttribute.setIndex(index);
		doubleAttribute.setDoubleValue(0.5 + index);
		return doubleAttribute;
	}

	private Attribute createIntegerAttribute(int index) {

		Attribute integerAttribute = new Attribute(testIntegerAttributeName,
				net.link.safeonline.osgi.plugin.DatatypeType.INTEGER);
		integerAttribute.setIndex(index);
		integerAttribute.setIntegerValue(index);
		return integerAttribute;
	}

}
