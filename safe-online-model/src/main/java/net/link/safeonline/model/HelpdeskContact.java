package net.link.safeonline.model;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;


@Local
public interface HelpdeskContact extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "HelpdeskContactBean/local";


    String getPhone();

    void setPhone(String phone);

    String getEmail();

    void setEmail(String email);

}
