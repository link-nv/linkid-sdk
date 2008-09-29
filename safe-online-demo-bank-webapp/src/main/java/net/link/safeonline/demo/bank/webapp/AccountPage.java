package net.link.safeonline.demo.bank.webapp;

import java.util.List;

import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.entity.BankTransactionEntity;
import net.link.safeonline.demo.wicket.tools.WicketUtil;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
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
public class AccountPage extends LayoutPage {

    private static final long serialVersionUID = 1L;


    /**
     * Assign components to the wicket IDs defined in the HTML.
     * 
     * If not logged in, redirects back to the {@link LoginPage}.
     */
    public AccountPage() {

        if (!BankSession.isUserSet()) {
            setResponsePage(LoginPage.class);
            return;
        }

        add(new AccountsForm("accounts"));
    }


    /**
     * <h2>{@link AccountsForm}<br>
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
    class AccountsForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        public AccountsForm(String id) {

            super(id);

            final List<BankAccountEntity> accounts = getUserService().getAccounts(BankSession.get().getUser());

            /* Accounts List. */
            add(new ListView<BankAccountEntity>("accountList", accounts) {

                private static final long serialVersionUID = 1L;

                {
                    setVisible(!accounts.isEmpty());
                }


                @Override
                protected void populateItem(ListItem<BankAccountEntity> accountItem) {

                    final BankAccountEntity account = accountItem.getModelObject();

                    /* Account Details. */
                    accountItem.add(new Label("name", account.getName()));
                    accountItem.add(new Label("amount", WicketUtil.format(getSession(), account.getAmount())));

                    /* Transactions List. */
                    accountItem.add(new ListView<BankTransactionEntity>("transactionList",
                            getTransactionService()
                            .getAllTransactions(account)) {

                        private static final long serialVersionUID = 1L;

                        {
                            setVisible(!accounts.isEmpty());
                        }


                        @Override
                        protected void populateItem(ListItem<BankTransactionEntity> transactionItem) {

                            final BankTransactionEntity transaction = transactionItem.getModelObject();

                            /* Transaction Details. */
                            transactionItem.add(new Label("target", transaction.getTarget()));
                            transactionItem.add(new Label("date", WicketUtil
                                    .format(getSession(), transaction.getDate())));
                            transactionItem.add(new Label("amount", WicketUtil.format(getSession(), transaction
                                    .getAmount())));
                        }
                    });
                }
            });

            add(new Link<String>("newAccount") {

                private static final long serialVersionUID = 1L;


                @Override
                public void onClick() {

                    setResponsePage(NewAccountPage.class);
                }
            });
            add(new Link<String>("newTransaction") {

                private static final long serialVersionUID = 1L;


                @Override
                public void onClick() {

                    setResponsePage(NewTransactionPage.class);
                }
            });
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
}
