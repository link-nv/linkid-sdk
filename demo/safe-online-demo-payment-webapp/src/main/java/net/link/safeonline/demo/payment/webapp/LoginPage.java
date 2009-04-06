package net.link.safeonline.demo.payment.webapp;

import net.link.safeonline.wicket.web.OlasLoginLink;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;


public class LoginPage extends LayoutPage {

    private static final long serialVersionUID = 1L;


    public LoginPage() {

        add(new OlasLoginLink("olasLoginLink"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBeforeRender() {

        // If logged in, send user to the ticket history page.
        if (PaymentSession.get().isUserSet())
            throw new RestartResponseException(AccountPage.class);

        super.onBeforeRender();
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
