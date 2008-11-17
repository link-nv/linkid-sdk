package net.link.safeonline.demo.bank.webapp;

import net.link.safeonline.demo.wicket.tools.WicketUtil;
import net.link.safeonline.demo.wicket.web.OlasAuthRedirectPage;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.link.InlineFrame;


public class OlasAuthPage extends LayoutPage {

    private static final long serialVersionUID = 1L;


    /**
     * If the user is logged in; continue to the account overview page.
     * 
     * If not, show a link to the OLAS authentication service for logging the user in.
     */
    public OlasAuthPage() {

        // If logged in, let the LoginPage handle where to go.
        if (WicketUtil.isAuthenticated(getRequest()))
            throw new RestartResponseException(LoginPage.class);

        // HTML Components.
        add(new InlineFrame("olasFrame", null, OlasAuthRedirectPage.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHeaderTitle() {

        return "OLAS Authentication";
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
