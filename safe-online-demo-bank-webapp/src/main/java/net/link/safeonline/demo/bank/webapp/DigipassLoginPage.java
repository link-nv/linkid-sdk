package net.link.safeonline.demo.bank.webapp;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;


public class DigipassLoginPage extends LayoutPage {

    private static final long serialVersionUID = 1L;


    /**
     * If the user is logged in; continue to the account overview page.
     * 
     * If not, let the user authenticate himself using his digipass device.
     */
    public DigipassLoginPage() {

        // If logged in, send user to the ticket history page.
        if (BankSession.isUserSet()) {
            setResponsePage(AccountPage.class);
            return;
        }
        
        add(new OTPForm("otpForm"));
    }

    final class OTPForm extends Form<String> {

        private static final long serialVersionUID = 1L;
        private Model<String>     bankId;
        private Model<String>     otp;

        
        public OTPForm(String id) {

            super(id);
            
            add(new TextField<String>("bankId", this.bankId = new Model<String>()));
            add(new TextField<String>("otp", this.otp = new Model<String>()));
        }

        @Override
        protected void onSubmit() {

            if (Integer.parseInt(this.otp.getObject()) % 2 == 0) {
                BankSession.get().setUser(getUserService().getBankUser(this.bankId.getObject()));
                setResponsePage(AccountPage.class);
                setRedirect(true);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHeaderTitle() {

        return "Digipass Authentication";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Class<? extends Page> getPageLinkDestination() {

        return LoginPage.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    String getPageLinkString() {

        return "Login Methods";
    }
}
