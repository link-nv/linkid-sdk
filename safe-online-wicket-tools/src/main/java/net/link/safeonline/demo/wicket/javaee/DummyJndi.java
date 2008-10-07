/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.wicket.javaee;

import java.util.HashMap;

import javax.ejb.EJBException;
import javax.ejb.Local;

import net.link.safeonline.demo.wicket.service.OlasNamingStrategy;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link DummyJndi}<br>
 * <sub>A simplistic replacement for JNDI.</sub></h2>
 * 
 * <p>
 * This is a {@link HashMap} based dummy JNDI service. Use {@link #register(String, Object)} to put your objects in it
 * and {@link #lookup(String)} to retrieve them based on their registration binding.
 * </p>
 * 
 * <p>
 * <i>Oct 7, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public class DummyJndi extends HashMap<String, Object> {

    private static final long               serialVersionUID   = 1L;
    private static final OlasNamingStrategy olasNamingStrategy = new OlasNamingStrategy();
    private static DummyJndi                instance;


    private DummyJndi() {

        super();
    }

    private static DummyJndi getInstance() {

        if (instance == null) {
            instance = new DummyJndi();
        }

        return instance;
    }

    /**
     * Retrieve a bean based on the given JNDI binding.
     */
    public static Object lookup(String jndiBinding) {

        return getInstance().get(jndiBinding);
    }

    /**
     * Retrieve a bean based the local interface it implements.
     */
    public static <B> B lookup(Class<B> localInterface) {

        if (!localInterface.isAnnotationPresent(Local.class))
            throw new IllegalArgumentException("Tried to look up a bean using a type that is not a local interface!");

        return localInterface.cast(getInstance().get(olasNamingStrategy.calculateName(null, localInterface)));
    }

    /**
     * Register a bean with this dummy JNDI under the given JNDI binding.
     */
    public static void register(String jndiBinding, Object value) {

        getInstance().put(jndiBinding, value);
    }

    /**
     * Register a bean with this dummy JNDI under the JNDI binding specified by the {@link LocalBinding} of the given
     * bean class or the binding specified by the given local interface class.
     * 
     * @param beanClass
     *            The class that defines the binding to use for the bean. If the class has a {@link LocalBinding}
     *            annotation, that will determine the JNDI binding. Otherwise, if the class is a {@link Local}
     *            interface, the {@link OlasNamingStrategy} will determine the binding to use.
     * 
     * @param bean
     *            The bean object to register.
     */
    public static void register(Class<?> beanClass, Object bean) {

        String jndiBinding = null;
        if (beanClass.isAnnotationPresent(LocalBinding.class)) {
            jndiBinding = beanClass.getAnnotation(LocalBinding.class).jndiBinding();
        } else if (beanClass.isAnnotationPresent(Local.class)) {
            jndiBinding = olasNamingStrategy.calculateName(null, beanClass);
        }

        if (jndiBinding == null)
            throw new IllegalArgumentException(
                    "Could not determine the JNDI binding to bind the following bean under: " + bean + " (" + beanClass
                            + " has no LocalBinding or Local annotation?)");

        getInstance().put(jndiBinding, bean);
    }

    /**
     * Register a bean with this dummy JNDI implementation using its {@link LocalBinding} annotation to specify the JNDI
     * binding to register the bean under.
     */
    public static void registerAll(Class<?>... beanClasses) {

        for (Class<?> beanClass : beanClasses) {
            if (!beanClass.isAnnotationPresent(LocalBinding.class))
                throw new IllegalArgumentException("Attempted to register a bean with no local binding!");

            try {
                register(beanClass.getAnnotation(LocalBinding.class).jndiBinding(), beanClass.newInstance());
            }

            catch (InstantiationException e) {
                throw new EJBException("Couldn't create dummy bean for " + beanClass);
            } catch (IllegalAccessException e) {
                throw new EJBException("No access to create dummy bean for " + beanClass);
            }
        }
    }

    /**
     * Register a bean with this dummy JNDI implementation using its {@link LocalBinding} annotation to specify the JNDI
     * binding to register the bean under.
     */
    public static void registerAll(Object... beans) {

        for (Object bean : beans) {
            if (!bean.getClass().isAnnotationPresent(LocalBinding.class))
                throw new IllegalArgumentException("Attempted to register a bean (" + bean.getClass()
                        + ") with no local binding!");

            register(bean.getClass().getAnnotation(LocalBinding.class).jndiBinding(), bean);
        }
    }
}
