package net.link.safeonline.model.bean;

import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.Task;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.model.Applications;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Local(Task.class)
@LocalBinding(jndiBinding = DataMiningTaskBean.JNDI_BINDING)
public class DataMiningTaskBean implements Task {

    public final static Log       LOG              = LogFactory.getLog(DataMiningTaskBean.class);

    public static final String    JNDI_BINDING     = Task.JNDI_PREFIX + "DataMiningTaskBean/local";

    public static final String    name             = "Data mining task";

    public static final String    dataMiningDomain = "Data Mining Domain";

    @EJB
    private StatisticDAO          statisticDAO;

    @EJB
    private StatisticDataPointDAO statisticDataPointDAO;

    @EJB
    private Applications          applications;

    @EJB
    private AttributeTypeDAO      attributeTypeDAO;


    public String getName() {

        return name;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void perform()
            throws Exception {

        LOG.debug("cleanDomain: " + dataMiningDomain);
        this.statisticDAO.cleanDomain(dataMiningDomain);

        for (ApplicationEntity application : this.applications.listApplications()) {

            for (ApplicationIdentityAttributeEntity attribute : this.applications.getCurrentApplicationIdentity(application)) {

                LOG
                   .debug("findOrAddStatisticByNameDomainAndApplication: " + attribute.getAttributeTypeName() + "," + application.getName());
                StatisticEntity statistic = this.statisticDAO.findOrAddStatisticByNameDomainAndApplication(
                        attribute.getAttributeTypeName(), dataMiningDomain, application);

                LOG.debug("categorize " + application.getName() + " - " + attribute.getAttributeTypeName());
                Map<Object, Long> result = this.attributeTypeDAO.categorize(application, attribute.getAttributeType());
                LOG.debug("result.size: " + result.size());

                for (Object key : result.keySet()) {
                    LOG.debug("key.toString: " + key.toString());
                    StatisticDataPointEntity datapoint = this.statisticDataPointDAO.findOrAddStatisticDataPoint(key.toString(), statistic);
                    datapoint.setX(result.get(key));
                }

            }
        }

    }
}
