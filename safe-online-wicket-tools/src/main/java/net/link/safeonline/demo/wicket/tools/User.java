package net.link.safeonline.demo.wicket.tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            username;

    private List<String>      roles            = new ArrayList<String>();


    public User() {

        // empty
    }

    public List<String> getRoles() {

        return this.roles;
    }

    public void setRoles(List<String> roles) {

        this.roles = roles;
    }

    public String getUsername() {

        return this.username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public boolean hasOneOf(List<String> roleList) {

        if (roleList == null)
            return true;
        if (roleList.size() == 0)
            return true;

        for (String hasRole : this.roles) {
            for (String needRole : roleList) {
                if (hasRole.equals(needRole))
                    return true;
            }
        }
        return false;
    }

    public boolean has(String role) {

        for (String hasRole : this.roles) {
            if (hasRole.equals(role))
                return true;
        }
        return false;
    }

}
