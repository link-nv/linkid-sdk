package net.link.safeonline.owner;

import javax.ejb.Local;

@Local
public interface Application {

	void applicationListFactory();

	String view();

	void destroyCallback();

	String edit();

	String save();
}
