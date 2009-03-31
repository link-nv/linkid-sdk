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
import net.link.safeonline.webapp.components.attribute.AttributeInputPanel;

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
public class AttributeInputPanelTest {

    private WicketTester wicket;


    @Before
    public void setUp()
            throws Exception {

        wicket = new WicketTester(new PanelTestApplication());

    }

    @After
    public void tearDown()
            throws Exception {

    }

    @Test
    public void testAttributeInputPanel()
            throws Exception {

        // setup
        AttributeDO attribute = new AttributeDO("attribute-name", DatatypeType.STRING);
        String newStringValue = "new-string-value";

        AttributeInputPanelPage attributeInputPanelPage = new AttributeInputPanelPage(attribute);
        attributeInputPanelPage = (AttributeInputPanelPage) wicket.startPage(attributeInputPanelPage);

        // verify
        wicket.dumpPage();
        wicket.assertComponent(AttributeInputPanelPage.ATTRIBUTE_FORM_ID + ":" + AttributeInputPanelPage.ATTRIBUTE_ID + ":"
                + AttributeInputPanel.STRING_ID, TextField.class);

        // operate
        FormTester attributeForm = wicket.newFormTester(AttributeInputPanelPage.ATTRIBUTE_FORM_ID);
        attributeForm.setValue(AttributeInputPanelPage.ATTRIBUTE_ID + ":" + AttributeInputPanel.STRING_ID, newStringValue);
        attributeForm.submit(AttributeInputPanelPage.SUBMIT_BUTTON_ID);

        // verify
        // assertEquals(newStringValue, attribute.getStringValue());
    }
}
