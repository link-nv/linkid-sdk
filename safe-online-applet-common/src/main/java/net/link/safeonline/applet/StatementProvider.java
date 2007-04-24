/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.applet;

import net.link.safeonline.p11sc.SmartCard;

/**
 * Interface for statement provider.
 * 
 * @author fcorneli
 * 
 */
public interface StatementProvider {

	byte[] createStatement(SmartCard smartCard);
}
