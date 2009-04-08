package net.link.safeonline.demo.bank.webapp;

import net.link.safeonline.wicket.web.RequireLogin;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;


/**
 * <h2>{@link NewAccountPage}<br>
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
public class NewAccountPage extends LayoutPage {

    private static final long serialVersionUID = 1L;


    /**
     * Assign components to the wicket IDs defined in the HTML.
     * 
     * If not logged in, redirects back to the {@link LoginPage}.
     */
    public NewAccountPage() {

        add(new AccountForm("newAccount"));
    }


    /**
     * <h2>{@link AccountForm}<br>
     * <sub>New Transaction Form.</sub></h2>
     * 
     * <p>
     * This form is used to specify all the details for the new account.
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

        private Model<String>     name;


        public AccountForm(String id) {

            super(id);

            TextField<String> nameField = new TextField<String>("name", name = new Model<String>());
            nameField.setRequired(true);

            add(nameField);
            focus(nameField);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBeforeRender() {

            super.onBeforeRender();
        }

        @Override
        protected void onSubmit() {

            createAccount(name.getObject());
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHeaderTitle() {

        return "Open Account";
    }

    void createAccount(String name) {

        if (accountService.createAccount(BankSession.get().getUser(), name) != null)
            throw new RestartResponseException(AccountPage.class);
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