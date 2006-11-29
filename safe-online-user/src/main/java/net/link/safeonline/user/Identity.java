package net.link.safeonline.user;

import javax.ejb.Local;

@Local
public interface Identity {

	String getLogin();

	String getName();

	void setName(String name);

	String save();

	void destroyCallback();
}
