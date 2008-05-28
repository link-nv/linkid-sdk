/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.accounting;

import javax.ejb.Local;

@Local
public interface Accounting {

	/*
	 * Accessors.
	 */

	/*
	 * Actions.
	 */
	String view();

	String viewStat();

	String export();

	String exportStat();

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	void postConstructCallback();

	/*
	 * Factories.
	 */
	void applicationListFactory();

	void statListFactory();
}
