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

	private BundleContext bundleContext;

	public TemplateAttributeService(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	public List<Attribute> getAttribute(String userId, String attributeName,
			String configuration) throws UnsupportedDataTypeException,
			AttributeTypeNotFoundException, AttributeNotFoundException {
		System.out.println("get attribute " + attributeName + " for user "
				+ userId + " (configuration=" + configuration + ")");

		if (attributeName.equals("test-attribute")) {
			return getAttribute(attributeName);
		} else {
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
	private List<Attribute> getAttribute(String attributeName) {
		List<Attribute> testAttribute = new LinkedList<Attribute>();

		testAttribute.addAll(createCompoundAttribute(attributeName, 1));
		testAttribute.addAll(createCompoundAttribute(attributeName, 2));

		return testAttribute;
	}

	/**
	 * Creates a compounded attribute, containing 1 member attribute for each
	 * attribute data type
	 */
	private List<Attribute> createCompoundAttribute(String attributeName,
			int index) {
		List<Attribute> attribute = new LinkedList<Attribute>();

		// create first compounded attribute + its member attributes
		Attribute compoundedAttribute1 = new Attribute("test-attribute",
				DatatypeType.COMPOUNDED);
		compoundedAttribute1.setIndex(index);
		compoundedAttribute1.setMember(false);

		Attribute stringMemberAttribute1 = new Attribute(
				"test-attribute-string", DatatypeType.STRING);
		stringMemberAttribute1.setIndex(index);
		stringMemberAttribute1.setMember(true);
		stringMemberAttribute1.setStringValue("string-value-" + index);

		Attribute booleanMemberAttribute1 = new Attribute(
				"test-attribute-boolean", DatatypeType.BOOLEAN);
		booleanMemberAttribute1.setIndex(index);
		booleanMemberAttribute1.setMember(true);
		booleanMemberAttribute1.setBooleanValue(true);

		Attribute dateMemberAttribute1 = new Attribute("test-attribute-date",
				DatatypeType.DATE);
		dateMemberAttribute1.setIndex(index);
		dateMemberAttribute1.setMember(true);
		dateMemberAttribute1.setDateValue(new Date());

		Attribute doubleMemberAttribute1 = new Attribute(
				"test-attribute-double", DatatypeType.DOUBLE);
		doubleMemberAttribute1.setIndex(index);
		doubleMemberAttribute1.setMember(true);
		doubleMemberAttribute1.setDoubleValue(0.5 + index);

		Attribute integerMemberAttribute1 = new Attribute(
				"test-attribute-integer", DatatypeType.INTEGER);
		integerMemberAttribute1.setIndex(index);
		integerMemberAttribute1.setMember(true);
		integerMemberAttribute1.setIntegerValue(index);

		attribute.add(compoundedAttribute1);
		attribute.add(stringMemberAttribute1);
		attribute.add(booleanMemberAttribute1);
		attribute.add(dateMemberAttribute1);
		attribute.add(doubleMemberAttribute1);
		attribute.add(integerMemberAttribute1);

		return attribute;
	}
}
