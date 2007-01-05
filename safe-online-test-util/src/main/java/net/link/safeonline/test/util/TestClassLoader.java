/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestClassLoader extends ClassLoader {

	private static final Log LOG = LogFactory.getLog(TestClassLoader.class);

	private final Map<String, List<URL>> resources;

	public TestClassLoader() {
		this.resources = new HashMap<String, List<URL>>();
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		LOG.debug("get resources for resource name: " + name);
		List<URL> resourceList = this.resources.get(name);
		if (null == resourceList) {
			return super.getResources(name);
		}
		LOG.debug("found test resources");
		Enumeration<URL> enumeration = Collections.enumeration(resourceList);
		return enumeration;
	}

	public void addResource(String name, URL resource) {
		if (null == resource) {
			throw new IllegalArgumentException("resource is null");
		}
		List<URL> resourceList = this.resources.get(name);
		if (null == resourceList) {
			resourceList = new LinkedList<URL>();
			this.resources.put(name, resourceList);
		}
		resourceList.add(resource);
	}
}
