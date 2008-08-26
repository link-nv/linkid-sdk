/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package test.unit.net.link.safeonline.authentication.service.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.link.safeonline.authentication.service.ProxyAttributeService;
import net.link.safeonline.authentication.service.bean.ProxyAttributeServiceBean;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.osgi.OSGIStartable;
import net.link.safeonline.osgi.plugin.Attribute;
import net.link.safeonline.osgi.plugin.PluginAttributeService;
import net.link.safeonline.osgi.plugin.exception.AttributeNotFoundException;
import net.link.safeonline.osgi.plugin.exception.AttributeTypeNotFoundException;
import net.link.safeonline.osgi.plugin.exception.UnsupportedDataTypeException;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.EJBTestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ProxyAttributeServiceBeanTest {

    private static final String   testCompoundAttributeName = "test-compound-attribute";

    private static final String   testStringAttributeName   = "test-string-attribute";
    private static final String   testBooleanAttributeName  = "test-boolean-attribute";
    private static final String   testDateAttributeName     = "test-date-attribute";
    private static final String   testDoubleAttributeName   = "test-double-attribute";
    private static final String   testIntegerAttributeName  = "test-integer-attribute";

    private ProxyAttributeService testedInstance;

    private OSGIStartable         mockOSGIStartable;

    private AttributeTypeDAO      mockAttributeTypeDAO;

    private SubjectService        mockSubjectService;

    private Object[]              mockObjects;


    @Before
    public void setUp() throws Exception {

        this.testedInstance = new ProxyAttributeServiceBean();

        this.mockOSGIStartable = createMock(OSGIStartable.class);
        EJBTestUtils.inject(this.testedInstance, this.mockOSGIStartable);

        this.mockAttributeTypeDAO = createMock(AttributeTypeDAO.class);
        EJBTestUtils.inject(this.testedInstance, this.mockAttributeTypeDAO);

        this.mockSubjectService = createMock(SubjectService.class);
        EJBTestUtils.inject(this.testedInstance, this.mockSubjectService);

        this.mockObjects = new Object[] { this.mockOSGIStartable, this.mockAttributeTypeDAO, this.mockSubjectService };

        EJBTestUtils.init(this.testedInstance);

    }

    @After
    public void tearDown() throws Exception {

    }

    @SuppressWarnings("unchecked")
    @Test
    public void fetchExternalCompoundAttribute() throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String pluginServiceName = "test-plugin-service";
        PluginAttributeService testPluginService = new TestPluginService();

        SubjectEntity testSubject = new SubjectEntity(userId);

        // define attribute type
        AttributeTypeEntity stringAttributeType = new AttributeTypeEntity(testStringAttributeName, DatatypeType.STRING,
                true, true);
        stringAttributeType.setMultivalued(true);

        AttributeTypeEntity booleanAttributeType = new AttributeTypeEntity(testBooleanAttributeName,
                DatatypeType.BOOLEAN, true, true);
        booleanAttributeType.setMultivalued(true);

        AttributeTypeEntity dateAttributeType = new AttributeTypeEntity(testDateAttributeName, DatatypeType.DATE, true,
                true);
        dateAttributeType.setMultivalued(true);

        AttributeTypeEntity doubleAttributeType = new AttributeTypeEntity(testDoubleAttributeName, DatatypeType.DOUBLE,
                true, true);
        doubleAttributeType.setMultivalued(true);

        AttributeTypeEntity integerAttributeType = new AttributeTypeEntity(testIntegerAttributeName,
                DatatypeType.INTEGER, true, true);
        integerAttributeType.setMultivalued(true);

        AttributeTypeEntity compoundedAttributeType = new AttributeTypeEntity(testCompoundAttributeName,
                DatatypeType.COMPOUNDED, true, true);
        compoundedAttributeType.setMultivalued(true);
        compoundedAttributeType.setPluginName(pluginServiceName);
        compoundedAttributeType.setPluginConfiguration("test-plugin-configuration");
        compoundedAttributeType.addMember(stringAttributeType, 0, true);
        compoundedAttributeType.addMember(booleanAttributeType, 1, true);
        compoundedAttributeType.addMember(dateAttributeType, 2, true);
        compoundedAttributeType.addMember(doubleAttributeType, 3, true);
        compoundedAttributeType.addMember(integerAttributeType, 4, true);

        // expectations
        expect(this.mockAttributeTypeDAO.getAttributeType(testCompoundAttributeName)).andStubReturn(
                compoundedAttributeType);
        expect(this.mockSubjectService.findSubject(userId)).andStubReturn(testSubject);
        expect(this.mockOSGIStartable.getPluginService(pluginServiceName)).andStubReturn(testPluginService);

        // prepare
        replay(this.mockObjects);

        // operate: fetch attribute value
        Object attributeValue = this.testedInstance.findAttributeValue(userId, testCompoundAttributeName);

        // verify
        verify(this.mockOSGIStartable);
        assertNotNull(attributeValue);
        assertTrue(attributeValue.getClass().equals(Map[].class));
        Map<String, Object>[] mapAttributeValue = (Map<String, Object>[]) attributeValue;
        assertEquals(3, mapAttributeValue.length);
        for (Map<String, Object> mapAttribute : mapAttributeValue) {
            assertTrue(mapAttribute.get(testStringAttributeName).getClass().equals(String.class));
            assertTrue(mapAttribute.get(testBooleanAttributeName).getClass().equals(Boolean.class));
            assertTrue(mapAttribute.get(testDateAttributeName).getClass().equals(Date.class));
            assertTrue(mapAttribute.get(testDoubleAttributeName).getClass().equals(Double.class));
            assertTrue(mapAttribute.get(testIntegerAttributeName).getClass().equals(Integer.class));
        }
    }

    @Test
    public void fetchExternalStringAttribute() throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String pluginServiceName = "test-plugin-service";
        PluginAttributeService testPluginService = new TestPluginService();

        SubjectEntity testSubject = new SubjectEntity(userId);

        // define attribute type
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testStringAttributeName, DatatypeType.STRING, true,
                true);
        attributeType.setMultivalued(true);
        attributeType.setPluginName(pluginServiceName);
        attributeType.setPluginConfiguration("test-plugin-configuration");

        // expectations
        expect(this.mockAttributeTypeDAO.getAttributeType(testStringAttributeName)).andStubReturn(attributeType);
        expect(this.mockSubjectService.findSubject(userId)).andStubReturn(testSubject);
        expect(this.mockOSGIStartable.getPluginService(pluginServiceName)).andStubReturn(testPluginService);

        // prepare
        replay(this.mockObjects);

        // operate: fetch attribute value
        Object attributeValue = this.testedInstance.findAttributeValue(userId, testStringAttributeName);

        // verify
        verify(this.mockOSGIStartable);
        assertNotNull(attributeValue);
        assertTrue(attributeValue.getClass().equals(String[].class));
        String[] stringAttributeValue = (String[]) attributeValue;
        assertEquals(3, stringAttributeValue.length);
    }

    @Test
    public void fetchExternalBooleanAttribute() throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String pluginServiceName = "test-plugin-service";
        PluginAttributeService testPluginService = new TestPluginService();

        SubjectEntity testSubject = new SubjectEntity(userId);

        // define attribute type
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testBooleanAttributeName, DatatypeType.BOOLEAN,
                true, true);
        attributeType.setMultivalued(true);
        attributeType.setPluginName(pluginServiceName);
        attributeType.setPluginConfiguration("test-plugin-configuration");

        // expectations
        expect(this.mockAttributeTypeDAO.getAttributeType(testBooleanAttributeName)).andStubReturn(attributeType);
        expect(this.mockSubjectService.findSubject(userId)).andStubReturn(testSubject);
        expect(this.mockOSGIStartable.getPluginService(pluginServiceName)).andStubReturn(testPluginService);

        // prepare
        replay(this.mockObjects);

        // operate: fetch attribute value
        Object attributeValue = this.testedInstance.findAttributeValue(userId, testBooleanAttributeName);

        // verify
        verify(this.mockOSGIStartable);
        assertNotNull(attributeValue);
        assertTrue(attributeValue.getClass().equals(Boolean[].class));
        Boolean[] booleanAttributeValue = (Boolean[]) attributeValue;
        assertEquals(3, booleanAttributeValue.length);
    }

    @Test
    public void fetchExternalDateAttribute() throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String pluginServiceName = "test-plugin-service";
        PluginAttributeService testPluginService = new TestPluginService();

        SubjectEntity testSubject = new SubjectEntity(userId);

        // define attribute type
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testDateAttributeName, DatatypeType.DATE, true,
                true);
        attributeType.setMultivalued(true);
        attributeType.setPluginName(pluginServiceName);
        attributeType.setPluginConfiguration("test-plugin-configuration");

        // expectations
        expect(this.mockAttributeTypeDAO.getAttributeType(testDateAttributeName)).andStubReturn(attributeType);
        expect(this.mockSubjectService.findSubject(userId)).andStubReturn(testSubject);
        expect(this.mockOSGIStartable.getPluginService(pluginServiceName)).andStubReturn(testPluginService);

        // prepare
        replay(this.mockObjects);

        // operate: fetch attribute value
        Object attributeValue = this.testedInstance.findAttributeValue(userId, testDateAttributeName);

        // verify
        verify(this.mockOSGIStartable);
        assertNotNull(attributeValue);
        assertTrue(attributeValue.getClass().equals(Date[].class));
        Date[] dateAttributeValue = (Date[]) attributeValue;
        assertEquals(3, dateAttributeValue.length);
    }

    @Test
    public void fetchExternalDoubleAttribute() throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String pluginServiceName = "test-plugin-service";
        PluginAttributeService testPluginService = new TestPluginService();

        SubjectEntity testSubject = new SubjectEntity(userId);

        // define attribute type
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testDoubleAttributeName, DatatypeType.DOUBLE, true,
                true);
        attributeType.setMultivalued(true);
        attributeType.setPluginName(pluginServiceName);
        attributeType.setPluginConfiguration("test-plugin-configuration");

        // expectations
        expect(this.mockAttributeTypeDAO.getAttributeType(testDoubleAttributeName)).andStubReturn(attributeType);
        expect(this.mockSubjectService.findSubject(userId)).andStubReturn(testSubject);
        expect(this.mockOSGIStartable.getPluginService(pluginServiceName)).andStubReturn(testPluginService);

        // prepare
        replay(this.mockObjects);

        // operate: fetch attribute value
        Object attributeValue = this.testedInstance.findAttributeValue(userId, testDoubleAttributeName);

        // verify
        verify(this.mockOSGIStartable);
        assertNotNull(attributeValue);
        assertTrue(attributeValue.getClass().equals(Double[].class));
        Double[] doubleAttributeValue = (Double[]) attributeValue;
        assertEquals(3, doubleAttributeValue.length);
    }

    @Test
    public void fetchExternalIntegerAttribute() throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String pluginServiceName = "test-plugin-service";
        PluginAttributeService testPluginService = new TestPluginService();

        SubjectEntity testSubject = new SubjectEntity(userId);

        // define attribute type
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testIntegerAttributeName, DatatypeType.INTEGER,
                true, true);
        attributeType.setMultivalued(true);
        attributeType.setPluginName(pluginServiceName);
        attributeType.setPluginConfiguration("test-plugin-configuration");

        // expectations
        expect(this.mockAttributeTypeDAO.getAttributeType(testIntegerAttributeName)).andStubReturn(attributeType);
        expect(this.mockSubjectService.findSubject(userId)).andStubReturn(testSubject);
        expect(this.mockOSGIStartable.getPluginService(pluginServiceName)).andStubReturn(testPluginService);

        // prepare
        replay(this.mockObjects);

        // operate: fetch attribute value
        Object attributeValue = this.testedInstance.findAttributeValue(userId, testIntegerAttributeName);

        // verify
        verify(this.mockOSGIStartable);
        assertNotNull(attributeValue);
        assertTrue(attributeValue.getClass().equals(Integer[].class));
        Integer[] integerAttributeValue = (Integer[]) attributeValue;
        assertEquals(3, integerAttributeValue.length);
    }


    static class TestPluginService implements PluginAttributeService {

        public List<Attribute> getAttribute(String userId, String attributeName, String configuration)
                throws UnsupportedDataTypeException, AttributeNotFoundException, AttributeTypeNotFoundException {

            List<Attribute> testAttribute = new LinkedList<Attribute>();

            if (attributeName.equals(testCompoundAttributeName)) {
                testAttribute.addAll(createCompoundAttribute(1));
                testAttribute.addAll(createCompoundAttribute(2));
                testAttribute.addAll(createCompoundAttribute(3));
            } else if (attributeName.equals(testStringAttributeName)) {
                testAttribute.add(createStringAttribute(1));
                testAttribute.add(createStringAttribute(2));
                testAttribute.add(createStringAttribute(3));
            } else if (attributeName.equals(testBooleanAttributeName)) {
                testAttribute.add(createBooleanAttribute(1));
                testAttribute.add(createBooleanAttribute(2));
                testAttribute.add(createBooleanAttribute(3));
            } else if (attributeName.equals(testDateAttributeName)) {
                testAttribute.add(createDateAttribute(1));
                testAttribute.add(createDateAttribute(2));
                testAttribute.add(createDateAttribute(3));
            } else if (attributeName.equals(testDoubleAttributeName)) {
                testAttribute.add(createDoubleAttribute(1));
                testAttribute.add(createDoubleAttribute(2));
                testAttribute.add(createDoubleAttribute(3));
            } else if (attributeName.equals(testIntegerAttributeName)) {
                testAttribute.add(createIntegerAttribute(1));
                testAttribute.add(createIntegerAttribute(2));
                testAttribute.add(createIntegerAttribute(3));
            }

            return testAttribute;
        }

        private Attribute createStringAttribute(int index) {

            Attribute stringAttribute = new Attribute(testStringAttributeName,
                    net.link.safeonline.osgi.plugin.DatatypeType.STRING);
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

        private List<Attribute> createCompoundAttribute(int index) {

            List<Attribute> attribute = new LinkedList<Attribute>();

            // create first compounded attribute + its member attributes
            Attribute compoundedAttribute1 = new Attribute(testCompoundAttributeName,
                    net.link.safeonline.osgi.plugin.DatatypeType.COMPOUNDED);
            compoundedAttribute1.setIndex(index);

            attribute.add(compoundedAttribute1);
            attribute.add(createStringAttribute(index));
            attribute.add(createBooleanAttribute(index));
            attribute.add(createDateAttribute(index));
            attribute.add(createDoubleAttribute(index));
            attribute.add(createIntegerAttribute(index));

            return attribute;
        }

    }
}
