/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.ProxyAttributeService;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.osgi.OSGIAttributeService;
import net.link.safeonline.osgi.OlasAttributeServiceImpl;
import net.link.safeonline.osgi.plugin.Attribute;
import net.link.safeonline.osgi.plugin.DatatypeType;
import net.link.safeonline.osgi.plugin.OlasAttributeService;
import net.link.safeonline.osgi.plugin.exception.UnsupportedDataTypeException;

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

    @EJB
    private ProxyAttributeService proxyAttributeService;

    @EJB
    private AttributeTypeDAO      attributeTypeDAO;


    /**
     * {@inheritDoc}
     * 
     */
    public List<Attribute> getAttribute(String userId, String attributeName)
            throws AttributeTypeNotFoundException, AttributeNotFoundException, UnsupportedDataTypeException, AttributeUnavailableException,
            SubjectNotFoundException {

        LOG.debug("get attribute " + attributeName + " for user " + userId);
        List<Attribute> attributeView = new LinkedList<Attribute>();
        AttributeTypeEntity attributeType = this.attributeTypeDAO.getAttributeType(attributeName);

        Object value;
        try {
            value = this.proxyAttributeService.findAttributeValue(userId, attributeName);
        } catch (PermissionDeniedException e) {
            LOG.debug("permission denied retrieving attribute " + attributeName + " for user " + userId);
            throw new AttributeNotFoundException();
        }
        if (null == value)
            throw new AttributeNotFoundException();

        // convert value to osgi attribute view
        addValueToView(value, attributeType, attributeView);

        return attributeView;
    }

    @SuppressWarnings("unchecked")
    private void addValueToView(Object value, AttributeTypeEntity attributeType, List<Attribute> attributesView)
            throws UnsupportedDataTypeException {

        LOG.debug("add attribute " + attributeType.getName() + " to view");
        if (!attributeType.isMultivalued()) {
            // single-valued
            attributesView.add(getAttributeView(attributeType, value, 0));
        } else if (!attributeType.isCompounded()) {
            // multi-valued but NOT compounded
            int idx = 0;
            for (Object attributeValue : (Object[]) value) {
                attributesView.add(getAttributeView(attributeType, attributeValue, idx));
                idx++;
            }
        } else {
            // compounded
            int idx = 0;
            for (Object attributeValue : (Object[]) value) {
                Map<String, Object> memberMap = (Map<String, Object>) attributeValue;
                // first add an attribute view for the parent attribute
                // type
                LOG.debug("add compounded attribute: " + attributeType.getName());
                attributesView.add(getAttributeView(attributeType, null, idx));
                for (CompoundedAttributeTypeMemberEntity memberAttributeType : attributeType.getMembers()) {
                    LOG.debug("add compounded member attribute: " + memberAttributeType.getMember().getName());
                    attributesView
                                  .add(getAttributeView(memberAttributeType.getMember(), memberMap.get(memberAttributeType.getMember()
                                                                                                                          .getName()), idx));
                }
                idx++;
            }
        }
    }

    /**
     * Returns an attribute view for the given attribute. Must be single valued at this point.
     * 
     * @throws UnsupportedDataTypeException
     */
    private Attribute getAttributeView(AttributeTypeEntity attributeType, Object value, int idx)
            throws UnsupportedDataTypeException {

        LOG.debug("get attribute view for type: " + attributeType.getName() + " with value: " + value);
        Attribute attributeView = new Attribute(attributeType.getName(), convertDataType(attributeType.getType()), idx);

        attributeView.setValue(value);

        return attributeView;
    }

    private DatatypeType convertDataType(net.link.safeonline.entity.DatatypeType dataType)
            throws UnsupportedDataTypeException {

        if (dataType.equals(net.link.safeonline.entity.DatatypeType.BOOLEAN))
            return DatatypeType.BOOLEAN;
        else if (dataType.equals(net.link.safeonline.entity.DatatypeType.COMPOUNDED))
            return DatatypeType.COMPOUNDED;
        else if (dataType.equals(net.link.safeonline.entity.DatatypeType.DATE))
            return DatatypeType.DATE;
        else if (dataType.equals(net.link.safeonline.entity.DatatypeType.DOUBLE))
            return DatatypeType.DOUBLE;
        else if (dataType.equals(net.link.safeonline.entity.DatatypeType.INTEGER))
            return DatatypeType.INTEGER;
        else if (dataType.equals(net.link.safeonline.entity.DatatypeType.STRING))
            return DatatypeType.STRING;
        else
            throw new UnsupportedDataTypeException("Unsupported datatype: " + dataType.getFriendlyName());
    }
}
