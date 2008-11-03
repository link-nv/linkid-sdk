package net.link.safeonline.model;

import javax.ejb.Local;


import net.link.safeonline.SafeOnlineService;

@Local
public interface Messaging extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/MessagingBean/local";

    void sendEmail(String to, String subject, String message);

}
