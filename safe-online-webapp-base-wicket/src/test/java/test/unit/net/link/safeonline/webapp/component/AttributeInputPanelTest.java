/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package test.unit.net.link.safeonline.webapp.component;

import junit.framework.TestCase;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.webapp.components.AttributeInputPanel;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * <h2>{@link AttributeInputPanelTest}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Feb 27, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class AttributeInputPanelTest extends TestCase {

    private WicketTester wicket;


    @Override
    @Before
    public void setUp()
            throws Exception {

        super.setUp();

        WicketUtil.setUnitTesting(true);

        wicket = new WicketTester(new PanelTestApplication());

    }

    @Override
    @After
    public void tearDown()
            throws Exception {

    }

    @Test
    public void testAttributeInputPanel()
            throws Exception {

        // setup
        final AttributeDO attribute = new AttributeDO("attribute-name", DatatypeType.INTEGER);
        Double newDoubleValue = 10.0;
        Integer newIntegerValue = 10;

        AttributeInputPanelPage attributeInputPanelPage = new AttributeInputPanelPage(attribute);
        attributeInputPanelPage = (AttributeInputPanelPage) wicket.startPage(attributeInputPanelPage);

        // verify
        wicket.dumpPage();
        wicket.assertComponent(AttributeInputPanelPage.ATTRIBUTE_FORM_ID + ":" + AttributeInputPanelPage.ATTRIBUTE_ID + ":"
                + AttributeInputPanel.INTEGER_ID, TextField.class);

        // operate
        FormTester attributeForm = wicket.newFormTester(AttributeInputPanelPage.ATTRIBUTE_FORM_ID);
        // attributeForm.setValue(AttributeInputPanelPage.ATTRIBUTE_ID + ":" + AttributeInputPanel.INTEGER_ID, newIntegerValue.toString());
        attributeForm.submit(AttributeInputPanelPage.SUBMIT_BUTTON_ID);

        // verify
        assertEquals(newDoubleValue, attribute.getDoubleValue());
    }
}
