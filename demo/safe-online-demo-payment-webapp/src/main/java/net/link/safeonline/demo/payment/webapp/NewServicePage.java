package net.link.safeonline.demo.payment.webapp;

import net.link.safeonline.demo.payment.webapp.AccountPage.AccountForm;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.web.ForceLogout;
import net.link.safeonline.wicket.web.OlasLoginLink;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;


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
    PaymentService            service;


    /**
     * Assign components to the wicket IDs defined in the HTML.
     * 
     * If not logged in, redirects back to the {@link LoginPage}.
     */
    public NewServicePage(@SuppressWarnings("unused") PageParameters parameters) {

        super(parameters);

        add(new ServiceForm("newService"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBeforeRender() {

        if (getPageParameters().getString("recipient") != null) {
            service = new PaymentService(getPageParameters().getString("recipient"), getPageParameters().getDouble("amount"),
                    getPageParameters().getString("message"), getPageParameters().getString("target"));
        }

        super.onBeforeRender();
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
        private OlasLoginLink     loginLink;


        public ServiceForm(String id) {

            super(id);

            add(new Label("description", new AbstractReadOnlyModel<String>() {

                private static final long serialVersionUID = 1L;


                @Override
                public String getObject() {

                    return service.getMessage();
                }
            }));
            add(new Label("target", new AbstractReadOnlyModel<String>() {

                private static final long serialVersionUID = 1L;


                @Override
                public String getObject() {

                    return service.getRecipient();
                }
            }));
            add(new Label("amount", new AbstractReadOnlyModel<String>() {

                private static final long serialVersionUID = 1L;


                @Override
                public String getObject() {

                    return WicketUtil.format(PaymentSession.CURRENCY, service.getAmount());
                }
            }));

            add(loginLink = new OlasLoginLink("olasLogin", NewTransactionPage.class));
            loginLink.setVisibilityAllowed(false);
        }

        @Override
        protected void onSubmit() {

            PaymentSession.get().startService(service);
            loginLink.onClick();
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
