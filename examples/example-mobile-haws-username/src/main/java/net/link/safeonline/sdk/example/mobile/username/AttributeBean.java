package net.link.safeonline.sdk.example.mobile.username;

import java.util.Map;
import javax.servlet.http.HttpSession;
import net.link.safeonline.sdk.auth.filter.LoginManager;


@SuppressWarnings("UnusedDeclaration")
public class AttributeBean {

    private HttpSession session;

    public AttributeBean() {

    }

    public HttpSession getSession() {

        return session;
    }

    public void setSession(HttpSession session) {

        this.session = session;
    }

    public Map getAttributes() {

        return LoginManager.findAttributes( session );
    }

    public String getUserId() {

        return LoginManager.findUserId( session );
    }
}
