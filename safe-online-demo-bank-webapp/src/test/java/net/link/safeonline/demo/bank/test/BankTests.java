package net.link.safeonline.demo.bank.test;

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
import net.link.safeonline.demo.bank.webapp.DigipassLoginPage;
import net.link.safeonline.demo.bank.webapp.LoginPage;
import net.link.safeonline.demo.wicket.javaee.DummyJndi;
import net.link.safeonline.demo.wicket.test.WicketTests;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;


public class BankTests extends WicketTests {

    /**
     * {@inheritDoc}
     */
    @Override
    public void setup() {

        super.setup();

        // Perform the Bank Initialization code that normally runs after webapp deployment.
        DummyJndi.lookup(InitializationService.class).buildEntities();
    }

    @Test
    public void testDigipassLogin() {

        // LoginPage: Click to login with digipass.
        this.wicket.processRequestCycle();
        this.wicket.assertRenderedPage(LoginPage.class);

        this.wicket.assertPageLink("digipassLoginLink", DigipassLoginPage.class);
        this.wicket.clickLink("digipassLoginLink");

        // DigipassLoginPage: Log in with our preset digipass user.
        this.wicket.assertRenderedPage(DigipassLoginPage.class);

        FormTester otpForm = this.wicket.newFormTester("otpForm");
        otpForm.setValue("bankId", InitializationService.digipassUser_BankId);
        otpForm.setValue("otp", "0");
        otpForm.submit();

        // AccountPage: We've logged in successfully.
        this.wicket.assertRenderedPage(AccountPage.class);
    }

    @Test
    public void testDigipassLoginFailure() {

        // LoginPage: Click to login with digipass.
        this.wicket.processRequestCycle();
        this.wicket.assertRenderedPage(LoginPage.class);

        this.wicket.assertPageLink("digipassLoginLink", DigipassLoginPage.class);
        this.wicket.clickLink("digipassLoginLink");

        // DigipassLoginPage: Log in with our preset digipass user.
        this.wicket.assertRenderedPage(DigipassLoginPage.class);

        FormTester otpForm = this.wicket.newFormTester("otpForm");
        otpForm.submit();

        // DigipassLoginPage: We didn't make it past this page.
        this.wicket.assertRenderedPage(DigipassLoginPage.class);
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
