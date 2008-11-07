/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.resources;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.servlet.annotation.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.Log4JLogChute;


/**
 * <h2>{@link ApplicationStyleServlet}<br>
 * <sub>This servlet generates CSS style for colouring web applications.</sub></h2>
 * 
 * <p>
 * CSS is generated as declared in <code>style.css.vm</code> in this project's resource folder. Color variables in there are calculated in
 * this servlet based off of the current application's configured base color.
 * </p>
 * 
 * <p>
 * <i>May 13, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class ApplicationStyleServlet extends AbstractInjectionServlet {

    /* Velocity Context Variables */
    private static final String BRIGHTER         = "brighter";
    private static final String BRIGHT           = "bright";
    private static final String DARKER           = "darker";

    /* To convert base into theme colors: */
    private static final int    BRIGHTER_OFFSET  = 29;
    private static final double BRIGHTER_FACTOR  = 1.39;
    private static final int    BRIGHT_OFFSET    = 0;
    private static final double BRIGHT_FACTOR    = 1.45;
    private static final int    DARKER_OFFSET    = 17;
    private static final double DARKER_FACTOR    = 1.26;

    private static final long   serialVersionUID = 1L;
    private static final Log    LOG              = LogFactory.getLog(ApplicationStyleServlet.class);

    @Context(name = "ApplicationColor", defaultValue = "#5A7500")
    private String              applicationColor;
    private VelocityEngine      velocity;


    /**
     * @{inheritDoc
     */
    @Override
    public void init(ServletConfig config)
            throws ServletException {

        try {
            super.init(config);
        } catch (Exception e) {
            LOG.error("Could not properly initialize style servlet.", e);
        }

        Properties velocityProperties = new Properties();
        velocityProperties.put("resource.loader", "class");
        velocityProperties.put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, Log4JLogChute.class.getName());
        velocityProperties.put(Log4JLogChute.RUNTIME_LOG_LOG4J_LOGGER, getClass().getName());
        velocityProperties.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        this.velocity = new VelocityEngine();
        try {
            this.velocity.init(velocityProperties);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * @{inheritDoc
     */
    @Override
    protected void invokeGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String applicationName = request.getParameter("applicationName");
        if (null == applicationName)
            throw new IllegalArgumentException("The application name must be provided.");

        // Figure out the base color for the style.
        Integer baseColor = null;
        Object colorAttribute = request.getAttribute(SafeOnlineConstants.COLOR_ATTRIBUTE);
        try {
            baseColor = colorAttribute == null? null: Integer.decode(colorAttribute.toString());
        } catch (NumberFormatException e) {
            LOG.warn(String.format("Couldn't parse color attribute '%s' into a 24-bit color integer.", colorAttribute), e);
        }

        // If not yet set, use the default application color.
        if (baseColor == null) {
            baseColor = Integer.decode(this.applicationColor);
        }

        // Merge the velocity style template with the color attributes.
        OutputStreamWriter responseWriter = new OutputStreamWriter(response.getOutputStream());
        try {
            VelocityContext velocityContext = new VelocityContext();
            velocityContext.put(DARKER, getThemedColor(baseColor, DARKER_FACTOR, DARKER_OFFSET));
            velocityContext.put(BRIGHT, getThemedColor(baseColor, BRIGHT_FACTOR, BRIGHT_OFFSET));
            velocityContext.put(BRIGHTER, getThemedColor(baseColor, BRIGHTER_FACTOR, BRIGHTER_OFFSET));

            response.setContentType("text/css");
            this.velocity.mergeTemplate("theme.css.vm", velocityContext, responseWriter);
        }

        catch (Exception e) {
            LOG.error("Velocity Failed:", e);
            throw new ServletException(e);
        }

        finally {
            responseWriter.flush();
            response.flushBuffer();
        }
    }

    private String getThemedColor(Integer base, double factor, int offset) {

        int red = Math.min((int) ((base >> 16) % (0xFF + 1) * factor + offset), 0xFF);
        int green = Math.min((int) ((base >> 8) % (0xFF + 1) * factor + offset), 0xFF);
        int blue = Math.min((int) ((base >> 0) % (0xFF + 1) * factor + offset), 0xFF);

        return String.format("#%02X%02X%02X", red, green, blue);
    }
}
