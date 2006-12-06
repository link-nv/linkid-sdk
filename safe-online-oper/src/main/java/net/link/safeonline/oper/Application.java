package net.link.safeonline.oper;

import javax.ejb.Local;

@Local
public interface Application {

	void applicationListFactory();

	String view();

	void destroyCallback();

	String getName();

	String getDescription();

	void setName(String name);

	void setDescription(String description);

	String add();

	String removeApplication();
}
