/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.resources;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Properties;

import javax.ejb.EJB;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.model.application.PublicApplication;
import net.link.safeonline.service.PublicApplicationService;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;

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
 * CSS is generated as declared in <code>style.css.vm</code> in this project's
 * resource folder. Color variables in there are calculated in this servlet
 * based off of the current application's configured base color.
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
    private static final String      BRIGHTER         = "brighter";
    private static final String      BRIGHT           = "bright";
    private static final String      DARKER           = "darker";

    /* To convert base into theme colors: */
    private static final int         BRIGHTER_OFFSET  = 29;
    private static final double      BRIGHTER_FACTOR  = 1.39;
    private static final int         BRIGHT_OFFSET    = 0;
    private static final double      BRIGHT_FACTOR    = 1.45;
    private static final int         DARKER_OFFSET    = 17;
    private static final double      DARKER_FACTOR    = 1.26;

    private static final long        serialVersionUID = 1L;
    private static final Log         LOG              = LogFactory
                                                              .getLog(ApplicationStyleServlet.class);

    @EJB(mappedName = PublicApplicationService.JNDI_BINDING)
    private PublicApplicationService publicApplicationService;
    private VelocityEngine           velocity;


    /**
     * @{inheritDoc
     */
    @Override
    public void init(ServletConfig config) throws ServletException {

        try {
            super.init(config);
        } catch (Exception e) {
            LOG.error("Could not properly initialize style servlet.", e);
        }

        Properties velocityProperties = new Properties();
        velocityProperties.put("resource.loader", "class");
        velocityProperties.put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                Log4JLogChute.class.getName());
        velocityProperties.put(Log4JLogChute.RUNTIME_LOG_LOG4J_LOGGER,
                getClass().getName());
        velocityProperties
                .put("class.resource.loader.class",
                        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

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
    protected void invokeGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        String applicationName = request.getParameter("applicationName");
        if (null == applicationName) {
            throw new IllegalArgumentException(
                    "The application name must be provided.");
        }

        // Figure out the base color for the style.
        Color baseColor = Color.decode("#5a7500"); // Default: Green.
        if (this.publicApplicationService != null) {
            try {
                PublicApplication application = this.publicApplicationService
                        .findPublicApplication(applicationName);

                if (application == null)
                    throw new IllegalStateException("Application not found: "
                            + applicationName);
                 
                if (application.getColor() != null) {
                    baseColor = application.getColor();
                }
            } catch (Exception e) {
                LOG.error("Couldn't retrieve application color for "
                        + applicationName + ", reverting to defaults.", e);
            }
        }

        // Merge the velocity style template with the color attributes.
        OutputStreamWriter responseWriter = new OutputStreamWriter(response
                .getOutputStream());
        try {
            VelocityContext velocityContext = new VelocityContext();
            velocityContext.put(DARKER, getThemedColor(baseColor,
                    DARKER_FACTOR, DARKER_OFFSET));
            velocityContext.put(BRIGHT, getThemedColor(baseColor,
                    BRIGHT_FACTOR, BRIGHT_OFFSET));
            velocityContext.put(BRIGHTER, getThemedColor(baseColor,
                    BRIGHTER_FACTOR, BRIGHTER_OFFSET));

            response.setContentType("text/css");
            this.velocity.mergeTemplate("theme.css.vm", velocityContext,
                    responseWriter);
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

    private String getThemedColor(Color base, double factor, int offset) {

        int red = (int) (base.getRed() * factor + offset);
        int green = (int) (base.getGreen() * factor + offset);
        int blue = (int) (base.getBlue() * factor + offset);

        return String.format("#%02x%02x%02x", red, green, blue);
    }
}
