package net.link.safeonline.demo.payment.webapp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServlet;

import net.link.safeonline.demo.payment.entity.PaymentEntity;
import net.link.safeonline.demo.payment.entity.PaymentUserEntity;
import net.link.safeonline.demo.payment.service.InitializationService;
import net.link.safeonline.demo.payment.service.bean.InitializationServiceBean;
import net.link.safeonline.demo.payment.service.bean.TransactionServiceBean;
import net.link.safeonline.demo.payment.service.bean.UserServiceBean;
import net.link.safeonline.demo.payment.servlet.LogoutServlet;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.wicket.javaee.DummyJndi;
import net.link.safeonline.wicket.test.AbstractWicketTests;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.tools.olas.DummyAttributeClient;
import net.link.safeonline.wicket.web.OlasLogoutLink;

import org.apache.wicket.AbortException;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;


public class PaymentWebTest extends AbstractWicketTests {

    @Override
    public void setUp()
            throws Exception {

        super.setUp();

        // Perform the Payment Initialization code that normally runs after webapp deployment.
        DummyJndi.lookup(InitializationService.class).buildEntities();
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
                PaymentSession.get().isUserSet());
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
                PaymentSession.get().isUserSet());
        wicket.assertRenderedPage(LoginPage.class);
    }

    /**
     * Log in using OLAS.<br>
     * <br>
     * Create a new transaction with a test visa to a test target for an amount of 1000 (EUR) with a description of "Test Transaction".<br>
     * <br>
     * We end up on the {@link AccountPage}.
     */
    @Test
    public void testNewTransaction() {

        // Test Data.
        String testDescription = "Test Transaction";
        String testVisas[] = { "0987654321", "1234567890" };
        String testVisa = testVisas[1];
        String testTarget = "Test Transaction Target";
        Double testAmount = 1000d;
        String testFormattedAmount = WicketUtil.format(PaymentSession.CURRENCY, testAmount);
        DummyAttributeClient.setAttribute(getOLASUserId(), DemoConstants.DEMO_VISA_ATTRIBUTE_NAME, testVisas);

        // Login using Digipass.
        testOlasLogin();

        // AccountPage: Verify.
        wicket.assertPageLink("account:newTransaction", NewTransactionPage.class);

        // AccountPage: Click to create new account.
        wicket.clickLink("account:newTransaction");

        // NewTransactionPage: Verify.
        wicket.assertRenderedPage(NewTransactionPage.class);
        wicket.assertComponent("newTransaction", Form.class);

        // NewTransactionPage: Create new account.
        FormTester newTransaction = wicket.newFormTester("newTransaction");
        newTransaction.setValue("description", testDescription);
        newTransaction.select("visa", findSelectIndex(newTransaction, "visa", testVisa));
        newTransaction.setValue("target", testTarget);
        newTransaction.setValue("amount", testAmount.toString());
        newTransaction.submit();

        // AccountPage: Verify && account created successfully.
        wicket.assertRenderedPage(AccountPage.class);
        wicket.assertComponent("account:transactionList", ListView.class);

        // - Find the account in the accounts list and the transaction in the account.
        @SuppressWarnings("unchecked")
        ListView<PaymentEntity> transactionList = (ListView<PaymentEntity>) wicket
                                                                                  .getComponentFromLastRenderedPage("account:transactionList");
        assertFalse(transactionList.getList().isEmpty());

        // - Collect sample data.
        PaymentEntity transactionEntity = transactionList.getList().get(0);
        String sampleEntityDescription = transactionEntity.getMessage();
        String sampleEntityVisa = transactionEntity.getVisa();
        String sampleEntityTarget = transactionEntity.getRecipient();
        Double sampleEntityAmount = transactionEntity.getAmount();

        ListItem<PaymentEntity> transaction = transactionList.iterator().next();
        Object sampleTarget = transaction.get("target").getDefaultModelObject();
        Object sampleFormattedAmount = transaction.get("amount").getDefaultModelObject();

        // - Test sample data against our original test data.
        assertTrue(String.format("desc mismatch: test: %s - sample: %s", testDescription, sampleEntityDescription), //
                testDescription.equals(sampleEntityDescription));
        assertTrue(String.format("source mismatch: test: %s - sample: %s", testVisa, sampleEntityVisa), //
                testVisa.equals(sampleEntityVisa));
        assertTrue(String.format("target mismatch: test: %s - sample: %s", testTarget, sampleEntityTarget), //
                testTarget.equals(sampleEntityTarget));
        assertTrue(String.format("amount mismatch: test: %s - sample: %s", testAmount, sampleEntityAmount), //
                testAmount.equals(sampleEntityAmount));

        assertTrue(String.format("target mismatch: test: %s - sample: %s", testTarget, sampleTarget), //
                testTarget.equals(sampleTarget));
        assertTrue(String.format("amount mismatch: test: %s - sample: %s", testFormattedAmount, sampleFormattedAmount), //
                testFormattedAmount.equals(sampleFormattedAmount));
    }

    /**
     * Log in using OLAS.<br>
     * <br>
     * Create a new transaction with a test visa to a test target for an amount of 1000 (EUR) with a description of "Test Transaction".<br>
     * <br>
     * We end up on the {@link AccountPage}.
     */
    @Test
    public void testNewService() {

        // Test Data.
        String testDescription = "Test Transaction Request";
        String testVisas[] = { "1234567890", "0987654321" };
        String testVisa = testVisas[1];
        String testTarget = "Test Transaction Request Target";
        Double testAmount = 1001d;
        String testFormattedAmount = WicketUtil.format(PaymentSession.CURRENCY, testAmount);
        DummyAttributeClient.setAttribute(getOLASUserId(), DemoConstants.DEMO_VISA_ATTRIBUTE_NAME, testVisas);

        // Requesting application redirects to our service page.
        wicket.startPage(NewServicePage.class, new PageParameters(String.format("recipient=%s,amount=%.2f,message=%s,target=%s",
                testTarget, testAmount, testDescription, "")));

        // NewServicePage: Verify.
        wicket.assertRenderedPage(NewServicePage.class);
        wicket.assertComponent("newService", Form.class);
        String sampleServiceTarget = wicket.getComponentFromLastRenderedPage("newService:target").getDefaultModelObjectAsString();
        String sampleServiceFormattedAmount = wicket.getComponentFromLastRenderedPage("newService:amount").getDefaultModelObjectAsString();
        String sampleServiceDescription = wicket.getComponentFromLastRenderedPage("newService:description").getDefaultModelObjectAsString();

        // - Test sample data against our original test data.
        assertTrue(String.format("desc mismatch: test: %s - sample: %s", testDescription, sampleServiceDescription), //
                testDescription.equals(sampleServiceDescription));
        assertTrue(String.format("target mismatch: test: %s - sample: %s", testTarget, sampleServiceTarget), //
                testTarget.equals(sampleServiceTarget));
        assertTrue(String.format("amount mismatch: test: %s - sample: %s", testFormattedAmount, sampleServiceFormattedAmount), //
                testFormattedAmount.equals(sampleServiceFormattedAmount));

        // NewServicePage: Click to authenticate with OLAS.
        FormTester newService = wicket.newFormTester("newService");
        newService.submit();

        // NewTransactionPage: Verify && we've logged in successfully and were redirected to the NewTransactionPage.
        assertTrue("Not logged in.", //
                PaymentSession.get().isUserSet());
        wicket.assertRenderedPage(NewTransactionPage.class);
        wicket.assertComponent("newTransaction", Form.class);
        FormTester newTransaction = wicket.newFormTester("newTransaction");
        String sampleTransactionTarget = newTransaction.getTextComponentValue("target");
        String sampleTransactionAmount = newTransaction.getTextComponentValue("amount");
        String sampleTransactionDescription = newTransaction.getTextComponentValue("description");

        // - Test sample data against our original test data.
        assertTrue(String.format("desc mismatch: test: %s - sample: %s", testDescription, sampleTransactionDescription), //
                testDescription.equals(sampleTransactionDescription));
        assertTrue(String.format("target mismatch: test: %s - sample: %s", testTarget, sampleTransactionTarget), //
                testTarget.equals(sampleTransactionTarget));
        assertTrue(String.format("amount mismatch: test: %f - sample: %f", testAmount, sampleTransactionAmount), //
                testAmount.equals(sampleTransactionAmount));

        // NewTransactionPage: Complete the requested transaction.
        newTransaction.select("visa", findSelectIndex(newTransaction, "visa", testVisa));
        newTransaction.submit();

        // AccountPage: Verify && transaction created successfully.
        wicket.assertRenderedPage(AccountPage.class);
        wicket.assertComponent("account:transactionList", ListView.class);

        // - Find the transaction in the account's transactions list.
        @SuppressWarnings("unchecked")
        ListView<PaymentEntity> transactionList = (ListView<PaymentEntity>) wicket
                                                                                  .getComponentFromLastRenderedPage("account:transactionList");
        assertFalse(transactionList.getList().isEmpty());

        // - Collect sample data.
        PaymentEntity transactionEntity = transactionList.getList().get(0);
        String sampleEntityDescription = transactionEntity.getMessage();
        String sampleEntityVisa = transactionEntity.getVisa();
        String sampleEntityTarget = transactionEntity.getRecipient();
        Double sampleEntityAmount = transactionEntity.getAmount();

        ListItem<PaymentEntity> transaction = transactionList.iterator().next();
        Object sampleTarget = transaction.get("target").getDefaultModelObject();
        Object sampleFormattedAmount = transaction.get("amount").getDefaultModelObject();

        // - Test sample data against our original test data.
        assertTrue(String.format("desc mismatch: test: %s - sample: %s", testDescription, sampleEntityDescription), //
                testDescription.equals(sampleEntityDescription));
        assertTrue(String.format("source mismatch: test: %s - sample: %s", testVisa, sampleEntityVisa), //
                testVisa.equals(sampleEntityVisa));
        assertTrue(String.format("target mismatch: test: %s - sample: %s", testTarget, sampleEntityTarget), //
                testTarget.equals(sampleEntityTarget));
        assertTrue(String.format("amount mismatch: test: %s - sample: %s", testAmount, sampleEntityAmount), //
                testAmount.equals(sampleEntityAmount));

        assertTrue(String.format("target mismatch: test: %s - sample: %s", testTarget, sampleTarget), //
                testTarget.equals(sampleTarget));
        assertTrue(String.format("amount mismatch: test: %s - sample: %s", testFormattedAmount, sampleFormattedAmount), //
                testFormattedAmount.equals(sampleFormattedAmount));
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

        return new PaymentApplication();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getServiceBeans() {

        return new Class[] { InitializationServiceBean.class, TransactionServiceBean.class, UserServiceBean.class };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getEntities() {

        return new Class[] { PaymentEntity.class, PaymentUserEntity.class };
    }
}
