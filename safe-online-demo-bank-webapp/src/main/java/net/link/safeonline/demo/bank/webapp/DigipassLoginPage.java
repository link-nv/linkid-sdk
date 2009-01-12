package net.link.safeonline.demo.bank.webapp;

import net.link.safeonline.demo.bank.entity.BankUserEntity;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
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

        add(new OTPForm("otpForm"));
    }


    final class OTPForm extends Form<String> {

        private static final long serialVersionUID = 1L;
        private Model<String>     bankId;
        private Model<String>     otp;


        public OTPForm(String id) {

            super(id);

            add(new TextField<String>("bankId", bankId = new Model<String>()));
            add(new TextField<String>("otp", otp = new Model<String>()));
        }

        @Override
        protected void onSubmit() {

            try {
                if (Integer.parseInt(otp.getObject()) % 2 == 0) {
                    BankUserEntity user = getUserService().getBankUser(bankId.getObject());
                    if (user == null) {
                        error("User was not found.");
                    }

                    else {
                        BankSession.get().setUser(user);
                        throw new RestartResponseException(AccountPage.class);
                    }
                }
            }

            catch (NumberFormatException e) {
                error("The OTP must be a valid number.");
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
    String getPageLinkString() {

        return "Login Portal";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Class<? extends Page> getPageLinkDestination() {

        return LoginPage.class;
    }
}
