/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.wicket.javaee;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wicketstuff.javaee.JndiObjectLocator;


/**
 * <h2>{@link DummyJndiObjectLocator}<br>
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
public class DummyJndiObjectLocator extends JndiObjectLocator {

    private static final long serialVersionUID = 1L;
    static final Log          LOG              = LogFactory.getLog(DummyJndiObjectLocator.class);


    public DummyJndiObjectLocator(String name, Class<?> type) {

        super(name, type);
    }


    @Override
    @SuppressWarnings("unchecked")
    protected Object lookup(String name, Class type) {

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
