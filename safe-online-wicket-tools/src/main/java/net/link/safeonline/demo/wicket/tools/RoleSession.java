package net.link.safeonline.demo.wicket.tools;

import org.apache.wicket.Request;
import org.apache.wicket.protocol.http.WebSession;


public class RoleSession extends WebSession {

    private static final long serialVersionUID = 1L;

    private User              user;


    public RoleSession(Request request) {

        super(request);
    }

    public User getUser() {

        return this.user;
    }

    public void setUser(User user) {

        this.user = user;
    }

}
