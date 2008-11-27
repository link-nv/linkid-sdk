/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.siemens.acceptance.attributepull;

/**
 * <h2>{@link MyClassLoader}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Nov 25, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class MyClassLoader extends ClassLoader {

    private ClassLoader clsLoader = null;


    public MyClassLoader(ClassLoader parent) {

        super(null);
        this.clsLoader = parent;
    }

    @Override
    protected Class<?> findClass(String name)
            throws ClassNotFoundException {

        System.out.println("MyClassLoader: findClass(" + name + ")");

        if (name.startsWith("javax.net.ssl.X509TrustManager"))
            return ClassLoader.getSystemClassLoader().loadClass(name);

        return this.clsLoader.loadClass(name);
    }
}
