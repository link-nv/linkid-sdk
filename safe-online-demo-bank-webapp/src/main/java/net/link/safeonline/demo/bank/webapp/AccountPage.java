package net.link.safeonline.demo.bank.webapp;

import java.util.List;

import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.entity.BankTransactionEntity;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.web.RequireLogin;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
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

            final List<BankAccountEntity> accounts = userService.getAccounts(BankSession.get().getUser());

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
                    accountItem.add(new Label("amount", WicketUtil.format(BankSession.CURRENCY, account.getAmount())));

                    /* Transactions List. */
                    accountItem.add(new ListView<BankTransactionEntity>("transactionList", transactionService.getAllTransactions(account)) {

                        private static final long serialVersionUID = 1L;

                        {
                            setVisible(!accounts.isEmpty());
                        }


                        @Override
                        protected void populateItem(ListItem<BankTransactionEntity> transactionItem) {

                            final BankTransactionEntity transaction = transactionItem.getModelObject();

                            /* Transaction Details. */
                            transactionItem.add(new Label("target", transaction.getTarget()));
                            transactionItem.add(new Label("date", WicketUtil.format(getLocale(), transaction.getDate())));
                            transactionItem.add(new Label("amount", WicketUtil.format(BankSession.CURRENCY, transaction.getAmount())));
                        }
                    });
                }
            });

            add(new PageLink<NewAccountPage>("newAccount", NewAccountPage.class));
            add(new PageLink<NewTransactionPage>("newTransaction", NewTransactionPage.class));
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
    Link<?> getPageLink() {

        return new PageLink<OlasAuthPage>("pageLink", OlasAuthPage.class) {

            private static final long serialVersionUID = 1L;

            {
                setVisible(BankSession.get().getUser().getOlasId() == null);
            }


            @Override
            public void onClick() {

                BankSession.get().setLinkingUser(BankSession.get().getUser());

                super.onClick();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    String getPageLinkString() {

        return "Link To OLAS";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Class<? extends Page> getPageLinkDestination() {

        throw new IllegalStateException("unused");
    }
}
