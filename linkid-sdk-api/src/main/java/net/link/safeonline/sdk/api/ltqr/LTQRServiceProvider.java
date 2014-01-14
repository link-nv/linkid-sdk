package net.link.safeonline.sdk.api.ltqr;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 14/01/14
 * Time: 10:58
 */
public class LTQRServiceProvider implements Serializable {

    private final String username;
    private final String password;

    public LTQRServiceProvider(final String username, final String password) {

        this.username = username;
        this.password = password;
    }

    public String getUsername() {

        return username;
    }

    public String getPassword() {

        return password;
    }
}
