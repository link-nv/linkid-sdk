/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.data;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.model.bean.AttributeManagerLWBean;


/**
 * <h2>{@link CompoundAttributeDO}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Feb 12, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public class CompoundAttributeDO {

    private AttributeEntity        compoundAttribute;
    private AttributeManagerLWBean attributeManager;


    public CompoundAttributeDO(AttributeEntity compoundAttribute, AttributeManagerLWBean attributeManager) {

        this.compoundAttribute = compoundAttribute;
        this.attributeManager = attributeManager;
    }

    public void addAttribute(String attributeTypeName, Object attributeValue)
            throws AttributeTypeNotFoundException {

        attributeManager.newAttribute(attributeTypeName, attributeValue, compoundAttribute);
    }

    public void addAttribute(AttributeTypeEntity attributeType, Object attributeValue) {

        attributeManager.newAttribute(attributeType, attributeValue, compoundAttribute);
    }

    public AttributeEntity getAttribute() {

        return compoundAttribute;
    }
}
