/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.wicket.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wicketstuff.javaee.naming.IJndiNamingStrategy;


/**
 * <h2>{@link OlasNamingStrategy}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Sep 25, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public class OlasNamingStrategy implements IJndiNamingStrategy {

    private static final long serialVersionUID = 1L;


    @SuppressWarnings("unchecked")
    public String calculateName(String ejbName, Class ejbType) {

        try {
            String binding = ejbType.getDeclaredField("BINDING").get(null).toString();
            System.out.println("OLAS NAMING STRATEGY: CALCULATING NAME FOR " + ejbType + " (name : '" + ejbName
                    + "') = " + binding);
            return binding;
        } catch (IllegalArgumentException e) {
            getLog().error("[BUG] Object is not the right type.", e);
        } catch (SecurityException e) {
            getLog().error("[BUG] Field injected not allowed.", e);
        } catch (IllegalAccessException e) {
            getLog().error("[BUG] Field injected not allowed.", e);
        } catch (NoSuchFieldException e) {
            getLog().error("[BUG] BINDING field is not declared.", e);
        }

        throw new IllegalArgumentException("Bean injection not supported for bean of type: " + ejbType);
    }

    private Log getLog() {

        return LogFactory.getLog(getClass());
    }
}
