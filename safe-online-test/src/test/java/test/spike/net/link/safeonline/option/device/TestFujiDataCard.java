/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package test.spike.net.link.safeonline.option.device;

import net.link.safeonline.option.device.impl.FujiDataCard;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <h2>{@link TestFujiDataCard}<br>
 * <sub>Tests the Option iCon 225 card.</sub></h2>
 * 
 * <p>
 * Tests the Option iCon 225 card.
 * </p>
 * 
 * <p>
 * <i>Aug 28, 2008</i>
 * </p>
 * 
 * @author dhouthoo
 */
public class TestFujiDataCard {

    private final static Logger logger = LoggerFactory.getLogger(TestFujiDataCard.class);

    private static FujiDataCard card;


    @BeforeClass
    public static void setUp() throws Exception {

        card = new FujiDataCard("/dev/tty.GTM HSDPA Control");
    }

    @Test
    public void testIMEI() throws Exception {

        // test twice in a row
        // lagging output can ruin the second call
        logger.info("IMEI: " + card.getIMEI());
        logger.info("IMEI: " + card.getIMEI());

    }

    @Test
    public void testConnection() throws Exception {

        card.authenticate("3316");
        card.authenticate("3316");
        card.connect("internet.proximus.be", null, null);
        card.disconnect();
    }

}
