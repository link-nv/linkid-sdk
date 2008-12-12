/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.javaee;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wicketstuff.javaee.JavaEEBeanLocator;
import org.wicketstuff.javaee.naming.IJndiNamingStrategy;


/**
 * <h2>{@link DummyJavaEEBeanLocator}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Oct 7, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public class DummyJavaEEBeanLocator extends JavaEEBeanLocator {

    private static final long serialVersionUID = 1L;
    static final Log          LOG              = LogFactory.getLog(DummyJavaEEBeanLocator.class);


    public DummyJavaEEBeanLocator(String beanId, Class<?> beanType, IJndiNamingStrategy namingStrategy) {

        super(beanId, beanType, namingStrategy);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Object lookupEjb(String name, Class type) {

        if (type.isInterface())
            return DummyJndi.lookup(type);

        try {
            return type.newInstance();
        }

        catch (InstantiationException err) {
            LOG.error("Couldn't instantiate: " + name + " (" + type + ")", err);
            throw new RuntimeException(err);
        } catch (IllegalAccessException err) {
            LOG.error("No access to instantiate: " + name + " (" + type + ")", err);
            throw new RuntimeException(err);
        }
    }
}
