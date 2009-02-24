/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.attribute;

import net.link.safeonline.osgi.OSGIAttributeService;
import net.link.safeonline.osgi.OlasAttributeService;
import net.link.safeonline.osgi.exception.AttributeNotFoundException;
import net.link.safeonline.osgi.exception.AttributeTypeNotFoundException;
import net.link.safeonline.osgi.exception.AttributeUnavailableException;
import net.link.safeonline.osgi.exception.SubjectNotFoundException;
import net.link.safeonline.util.ee.EjbUtils;


/**
 * <h2>{@link OlasAttributeServiceImpl}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Aug 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class OlasAttributeServiceImpl implements OlasAttributeService {

    /**
     * {@inheritDoc}
     */
    public Object getAttribute(String userId, String attributeName)
            throws AttributeTypeNotFoundException, AttributeNotFoundException, AttributeUnavailableException, SubjectNotFoundException {

        OSGIAttributeService attributeService = EjbUtils.getEJB(OSGIAttributeService.JNDI_BINDING, OSGIAttributeService.class);
        try {
            return attributeService.getAttribute(userId, attributeName);
        } catch (net.link.safeonline.authentication.exception.AttributeTypeNotFoundException e) {
            throw new AttributeTypeNotFoundException(e.getMessage());
        } catch (net.link.safeonline.authentication.exception.AttributeNotFoundException e) {
            throw new AttributeNotFoundException(e.getMessage());
        } catch (net.link.safeonline.authentication.exception.AttributeUnavailableException e) {
            throw new AttributeUnavailableException(e.getMessage());
        } catch (net.link.safeonline.authentication.exception.SubjectNotFoundException e) {
            throw new SubjectNotFoundException(e.getMessage());
        }
    }

}
