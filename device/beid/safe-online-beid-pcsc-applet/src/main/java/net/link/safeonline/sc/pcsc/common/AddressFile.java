/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sc.pcsc.common;

public class AddressFile {

    @Tag(1)
    String streetAndNumber;

    @Tag(2)
    String zip;

    @Tag(3)
    String municipality;


    public String getStreetAndNumber() {

        return streetAndNumber;
    }

    public String getZip() {

        return zip;
    }

    public String getMunicipality() {

        return municipality;
    }
}
