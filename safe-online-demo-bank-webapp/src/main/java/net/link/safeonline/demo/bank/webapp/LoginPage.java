package net.link.safeonline.demo.bank.webapp;

import javax.servlet.ServletException;

import net.link.safeonline.demo.bank.entity.BankUserEntity;
import net.link.safeonline.wicket.tools.WicketUtil;

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

        // If logged in using OLAS, create/obtain the bank user from the OLAS user.
        if (WicketUtil.isAuthenticated(getRequest())) {
            try {
                BankUserEntity user = BankSession.get().getUser();
                if (BankSession.isLinking()) {
                    BankSession.get().setLinkingUser(null);
                    user = getUserService().linkOLASUser(user, WicketUtil.getUserId(getRequest()));
                } else {
                    user = getUserService().getOLASUser(WicketUtil.getUserId(getRequest()));
                }

                user = getUserService().updateUser(user, WicketUtil.toServletRequest(getRequest()));
                BankSession.get().setUser(user);
            }

            catch (ServletException e) {
                this.LOG.error("[BUG] Not really logged in?!", e);
            }
        }

        // If logged in, send user to the ticket history page.
        if (BankSession.isUserSet())
            throw new RestartResponseException(AccountPage.class);

        // HTML Components.
        add(new PageLink("olasLoginLink", OlasAuthPage.class));
        add(new PageLink("digipassLoginLink", DigipassLoginPage.class));
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
