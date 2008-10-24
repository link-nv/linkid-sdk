/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package test.spike.net.link.safeonline;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.junit.Test;


/**
 * <h2>{@link VelocityTest}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>May 13, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class VelocityTest {

    @Test
    public void volicityTest() throws Exception {

        Properties velocityProperties = new Properties();
        velocityProperties.put("resource.loader", "class");
        velocityProperties.put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, Log4JLogChute.class.getName());
        velocityProperties.put(Log4JLogChute.RUNTIME_LOG_LOG4J_LOGGER, VelocityTest.class.getName());
        velocityProperties.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        VelocityEngine velocity = new VelocityEngine();
        velocity.init(velocityProperties);

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("foo", "bar");
        velocityContext.put("bar", "foo");

        StringWriter output = new StringWriter();
        velocity.mergeTemplate("velocityTest.vm", velocityContext, output);

        assertNotNull(output);
        assertTrue(output.toString().length() > 0);

        System.out.println(output);
    }
}
