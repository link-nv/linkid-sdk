/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.webapp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServlet;

import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.entity.BankTransactionEntity;
import net.link.safeonline.demo.bank.entity.BankUserEntity;
import net.link.safeonline.demo.bank.service.UserService;
import net.link.safeonline.demo.bank.service.bean.AccountServiceBean;
import net.link.safeonline.demo.bank.service.bean.InitializationServiceBean;
import net.link.safeonline.demo.bank.service.bean.TransactionServiceBean;
import net.link.safeonline.demo.bank.service.bean.UserServiceBean;
import net.link.safeonline.demo.bank.webapp.servlet.LogoutServlet;
import net.link.safeonline.wicket.test.AbstractWicketTests;
import net.link.safeonline.wicket.test.UrlPageSource;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;


/**
 * <h2>{@link AdminPageTest}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jan 16, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public class AdminPageTest extends AbstractWicketTests {

    @Test
    public void testToggleLinkedState()
            throws Exception {

        // Test data.
        String testBankId = "TestBankId";
        String testOlasId = "TestOlasId";
        String testUserName = "TestUserName";

        // Open the admin page.
        wicket.startPage(new UrlPageSource(BankApplication.ADMIN_MOUNTPOINT));

        // AdminPage: Verify && blank search form.
        wicket.assertNoErrorMessage();
        wicket.assertRenderedPage(AdminPage.class);
        wicket.assertComponent("adminForm", Form.class);
        assertTrue(wicket.getComponentFromLastRenderedPage("adminForm:bankId").isEnabled());
        wicket.assertInvisible("adminForm:name");
        wicket.assertInvisible("adminForm:linked");

        // AdminPage: Search for our new user.
        FormTester adminForm = wicket.newFormTester("adminForm");
        adminForm.setValue("bankId", testBankId);
        adminForm.submit();

        // AdminPage: Verify && form with name & create button.
        wicket.assertNoErrorMessage();
        wicket.assertRenderedPage(AdminPage.class);
        wicket.assertComponent("adminForm", Form.class);
        assertFalse(wicket.getComponentFromLastRenderedPage("adminForm:bankId").isEnabled());
        wicket.assertVisible("adminForm:name");
        wicket.assertInvisible("adminForm:linked");

        // AdminPage: Hit create button.
        adminForm = wicket.newFormTester("adminForm");
        adminForm.setValue("name", testUserName);
        adminForm.submit();

        // AdminPage: Verify && blank search form with no errors.
        wicket.assertNoErrorMessage();
        wicket.assertRenderedPage(AdminPage.class);
        assertTrue(wicket.getComponentFromLastRenderedPage("adminForm:bankId").isEnabled());
        wicket.assertInvisible("adminForm:name");
        wicket.assertInvisible("adminForm:linked");

        // AdminPage: Look up our new user.
        adminForm = wicket.newFormTester("adminForm");
        adminForm.setValue("bankId", testBankId);
        adminForm.submit();

        // AdminPage: Verify && form with disabled unchecked linked box & return button.
        wicket.assertNoErrorMessage();
        wicket.assertRenderedPage(AdminPage.class);
        assertFalse(wicket.getComponentFromLastRenderedPage("adminForm:bankId").isEnabled());
        wicket.assertInvisible("adminForm:name");
        wicket.assertVisible("adminForm:linked");
        assertFalse(wicket.getComponentFromLastRenderedPage("adminForm:linked").isEnabled());
        assertFalse((Boolean) wicket.getComponentFromLastRenderedPage("adminForm:linked").getDefaultModelObject());

        // AdminPage: Hit return.
        adminForm = wicket.newFormTester("adminForm");
        adminForm.submit();

        // AdminPage: Verify && blank search form with no errors.
        wicket.assertNoErrorMessage();
        wicket.assertRenderedPage(AdminPage.class);
        assertTrue(wicket.getComponentFromLastRenderedPage("adminForm:bankId").isEnabled());
        wicket.assertInvisible("adminForm:name");
        wicket.assertInvisible("adminForm:linked");

        // Link our new user to an olas account.
        jndiTestUtils.lookup(UserService.class).getBankUser(testBankId).setOlasId(testOlasId);

        // AdminPage: Look up our new user.
        adminForm = wicket.newFormTester("adminForm");
        adminForm.setValue("bankId", testBankId);
        adminForm.submit();

        // AdminPage: Verify && form with enabled checked linked box & apply button.
        wicket.assertNoErrorMessage();
        wicket.assertRenderedPage(AdminPage.class);
        assertFalse(wicket.getComponentFromLastRenderedPage("adminForm:bankId").isEnabled());
        wicket.assertInvisible("adminForm:name");
        wicket.assertVisible("adminForm:linked");
        assertTrue(wicket.getComponentFromLastRenderedPage("adminForm:linked").isEnabled());
        assertTrue((Boolean) wicket.getComponentFromLastRenderedPage("adminForm:linked").getDefaultModelObject());

        // AdminPage: Uncheck linked box & apply.
        adminForm = wicket.newFormTester("adminForm");
        adminForm.setValue("linked", Boolean.toString(false));
        adminForm.submit();

        // AdminPage: Verify && blank search form with no errors.
        wicket.assertNoErrorMessage();
        wicket.assertRenderedPage(AdminPage.class);
        assertTrue(wicket.getComponentFromLastRenderedPage("adminForm:bankId").isEnabled());
        wicket.assertInvisible("adminForm:name");
        wicket.assertInvisible("adminForm:linked");

        // Verify that the user is no longer linked.
        assertNull(jndiTestUtils.lookup(UserService.class).getBankUser(testBankId).getOlasId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<? extends HttpServlet> getLogoutServlet() {

        return LogoutServlet.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected WebApplication getApplication() {

        return new BankApplication();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getServiceBeans() {

        return new Class[] { AccountServiceBean.class, InitializationServiceBean.class, TransactionServiceBean.class, UserServiceBean.class };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getEntities() {

        return new Class[] { BankAccountEntity.class, BankTransactionEntity.class, BankUserEntity.class };
    }
}
