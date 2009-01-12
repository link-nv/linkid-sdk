package net.link.safeonline.demo.bank.webapp;

import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.webapp.NewAccountPage.AccountForm;
import net.link.safeonline.wicket.web.RequireLogin;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;


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

        private static final long        serialVersionUID = 1L;

        private Model<String>            description;
        private Model<BankAccountEntity> source;
        private Model<String>            target;
        private Model<String>            amount;


        public TransactionForm(String id) {

            super(id);

            add(new TextArea<String>("description", description = new Model<String>()));
            add(new RadioChoice<BankAccountEntity>("source", source = new Model<BankAccountEntity>(), getUserService().getAccounts(
                    BankSession.get().getUser())));
            add(new TextField<String>("target", target = new Model<String>()));
            add(new TextField<String>("amount", amount = new Model<String>()));
        }

        @Override
        protected void onSubmit() {

            if (getTransactionService().createTransaction(description.getObject(), source.getObject(), target.getObject(),
                    Double.parseDouble(amount.getObject())) != null)
                throw new RestartResponseException(AccountPage.class);
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
