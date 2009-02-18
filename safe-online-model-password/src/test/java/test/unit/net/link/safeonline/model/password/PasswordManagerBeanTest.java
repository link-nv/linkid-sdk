/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package test.unit.net.link.safeonline.model.password;

public class PasswordManagerBeanTest {
    //
    // private EntityTestManager entityTestManager;
    //
    // private PasswordManager testedInstance;
    //
    // private AttributeTypeDAO attributeTypeDAO;
    //
    // private AttributeDAO attributeDAO;
    //
    // private SubjectDAO subjectDAO;
    //
    //
    // @Before
    // public void setUp()
    // throws Exception {
    //
    // entityTestManager = new EntityTestManager();
    // /*
    // * If you add entities to this list, also add them to safe-online-sql-ddl.
    // */
    // entityTestManager.setUp(SubjectEntity.class, ApplicationEntity.class, ApplicationOwnerEntity.class, AttributeEntity.class,
    // AttributeTypeEntity.class, SubscriptionEntity.class, TrustDomainEntity.class, ApplicationIdentityEntity.class,
    // ConfigGroupEntity.class, ConfigItemEntity.class, ConfigItemValueEntity.class, SchedulingEntity.class, TaskEntity.class,
    // TaskHistoryEntity.class, TrustPointEntity.class, ApplicationIdentityAttributeEntity.class,
    // AttributeTypeDescriptionEntity.class, AttributeProviderEntity.class, DeviceEntity.class, DeviceClassEntity.class,
    // DeviceDescriptionEntity.class, DevicePropertyEntity.class, DeviceClassDescriptionEntity.class, AllowedDeviceEntity.class,
    // CompoundedAttributeTypeMemberEntity.class, SubjectIdentifierEntity.class, UsageAgreementEntity.class,
    // UsageAgreementTextEntity.class, NodeEntity.class, EndpointReferenceEntity.class,
    // NotificationProducerSubscriptionEntity.class, ApplicationScopeIdEntity.class, AttributeCacheEntity.class,
    // ApplicationPoolEntity.class);
    //
    // testedInstance = new PasswordManagerBean();
    // attributeDAO = new AttributeDAOBean();
    // attributeTypeDAO = new AttributeTypeDAOBean();
    // subjectDAO = new SubjectDAOBean();
    //
    // EJBTestUtils.inject(attributeDAO, entityTestManager.getEntityManager());
    // EJBTestUtils.inject(attributeTypeDAO, entityTestManager.getEntityManager());
    // EJBTestUtils.inject(subjectDAO, entityTestManager.getEntityManager());
    // EJBTestUtils.inject(testedInstance, attributeDAO);
    // EJBTestUtils.inject(testedInstance, attributeTypeDAO);
    //
    // EJBTestUtils.init(attributeDAO);
    // EJBTestUtils.init(attributeTypeDAO);
    // EJBTestUtils.init(testedInstance);
    //
    // attributeTypeDAO.addAttributeType(new AttributeTypeEntity(PasswordConstants.PASSWORD_HASH_ATTRIBUTE, DatatypeType.STRING, false,
    // false));
    // attributeTypeDAO.addAttributeType(new AttributeTypeEntity(PasswordConstants.PASSWORD_SEED_ATTRIBUTE, DatatypeType.STRING, false,
    // false));
    // attributeTypeDAO.addAttributeType(new AttributeTypeEntity(PasswordConstants.PASSWORD_DEVICE_DISABLE_ATTRIBUTE,
    // DatatypeType.BOOLEAN, false, false));
    // attributeTypeDAO.addAttributeType(new AttributeTypeEntity(PasswordConstants.PASSWORD_ALGORITHM_ATTRIBUTE, DatatypeType.STRING,
    // false, false));
    // attributeTypeDAO.addAttributeType(new AttributeTypeEntity(PasswordConstants.PASSWORD_NEW_ALGORITHM_ATTRIBUTE, DatatypeType.STRING,
    // false, false));
    // attributeTypeDAO.addAttributeType(new AttributeTypeEntity(PasswordConstants.PASSWORD_DEVICE_ATTRIBUTE, DatatypeType.COMPOUNDED,
    // false, false));
    //
    // }
    //
    // @After
    // public void tearDown()
    // throws Exception {
    //
    // entityTestManager.tearDown();
    // }
    //
    // @Test
    // public void testSetPassword()
    // throws Exception {
    //
    // // prepare
    // UUID subjectUUID = UUID.randomUUID();
    // SubjectEntity subject = subjectDAO.addSubject(subjectUUID.toString());
    // String password = "password";
    //
    // // operate
    // testedInstance.registerPassword(subject, password);
    //
    // // validate
    // boolean validationResult = testedInstance.validatePassword(subject, password);
    // assertTrue(validationResult);
    // }
    //
    // @Test
    // public void testChangePassword()
    // throws Exception {
    //
    // // prepare
    // UUID subjectUUID = UUID.randomUUID();
    // SubjectEntity subject = subjectDAO.addSubject(subjectUUID.toString());
    // String password = "password";
    // String newPassword = "newpassword";
    //
    // // operate
    // testedInstance.registerPassword(subject, password);
    // testedInstance.updatePassword(subject, password, newPassword);
    //
    // // validate
    // boolean validationResult = testedInstance.validatePassword(subject, password);
    // assertFalse(validationResult);
    //
    // validationResult = testedInstance.validatePassword(subject, newPassword);
    // assertTrue(validationResult);
    // }
    //
    // @Test
    // public void validatePassword()
    // throws Exception {
    //
    // // prepare
    // UUID subjectUUID = UUID.randomUUID();
    // SubjectEntity subject = subjectDAO.addSubject(subjectUUID.toString());
    // String password = "password";
    //
    // // operate
    // testedInstance.registerPassword(subject, password);
    //
    // // validate
    // boolean validationResult = testedInstance.validatePassword(subject, password);
    // assertTrue(validationResult);
    // }
    //
    // @Test
    // public void testIsPasswordConfigured()
    // throws Exception {
    //
    // // prepare
    // UUID subjectUUID = UUID.randomUUID();
    // SubjectEntity subject = subjectDAO.addSubject(subjectUUID.toString());
    // String password = "password";
    //
    // // operate
    // testedInstance.registerPassword(subject, password);
    //
    // // validate
    // boolean validationResult = testedInstance.isPasswordConfigured(subject);
    // assertTrue(validationResult);
    // }

}
