/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.ee;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Utils to ease the working with EJBs.
 * 
 * @author fcorneli
 * 
 */
public class EjbUtils {

    private static final Log LOG = LogFactory.getLog(EjbUtils.class);


    private EjbUtils() {

        // empty
    }

    /**
     * Lookup an EJB within JNDI.
     * 
     * @param <Type>
     * @param initialContext
     * @param jndiName
     * @param type
     */
    public static <Type> Type getEJB(InitialContext initialContext, String jndiName, Class<Type> type) {

        try {
            LOG.debug("ejb jndi lookup: " + jndiName);
            Object object = initialContext.lookup(jndiName);
            Type instance = type.cast(PortableRemoteObject.narrow(object, type));
            return instance;
        } catch (NamingException e) {
            throw new RuntimeException("naming error: " + e.getMessage(), e);
        }
    }

    public static <Type> Type getEJB(String jndiName, Class<Type> type) {

        InitialContext initialContext = getInitialContext();
        return getEJB(initialContext, jndiName, type);
    }

    private static InitialContext getInitialContext() {

        InitialContext initialContext;
        try {
            initialContext = new InitialContext();
        } catch (NamingException e) {
            throw new RuntimeException("naming error: " + e.getMessage(), e);
        }
        return initialContext;
    }

    public static <Type> List<Type> getComponents(InitialContext initialContext, String jndiPrefix, Class<Type> type) {

        LOG.debug("get components at " + jndiPrefix);
        List<Type> components = new LinkedList<Type>();
        try {
            Context context;
            try {
                context = (Context) initialContext.lookup(jndiPrefix);
            } catch (NameNotFoundException e) {
                return components;
            }
            NamingEnumeration<NameClassPair> result = initialContext.list(jndiPrefix);
            while (result.hasMore()) {
                NameClassPair nameClassPair = result.next();
                String objectName = nameClassPair.getName();
                LOG.debug(objectName + ":" + nameClassPair.getClassName());
                Object object = context.lookup(objectName);
                if (!type.isInstance(object)) {
                    String message = "object \"" + jndiPrefix + "/" + objectName + "\" is not a " + type.getName() + "; it is "
                            + (object == null? "null": "a " + object.getClass().getName());
                    LOG.error(message);
                    throw new IllegalStateException(message);
                }
                Type component = type.cast(object);
                components.add(component);
            }
            return components;
        } catch (NamingException e) {
            throw new RuntimeException("naming error: " + e.getMessage(), e);
        }
    }

    public static <Type> List<Type> getComponents(String jndiPrefix, Class<Type> type) {

        InitialContext initialContext;
        try {
            initialContext = new InitialContext();
        } catch (NamingException e) {
            throw new RuntimeException("naming error: " + e.getMessage(), e);
        }
        return getComponents(initialContext, jndiPrefix, type);
    }

    public static <Type> Map<String, Type> getComponentNames(InitialContext initialContext, String jndiPrefix, Class<Type> type) {

        LOG.debug("get component names at " + jndiPrefix);
        HashMap<String, Type> names = new HashMap<String, Type>();
        NamingEnumeration<NameClassPair> result;
        try {
            Context context;
            try {
                context = (Context) initialContext.lookup(jndiPrefix);
                result = initialContext.list(jndiPrefix);
            } catch (NameNotFoundException e) {
                return names;
            }

            while (result.hasMore()) {
                NameClassPair nameClassPair = result.next();
                String objectName = nameClassPair.getName();
                LOG.debug(objectName + ":" + nameClassPair.getClassName());
                Object object = context.lookup(objectName);
                if (!type.isInstance(object)) {
                    String message = "object \"" + jndiPrefix + "/" + objectName + "\" is not a " + type.getName() + "; it is "
                            + (object == null? "null": "a " + object.getClass().getName());
                    LOG.error(message);
                    throw new IllegalStateException(message);
                }
                Type component = type.cast(object);
                names.put(objectName, component);
            }
            return names;
        } catch (NamingException e) {
            throw new RuntimeException("naming error: " + e.getMessage(), e);
        }
    }

    public static <Type> Map<String, Type> getComponentNames(String jndiPrefix, Class<Type> type) {

        InitialContext initialContext;
        try {
            initialContext = new InitialContext();
        } catch (NamingException e) {
            throw new RuntimeException("naming error: " + e.getMessage(), e);
        }
        return getComponentNames(initialContext, jndiPrefix, type);
    }

    public static void bindComponent(String jndiName, Object component) throws NamingException {

        LOG.debug("bind component: " + jndiName);
        InitialContext initialContext = new InitialContext();
        String[] names = jndiName.split("/");
        Context context = initialContext;
        for (int idx = 0; idx < names.length - 1; idx++) {
            String name = names[idx];
            LOG.debug("name: " + name);
            NamingEnumeration<NameClassPair> listContent = context.list("");
            boolean subContextPresent = false;
            while (listContent.hasMore()) {
                NameClassPair nameClassPair = listContent.next();
                if (false == name.equals(nameClassPair.getName())) {
                    continue;
                }
                subContextPresent = true;
            }
            if (false == subContextPresent) {
                context = context.createSubcontext(name);
            } else {
                context = (Context) context.lookup(name);
            }
        }
        String name = names[names.length - 1];
        context.rebind(name, component);
    }

    public static Object getComponent(String jndiName) throws NamingException {

        InitialContext initialContext = new InitialContext();
        return initialContext.lookup(jndiName);

    }
}
