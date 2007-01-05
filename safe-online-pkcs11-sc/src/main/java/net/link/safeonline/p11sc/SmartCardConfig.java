/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.p11sc;

import java.io.File;
import java.util.List;

import javax.smartcardio.ATR;

public interface SmartCardConfig {

	String getCardAlias();

	boolean isSupportedATR(ATR atr);

	String getAuthenticationKeyAlias();

	String getSignatureKeyAlias();

	List<File> getPkcs11DriverLocations(String platform);

	String getIdentityExtractorClassname();
}
