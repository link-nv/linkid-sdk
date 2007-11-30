/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package org.jboss.seam.deployment;

import java.util.HashSet;
import java.util.Set;

import javassist.bytecode.ClassFile;
import net.link.safeonline.common.Configurable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.deployment.Scanner;

public class ConfigurableScanner extends Scanner {

	private static final Log LOG = LogFactory.getLog(ConfigurableScanner.class);

	private Set<Class<Object>> classes;

	public ConfigurableScanner(String resourceName) {
		super(resourceName);
	}

	public Set<Class<Object>> getClasses() {
		if (this.classes == null) {
			this.classes = new HashSet<Class<Object>>();
			scan();
		}
		return this.classes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleItem(String name) {
		if (name.endsWith(".class")) {
			String classname = filenameToClassname(name);
			try {
				ClassFile classFile = getClassFile(name);
				if (hasAnnotation(classFile, Configurable.class)) {
					this.classes.add((Class<Object>) this.classLoader
							.loadClass(classname));
				}
			} catch (Exception e) {
				LOG.warn("error: " + e.getMessage());
			}
		}
	}

}
