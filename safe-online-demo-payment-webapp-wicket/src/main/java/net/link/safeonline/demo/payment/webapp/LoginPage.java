package net.link.safeonline.demo.payment.webapp;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.link.PageLink;


public class LoginPage extends LayoutPage {

    private static final long serialVersionUID = 1L;


    /**
     * If the user is logged in; continue to the account overview page.
     * 
     * If not, show a link to the OLAS authentication service for logging the user in.
     */
    public LoginPage() {

        // If logged in, send user to the ticket history page.
        if (PaymentSession.get().isUserSet())
            throw new RestartResponseException(AccountPage.class);

        // HTML Components.
        add(new PageLink("olasLoginLink", OlasAuthPage.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHeaderTitle() {

        return "Login Page";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    String getPageLinkString() {

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Class<? extends Page> getPageLinkDestination() {

        return LoginPage.class;
    }
}
