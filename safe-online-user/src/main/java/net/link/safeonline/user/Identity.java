package net.link.safeonline.user;

import javax.ejb.Local;

@Local
public interface Identity {

	String getLogin();
}
