/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.dao.bean;

import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.dao.bean.ApplicationDAOBean;
import net.link.safeonline.dao.bean.ApplicationIdentityDAOBean;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.dao.bean.SubjectIdentifierDAOBean;
import net.link.safeonline.dao.bean.SubscriptionDAOBean;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.model.bean.IdGeneratorBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class AttributeTypeDAOBeanTest extends TestCase {

    private EntityTestManager          entityTestManager;

    private ApplicationDAOBean         applicationDAO;

    private ApplicationOwnerDAOBean    applicationOwnerDAO;

    private SubjectDAOBean             subjectDAO;

    private AttributeDAOBean           attributeDAO;

    private ApplicationIdentityDAOBean applicationIdentityDAO;

    private SubscriptionDAOBean        subscriptionDAO;

    private IdGeneratorBean            idGenerator;

    private SubjectIdentifierDAOBean   subjectIdentifierDAO;

    private AttributeTypeDAOBean       testedInstance;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();
        entityTestManager = new EntityTestManager();
        /*
         * If you add entities to this list, also add them to safe-online-sql-ddl.
         */
        entityTestManager.setUp(SafeOnlineTestContainer.entities);
        // StatisticDataPointEntity.class,
        testedInstance = new AttributeTypeDAOBean();
        applicationDAO = new ApplicationDAOBean();
        applicationOwnerDAO = new ApplicationOwnerDAOBean();
        subjectDAO = new SubjectDAOBean();
        attributeDAO = new AttributeDAOBean();
        applicationIdentityDAO = new ApplicationIdentityDAOBean();
        subscriptionDAO = new SubscriptionDAOBean();
        idGenerator = new IdGeneratorBean();
        subjectIdentifierDAO = new SubjectIdentifierDAOBean();

        EJBTestUtils.inject(testedInstance, entityTestManager.getEntityManager());
        EJBTestUtils.inject(applicationDAO, entityTestManager.getEntityManager());
        EJBTestUtils.inject(applicationOwnerDAO, entityTestManager.getEntityManager());
        EJBTestUtils.inject(subjectDAO, entityTestManager.getEntityManager());
        EJBTestUtils.inject(attributeDAO, entityTestManager.getEntityManager());
        EJBTestUtils.inject(applicationIdentityDAO, entityTestManager.getEntityManager());
        EJBTestUtils.inject(subscriptionDAO, entityTestManager.getEntityManager());
        EJBTestUtils.inject(subscriptionDAO, idGenerator);
        EJBTestUtils.inject(subjectIdentifierDAO, entityTestManager.getEntityManager());

        EJBTestUtils.init(testedInstance);
        EJBTestUtils.init(applicationDAO);
        EJBTestUtils.init(applicationOwnerDAO);
        EJBTestUtils.init(subjectDAO);
        EJBTestUtils.init(attributeDAO);
        EJBTestUtils.init(applicationIdentityDAO);
        EJBTestUtils.init(subscriptionDAO);
        EJBTestUtils.init(subjectIdentifierDAO);
    }

    @Override
    protected void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        super.tearDown();
    }

    public void testDataMining()
            throws Exception {

        // setup users
        String user1name = UUID.randomUUID().toString();
        String user2name = UUID.randomUUID().toString();
        SubjectEntity user1 = subjectDAO.addSubject(user1name);
        SubjectEntity user2 = subjectDAO.addSubject(user2name);

        // setup attribute types
        String attributeName = UUID.randomUUID().toString();
        AttributeTypeEntity attributeType = new AttributeTypeEntity(attributeName, DatatypeType.STRING, false, false);
        testedInstance.addAttributeType(attributeType);

        // setup attribute
        String attributeValue = UUID.randomUUID().toString();
        attributeDAO.addAttribute(attributeType, user1, attributeValue);
        attributeDAO.addAttribute(attributeType, user2, attributeValue);

        // setup application owner
        String ownerName = UUID.randomUUID().toString();
        applicationOwnerDAO.addApplicationOwner(ownerName, user2);
        ApplicationOwnerEntity owner = applicationOwnerDAO.findApplicationOwner(ownerName);

        // setup application
        String applicationName = UUID.randomUUID().toString();
        ApplicationEntity application = applicationDAO.addApplication(applicationName, null, owner, null, null, null, null);

        // setup application identity
        applicationIdentityDAO.addApplicationIdentity(application, 1);
        ApplicationIdentityEntity applicationIdentity = applicationIdentityDAO.getApplicationIdentity(application, 1);
        applicationIdentityDAO.addApplicationIdentityAttribute(applicationIdentity, attributeType, true, false);

        // setup subscription
        subscriptionDAO.addSubscription(SubscriptionOwnerType.SUBJECT, user1, application);
        SubscriptionEntity subscription = subscriptionDAO.findSubscription(user1, application);
        subscription.setConfirmedIdentityVersion(Long.valueOf(1));

        // operate
        Map<Object, Long> result = testedInstance.categorize(application, attributeType);

        // verify
        for (Object value : result.keySet())
            if (value.equals(attributeValue)) {
                assertEquals(result.get(value), Long.valueOf(1));
            }

    }
}
