package net.link.safeonline.demo.cinema.webapp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;

public class LoginPage extends Layout {

    private static final long serialVersionUID = 1L;

    private static final Log  LOG              = LogFactory
                                                       .getLog(LoginPage.class);


    public LoginPage() {

        HttpServletRequest request = getWebRequestCycle().getWebRequest()
                .getHttpServletRequest();
        HttpServletResponse response = getWebRequestCycle().getWebResponse()
                .getHttpServletResponse();

        String target = request.getRequestURL().toString();
        SafeOnlineLoginUtils.login(target, request, response);

        add(new Label("headerTitle", "Login Page"));
        add(new ExternalLink("loginlink", "#"));
    }

}
