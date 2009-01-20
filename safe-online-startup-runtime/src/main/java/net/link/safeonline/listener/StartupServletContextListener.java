/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.listener;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.link.safeonline.Startable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This servlet context listener will start and stop all components registered in JNDI under the prefix defined by the web.xml context-param
 * StartableJndiPrefix. These components should implement the {@link Startable} interface.
 * 
 * @see Startable
 * @author fcorneli
 * 
 */
public class StartupServletContextListener implements ServletContextListener {

    private static final Log LOG = LogFactory.getLog(StartupServletContextListener.class);


    public void contextInitialized(ServletContextEvent event) {

        LOG.debug("context initialized");
        List<Startable> startables = getStartables(event, true);
        for (Startable startable : startables) {
            LOG.debug("starting: " + startable + " (priority " + startable.getPriority() + ")");
            try {
                startable.postStart();
            } catch (Exception e) {
                LOG.error("error starting: " + e.getMessage(), e);
                throw new RuntimeException("error starting: " + e.getMessage(), e);
            }
        }
    }

    public void contextDestroyed(ServletContextEvent event) {

        LOG.debug("context destroyed");
        List<Startable> startables = getStartables(event, false);
        for (Startable startable : startables) {
            LOG.debug("stopping: " + startable + " (priority " + startable.getPriority() + ")");
            startable.preStop();
        }
    }

    private List<Startable> getStartables(ServletContextEvent event, boolean priorityDescSort) {

        List<Startable> startables = getUnsortedStartables(event);
        Comparator<Startable> startableComparator = new StartablePriorityComparator(priorityDescSort);
        Collections.sort(startables, startableComparator);
        return startables;
    }


    private static class StartablePriorityComparator implements Comparator<Startable> {

        private final boolean priorityDescSort;


        public StartablePriorityComparator(boolean priorityDescSort) {

            this.priorityDescSort = priorityDescSort;
        }

        public int compare(Startable startable1, Startable startable2) {

            if (false == priorityDescSort)
                return startable1.getPriority() - startable2.getPriority();
            return startable2.getPriority() - startable1.getPriority();
        }
    }


    private List<Startable> getUnsortedStartables(ServletContextEvent event) {

        ServletContext servletContext = event.getServletContext();
        String startableJnidPrefix = servletContext.getInitParameter("StartableJndiPrefix");
        LOG.debug("get startables at " + startableJnidPrefix);
        try {
            InitialContext initialContext = new InitialContext();
            Context context = (Context) initialContext.lookup(startableJnidPrefix);
            NamingEnumeration<NameClassPair> result = initialContext.list(startableJnidPrefix);
            List<Startable> startables = new LinkedList<Startable>();
            while (result.hasMore()) {
                NameClassPair nameClassPair = result.next();
                String objectName = nameClassPair.getName();
                LOG.debug(objectName + ":" + nameClassPair.getClassName());
                Object object = context.lookup(objectName);
                if (!(object instanceof Startable)) {
                    String message = "object \"" + startableJnidPrefix + "/" + objectName + "\" is not a:\n"
                            + getClassInfo(Startable.class) + "\nit is:\n" + getClassInfo(object);
                    LOG.error(message);
                    throw new IllegalStateException(message);
                }
                Startable startable = (Startable) object;
                startables.add(startable);
                LOG.debug(objectName + " has priority " + startable.getPriority());
            }
            return startables;
        } catch (NamingException e) {
            throw new RuntimeException("naming error: " + e.getMessage(), e);
        }
    }

    private String getClassInfo(Object object) {

        if (object == null)
            return "null";

        String info = object.getClass().getCanonicalName();
        if (object.getClass().getSuperclass() != Object.class) {
            info += " extends " + object.getClass().getSuperclass().getCanonicalName();
        }
        if (object.getClass().getInterfaces().length > 0) {
            info += " implements ";
            for (int i = 0; i < object.getClass().getInterfaces().length; ++i) {
                info += object.getClass().getInterfaces()[i];
                if (i < object.getClass().getInterfaces().length - 1) {
                    info += ", ";
                }
            }
        }

        return info;
    }
}
