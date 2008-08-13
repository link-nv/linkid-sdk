package net.link.safeonline.digipass;

import java.io.IOException;

import javax.ejb.Local;


@Local
public interface Authentication {

    /*
     * Accessors.
     */
    String getLoginName();

    void setLoginName(String loginName);

    String getToken();

    void setToken(String token);

    /*
     * Actions.
     */
    String login() throws IOException;

    String cancel() throws IOException;

    String tryAnotherDevice() throws IOException;

    /*
     * Lifecycle.
     */
    void init();

    void destroyCallback();

}
