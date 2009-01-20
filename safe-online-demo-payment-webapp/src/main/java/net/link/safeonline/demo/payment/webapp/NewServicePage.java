package net.link.safeonline.demo.payment.webapp;

import net.link.safeonline.demo.payment.webapp.AccountPage.AccountForm;


/**
 * <h2>{@link NewServicePage}<br>
 * <sub>Wicket backend for servicing a new transaction request from a remote application.</sub></h2>
 * 
 * <p>
 * On this page the details submitted by the remote application for a new transaction are shown.
 * </p>
 * 
 * <p>
 * The user can log in to complete the transaction.
 * </p>
 * 
 * <p>
 * <i>Jun 20, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@ForceLogout
public class NewServicePage extends LayoutPage {

    private static final long serialVersionUID = 1L;


    /**
     * Assign components to the wicket IDs defined in the HTML.
     * 
     * If not logged in, redirects back to the {@link LoginPage}.
     */
    public NewServicePage(PageParameters parameters) {

        PaymentSession.get().startService(
                new PaymentService(parameters.getString("recipient"), parameters.getDouble("amount"), parameters.getString("message"),
                        parameters.getString("target")));

        add(new ServiceForm("newService"));
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
    class ServiceForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        public ServiceForm(String id) {

            super(id);

            add(new Label("description", PaymentSession.get().getService().getMessage()));
            add(new Label("target", PaymentSession.get().getService().getRecipient()));
            add(new Label("amount", WicketUtil.format(PaymentSession.CURRENCY, PaymentSession.get().getService().getAmount())));
        }

        @Override
        protected void onSubmit() {

            new OlasLoginLink("olasLogin", NewTransactionPage.class).onClick();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHeaderTitle() {

        return "New Transaction Request";
    }

    @Override
    Link<?> getPageLink() {

        return new Link<String>("pageLink") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                throw new RedirectToUrlException(PaymentSession.get().getService().getTarget());
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    String getPageLinkString() {

        return "Abort";
    }
}
