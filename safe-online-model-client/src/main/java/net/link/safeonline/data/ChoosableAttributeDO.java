/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Wrapper class for 2 AttributeDO's which a user can choose from.
 * 
 * @author wvdhaute
 * 
 */
public class ChoosableAttributeDO {

    private static final Log LOG = LogFactory.getLog(ChoosableAttributeDO.class);

    private AttributeDO      targetAttribute;

    private boolean          sourceSelected;

    private AttributeDO      sourceAttribute;


    public ChoosableAttributeDO(AttributeDO targetAttribute, AttributeDO sourceAttribute) {

        this.targetAttribute = targetAttribute;
        sourceSelected = false;
        this.sourceAttribute = sourceAttribute;
    }

    public AttributeDO getTargetAttribute() {

        return targetAttribute;
    }

    public AttributeDO getSourceAttribute() {

        return sourceAttribute;
    }

    public boolean isSourceSelected() {

        return sourceSelected;
    }

    public void setSourceSelected(boolean sourceSelected) {

        this.sourceSelected = sourceSelected;
        LOG.debug("attribute: " + targetAttribute.getName() + " : set source : " + this.sourceSelected);
    }
}
