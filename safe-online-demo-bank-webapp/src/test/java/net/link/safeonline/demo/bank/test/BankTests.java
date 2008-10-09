package net.link.safeonline.demo.bank.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import junit.framework.AssertionFailedError;
import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.entity.BankTransactionEntity;
import net.link.safeonline.demo.bank.entity.BankUserEntity;
import net.link.safeonline.demo.bank.service.InitializationService;
import net.link.safeonline.demo.bank.service.bean.AccountServiceBean;
import net.link.safeonline.demo.bank.service.bean.InitializationServiceBean;
import net.link.safeonline.demo.bank.service.bean.TransactionServiceBean;
import net.link.safeonline.demo.bank.service.bean.UserServiceBean;
import net.link.safeonline.demo.bank.webapp.AccountPage;
import net.link.safeonline.demo.bank.webapp.BankApplication;
import net.link.safeonline.demo.bank.webapp.BankSession;
import net.link.safeonline.demo.bank.webapp.DigipassLoginPage;
import net.link.safeonline.demo.bank.webapp.LoginPage;
import net.link.safeonline.demo.bank.webapp.NewAccountPage;
import net.link.safeonline.demo.wicket.javaee.DummyJndi;
import net.link.safeonline.demo.wicket.test.WicketTests;
import net.link.safeonline.demo.wicket.tools.OlasAuthLink;
import net.link.safeonline.sdk.auth.test.TestAuthenticationProtocolHandler;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;


public class BankTests extends WicketTests {

    private static final String TEST_ACCOUNT_NAME = "TestAccount";

    static {
        TestAuthenticationProtocolHandler.setAuthenticatingUser("admin");
    }


    @Override
    public void setup() {

        super.setup();

        // Perform the Bank Initialization code that normally runs after webapp deployment.
        DummyJndi.lookup(InitializationService.class).buildEntities();
    }

    @Test
    public void testDigipassLogin() {

        // LoginPage: Verify.
        this.wicket.assertRenderedPage(LoginPage.class);
        this.wicket.assertPageLink("digipassLoginLink", DigipassLoginPage.class);

        // LoginPage: Click to login with digipass.
        this.wicket.clickLink("digipassLoginLink");

        // DigipassLoginPage: Verify.
        this.wicket.assertRenderedPage(DigipassLoginPage.class);
        this.wicket.assertComponent("otpForm", Form.class);

        // DigipassLoginPage: Log in with our preset digipass user.
        FormTester otpForm = this.wicket.newFormTester("otpForm");
        otpForm.setValue("bankId", InitializationService.digipassUser_BankId);
        otpForm.setValue("otp", "0");
        otpForm.submit();

        // AccountPage: Verify && we've logged in successfully.
        this.wicket.assertRenderedPage(AccountPage.class);
        assertTrue(BankSession.isUserSet());
    }

    @Test
    public void testDigipassLoginFailure() {

        // LoginPage: Verify.
        this.wicket.assertRenderedPage(LoginPage.class);
        this.wicket.assertPageLink("digipassLoginLink", DigipassLoginPage.class);

        // LoginPage: Click to login with digipass.
        this.wicket.clickLink("digipassLoginLink");

        // DigipassLoginPage: Verify.
        this.wicket.assertRenderedPage(DigipassLoginPage.class);
        this.wicket.assertComponent("otpForm", Form.class);

        // DigipassLoginPage: Log in with our preset digipass user.
        FormTester otpForm = this.wicket.newFormTester("otpForm");
        otpForm.submit();

        // DigipassLoginPage: Verify && we didn't make it past this page.
        this.wicket.assertRenderedPage(DigipassLoginPage.class);
        assertFalse(BankSession.isUserSet());
    }

    @Test
    public void testOlasLogin() {

        // LoginPage: Verify.
        this.wicket.processRequestCycle();
        this.wicket.assertRenderedPage(LoginPage.class);
        this.wicket.assertComponent("olasLoginLink", OlasAuthLink.class);

        // LoginPage: Click to login with digipass.
        this.wicket.clickLink("olasLoginLink");

        // AccountPage: Verify && we've logged in successfully.
        this.wicket.assertRenderedPage(AccountPage.class);
        assertTrue(BankSession.isUserSet());
    }

    @Test
    public void testNewAccount() {

        // Login using OLAS.
        testOlasLogin();

        // AccountPage: Verify.
        this.wicket.assertPageLink("newAccount", NewAccountPage.class);

        // AccountPage: Click to create new account.
        this.wicket.clickLink("newAccount");

        // NewAccountPage: Verify.
        this.wicket.assertRenderedPage(NewAccountPage.class);
        this.wicket.assertComponent("newAccount", Form.class);
        this.wicket.assertComponent("newAccount:name", TextField.class);

        // NewAccountPage: Create new account.
        FormTester newAccount = this.wicket.newFormTester("newAccount");
        newAccount.setValue("name", TEST_ACCOUNT_NAME);
        newAccount.submit();

        // AccountPage: Verify && account created successfully.
        this.wicket.assertRenderedPage(AccountPage.class);
        this.wicket.assertComponent("accounts:accountList", ListView.class);

        @SuppressWarnings("unchecked")
        ListView<BankAccountEntity> accountList = (ListView<BankAccountEntity>) this.wicket
                .getComponentFromLastRenderedPage("accounts:accountList");
        for (BankAccountEntity account : accountList.getList())
            if (account.getName().equals(TEST_ACCOUNT_NAME))
                return;

        throw new AssertionFailedError("Account " + TEST_ACCOUNT_NAME + " is not in account list.");
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

        return new Class[] { AccountServiceBean.class, InitializationServiceBean.class, TransactionServiceBean.class,
                UserServiceBean.class };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getEntities() {

        return new Class[] { BankAccountEntity.class, BankTransactionEntity.class, BankUserEntity.class };
    }
}
