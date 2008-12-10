/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.bean;

import java.util.Arrays;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.ProxyAttributeService;
import net.link.safeonline.osgi.OSGIAttributeService;
import net.link.safeonline.osgi.OlasAttributeService;
import net.link.safeonline.osgi.OlasAttributeServiceImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link OSGIAttributeServiceBean}<br>
 * <sub>OLAS Attribute Service used by OSGi plugin bundles.</sub></h2>
 * 
 * <p>
 * This EJB is used by OSGi plugin bundles when retrieving attributes from OLAS. It is called from {@link OlasAttributeServiceImpl} which is
 * an implementation of the OLAS attribute service ( {@link OlasAttributeService} that external plugins use.
 * </p>
 * 
 * <p>
 * <i>Aug 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
@Stateless
@LocalBinding(jndiBinding = OSGIAttributeService.JNDI_BINDING)
public class OSGIAttributeServiceBean implements OSGIAttributeService {

    private static final Log      LOG = LogFactory.getLog(OSGIAttributeServiceBean.class);

    @EJB(mappedName = ProxyAttributeService.JNDI_BINDING)
    private ProxyAttributeService proxyAttributeService;


    /**
     * {@inheritDoc}
     */
    public Object getAttribute(String userId, String attributeName)
            throws AttributeNotFoundException, AttributeUnavailableException, SubjectNotFoundException, AttributeTypeNotFoundException {

        LOG.debug("get attribute " + attributeName + " for user " + userId);
        Object value;
        try {
            value = this.proxyAttributeService.findAttributeValue(userId, attributeName);
        } catch (PermissionDeniedException e) {
            LOG.debug("permission denied retrieving attribute " + attributeName + " for user " + userId);
            throw new AttributeNotFoundException();
        }
        if (null == value)
            throw new AttributeNotFoundException();

        // we provide List's to the OSGI plugins, convert
        if (value.getClass().isArray())
            return Arrays.asList(value);

        return value;
    }
}
