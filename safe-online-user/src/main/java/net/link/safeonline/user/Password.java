package net.link.safeonline.user;

import javax.ejb.Local;

@Local
public interface Password {

	String getNewPassword();

	void setNewPassword(String newPassword);

	String getOldPassword();

	void setOldPassword(String oldPassword);

	String change();
}
