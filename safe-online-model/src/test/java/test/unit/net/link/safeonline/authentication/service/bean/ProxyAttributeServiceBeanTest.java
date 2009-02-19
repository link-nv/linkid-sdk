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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.link.safeonline.authentication.service.ProxyAttributeService;
import net.link.safeonline.authentication.service.bean.ProxyAttributeServiceBean;
import net.link.safeonline.dao.AttributeCacheDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.entity.AttributeCacheEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.osgi.OSGIService;
import net.link.safeonline.osgi.OSGIStartable;
import net.link.safeonline.osgi.OSGIConstants.OSGIServiceType;
import net.link.safeonline.osgi.exception.AttributeNotFoundException;
import net.link.safeonline.osgi.exception.AttributeTypeNotFoundException;
import net.link.safeonline.osgi.plugin.PluginAttributeService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.EJBTestUtils;

import org.easymock.classextension.EasyMock;
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
    private OSGIService           mockOSGIService;

    private AttributeTypeDAO      mockAttributeTypeDAO;

    private AttributeCacheDAO     mockAttributeCacheDAO;

    private SubjectService        mockSubjectService;

    private Object[]              mockObjects;


    @Before
    public void setUp()
            throws Exception {

        testedInstance = new ProxyAttributeServiceBean();

        mockOSGIStartable = createMock(OSGIStartable.class);
        EJBTestUtils.inject(testedInstance, mockOSGIStartable);

        mockOSGIService = EasyMock.createMock(OSGIService.class);

        mockAttributeTypeDAO = createMock(AttributeTypeDAO.class);
        EJBTestUtils.inject(testedInstance, mockAttributeTypeDAO);

        mockAttributeCacheDAO = createMock(AttributeCacheDAO.class);
        EJBTestUtils.inject(testedInstance, mockAttributeCacheDAO);

        mockSubjectService = createMock(SubjectService.class);
        EJBTestUtils.inject(testedInstance, mockSubjectService);

        mockObjects = new Object[] { mockOSGIStartable, mockAttributeTypeDAO, mockSubjectService, mockAttributeCacheDAO };

        EJBTestUtils.init(testedInstance);

    }

    @After
    public void tearDown()
            throws Exception {

    }

    @Test
    public void findCachedStringAttribute()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        SubjectEntity testSubject = new SubjectEntity(userId);
        String testCachedString = "test-cached-string-" + UUID.randomUUID().toString();
        String pluginServiceName = "test-plugin-service";

        // define attribute type
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testStringAttributeName, DatatypeType.STRING, true, true);
        attributeType.setPluginName(pluginServiceName);
        attributeType.setPluginConfiguration("test-plugin-configuration");
        attributeType.setAttributeCacheTimeoutMillis(5000);

        // define cached attribute
        AttributeCacheEntity attribute = new AttributeCacheEntity(attributeType, testSubject, 0);
        attribute.setStringValue(testCachedString);

        // expectations
        expect(mockAttributeTypeDAO.getAttributeType(testStringAttributeName)).andStubReturn(attributeType);
        expect(mockSubjectService.getSubject(userId)).andStubReturn(testSubject);
        expect(mockAttributeCacheDAO.listAttributes(testSubject, attributeType)).andStubReturn(Collections.singletonList(attribute));

        // prepare
        replay(mockObjects);

        // operate: fetch attribute value
        Object attributeValue = testedInstance.findAttributeValue(userId, testStringAttributeName);

        // verify
        verify(mockObjects);
        assertNotNull(attributeValue);
        assertTrue(attributeValue.getClass().equals(String.class));
        String stringAttributeValue = (String) attributeValue;
        assertEquals(testCachedString, stringAttributeValue);
    }

    @Test
    public void findExpiredCachedExternalStringAttribute()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        SubjectEntity testSubject = new SubjectEntity(userId);

        String pluginServiceName = "test-plugin-service";
        PluginAttributeService testPluginService = new TestPluginService();

        // define attribute type
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testStringAttributeName, DatatypeType.STRING, true, true);
        attributeType.setMultivalued(true);
        attributeType.setPluginName(pluginServiceName);
        attributeType.setPluginConfiguration("test-plugin-configuration");
        attributeType.setAttributeCacheTimeoutMillis(5000);

        // define cached attribute
        AttributeCacheEntity attribute = new AttributeCacheEntity(attributeType, testSubject, 0);
        attribute.setEntryDate(new Date(0));

        // expectations
        expect(mockAttributeTypeDAO.getAttributeType(testStringAttributeName)).andStubReturn(attributeType);
        expect(mockSubjectService.getSubject(userId)).andStubReturn(testSubject);
        expect(mockAttributeCacheDAO.listAttributes(testSubject, attributeType)).andStubReturn(Collections.singletonList(attribute));
        mockAttributeCacheDAO.removeAttributes(testSubject, attributeType);
        expect(mockOSGIStartable.getService(pluginServiceName, OSGIServiceType.PLUGIN_SERVICE)).andStubReturn(mockOSGIService);
        expect(mockOSGIService.getService()).andStubReturn(testPluginService);
        mockOSGIService.ungetService();
        expect(mockAttributeCacheDAO.addAttribute(attributeType, testSubject, 0)).andStubReturn(
                new AttributeCacheEntity(attributeType, testSubject, 0));
        expect(mockAttributeCacheDAO.addAttribute(attributeType, testSubject, 1)).andStubReturn(
                new AttributeCacheEntity(attributeType, testSubject, 1));
        expect(mockAttributeCacheDAO.addAttribute(attributeType, testSubject, 2)).andStubReturn(
                new AttributeCacheEntity(attributeType, testSubject, 2));

        // prepare
        replay(mockObjects);
        EasyMock.replay(mockOSGIService);

        // operate: fetch attribute value
        Object attributeValue = testedInstance.findAttributeValue(userId, testStringAttributeName);

        // verify
        verify(mockObjects);
        EasyMock.verify(mockOSGIService);
        assertNotNull(attributeValue);
        assertTrue(attributeValue.getClass().equals(String[].class));
        String[] stringAttributeValue = (String[]) attributeValue;
        assertEquals(3, stringAttributeValue.length);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void findCachedCompoundAttribute()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String pluginServiceName = "test-plugin-service";

        SubjectEntity testSubject = new SubjectEntity(userId);

        // define attribute type
        AttributeTypeEntity stringAttributeType = new AttributeTypeEntity(testStringAttributeName, DatatypeType.STRING, true, true);
        stringAttributeType.setMultivalued(true);
        stringAttributeType.setPluginName(pluginServiceName);
        stringAttributeType.setAttributeCacheTimeoutMillis(3600000);

        AttributeTypeEntity booleanAttributeType = new AttributeTypeEntity(testBooleanAttributeName, DatatypeType.BOOLEAN, true, true);
        booleanAttributeType.setMultivalued(true);
        booleanAttributeType.setPluginName(pluginServiceName);
        booleanAttributeType.setAttributeCacheTimeoutMillis(3600000);

        AttributeTypeEntity dateAttributeType = new AttributeTypeEntity(testDateAttributeName, DatatypeType.DATE, true, true);
        dateAttributeType.setMultivalued(true);
        dateAttributeType.setPluginName(pluginServiceName);
        dateAttributeType.setAttributeCacheTimeoutMillis(3600000);

        AttributeTypeEntity doubleAttributeType = new AttributeTypeEntity(testDoubleAttributeName, DatatypeType.DOUBLE, true, true);
        doubleAttributeType.setMultivalued(true);
        doubleAttributeType.setPluginName(pluginServiceName);
        doubleAttributeType.setAttributeCacheTimeoutMillis(3600000);

        AttributeTypeEntity integerAttributeType = new AttributeTypeEntity(testIntegerAttributeName, DatatypeType.INTEGER, true, true);
        integerAttributeType.setMultivalued(true);
        integerAttributeType.setPluginName(pluginServiceName);
        integerAttributeType.setAttributeCacheTimeoutMillis(3600000);

        AttributeTypeEntity compoundedAttributeType = new AttributeTypeEntity(testCompoundAttributeName, DatatypeType.COMPOUNDED, true,
                true);
        compoundedAttributeType.setMultivalued(true);
        compoundedAttributeType.setPluginName(pluginServiceName);
        compoundedAttributeType.setPluginConfiguration("test-plugin-configuration");
        compoundedAttributeType.addMember(stringAttributeType, 0, true);
        compoundedAttributeType.addMember(booleanAttributeType, 1, true);
        compoundedAttributeType.addMember(dateAttributeType, 2, true);
        compoundedAttributeType.addMember(doubleAttributeType, 3, true);
        compoundedAttributeType.addMember(integerAttributeType, 4, true);
        compoundedAttributeType.setPluginName(pluginServiceName);
        compoundedAttributeType.setAttributeCacheTimeoutMillis(3600000);

        // define cached attributes
        List<AttributeCacheEntity> members = new LinkedList<AttributeCacheEntity>();

        AttributeCacheEntity stringAttribute = new AttributeCacheEntity(stringAttributeType, testSubject, 0);
        stringAttribute.setStringValue("test-cached-string");
        members.add(stringAttribute);

        AttributeCacheEntity booleanAttribute = new AttributeCacheEntity(booleanAttributeType, testSubject, 0);
        booleanAttribute.setBooleanValue(true);
        members.add(booleanAttribute);

        AttributeCacheEntity dateAttribute = new AttributeCacheEntity(dateAttributeType, testSubject, 0);
        dateAttribute.setDateValue(new Date());
        members.add(dateAttribute);

        AttributeCacheEntity doubleAttribute = new AttributeCacheEntity(doubleAttributeType, testSubject, 0);
        doubleAttribute.setDoubleValue(99.9);
        members.add(doubleAttribute);

        AttributeCacheEntity integerAttribute = new AttributeCacheEntity(integerAttributeType, testSubject, 0);
        integerAttribute.setIntegerValue(9);
        members.add(integerAttribute);

        AttributeCacheEntity compoundedAttribute = new AttributeCacheEntity(compoundedAttributeType, testSubject, 0);
        compoundedAttribute.setMembers(members);

        // expectations
        expect(mockAttributeTypeDAO.getAttributeType(testCompoundAttributeName)).andStubReturn(compoundedAttributeType);
        expect(mockSubjectService.getSubject(userId)).andStubReturn(testSubject);
        expect(mockAttributeCacheDAO.listAttributes(testSubject, compoundedAttributeType)).andStubReturn(
                Collections.singletonList(compoundedAttribute));
        expect(mockAttributeCacheDAO.findAttribute(testSubject, stringAttributeType, 0)).andStubReturn(stringAttribute);
        expect(mockAttributeCacheDAO.findAttribute(testSubject, booleanAttributeType, 0)).andStubReturn(booleanAttribute);
        expect(mockAttributeCacheDAO.findAttribute(testSubject, dateAttributeType, 0)).andStubReturn(dateAttribute);
        expect(mockAttributeCacheDAO.findAttribute(testSubject, doubleAttributeType, 0)).andStubReturn(doubleAttribute);
        expect(mockAttributeCacheDAO.findAttribute(testSubject, integerAttributeType, 0)).andStubReturn(integerAttribute);

        // prepare
        replay(mockObjects);

        // operate: fetch attribute value
        Object attributeValue = testedInstance.findAttributeValue(userId, testCompoundAttributeName);

        // verify
        verify(mockObjects);
        assertNotNull(attributeValue);
        assertTrue(attributeValue.getClass().equals(Map[].class));
        Map<String, Object>[] mapAttributeValue = (Map<String, Object>[]) attributeValue;
        assertEquals(1, mapAttributeValue.length);
        for (Map<String, Object> mapAttribute : mapAttributeValue) {
            assertTrue(mapAttribute.get(testStringAttributeName).getClass().equals(String.class));
            assertTrue(mapAttribute.get(testBooleanAttributeName).getClass().equals(Boolean.class));
            assertTrue(mapAttribute.get(testDateAttributeName).getClass().equals(Date.class));
            assertTrue(mapAttribute.get(testDoubleAttributeName).getClass().equals(Double.class));
            assertTrue(mapAttribute.get(testIntegerAttributeName).getClass().equals(Integer.class));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void findCachedCompoundAttributeButMemberExpired()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        SubjectEntity testSubject = new SubjectEntity(userId);

        String pluginServiceName = "test-plugin-service";
        PluginAttributeService testPluginService = new TestPluginService();

        // define attribute type
        AttributeTypeEntity stringAttributeType = new AttributeTypeEntity(testStringAttributeName, DatatypeType.STRING, true, true);
        stringAttributeType.setMultivalued(true);
        stringAttributeType.setPluginName(pluginServiceName);
        stringAttributeType.setAttributeCacheTimeoutMillis(3600000);

        AttributeTypeEntity booleanAttributeType = new AttributeTypeEntity(testBooleanAttributeName, DatatypeType.BOOLEAN, true, true);
        booleanAttributeType.setMultivalued(true);
        booleanAttributeType.setPluginName(pluginServiceName);
        booleanAttributeType.setAttributeCacheTimeoutMillis(3600000);

        AttributeTypeEntity dateAttributeType = new AttributeTypeEntity(testDateAttributeName, DatatypeType.DATE, true, true);
        dateAttributeType.setMultivalued(true);
        dateAttributeType.setPluginName(pluginServiceName);
        dateAttributeType.setAttributeCacheTimeoutMillis(3600000);

        AttributeTypeEntity doubleAttributeType = new AttributeTypeEntity(testDoubleAttributeName, DatatypeType.DOUBLE, true, true);
        doubleAttributeType.setMultivalued(true);
        doubleAttributeType.setPluginName(pluginServiceName);
        doubleAttributeType.setAttributeCacheTimeoutMillis(3600000);

        AttributeTypeEntity integerAttributeType = new AttributeTypeEntity(testIntegerAttributeName, DatatypeType.INTEGER, true, true);
        integerAttributeType.setMultivalued(true);
        integerAttributeType.setPluginName(pluginServiceName);
        integerAttributeType.setAttributeCacheTimeoutMillis(3600000);

        AttributeTypeEntity compoundedAttributeType = new AttributeTypeEntity(testCompoundAttributeName, DatatypeType.COMPOUNDED, true,
                true);
        compoundedAttributeType.setMultivalued(true);
        compoundedAttributeType.setPluginName(pluginServiceName);
        compoundedAttributeType.setPluginConfiguration("test-plugin-configuration");
        compoundedAttributeType.addMember(stringAttributeType, 0, true);
        compoundedAttributeType.addMember(booleanAttributeType, 1, true);
        compoundedAttributeType.addMember(dateAttributeType, 2, true);
        compoundedAttributeType.addMember(doubleAttributeType, 3, true);
        compoundedAttributeType.addMember(integerAttributeType, 4, true);
        compoundedAttributeType.setPluginName(pluginServiceName);
        compoundedAttributeType.setAttributeCacheTimeoutMillis(3600000);

        // define cached attributes
        List<AttributeCacheEntity> members = new LinkedList<AttributeCacheEntity>();

        AttributeCacheEntity stringAttribute = new AttributeCacheEntity(stringAttributeType, testSubject, 0);
        stringAttribute.setStringValue("test-cached-string");
        members.add(stringAttribute);

        AttributeCacheEntity booleanAttribute = new AttributeCacheEntity(booleanAttributeType, testSubject, 0);
        booleanAttribute.setBooleanValue(true);
        members.add(booleanAttribute);

        AttributeCacheEntity dateAttribute = new AttributeCacheEntity(dateAttributeType, testSubject, 0);
        dateAttribute.setDateValue(new Date());
        dateAttribute.setEntryDate(new Date(0));
        members.add(dateAttribute);

        AttributeCacheEntity doubleAttribute = new AttributeCacheEntity(doubleAttributeType, testSubject, 0);
        doubleAttribute.setDoubleValue(99.9);
        members.add(doubleAttribute);

        AttributeCacheEntity integerAttribute = new AttributeCacheEntity(integerAttributeType, testSubject, 0);
        integerAttribute.setIntegerValue(9);
        members.add(integerAttribute);

        AttributeCacheEntity compoundedAttribute = new AttributeCacheEntity(compoundedAttributeType, testSubject, 0);
        compoundedAttribute.setMembers(members);

        // expectations
        expect(mockAttributeTypeDAO.getAttributeType(testCompoundAttributeName)).andStubReturn(compoundedAttributeType);
        expect(mockSubjectService.getSubject(userId)).andStubReturn(testSubject);

        // lookup cache
        expect(mockAttributeCacheDAO.listAttributes(testSubject, compoundedAttributeType)).andStubReturn(
                Collections.singletonList(compoundedAttribute));
        expect(mockAttributeCacheDAO.findAttribute(testSubject, stringAttributeType, 0)).andStubReturn(stringAttribute);
        expect(mockAttributeCacheDAO.findAttribute(testSubject, booleanAttributeType, 0)).andStubReturn(booleanAttribute);
        expect(mockAttributeCacheDAO.findAttribute(testSubject, dateAttributeType, 0)).andStubReturn(dateAttribute);
        mockAttributeCacheDAO.removeAttributes(testSubject, compoundedAttributeType);

        // find external
        expect(mockOSGIStartable.getService(pluginServiceName, OSGIServiceType.PLUGIN_SERVICE)).andStubReturn(mockOSGIService);
        expect(mockOSGIService.getService()).andStubReturn(testPluginService);
        mockOSGIService.ungetService();

        // cache
        expect(mockAttributeCacheDAO.addAttribute(compoundedAttributeType, testSubject, 0)).andStubReturn(
                new AttributeCacheEntity(compoundedAttributeType, testSubject, 0));
        expect(mockAttributeCacheDAO.addAttribute(compoundedAttributeType, testSubject, 1)).andStubReturn(
                new AttributeCacheEntity(compoundedAttributeType, testSubject, 1));

        expect(mockAttributeCacheDAO.findAttribute(testSubject, stringAttributeType, 0)).andStubReturn(null);
        expect(mockAttributeCacheDAO.findAttribute(testSubject, stringAttributeType, 1)).andStubReturn(null);
        expect(mockAttributeCacheDAO.addAttribute(stringAttributeType, testSubject, 0)).andStubReturn(
                new AttributeCacheEntity(stringAttributeType, testSubject, 0));
        expect(mockAttributeCacheDAO.addAttribute(stringAttributeType, testSubject, 1)).andStubReturn(
                new AttributeCacheEntity(stringAttributeType, testSubject, 1));

        expect(mockAttributeCacheDAO.findAttribute(testSubject, dateAttributeType, 0)).andStubReturn(null);
        expect(mockAttributeCacheDAO.findAttribute(testSubject, dateAttributeType, 1)).andStubReturn(null);
        expect(mockAttributeCacheDAO.addAttribute(dateAttributeType, testSubject, 0)).andStubReturn(
                new AttributeCacheEntity(dateAttributeType, testSubject, 0));
        expect(mockAttributeCacheDAO.addAttribute(dateAttributeType, testSubject, 1)).andStubReturn(
                new AttributeCacheEntity(dateAttributeType, testSubject, 1));

        expect(mockAttributeCacheDAO.findAttribute(testSubject, booleanAttributeType, 0)).andStubReturn(null);
        expect(mockAttributeCacheDAO.findAttribute(testSubject, booleanAttributeType, 1)).andStubReturn(null);
        expect(mockAttributeCacheDAO.addAttribute(booleanAttributeType, testSubject, 0)).andStubReturn(
                new AttributeCacheEntity(booleanAttributeType, testSubject, 0));
        expect(mockAttributeCacheDAO.addAttribute(booleanAttributeType, testSubject, 1)).andStubReturn(
                new AttributeCacheEntity(booleanAttributeType, testSubject, 1));

        expect(mockAttributeCacheDAO.findAttribute(testSubject, doubleAttributeType, 0)).andStubReturn(null);
        expect(mockAttributeCacheDAO.findAttribute(testSubject, doubleAttributeType, 1)).andStubReturn(null);
        expect(mockAttributeCacheDAO.addAttribute(doubleAttributeType, testSubject, 0)).andStubReturn(
                new AttributeCacheEntity(doubleAttributeType, testSubject, 0));
        expect(mockAttributeCacheDAO.addAttribute(doubleAttributeType, testSubject, 1)).andStubReturn(
                new AttributeCacheEntity(doubleAttributeType, testSubject, 1));

        expect(mockAttributeCacheDAO.findAttribute(testSubject, integerAttributeType, 0)).andStubReturn(null);
        expect(mockAttributeCacheDAO.findAttribute(testSubject, integerAttributeType, 1)).andStubReturn(null);
        expect(mockAttributeCacheDAO.addAttribute(integerAttributeType, testSubject, 0)).andStubReturn(
                new AttributeCacheEntity(integerAttributeType, testSubject, 0));
        expect(mockAttributeCacheDAO.addAttribute(integerAttributeType, testSubject, 1)).andStubReturn(
                new AttributeCacheEntity(integerAttributeType, testSubject, 1));

        // prepare
        replay(mockObjects);
        EasyMock.replay(mockOSGIService);

        // operate: fetch attribute value
        Object attributeValue = testedInstance.findAttributeValue(userId, testCompoundAttributeName);

        // verify
        verify(mockObjects);
        EasyMock.verify(mockOSGIService);
        assertNotNull(attributeValue);
        assertTrue(attributeValue.getClass().equals(Map[].class));
        Map<String, Object>[] mapAttributeValue = (Map<String, Object>[]) attributeValue;
        assertEquals(2, mapAttributeValue.length);
        for (Map<String, Object> mapAttribute : mapAttributeValue) {
            assertTrue(mapAttribute.get(testStringAttributeName).getClass().equals(String.class));
            assertTrue(mapAttribute.get(testBooleanAttributeName).getClass().equals(Boolean.class));
            assertTrue(mapAttribute.get(testDateAttributeName).getClass().equals(Date.class));
            assertTrue(mapAttribute.get(testDoubleAttributeName).getClass().equals(Double.class));
            assertTrue(mapAttribute.get(testIntegerAttributeName).getClass().equals(Integer.class));
        }
    }

    @Test
    public void findExternalStringAttributeAndCache()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        SubjectEntity testSubject = new SubjectEntity(userId);

        String pluginServiceName = "test-plugin-service";
        PluginAttributeService testPluginService = new TestPluginService();

        // define attribute type
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testStringAttributeName, DatatypeType.STRING, true, true);
        attributeType.setMultivalued(true);
        attributeType.setPluginName(pluginServiceName);
        attributeType.setPluginConfiguration("test-plugin-configuration");
        attributeType.setAttributeCacheTimeoutMillis(3600000);

        // expectations
        expect(mockAttributeTypeDAO.getAttributeType(testStringAttributeName)).andStubReturn(attributeType);
        expect(mockSubjectService.getSubject(userId)).andStubReturn(testSubject);
        expect(mockAttributeCacheDAO.listAttributes(testSubject, attributeType)).andStubReturn(null);
        expect(mockOSGIStartable.getService(pluginServiceName, OSGIServiceType.PLUGIN_SERVICE)).andStubReturn(mockOSGIService);
        expect(mockOSGIService.getService()).andStubReturn(testPluginService);
        mockOSGIService.ungetService();
        expect(mockAttributeCacheDAO.addAttribute(attributeType, testSubject, 0)).andStubReturn(
                new AttributeCacheEntity(attributeType, testSubject, 0));
        expect(mockAttributeCacheDAO.addAttribute(attributeType, testSubject, 1)).andStubReturn(
                new AttributeCacheEntity(attributeType, testSubject, 1));
        expect(mockAttributeCacheDAO.addAttribute(attributeType, testSubject, 2)).andStubReturn(
                new AttributeCacheEntity(attributeType, testSubject, 2));

        // prepare
        replay(mockObjects);
        EasyMock.replay(mockOSGIService);

        // operate: fetch attribute value
        Object attributeValue = testedInstance.findAttributeValue(userId, testStringAttributeName);

        // verify
        verify(mockObjects);
        EasyMock.verify(mockOSGIService);
        assertNotNull(attributeValue);
        assertTrue(attributeValue.getClass().equals(String[].class));
        String[] stringAttributeValue = (String[]) attributeValue;
        assertEquals(3, stringAttributeValue.length);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void findExternalCompoundAttribute()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String pluginServiceName = "test-plugin-service";
        PluginAttributeService testPluginService = new TestPluginService();

        SubjectEntity testSubject = new SubjectEntity(userId);

        // define attribute type
        AttributeTypeEntity stringAttributeType = new AttributeTypeEntity(testStringAttributeName, DatatypeType.STRING, true, true);
        stringAttributeType.setMultivalued(true);
        stringAttributeType.setPluginName(pluginServiceName);

        AttributeTypeEntity booleanAttributeType = new AttributeTypeEntity(testBooleanAttributeName, DatatypeType.BOOLEAN, true, true);
        booleanAttributeType.setMultivalued(true);
        booleanAttributeType.setPluginName(pluginServiceName);

        AttributeTypeEntity dateAttributeType = new AttributeTypeEntity(testDateAttributeName, DatatypeType.DATE, true, true);
        dateAttributeType.setMultivalued(true);
        dateAttributeType.setPluginName(pluginServiceName);

        AttributeTypeEntity doubleAttributeType = new AttributeTypeEntity(testDoubleAttributeName, DatatypeType.DOUBLE, true, true);
        doubleAttributeType.setMultivalued(true);
        doubleAttributeType.setPluginName(pluginServiceName);

        AttributeTypeEntity integerAttributeType = new AttributeTypeEntity(testIntegerAttributeName, DatatypeType.INTEGER, true, true);
        integerAttributeType.setMultivalued(true);
        integerAttributeType.setPluginName(pluginServiceName);

        AttributeTypeEntity compoundedAttributeType = new AttributeTypeEntity(testCompoundAttributeName, DatatypeType.COMPOUNDED, true,
                true);
        compoundedAttributeType.setMultivalued(true);
        compoundedAttributeType.setPluginName(pluginServiceName);
        compoundedAttributeType.setPluginConfiguration("test-plugin-configuration");
        compoundedAttributeType.addMember(stringAttributeType, 0, true);
        compoundedAttributeType.addMember(booleanAttributeType, 1, true);
        compoundedAttributeType.addMember(dateAttributeType, 2, true);
        compoundedAttributeType.addMember(doubleAttributeType, 3, true);
        compoundedAttributeType.addMember(integerAttributeType, 4, true);
        compoundedAttributeType.setPluginName(pluginServiceName);

        // expectations
        expect(mockAttributeTypeDAO.getAttributeType(testCompoundAttributeName)).andStubReturn(compoundedAttributeType);
        expect(mockSubjectService.getSubject(userId)).andStubReturn(testSubject);
        expect(mockAttributeCacheDAO.listAttributes(testSubject, compoundedAttributeType)).andStubReturn(null);
        expect(mockOSGIStartable.getService(pluginServiceName, OSGIServiceType.PLUGIN_SERVICE)).andStubReturn(mockOSGIService);
        expect(mockOSGIService.getService()).andStubReturn(testPluginService);
        mockOSGIService.ungetService();

        // prepare
        replay(mockObjects);
        EasyMock.replay(mockOSGIService);

        // operate: fetch attribute value
        Object attributeValue = testedInstance.findAttributeValue(userId, testCompoundAttributeName);

        // verify
        verify(mockObjects);
        EasyMock.verify(mockOSGIService);
        assertNotNull(attributeValue);
        assertTrue(attributeValue.getClass().equals(Map[].class));
        Map<String, Object>[] mapAttributeValue = (Map<String, Object>[]) attributeValue;
        assertEquals(2, mapAttributeValue.length);
        for (Map<String, Object> mapAttribute : mapAttributeValue) {
            assertTrue(mapAttribute.get(testStringAttributeName).getClass().equals(String.class));
            assertTrue(mapAttribute.get(testBooleanAttributeName).getClass().equals(Boolean.class));
            assertTrue(mapAttribute.get(testDateAttributeName).getClass().equals(Date.class));
            assertTrue(mapAttribute.get(testDoubleAttributeName).getClass().equals(Double.class));
            assertTrue(mapAttribute.get(testIntegerAttributeName).getClass().equals(Integer.class));
        }
    }

    @Test
    public void findExternalStringAttribute()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String pluginServiceName = "test-plugin-service";
        PluginAttributeService testPluginService = new TestPluginService();

        SubjectEntity testSubject = new SubjectEntity(userId);

        // define attribute type
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testStringAttributeName, DatatypeType.STRING, true, true);
        attributeType.setMultivalued(true);
        attributeType.setPluginName(pluginServiceName);
        attributeType.setPluginConfiguration("test-plugin-configuration");

        // expectations
        expect(mockAttributeTypeDAO.getAttributeType(testStringAttributeName)).andStubReturn(attributeType);
        expect(mockSubjectService.getSubject(userId)).andStubReturn(testSubject);
        expect(mockAttributeCacheDAO.listAttributes(testSubject, attributeType)).andStubReturn(null);
        expect(mockOSGIStartable.getService(pluginServiceName, OSGIServiceType.PLUGIN_SERVICE)).andStubReturn(mockOSGIService);
        expect(mockOSGIService.getService()).andStubReturn(testPluginService);
        mockOSGIService.ungetService();

        // prepare
        replay(mockObjects);
        EasyMock.replay(mockOSGIService);

        // operate: fetch attribute value
        Object attributeValue = testedInstance.findAttributeValue(userId, testStringAttributeName);

        // verify
        verify(mockObjects);
        EasyMock.verify(mockOSGIService);
        assertNotNull(attributeValue);
        assertTrue(attributeValue.getClass().equals(String[].class));
        String[] stringAttributeValue = (String[]) attributeValue;
        assertEquals(3, stringAttributeValue.length);
    }

    @Test
    public void findExternalBooleanAttribute()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String pluginServiceName = "test-plugin-service";
        PluginAttributeService testPluginService = new TestPluginService();

        SubjectEntity testSubject = new SubjectEntity(userId);

        // define attribute type
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testBooleanAttributeName, DatatypeType.BOOLEAN, true, true);
        attributeType.setMultivalued(true);
        attributeType.setPluginName(pluginServiceName);
        attributeType.setPluginConfiguration("test-plugin-configuration");

        // expectations
        expect(mockAttributeTypeDAO.getAttributeType(testBooleanAttributeName)).andStubReturn(attributeType);
        expect(mockSubjectService.getSubject(userId)).andStubReturn(testSubject);
        expect(mockAttributeCacheDAO.listAttributes(testSubject, attributeType)).andStubReturn(null);
        expect(mockOSGIStartable.getService(pluginServiceName, OSGIServiceType.PLUGIN_SERVICE)).andStubReturn(mockOSGIService);
        expect(mockOSGIService.getService()).andStubReturn(testPluginService);
        mockOSGIService.ungetService();

        // prepare
        replay(mockObjects);
        EasyMock.replay(mockOSGIService);

        // operate: fetch attribute value
        Object attributeValue = testedInstance.findAttributeValue(userId, testBooleanAttributeName);

        // verify
        verify(mockObjects);
        EasyMock.verify(mockOSGIService);
        assertNotNull(attributeValue);
        assertTrue(attributeValue.getClass().equals(Boolean[].class));
        Boolean[] booleanAttributeValue = (Boolean[]) attributeValue;
        assertEquals(3, booleanAttributeValue.length);
    }

    @Test
    public void findExternalDateAttribute()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String pluginServiceName = "test-plugin-service";
        PluginAttributeService testPluginService = new TestPluginService();

        SubjectEntity testSubject = new SubjectEntity(userId);

        // define attribute type
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testDateAttributeName, DatatypeType.DATE, true, true);
        attributeType.setMultivalued(true);
        attributeType.setPluginName(pluginServiceName);
        attributeType.setPluginConfiguration("test-plugin-configuration");

        // expectations
        expect(mockAttributeTypeDAO.getAttributeType(testDateAttributeName)).andStubReturn(attributeType);
        expect(mockSubjectService.getSubject(userId)).andStubReturn(testSubject);
        expect(mockAttributeCacheDAO.listAttributes(testSubject, attributeType)).andStubReturn(null);
        expect(mockOSGIStartable.getService(pluginServiceName, OSGIServiceType.PLUGIN_SERVICE)).andStubReturn(mockOSGIService);
        expect(mockOSGIService.getService()).andStubReturn(testPluginService);
        mockOSGIService.ungetService();

        // prepare
        replay(mockObjects);
        EasyMock.replay(mockOSGIService);

        // operate: fetch attribute value
        Object attributeValue = testedInstance.findAttributeValue(userId, testDateAttributeName);

        // verify
        verify(mockObjects);
        EasyMock.verify(mockOSGIService);
        assertNotNull(attributeValue);
        assertTrue(attributeValue.getClass().equals(Date[].class));
        Date[] dateAttributeValue = (Date[]) attributeValue;
        assertEquals(3, dateAttributeValue.length);
    }

    @Test
    public void findExternalDoubleAttribute()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String pluginServiceName = "test-plugin-service";
        PluginAttributeService testPluginService = new TestPluginService();

        SubjectEntity testSubject = new SubjectEntity(userId);

        // define attribute type
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testDoubleAttributeName, DatatypeType.DOUBLE, true, true);
        attributeType.setMultivalued(true);
        attributeType.setPluginName(pluginServiceName);
        attributeType.setPluginConfiguration("test-plugin-configuration");

        // expectations
        expect(mockAttributeTypeDAO.getAttributeType(testDoubleAttributeName)).andStubReturn(attributeType);
        expect(mockSubjectService.getSubject(userId)).andStubReturn(testSubject);
        expect(mockAttributeCacheDAO.listAttributes(testSubject, attributeType)).andStubReturn(null);
        expect(mockOSGIStartable.getService(pluginServiceName, OSGIServiceType.PLUGIN_SERVICE)).andStubReturn(mockOSGIService);
        expect(mockOSGIService.getService()).andStubReturn(testPluginService);
        mockOSGIService.ungetService();

        // prepare
        replay(mockObjects);
        EasyMock.replay(mockOSGIService);

        // operate: fetch attribute value
        Object attributeValue = testedInstance.findAttributeValue(userId, testDoubleAttributeName);

        // verify
        verify(mockObjects);
        EasyMock.verify(mockOSGIService);
        assertNotNull(attributeValue);
        assertTrue(attributeValue.getClass().equals(Double[].class));
        Double[] doubleAttributeValue = (Double[]) attributeValue;
        assertEquals(3, doubleAttributeValue.length);
    }

    @Test
    public void findExternalIntegerAttribute()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String pluginServiceName = "test-plugin-service";
        PluginAttributeService testPluginService = new TestPluginService();

        SubjectEntity testSubject = new SubjectEntity(userId);

        // define attribute type
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testIntegerAttributeName, DatatypeType.INTEGER, true, true);
        attributeType.setMultivalued(true);
        attributeType.setPluginName(pluginServiceName);
        attributeType.setPluginConfiguration("test-plugin-configuration");

        // expectations
        expect(mockAttributeTypeDAO.getAttributeType(testIntegerAttributeName)).andStubReturn(attributeType);
        expect(mockSubjectService.getSubject(userId)).andStubReturn(testSubject);
        expect(mockAttributeCacheDAO.listAttributes(testSubject, attributeType)).andStubReturn(null);
        expect(mockOSGIStartable.getService(pluginServiceName, OSGIServiceType.PLUGIN_SERVICE)).andStubReturn(mockOSGIService);
        expect(mockOSGIService.getService()).andStubReturn(testPluginService);
        mockOSGIService.ungetService();

        // prepare
        replay(mockObjects);
        EasyMock.replay(mockOSGIService);

        // operate: fetch attribute value
        Object attributeValue = testedInstance.findAttributeValue(userId, testIntegerAttributeName);

        // verify
        verify(mockObjects);
        EasyMock.verify(mockOSGIService);
        assertNotNull(attributeValue);
        assertTrue(attributeValue.getClass().equals(Integer[].class));
        Integer[] integerAttributeValue = (Integer[]) attributeValue;
        assertEquals(3, integerAttributeValue.length);
    }


    static class TestPluginService implements PluginAttributeService {

        @SuppressWarnings("unchecked")
        public Object getAttribute(String userId, String attributeName, String configuration)
                throws AttributeNotFoundException, AttributeTypeNotFoundException {

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
            }
            return null;
        }

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
}
