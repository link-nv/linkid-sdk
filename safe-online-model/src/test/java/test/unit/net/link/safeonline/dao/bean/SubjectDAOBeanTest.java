/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.dao.bean;

import java.util.UUID;

import javax.persistence.RollbackException;

import junit.framework.TestCase;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.test.util.EntityTestManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SubjectDAOBeanTest extends TestCase {

    private static final Log  LOG = LogFactory.getLog(SubjectDAOBeanTest.class);

    private SubjectDAO        testedInstance;

    private EntityTestManager entityTestManager;


    @Override
    protected void setUp() throws Exception {

        super.setUp();

        this.entityTestManager = new EntityTestManager();
        this.entityTestManager.setUp(SubjectEntity.class);

        this.testedInstance = this.entityTestManager.newInstance(SubjectDAOBean.class);
    }

    @Override
    protected void tearDown() throws Exception {

        this.entityTestManager.tearDown();
        super.tearDown();
    }

    public void testFindNonExistingSubjectReturnsNull() throws Exception {

        // setup
        String nonExistingSubjectLogin = UUID.randomUUID().toString();

        // operate
        SubjectEntity result = this.testedInstance.findSubject(nonExistingSubjectLogin);

        // verify
        assertNull(result);
    }

    public void testAddSubjectAndGet() throws Exception {

        // setup
        String subjectLogin = UUID.randomUUID().toString();

        // operate
        this.testedInstance.addSubject(subjectLogin);
        SubjectEntity resultSubject = this.testedInstance.getSubject(subjectLogin);

        // verify
        assertEquals(subjectLogin, resultSubject.getUserId());
    }

    public void testAddingTwiceFails() throws Exception {

        // setup
        String subjectLogin = UUID.randomUUID().toString();

        // operate
        this.testedInstance.addSubject(subjectLogin);

        // operate & verify
        try {
            this.testedInstance.addSubject(subjectLogin);
            fail();
        } catch (RollbackException e) {
            // expected
            LOG.debug("exception type: " + e.getClass().getName());
        }
    }
}
