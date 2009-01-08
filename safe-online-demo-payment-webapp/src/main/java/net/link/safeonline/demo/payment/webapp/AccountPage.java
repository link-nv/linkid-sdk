package net.link.safeonline.demo.payment.webapp;

import net.link.safeonline.demo.payment.entity.PaymentEntity;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.web.RequireLogin;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;


/**
 * <h2>{@link AccountPage}<br>
 * <sub>Wicket backend for account overview page.</sub></h2>
 * 
 * <p>
 * On this page the user sees an overview of all his accounts and recent transactions made on them.
 * </p>
 * 
 * <p>
 * <i>Jun 20, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@RequireLogin(loginPage = LoginPage.class)
public class AccountPage extends LayoutPage {

    private static final long serialVersionUID = 1L;


    /**
     * Assign components to the wicket IDs defined in the HTML.
     * 
     * If not logged in, redirects back to the {@link LoginPage}.
     */
    public AccountPage() {

        add(new AccountForm("account"));
    }


    /**
     * <h2>{@link AccountForm}<br>
     * <sub>Account Overview Form.</sub></h2>
     * 
     * <p>
     * This form shows some information on each user account.
     * 
     * A link lets the user begin a new transaction.
     * </p>
     * 
     * <p>
     * <i>Jun 23, 2008</i>
     * </p>
     * 
     * @author mbillemo
     */
    class AccountForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        public AccountForm(String id) {

            super(id);

            /* Transactions List. */
            add(new ListView<PaymentEntity>("transactionList", getTransactionService().getAllTransactions(PaymentSession.get().getUser())) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void populateItem(ListItem<PaymentEntity> transactionItem) {

                    final PaymentEntity transaction = transactionItem.getModelObject();

                    /* Transaction Details. */
                    transactionItem.add(new Label("target", transaction.getRecipient()));
                    transactionItem.add(new Label("date", WicketUtil.format(getLocale(), transaction.getPaymentDate())));
                    transactionItem.add(new Label("amount", WicketUtil.format(PaymentSession.CURRENCY, transaction.getAmount())));
                }
            });

            add(new PageLink("newTransaction", NewTransactionPage.class));
        }

        @Override
        protected void onSubmit() {

        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHeaderTitle() {

        return "Account Overview";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    String getPageLinkString() {

        return null;
    }
}
