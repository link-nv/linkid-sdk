/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package test.spike.net.link.safeonline;

import java.awt.image.BufferedImage;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import junit.framework.TestCase;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Millisecond;


/**
 * @author mbillemo
 * 
 */
public class JFreeChartTest extends TestCase {

    public void testChartFrame() {

        // Data.
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 4; ++j)
                dataset.addValue(Math.random() * 10, "row" + i, "col" + j);
        dataset.addValue(3, "row0", "col4");
        dataset.addValue(2, "row1", "col4");

        // Chart.
        BarRenderer renderer = new StackedBarRenderer();
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("domainAxis"), new PeriodAxis("periodAxis",
                new Millisecond(new Date(0)), new Millisecond(new Date(15))), renderer);
        JFreeChart chart = new JFreeChart("JFreeChart Test", plot);
        BufferedImage image = chart.createBufferedImage(800, 600);

        // Frame.
        JFrame frame = new JFrame("JFreeChart Test");
        frame.setContentPane(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void main(String[] args) {

        new JFreeChartTest().testChartFrame();
    }
}
