package net.link.safeonline.model;

import javax.ejb.Local;


@Local
public interface Messaging {

    void sendEmail(String to, String subject, String message);

}
