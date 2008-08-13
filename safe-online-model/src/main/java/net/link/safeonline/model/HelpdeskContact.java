package net.link.safeonline.model;

import javax.ejb.Local;


@Local
public interface HelpdeskContact {

    String getPhone();

    void setPhone(String phone);

    String getEmail();

    void setEmail(String email);

}
