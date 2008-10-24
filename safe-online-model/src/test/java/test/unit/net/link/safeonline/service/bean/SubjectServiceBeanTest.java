/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.service.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import javax.persistence.EntityManager;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.service.bean.SubjectServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class SubjectServiceBeanTest {

    private SubjectService    testedInstance;

    private AttributeTypeDAO  attributeTypeDAO;

    private EntityTestManager entityTestManager;


    @Before
    public void setUp() throws Exception {

        this.entityTestManager = new EntityTestManager();
        this.entityTestManager.setUp(SafeOnlineTestContainer.entities);
        EntityManager entityManager = this.entityTestManager.getEntityManager();
        this.testedInstance = EJBTestUtils.newInstance(SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans, entityManager);

        this.attributeTypeDAO = new AttributeTypeDAOBean();
        EJBTestUtils.inject(this.attributeTypeDAO, entityManager);
        EJBTestUtils.init(this.attributeTypeDAO);

        this.attributeTypeDAO.addAttributeType(new AttributeTypeEntity(SafeOnlineConstants.LOGIN_ATTRIBTUE, DatatypeType.STRING, false,
                false));
    }

    @After
    public void tearDown() throws Exception {

        this.entityTestManager.tearDown();
    }

    @Test
    public void testFindNonExistingSubjectReturnsNull() throws Exception {

        // setup
        String nonExistingSubjectLogin = UUID.randomUUID().toString();

        // operate
        SubjectEntity result = this.testedInstance.findSubject(nonExistingSubjectLogin);

        // verify
        assertNull(result);
    }

    @Test
    public void testAddSubjectAndGet() throws Exception {

        // setup
        String subjectLogin = UUID.randomUUID().toString();

        // operate
        this.testedInstance.addSubject(subjectLogin);
        SubjectEntity resultSubject = this.testedInstance.findSubjectFromUserName(subjectLogin);
        String resultSubjectLogin = this.testedInstance.getSubjectLogin(resultSubject.getUserId());

        // verify
        assertEquals(subjectLogin, resultSubjectLogin);
    }

}
