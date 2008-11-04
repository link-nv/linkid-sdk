/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.p11sc.beid;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import junit.framework.TestCase;
import net.link.safeonline.identity.IdentityApplet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class IdentityAppletTest extends TestCase {

    static final Log LOG = LogFactory.getLog(IdentityAppletTest.class);


    private static class AppletFrame extends JFrame {

        private static final long serialVersionUID = 1L;


        public AppletFrame(Applet applet) {

            super(applet.getName());

            add(applet);

            setSize(500, 500);
            setVisible(true);
        }
    }

    private static class TestAppletStub implements AppletStub {

        private Map<String, String> parameters;


        public TestAppletStub() {

            this.parameters = new HashMap<String, String>();
        }

        public void setParameter(String name, String value) {

            this.parameters.put(name, value);
        }

        @SuppressWarnings("unused")
        public void appletResize(int width, int height) {

            // empty
        }

        public AppletContext getAppletContext() {

            return null;
        }

        public URL getCodeBase() {

            return null;
        }

        public URL getDocumentBase() {

            return null;
        }

        public String getParameter(String name) {

            LOG.debug("get parameter: " + name);
            String value = this.parameters.get(name);
            if (null == value)
                throw new IllegalStateException("parameter not set: " + name);
            return value;
        }

        public boolean isActive() {

            return false;
        }
    }


    public void testLayout()
            throws Exception {

        final IdentityApplet identityApplet = new IdentityApplet();

        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {

                new AppletFrame(identityApplet);
            }
        });

        final TestAppletStub appletStub = new TestAppletStub();
        appletStub.setParameter("SmartCardConfig", "beid");
        appletStub.setParameter("User", "test-user");
        appletStub.setParameter("ServletPath", "/whereever");

        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {

                identityApplet.setStub(appletStub);
                identityApplet.init();
                identityApplet.start();
            }
        });

        LOG.debug("number of threads: " + Thread.activeCount());
        Thread[] threads = new Thread[8];
        Thread.enumerate(threads);
        for (Thread thread : threads) {
            LOG.debug("thread: " + thread.getName());
        }

        Thread currentThread = Thread.currentThread();
        currentThread.join();
    }
}
