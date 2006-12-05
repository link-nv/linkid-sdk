package net.link.safeonline.oper;

import javax.ejb.Local;

@Local
public interface Application {

	void applicationListFactory();

	String view();

	void destroyCallback();
}
