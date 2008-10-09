/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.wicket.test;

import javax.persistence.EntityManager;

import net.link.safeonline.demo.wicket.javaee.DummyJndi;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;


/**
 * <h2>{@link WicketTests}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Oct 7, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public abstract class WicketTests {

    protected WicketTester    wicket;
    protected final Log       LOG = LogFactory.getLog(getClass());

    private EntityTestManager entityTestManager;


    @Before
    public void setup() {

        this.wicket = new WicketTester(getApplication());

        // Set up an HSQL entity manager.
        this.entityTestManager = new EntityTestManager();
        Class<?>[] serviceBeans = getServiceBeans();
        try {
            this.entityTestManager.setUp(getEntities());
        } catch (Exception err) {
            this.LOG.error("Couldn't set up entity manager", err);
            throw new IllegalStateException(err);
        }

        // Perform injections on our service beans.
        EntityManager entityManager = this.entityTestManager.getEntityManager();
        for (Class<?> beanClass : serviceBeans) {
            DummyJndi.register(beanClass, EJBTestUtils.newInstance(beanClass, serviceBeans, entityManager));
        }
    }

    @After
    public void tearDown() throws Exception {

        this.entityTestManager.tearDown();
    }

    protected abstract WebApplication getApplication();

    protected abstract Class<?>[] getServiceBeans();

    protected abstract Class<?>[] getEntities();
}
