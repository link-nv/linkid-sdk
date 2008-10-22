/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.wicket.test;

import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServlet;

import net.link.safeonline.demo.wicket.javaee.DummyJndi;
import net.link.safeonline.demo.wicket.tools.WicketUtil;
import net.link.safeonline.demo.wicket.tools.olas.DummyAttributeClient;
import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;
import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.form.AbstractChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.protocol.http.MockServletContext;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;


/**
 * <h2>{@link AbstractWicketTests}<br>
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
public abstract class AbstractWicketTests {

    protected final Log         LOG = LogFactory.getLog(getClass());
    protected EntityTestManager entityTestManager;
    protected WicketTester      wicket;


    @BeforeClass
    public static void init() {

        AuthenticationProtocolManager.registerProtocolHandler(TestAuthenticationProtocolHandler.class);
        WicketUtil.setUnitTesting(true);
    }

    @Before
    public void setup() {

        // Dummy OLAS Setup.
        TestAuthenticationProtocolHandler.setAuthenticatingUser(getOLASUser());
        TestAuthenticationProtocolHandler.setLogoutServlet(getLogoutServlet());
        DummyAttributeClient.setAttribute(getOLASUser(), "urn:net:lin-k:safe-online:attribute:login", getOLASUser());

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

        // Initialize our dummy web container and set our dummy authentication protocol as the one to use.
        this.wicket = new WicketTester(getApplication());
        MockServletContext wicketContext = (MockServletContext) this.wicket.getServletSession().getServletContext();
        wicketContext.addInitParameter(AuthenticationProtocolManager.LOGOUT_LANDING_PAGE_INIT_PARAM, "");
        wicketContext.addInitParameter(AuthenticationProtocolManager.LANDING_PAGE_INIT_PARAM, "");
        wicketContext.addInitParameter(SafeOnlineLoginUtils.LOGOUT_SERVICE_URL_INIT_PARAM, "");
        wicketContext.addInitParameter(SafeOnlineLoginUtils.TARGET_BASE_URL_INIT_PARAM, "");
        wicketContext.addInitParameter(SafeOnlineLoginUtils.AUTH_SERVICE_URL_INIT_PARAM, "");
        wicketContext.addInitParameter(SafeOnlineLoginUtils.APPLICATION_NAME_INIT_PARAM, getClass().toString());
        wicketContext.addInitParameter(SafeOnlineLoginUtils.AUTHN_PROTOCOL_INIT_PARAM, AuthenticationProtocol.UNIT_TEST
                .name());
        this.wicket.processRequestCycle();
    }

    @After
    public void tearDown() throws Exception {

        this.entityTestManager.tearDown();
    }

    /**
     * @return The index at which the {@link FormComponent} with the given id in the form of the given
     *         {@link FormTester} has a value that matches the given pattern.
     */
    @SuppressWarnings("unchecked")
    protected int findSelectIndex(FormTester form, String id, String pattern) {

        AbstractChoice<?, ?> choiceComponent = (AbstractChoice<?, ?>) form.getForm().get(id);
        List<?> values = choiceComponent.getChoices();

        for (int index = 0; index < values.size(); ++index) {
            Object value = values.get(index);
            if (value == null) {
                if (pattern == null)
                    return index;
            }

            else {
                Locale currentLocale = this.wicket.getServletRequest().getLocale();
                IConverter converter = choiceComponent.getConverter(value.getClass());
                String stringValue = converter.convertToString(value, currentLocale);

                System.err.println("checking " + stringValue);
                if (stringValue.matches(".*" + pattern + ".*"))
                    return index;
            }
        }

        throw new IllegalArgumentException("Component " + id + " in form " + form.getForm().getPath()
                + " has no element that matches " + pattern);
    }

    /**
     * @return The userId that is provided by and for the dummy OLAS services.
     */
    protected abstract String getOLASUser();

    /**
     * @return The application specific logout servlet.
     */
    protected abstract Class<? extends HttpServlet> getLogoutServlet();

    /**
     * @return The wicket application that is being tested.
     */
    protected abstract WebApplication getApplication();

    /**
     * @return All the service beans that are used by the wicket application that is being tested.
     */
    protected abstract Class<?>[] getServiceBeans();

    /**
     * @return All the entity beans that are used by the wicket application that is being tested.
     */
    protected abstract Class<?>[] getEntities();
}
