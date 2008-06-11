package net.link.safeonline.demo.cinema.webapp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;

public class LoginPage extends Layout {

    private static final long serialVersionUID = 1L;


    public LoginPage() {

        // If logged in, redirect to HomePage.
        if (LoginManager.isAuthenticated(((WebRequest) getRequest())
                .getHttpServletRequest())) {
            setResponsePage(HomePage.class);
            return;
        }

        add(new Label<String>("headerTitle", "Login Page"));
        add(new Link<Object>("loginlink") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                getRequestCycle().setRequestTarget(new IRequestTarget() {

                    public void detach(RequestCycle requestCycle) {

                    }

                    public void respond(RequestCycle requestCycle) {

                        HttpServletRequest request = ((WebRequest) requestCycle
                                .getRequest()).getHttpServletRequest();
                        HttpServletResponse response = ((WebResponse) requestCycle
                                .getResponse()).getHttpServletResponse();
                        String target = request.getRequestURL().toString();

                        SafeOnlineLoginUtils.login(target, request, response);
                    }

                });
            }
        });
    }
}
