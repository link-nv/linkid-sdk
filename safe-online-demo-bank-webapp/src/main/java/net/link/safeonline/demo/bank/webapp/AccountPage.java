package net.link.safeonline.demo.bank.webapp;

import java.util.List;

import net.link.safeonline.demo.bank.entity.AccountEntity;
import net.link.safeonline.demo.bank.entity.TransactionEntity;
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

        add(new Label<String>("headerTitle", "Ticket History"));

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

            final List<AccountEntity> accounts = AccountPage.this.userService.getAccounts(BankSession.get().getUser());

            /* Accounts List. */
            add(new ListView<AccountEntity>("accountList", accounts) {

                private static final long serialVersionUID = 1L;

                {
                    setVisible(!accounts.isEmpty());
                }


                @Override
                protected void populateItem(ListItem<AccountEntity> accountItem) {

                    final AccountEntity account = accountItem.getModelObject();

                    /* Account Details. */
                    accountItem.add(new Label<String>("name", account.getName()));
                    accountItem.add(new Label<String>("amount", WicketUtil.formatCurrency(getSession(), account
                            .getAmount())));

                    /* Transactions List. */
                    accountItem.add(new ListView<TransactionEntity>("transactionList",
                            AccountPage.this.transactionService.getAllTransactions(account)) {

                        private static final long serialVersionUID = 1L;

                        {
                            setVisible(!accounts.isEmpty());
                        }


                        @Override
                        protected void populateItem(ListItem<TransactionEntity> transactionItem) {

                            final TransactionEntity transaction = transactionItem.getModelObject();

                            /* Transaction Details. */
                            transactionItem.add(new Label<String>("target", transaction.getTarget()));
                            transactionItem.add(new Label<String>("date", WicketUtil.formatDate(getSession(),
                                    transaction.getDate())));
                            transactionItem.add(new Label<String>("amount", WicketUtil.formatCurrency(getSession(),
                                    transaction.getAmount())));
                        }
                    });
                }
            });

            add(new Link<String>("new") {

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
}
