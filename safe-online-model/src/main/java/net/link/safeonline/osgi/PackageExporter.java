/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link PackageExporter}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Nov 27, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class PackageExporter {

    private static final Log LOG = LogFactory.getLog(PackageExporter.class);


    public static List<String> getPackageNames(String packageExpression) {

        List<String> packageNames = new LinkedList<String>();
        Package[] packages = Package.getPackages();
        for (Package pkg : packages) {
            LOG.debug("pkg: " + pkg.getName());
            if (pkg.getName().startsWith(packageExpression)) {
                packageNames.add(pkg.getName());
            }
        }

        return packageNames;

    }

}
