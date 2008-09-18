package net.link.safeonline.demo.bank.webapp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.demo.bank.entity.UserEntity;
import net.link.safeonline.demo.wicket.tools.WicketUtil;
import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;


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
                UserEntity user = this.userService.getOLASUser(WicketUtil.getUsername(getRequest()));
                user = this.userService.updateUser(user, WicketUtil.toServletRequest(getRequest()));
                BankSession.get().setUser(user);
            }

            catch (ServletException e) {
                this.LOG.error("[BUG] Not really logged in?!", e);
            }
        }
        
        // If logged in, send user to the ticket history page.
        if (BankSession.get().getUser() != null) {
            setResponsePage(AccountPage.class);
            return;
        }

        // HTML Components.
        add(new Label<String>("headerTitle", "Login Page"));
        add(new Link<Object>("loginlink") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                getRequestCycle().setRequestTarget(new IRequestTarget() {

                    public void detach(RequestCycle requestCycle) {

                    }

                    public void respond(RequestCycle requestCycle) {

                        HttpServletRequest request = ((WebRequest) requestCycle.getRequest()).getHttpServletRequest();
                        HttpServletResponse response = ((WebResponse) requestCycle.getResponse())
                                .getHttpServletResponse();
                        String target = request.getServletPath();

                        SafeOnlineLoginUtils.login(target, request, response);
                    }

                });
            }
        });
    }
}
