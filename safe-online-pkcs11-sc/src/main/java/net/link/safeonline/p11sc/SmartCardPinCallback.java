/*
 * SafeOnline project.
 * 
 * Copyright 2005-2006 Frank Cornelis.
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.p11sc;

/**
 * Smart Card PIN code callback interface.
 * 
 * @author fcorneli
 * 
 */
public interface SmartCardPinCallback {

	/**
	 * Gives back the PIN code to unlock the smart card key store.
	 * 
	 * @return
	 */
	char[] getPin();
}
