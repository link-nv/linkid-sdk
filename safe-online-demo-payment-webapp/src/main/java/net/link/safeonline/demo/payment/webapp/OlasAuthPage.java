package net.link.safeonline.demo.payment.webapp;


public class OlasAuthPage extends LayoutPage {

    private static final long serialVersionUID = 1L;


    /**
     * If the user is logged in; continue to the account overview page.
     * 
     * If not, show a link to the OLAS authentication service for logging the user in.
     */
    public OlasAuthPage() {

        // If logged in, let the LoginPage handle where to go.
        if (WicketUtil.isOlasAuthenticated(getRequest()))
            throw new RestartResponseException(LoginPage.class);

        // HTML Components.
        add(new InlineFrame("olasFrame", PageMap.forName("olasFrame"), OlasAuthRedirectPage.class));
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
