package net.link.safeonline.user;

import javax.ejb.Local;

@Local
public interface Login {
	String login();

	String logout();
	
	String getUsername();

	void setUsername(String username);

	String getPassword();

	void setPassword(String password);
}
