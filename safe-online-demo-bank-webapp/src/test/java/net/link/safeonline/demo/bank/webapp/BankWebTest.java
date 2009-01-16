package net.link.safeonline.demo.bank.webapp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServlet;

import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.entity.BankTransactionEntity;
import net.link.safeonline.demo.bank.entity.BankUserEntity;
import net.link.safeonline.demo.bank.service.InitializationService;
import net.link.safeonline.demo.bank.service.bean.AccountServiceBean;
import net.link.safeonline.demo.bank.service.bean.InitializationServiceBean;
import net.link.safeonline.demo.bank.service.bean.TransactionServiceBean;
import net.link.safeonline.demo.bank.service.bean.UserServiceBean;
import net.link.safeonline.demo.bank.webapp.servlet.LogoutServlet;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.wicket.javaee.DummyJndi;
import net.link.safeonline.wicket.test.AbstractWicketTests;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.web.OlasLogoutLink;

import org.apache.wicket.AbortException;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;


public class BankWebTest extends AbstractWicketTests {

    @Override
    public void setUp()
            throws Exception {

        super.setUp();

        // Perform the Bank Initialization code that normally runs after webapp deployment.
        DummyJndi.lookup(InitializationService.class).buildEntities();
    }

    /**
     * Log the user in using his digipass (see {@link InitializationService#digipassUser_BankId}).<br>
     * <br>
     * We end up on the {@link AccountPage}.
     */
    @Test
    public void testDigipassLogin() {

        // LoginPage: Verify.
        wicket.assertRenderedPage(LoginPage.class);
        wicket.assertPageLink("digipassLoginLink", DigipassLoginPage.class);

        // LoginPage: Click to login with digipass.
        wicket.clickLink("digipassLoginLink");

        // DigipassLoginPage: Verify.
        wicket.assertRenderedPage(DigipassLoginPage.class);
        wicket.assertComponent("otpForm", Form.class);

        // DigipassLoginPage: Log in with our preset digipass user.
        FormTester otpForm = wicket.newFormTester("otpForm");
        otpForm.setValue("bankId", InitializationService.digipassUser_BankId);
        otpForm.setValue("otp", "0");
        otpForm.submit();

        // AccountPage: Verify && we've logged in successfully.
        assertTrue("Not logged in.", //
                BankSession.get().isUserSet());
        wicket.assertRenderedPage(AccountPage.class);
    }

    /**
     * Attempt an invalid digipass login.<br>
     * <br>
     * We end up on the {@link DigipassLoginPage} without being logged in.
     */
    @Test
    public void testDigipassLoginFailure() {

        // LoginPage: Verify.
        wicket.assertRenderedPage(LoginPage.class);
        wicket.assertPageLink("digipassLoginLink", DigipassLoginPage.class);

        // LoginPage: Click to login with digipass.
        wicket.clickLink("digipassLoginLink");

        // DigipassLoginPage: Verify.
        wicket.assertRenderedPage(DigipassLoginPage.class);
        wicket.assertComponent("otpForm", Form.class);

        // DigipassLoginPage: Log in with our preset digipass user.
        FormTester otpForm = wicket.newFormTester("otpForm");
        otpForm.submit();

        // DigipassLoginPage: Verify && we didn't make it past this page.
        assertFalse("Shouldn't be logged in.", //
                BankSession.get().isUserSet());
        wicket.assertRenderedPage(DigipassLoginPage.class);
    }

    /**
     * Log the user in using (dummy) OLAS services.<br>
     * <br>
     * We end up on the {@link AccountPage}.
     */
    @Test
    public void testOlasLogin() {

        // LoginPage: Verify.
        wicket.processRequestCycle();
        wicket.assertRenderedPage(LoginPage.class);
        wicket.assertPageLink("olasLoginLink", OlasAuthPage.class);

        // LoginPage: Click to login with digipass.
        wicket.clickLink("olasLoginLink");

        wicket.assertComponent("olasFrame", InlineFrame.class);
        InlineFrame olasFrame = (InlineFrame) wicket.getLastRenderedPage().get("olasFrame");
        try {
            olasFrame.onLinkClicked();
        } catch (AbortException e) {
            RequestCycle.get().request(RequestCycle.get().getRequestTarget());
            wicket.processRequestCycle();
        }

        // AccountPage: Verify && we've logged in successfully.
        assertTrue("Not logged in.", //
                BankSession.get().isUserSet());
        wicket.assertRenderedPage(AccountPage.class);
    }

    /**
     * Log the user in using (dummy) OLAS services.<br>
     * <br>
     * Log the user out.<br>
     * <br>
     * Log the user in using digipass.<br>
     * <br>
     * Log the user out.<br>
     * <br>
     * We end up on the {@link LoginPage}.
     */
    @Test
    public void testLogout() {

        // Login using OLAS.
        testOlasLogin();

        // AccountPage: Verify.
        wicket.assertComponent("user:logout", OlasLogoutLink.class);

        // AccountPage: Log out.
        wicket.clickLink("user:logout");

        // LoginPage: Verify && OLAS user logged out successfully.
        assertFalse("OLAS credentials shouldn't be present.", //
                LoginManager.isAuthenticated(wicket.getServletRequest()));
        assertFalse("Shouldn't be logged in.", //
                BankSession.get().isUserSet());
        wicket.assertRenderedPage(LoginPage.class);

        // Login using Digipass.
        testDigipassLogin();

        // AccountPage: Verify.
        wicket.assertComponent("user:logout", OlasLogoutLink.class);

        // AccountPage: Log out.
        wicket.clickLink("user:logout");

        // LoginPage: Verify && Digipass user logged out successfully.
        assertFalse("Shouldn't be logged in.", //
                BankSession.get().isUserSet());
        wicket.assertRenderedPage(LoginPage.class);
    }

    /**
     * Log in using OLAS.<br>
     * <br>
     * Create a new account with the name "Test Account".<br>
     * <br>
     * We end up on the {@link AccountPage}.
     */
    @Test
    public void testNewAccount() {

        // Test data.
        String testAccountName = "Test Account";

        // Login using OLAS.
        testOlasLogin();

        // AccountPage: Verify.
        wicket.assertPageLink("accounts:newAccount", NewAccountPage.class);

        // AccountPage: Click to create new account.
        wicket.clickLink("accounts:newAccount");

        // NewAccountPage: Verify.
        wicket.assertRenderedPage(NewAccountPage.class);
        wicket.assertComponent("newAccount", Form.class);

        // NewAccountPage: Create new account.
        FormTester newAccount = wicket.newFormTester("newAccount");
        newAccount.setValue("name", testAccountName);
        newAccount.submit();

        // AccountPage: Verify && account created successfully.
        wicket.assertRenderedPage(AccountPage.class);
        wicket.assertComponent("accounts:accountList", ListView.class);

        // - Collect sample data.
        List<String> sampleAccountNames = new LinkedList<String>();
        @SuppressWarnings("unchecked")
        ListView<BankAccountEntity> accountList = (ListView<BankAccountEntity>) wicket
                                                                                      .getComponentFromLastRenderedPage("accounts:accountList");
        for (BankAccountEntity account : accountList.getList()) {
            sampleAccountNames.add(account.getName());
        }

        // - Test sample data against our original test data.
        assertTrue(String.format("account not found: test: %s - sample: %s", testAccountName, sampleAccountNames), //
                sampleAccountNames.contains(testAccountName));
    }

    /**
     * Log in using digipass.<br>
     * <br>
     * Create a new transaction from the last account (see {@link InitializationService#digipassUser_AccountCodes}) to the first with an
     * amount of 1000 (EUR) with a description of "Test Transaction".<br>
     * <br>
     * We end up on the {@link AccountPage}.
     */
    @Test
    public void testNewTransaction() {

        // Test Data.
        String testDescription = "Test Transaction";
        int testSourceIndex = 0, testTargetIndex = InitializationService.digipassUser_AccountCodes.length - 1;
        String testSourceCode = InitializationService.digipassUser_AccountCodes[testSourceIndex];
        String testTargetCode = InitializationService.digipassUser_AccountCodes[testTargetIndex];
        Double testAmount = 1000d;

        // Login using Digipass.
        testDigipassLogin();

        // AccountPage: Verify.
        wicket.assertPageLink("accounts:newTransaction", NewTransactionPage.class);

        // AccountPage: Click to create new account.
        wicket.clickLink("accounts:newTransaction");

        // NewTransactionPage: Verify.
        wicket.assertRenderedPage(NewTransactionPage.class);
        wicket.assertComponent("newTransaction", Form.class);

        // NewTransactionPage: Create new account.
        FormTester newTransaction = wicket.newFormTester("newTransaction");
        newTransaction.setValue("description", testDescription);
        newTransaction.select("source", findSelectIndex(newTransaction, "source", testSourceCode));
        newTransaction.setValue("target", testTargetCode);
        newTransaction.setValue("amount", testAmount.toString());
        newTransaction.submit();

        // AccountPage: Verify && account created successfully.
        wicket.assertRenderedPage(AccountPage.class);
        wicket.assertComponent("accounts:accountList", ListView.class);

        // - Find the account in the accounts list and the transaction in the account.
        @SuppressWarnings("unchecked")
        ListView<BankAccountEntity> accountList = (ListView<BankAccountEntity>) wicket
                                                                                      .getComponentFromLastRenderedPage("accounts:accountList");
        ListItem<BankAccountEntity> firstAccount = accountList.iterator().next();
        @SuppressWarnings("unchecked")
        ListView<BankTransactionEntity> transactionList = (ListView<BankTransactionEntity>) firstAccount.get("transactionList");
        assertFalse(transactionList.getList().isEmpty());

        // - Collect sample data.
        BankTransactionEntity transactionEntity = transactionList.getList().get(0);
        String sampleEntityDescription = transactionEntity.getDescription();
        String sampleEntitySourceCode = transactionEntity.getSource().getCode();
        String sampleEntityTargetCode = transactionEntity.getTarget();
        Double sampleEntityAmount = transactionEntity.getAmount();

        ListItem<BankTransactionEntity> transaction = transactionList.iterator().next();
        Object sampleTargetCode = transaction.get("target").getDefaultModelObject();
        Object sampleFormattedAmount = transaction.get("amount").getDefaultModelObject();
        Object testFormattedAmount = WicketUtil.format(BankSession.CURRENCY, testAmount);

        // - Test sample data against our original test data.
        assertTrue(String.format("desc mismatch: test: %s - sample: %s", testDescription, sampleEntityDescription), //
                testDescription.equals(sampleEntityDescription));
        assertTrue(String.format("source mismatch: test: %s - sample: %s", testSourceCode, sampleEntitySourceCode), //
                testSourceCode.equals(sampleEntitySourceCode));
        assertTrue(String.format("target mismatch: test: %s - sample: %s", testTargetCode, sampleEntityTargetCode), //
                testTargetCode.equals(sampleEntityTargetCode));
        assertTrue(String.format("amount mismatch: test: %s - sample: %s", testAmount, sampleEntityAmount), //
                testAmount.equals(sampleEntityAmount));

        assertTrue(String.format("target mismatch: test: %s - sample: %s", testTargetCode, sampleTargetCode), //
                testTargetCode.equals(sampleTargetCode));
        assertTrue(String.format("amount mismatch: test: %s - sample: %s", testFormattedAmount, sampleFormattedAmount), //
                testFormattedAmount.equals(sampleFormattedAmount));
    }

    /**
     * Log in using digipass.<br>
     * <br>
     * Link the account to our OLAS user.<br>
     * <br>
     * Log out.<br>
     * <br>
     * Log in using OLAS && verify same account.<br>
     * <br>
     * Log out.<br>
     * <br>
     * Log in using Digipass && verify same account.<br>
     * <br>
     * Log out.<br>
     */
    @Test
    public void testLinking() {

        // Test Data.
        List<String> testAccountCodes = Arrays.asList(InitializationService.digipassUser_AccountCodes);

        // Login using Digipass.
        testDigipassLogin();

        // AccountPage: Verify.
        wicket.assertPageLink("user:pageLink", OlasAuthPage.class);

        // AccountPage: Click to link our account to OLAS.
        wicket.clickLink("user:pageLink");

        wicket.assertComponent("olasFrame", InlineFrame.class);
        InlineFrame olasFrame = (InlineFrame) wicket.getLastRenderedPage().get("olasFrame");
        try {
            olasFrame.onLinkClicked();
        } catch (AbortException e) {
            RequestCycle.get().request(RequestCycle.get().getRequestTarget());
            wicket.processRequestCycle();
        }

        // AccountPage: Verify && old accounts still there.
        wicket.assertRenderedPage(AccountPage.class);

        // - Collect sample data.
        List<String> sampleAccountCodes = new LinkedList<String>();
        @SuppressWarnings("unchecked")
        ListView<BankAccountEntity> accountOrigList = (ListView<BankAccountEntity>) wicket
                                                                                          .getComponentFromLastRenderedPage("accounts:accountList");
        for (BankAccountEntity account : accountOrigList.getList()) {
            sampleAccountCodes.add(account.getCode());
        }

        // - Test sample data against our original test data.
        assertTrue(String.format("accounts not found: test: %s - sample: %s", testAccountCodes, sampleAccountCodes), //
                testAccountCodes.size() == sampleAccountCodes.size() && testAccountCodes.containsAll(sampleAccountCodes));

        // AccountPage: Click to log out.
        wicket.clickLink("user:logout");

        // LoginPage: Verify && logout successful.
        assertFalse("Shouldn't be logged in.", //
                BankSession.get().isUserSet());
        wicket.assertRenderedPage(LoginPage.class);

        // Login using Digipass.
        testDigipassLogin();

        // AccountPage: Verify && digipass user's accounts still there.
        wicket.assertRenderedPage(AccountPage.class);

        // - Collect sample data.
        sampleAccountCodes = new LinkedList<String>();
        @SuppressWarnings("unchecked")
        ListView<BankAccountEntity> accountDigipassList = (ListView<BankAccountEntity>) wicket
                                                                                              .getComponentFromLastRenderedPage("accounts:accountList");
        for (BankAccountEntity account : accountDigipassList.getList()) {
            sampleAccountCodes.add(account.getCode());
        }

        // - Test sample data against our original test data.
        assertTrue(String.format("accounts not found: test: %s - sample: %s", testAccountCodes, sampleAccountCodes), //
                testAccountCodes.size() == sampleAccountCodes.size() && testAccountCodes.containsAll(sampleAccountCodes));

        // Login using OLAS.
        testOlasLogin();

        // AccountPage: Verify && digipass user's accounts still there.
        wicket.assertRenderedPage(AccountPage.class);

        // - Collect sample data.
        sampleAccountCodes = new LinkedList<String>();
        @SuppressWarnings("unchecked")
        ListView<BankAccountEntity> accountOLASList = (ListView<BankAccountEntity>) wicket
                                                                                          .getComponentFromLastRenderedPage("accounts:accountList");
        for (BankAccountEntity account : accountOLASList.getList()) {
            sampleAccountCodes.add(account.getCode());
        }

        // - Test sample data against our original test data.
        assertTrue(String.format("accounts not found: test: %s - sample: %s", testAccountCodes, sampleAccountCodes), //
                testAccountCodes.size() == sampleAccountCodes.size() && testAccountCodes.containsAll(sampleAccountCodes));

        // AccountPage: Click to log out.
        wicket.clickLink("user:logout");

        // LoginPage: Verify && logout successful.
        assertFalse("Shouldn't be logged in.", //
                BankSession.get().isUserSet());
        wicket.assertRenderedPage(LoginPage.class);

        // Login using Digipass.
        testDigipassLogin();

        // AccountPage: Verify && digipass user's accounts still there.
        wicket.assertRenderedPage(AccountPage.class);

        // - Collect sample data.
        sampleAccountCodes = new LinkedList<String>();
        @SuppressWarnings("unchecked")
        ListView<BankAccountEntity> accountDigipassList = (ListView<BankAccountEntity>) wicket
                                                                                              .getComponentFromLastRenderedPage("accounts:accountList");
        for (BankAccountEntity account : accountDigipassList.getList()) {
            sampleAccountCodes.add(account.getCode());
        }

        // - Test sample data against our original test data.
        assertTrue(String.format("accounts not found: test: %s - sample: %s", testAccountCodes, sampleAccountCodes), //
                testAccountCodes.size() == sampleAccountCodes.size() && testAccountCodes.containsAll(sampleAccountCodes));

        // AccountPage: Click to log out.
        wicket.clickLink("user:logout");

        // LoginPage: Verify && logout successful.
        assertFalse("Shouldn't be logged in.", //
                BankSession.get().isUserSet());
        wicket.assertRenderedPage(LoginPage.class);
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
