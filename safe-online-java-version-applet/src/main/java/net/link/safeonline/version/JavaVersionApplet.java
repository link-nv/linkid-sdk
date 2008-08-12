/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.version;

import java.applet.Applet;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Detects the Java runtime. This covers the Java version and the availability
 * of PKCS#11 drivers.
 * 
 * @author fcorneli
 * 
 */
public class JavaVersionApplet extends Applet {

    private static final long serialVersionUID = 1L;

    private final String      version;

    private final String      vendor;

    private boolean           hasPkcs11;


    public static class PlatformDrivers {

        final String platform;

        final List   driverLocations;


        public PlatformDrivers(String platform, List driverLocations) {

            this.platform = platform;
            this.driverLocations = driverLocations;
        }
    }


    private static final List platformsDrivers = new LinkedList();

    static {
        List linuxDrivers = new LinkedList();
        linuxDrivers.add("/usr/local/lib/libbeidpkcs11.so");
        linuxDrivers.add("/usr/lib/libbeidpkcs11.so");
        linuxDrivers.add("/usr/lib/opensc-pkcs11.so");
        platformsDrivers.add(new PlatformDrivers("Linux", linuxDrivers));

        List windowsDrivers = new LinkedList();
        windowsDrivers.add("C:\\WINDOWS\\system32\\beidpkcs11.dll");
        windowsDrivers
                .add("C:\\WINDOWS\\system32\\Belgium Identity Card PKCS11.dll");
        platformsDrivers.add(new PlatformDrivers("Windows XP", windowsDrivers));

        List macDrivers = new LinkedList();
        macDrivers
                .add("/usr/local/lib/beid-pkcs11.bundle/Contents/MacOS/libbeidpkcs11.2.1.0.dylib");
        macDrivers
                .add("/usr/local/lib/beid-pkcs11.bundle/Contents/MacOS/libbeidpkcs11.dylib");
        macDrivers.add("/Library/OpenSC/lib/opensc-pkcs11.so");
        platformsDrivers.add(new PlatformDrivers("Mac OS X", macDrivers));
    }


    public void init() {

        super.init();
        String osName = System.getProperty("os.name");
        Iterator platformIterator = platformsDrivers.iterator();
        this.hasPkcs11 = false;
        while (platformIterator.hasNext()) {
            PlatformDrivers platformDrivers = (PlatformDrivers) platformIterator
                    .next();
            if (true == osName.matches(platformDrivers.platform)) {
                List driverLocations = platformDrivers.driverLocations;
                Iterator driverIterator = driverLocations.iterator();
                while (driverIterator.hasNext()) {
                    String driverLocation = (String) driverIterator.next();
                    File driverPath = new File(driverLocation);
                    if (driverPath.exists()) {
                        this.hasPkcs11 = true;
                    }
                }
            }
        }
    }

    public JavaVersionApplet() {

        this.version = System.getProperty("java.version");
        this.vendor = System.getProperty("java.vendor");
    }

    public String getVersion() {

        return this.version;
    }

    public String getVendor() {

        return this.vendor;
    }

    public boolean hasPkcs11() {

        return this.hasPkcs11;
    }
}
