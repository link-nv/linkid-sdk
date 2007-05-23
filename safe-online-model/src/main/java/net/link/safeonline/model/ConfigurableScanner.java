package net.link.safeonline.model;

import java.util.HashSet;
import java.util.Set;

import javassist.bytecode.ClassFile;

import net.link.safeonline.common.Configurable;

import org.jboss.seam.deployment.Scanner;

public class ConfigurableScanner extends Scanner {

	private Set<Class<Object>> classes;

	public ConfigurableScanner(String resourceName) {
		super(resourceName);
	}

	public Set<Class<Object>> getClasses() {
		if (classes == null) {
			classes = new HashSet<Class<Object>>();
			scan();
		}
		return classes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleItem(String name) {
		if (name.endsWith(".class")) {
			String classname = filenameToClassname(name);
			try {
				ClassFile classFile = getClassFile(name);
				if (hasAnnotation(classFile, Configurable.class)) {
					classes.add((Class<Object>) classLoader
							.loadClass(classname));
				}
			} catch (Exception e) {
				// empty
			}
		}
	}

}
