package net.link.safeonline.digipass;

import java.io.IOException;

import javax.ejb.Local;


import net.link.safeonline.SafeOnlineService;

@Local
public interface Authentication extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/AuthenticationBean/local";

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
