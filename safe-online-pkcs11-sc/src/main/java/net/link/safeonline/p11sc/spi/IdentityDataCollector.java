/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.p11sc.spi;

public interface IdentityDataCollector {

	void setSurname(String surname);

	void setGivenName(String givenName);

	void setCountryCode(String countryCode);

	void setStreet(String street);

	void setPostalCode(String postalCode);

	void setCity(String city);
}
