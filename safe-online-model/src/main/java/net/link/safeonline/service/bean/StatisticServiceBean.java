/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.StatisticNotFoundException;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.service.ApplicationOwnerAccessControlInterceptor;
import net.link.safeonline.service.StatisticService;
import net.link.safeonline.service.StatisticServiceRemote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@LocalBinding(jndiBinding = StatisticService.JNDI_BINDING)
@RemoteBinding(jndiBinding = StatisticServiceRemote.JNDI_BINDING)
public class StatisticServiceBean implements StatisticService, StatisticServiceRemote {

    private static final Log    LOG            = LogFactory.getLog(StatisticServiceBean.class);

    private static final String usageStatistic = "Usage statistic";

    @EJB
    private ApplicationDAO      applicationDAO;

    @EJB
    private StatisticDAO        statisticDAO;


    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public StatisticEntity getStatistic(String statisticName, String statisticDomain, String applicationName)
                                                                                                             throws StatisticNotFoundException {

        ApplicationEntity application = null;
        if (applicationName != null) {
            LOG.debug("finding application");
            application = this.applicationDAO.findApplication(applicationName);
        }

        return this.getStatistic(statisticName, statisticDomain, application);
    }

    @Interceptors(ApplicationOwnerAccessControlInterceptor.class)
    private StatisticEntity getStatistic(String statisticName, String statisticDomain, ApplicationEntity application)
                                                                                                                     throws StatisticNotFoundException {

        LOG.debug("finding statistic");
        StatisticEntity statistic = this.statisticDAO.findStatisticByNameDomainAndApplication(statisticName, statisticDomain, application);
        if (statistic == null)
            throw new StatisticNotFoundException();

        // trigger fetching
        LOG.debug("fetching datapoints");
        for (StatisticDataPointEntity dp : statistic.getStatisticDataPoints()) {
            dp.getId();
        }
        return statistic;
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    @Interceptors(ApplicationOwnerAccessControlInterceptor.class)
    public List<StatisticEntity> getStatistics(ApplicationEntity application) {

        return this.statisticDAO.listStatistics(application);
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public JFreeChart getChart(String statisticName, String statisticDomain, String applicationName) throws StatisticNotFoundException {

        StatisticEntity statistic = this.getStatistic(statisticName, statisticDomain, applicationName);

        JFreeChart chart = null;

        LOG.debug("found statistic");
        // hook specific chart generation functions here
        if (statisticName.equals(usageStatistic)) {
            chart = usageChart(statistic);
        } else if (statisticDomain.equals("Data Mining Domain")) {
            chart = dataMiningChart(statistic);
        } else {
            chart = defaultChart(statistic);
        }
        return chart;
    }

    private JFreeChart defaultChart(StatisticEntity statistic) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (StatisticDataPointEntity dp : statistic.getStatisticDataPoints()) {
            dataset.addValue(dp.getX(), "X", dp.getName());
            dataset.addValue(dp.getY(), "Y", dp.getName());
            dataset.addValue(dp.getZ(), "Z", dp.getName());
        }

        JFreeChart chart = ChartFactory.createBarChart(statistic.getName(), // chart
                // title
                "Category", // domain axis label
                "Value", // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips?
                false // URLs?
                                       );
        return chart;
    }

    private JFreeChart usageChart(StatisticEntity statistic) {

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries numberOfUsers = new XYSeries("Number of Users");
        XYSeries numberOfActiveUsers = new XYSeries("Number of Active Users");
        XYSeries numberOfLogins = new XYSeries("Number of Logins");

        for (StatisticDataPointEntity dp : statistic.getStatisticDataPoints()) {
            if (dp.getName().equals(usageStatistic)) {
                numberOfUsers.add(dp.getCreationTime().getTime(), dp.getX());
                numberOfActiveUsers.add(dp.getCreationTime().getTime(), dp.getY());
                numberOfLogins.add(dp.getCreationTime().getTime(), dp.getZ());
            }
        }

        dataset.addSeries(numberOfUsers);
        dataset.addSeries(numberOfActiveUsers);
        dataset.addSeries(numberOfLogins);

        JFreeChart chart = ChartFactory.createXYLineChart(statistic.getName(), "Time", "Number", dataset, PlotOrientation.VERTICAL, true,
                true, false);

        chart.getXYPlot().setDomainAxis(new DateAxis());
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShapesVisible(1, true);
        renderer.setSeriesShapesVisible(2, true);
        chart.getXYPlot().setRenderer(renderer);
        chart.getXYPlot().getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        return chart;
    }

    private JFreeChart dataMiningChart(StatisticEntity statistic) {

        DefaultPieDataset dataset = new DefaultPieDataset();
        for (StatisticDataPointEntity dp : statistic.getStatisticDataPoints()) {
            dataset.setValue(dp.getName(), dp.getX());
        }

        JFreeChart chart = ChartFactory.createPieChart(statistic.getName(), dataset, true, true, false);

        return chart;
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public HSSFWorkbook exportStatistics(String applicationName) throws ApplicationNotFoundException, StatisticNotFoundException {

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);

        List<StatisticEntity> statistics = this.statisticDAO.listStatistics(application);
        HSSFWorkbook workbook = new HSSFWorkbook();
        for (StatisticEntity statistic : statistics) {
            exportStatistic(statistic, workbook);
        }
        return workbook;
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public HSSFWorkbook exportStatistic(String statisticName, String statisticDomain, String applicationName)
                                                                                                             throws StatisticNotFoundException {

        StatisticEntity statistic = this.getStatistic(statisticName, statisticDomain, applicationName);

        HSSFWorkbook workbook = new HSSFWorkbook();
        exportStatistic(statistic, workbook);
        return workbook;

    }

    private void exportStatistic(StatisticEntity statistic, HSSFWorkbook workbook) throws StatisticNotFoundException {

        HSSFSheet mainSheet = workbook.createSheet();

        HSSFCellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));

        HSSFRow row1 = mainSheet.createRow(0);
        row1.createCell((short) 0).setCellValue(new HSSFRichTextString("Statistic"));
        row1.createCell((short) 1).setCellValue(new HSSFRichTextString(statistic.getName()));
        HSSFRow row2 = mainSheet.createRow(1);
        row2.createCell((short) 0).setCellValue(new HSSFRichTextString("Application"));
        row2.createCell((short) 1).setCellValue(new HSSFRichTextString(statistic.getApplication().getName()));
        HSSFRow row3 = mainSheet.createRow(2);
        row3.createCell((short) 0).setCellValue(new HSSFRichTextString("Creation time"));
        HSSFCell creationCell = row3.createCell((short) 1);
        creationCell.setCellStyle(dateCellStyle);
        creationCell.setCellValue(statistic.getCreationTime());
        mainSheet.autoSizeColumn((short) 0);
        mainSheet.autoSizeColumn((short) 1);

        HSSFRow row5 = mainSheet.createRow(4);
        if (statistic.getName().equals(usageStatistic)) {
            row5.createCell((short) 11).setCellValue(new HSSFRichTextString("Number of Users"));
            row5.createCell((short) 12).setCellValue(new HSSFRichTextString("Number of Active Users"));
            row5.createCell((short) 13).setCellValue(new HSSFRichTextString("Number of Logins"));
        } else {
            row5.createCell((short) 11).setCellValue(new HSSFRichTextString("X"));
            row5.createCell((short) 12).setCellValue(new HSSFRichTextString("Y"));
            row5.createCell((short) 13).setCellValue(new HSSFRichTextString("Z"));

        }
        int idx = 5;
        for (StatisticDataPointEntity dp : statistic.getStatisticDataPoints()) {
            HSSFRow row = mainSheet.createRow(idx);

            HSSFCell timeCell = row.createCell((short) 10);
            timeCell.setCellStyle(dateCellStyle);
            timeCell.setCellValue(dp.getCreationTime());

            row.createCell((short) 11).setCellValue(dp.getX());
            row.createCell((short) 12).setCellValue(dp.getY());
            row.createCell((short) 13).setCellValue(dp.getZ());
            idx++;
        }
        mainSheet.autoSizeColumn((short) 10);
        mainSheet.autoSizeColumn((short) 11);
        mainSheet.autoSizeColumn((short) 12);
        mainSheet.autoSizeColumn((short) 13);

        JFreeChart chart = getChart(statistic.getName(), statistic.getDomain(), statistic.getApplication().getName());

        byte[] image = null;

        try {
            image = ChartUtilities.encodeAsPNG(chart.createBufferedImage(600, 800));
        } catch (Exception e) {
            LOG.debug("Could not generate image");
            return;
        }

        int pictureIndex = workbook.addPicture(image, HSSFWorkbook.PICTURE_TYPE_PNG);

        HSSFPatriarch patriarch = mainSheet.createDrawingPatriarch();
        HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 0, 255, (short) 0, 4, (short) 10, 34);
        patriarch.createPicture(anchor, pictureIndex);

    }
}
