package net.link.safeonline.demo.payment.webapp;

import java.util.Arrays;
import java.util.List;

import net.link.safeonline.demo.payment.entity.PaymentUserEntity;
import net.link.safeonline.demo.payment.keystore.DemoPaymentKeyStoreUtils;
import net.link.safeonline.demo.payment.webapp.AccountPage.AccountForm;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.AttributeUnavailableException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.web.RequireLogin;

import org.apache.wicket.Page;
import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebRequest;


/**
 * <h2>{@link NewTransactionPage}<br>
 * <sub>Wicket backend for creating a new transaction.</sub></h2>
 * 
 * <p>
 * On this page the user can begin a new payment transaction from one of his accounts to another account.
 * </p>
 * 
 * <p>
 * <i>Jun 20, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@RequireLogin(loginPage = LoginPage.class)
public class NewTransactionPage extends LayoutPage {

    private static final long serialVersionUID = 1L;


    /**
     * Assign components to the wicket IDs defined in the HTML.
     * 
     * If not logged in, redirects back to the {@link LoginPage}.
     */
    public NewTransactionPage() {

        add(new TransactionForm("newTransaction"));
    }


    /**
     * <h2>{@link AccountForm}<br>
     * <sub>New Transaction Form.</sub></h2>
     * 
     * <p>
     * This form is used to specify all the details for the new transaction.
     * </p>
     * 
     * <p>
     * <i>Jun 23, 2008</i>
     * </p>
     * 
     * @author mbillemo
     */
    class TransactionForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        private Model<String>     description;
        private Model<String>     visa;
        private Model<String>     target;
        private Model<String>     amount;


        public TransactionForm(String id) {

            super(id);

            try {
                PaymentUserEntity user = PaymentSession.get().getUser();

                AttributeClient attribService = WicketUtil.getOLASAttributeService(((WebRequest) getRequest()).getHttpServletRequest(),
                        DemoPaymentKeyStoreUtils.getPrivateKeyEntry());
                List<String> visas;
                visas = Arrays.asList(attribService.getAttributeValue(user.getOlasId(), DemoConstants.DEMO_VISA_ATTRIBUTE_NAME,
                        String[].class));

                TextArea<String> descriptionField = new TextArea<String>("description", description = new Model<String>());
                RadioChoice<String> visaField = new RadioChoice<String>("visa", visa = new Model<String>(), visas);
                visaField.setRequired(true);
                TextField<String> targetField = new TextField<String>("target", target = new Model<String>());
                targetField.setRequired(true);
                TextField<String> amountField = new TextField<String>("amount", amount = new Model<String>());
                amountField.setRequired(true);

                if (PaymentSession.get().getService() != null) {
                    description.setObject(PaymentSession.get().getService().getMessage());
                    descriptionField.setEnabled(false);
                    target.setObject(PaymentSession.get().getService().getRecipient());
                    targetField.setEnabled(false);
                    amount.setObject(String.valueOf(PaymentSession.get().getService().getAmount()));
                    amountField.setEnabled(false);
                }

                add(descriptionField, visaField, targetField, amountField);
                add(new FeedbackPanel("feedback", IFeedbackMessageFilter.ALL));
            }

            catch (AttributeNotFoundException e) {
                LOG.error("[BUG] VISA attribute not found for user.", e);
            } catch (RequestDeniedException e) {
                LOG.error("VISA attribute access denied.", e);
            } catch (WSClientTransportException e) {
                LOG.error("WS transport problem occurred.", e);
            } catch (AttributeUnavailableException e) {
                LOG.error("Attribute plugin service unavailable.", e);
            }
        }

        @Override
        protected void onSubmit() {

            if (getTransactionService().createTransaction(PaymentSession.get().getUser(), visa.getObject(), description.getObject(),
                    target.getObject(), Double.parseDouble(amount.getObject())) != null) {

                if (PaymentSession.get().getService() != null) {
                    String targetUrl = PaymentSession.get().getService().getTarget();
                    PaymentSession.get().stopService();

                    throw new RedirectToUrlException(targetUrl);
                }

                throw new RestartResponseException(AccountPage.class);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHeaderTitle() {

        return "New Transaction";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Class<? extends Page> getPageLinkDestination() {

        return AccountPage.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    String getPageLinkString() {

        return "Account Overview";
    }
}
