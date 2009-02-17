package net.link.safeonline.model.bean;

import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationIdentityDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.model.Applications;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = Applications.JNDI_BINDING)
public class ApplicationsBean implements Applications {

    private static final Log       LOG = LogFactory.getLog(ApplicationsBean.class);

    @EJB(mappedName = ApplicationDAO.JNDI_BINDING)
    private ApplicationDAO         applicationDAO;

    @EJB(mappedName = ApplicationIdentityDAO.JNDI_BINDING)
    private ApplicationIdentityDAO applicationIdentityDAO;


    public ApplicationEntity getApplication(long applicationId)
            throws ApplicationNotFoundException {

        return applicationDAO.getApplication(applicationId);
    }

    public List<ApplicationEntity> listApplications() {

        List<ApplicationEntity> applications = applicationDAO.listApplications();
        return applications;
    }

    public List<ApplicationEntity> listUserApplications() {

        List<ApplicationEntity> applications = applicationDAO.listUserApplications();
        return applications;
    }

    public Set<ApplicationIdentityAttributeEntity> getCurrentApplicationIdentity(ApplicationEntity application)
            throws ApplicationIdentityNotFoundException {

        LOG.debug("get current application identity: " + application.getName());

        long currentIdentityVersion = application.getCurrentApplicationIdentity();
        ApplicationIdentityEntity applicationIdentity = applicationIdentityDAO.getApplicationIdentity(application, currentIdentityVersion);
        Set<ApplicationIdentityAttributeEntity> attributes = applicationIdentity.getAttributes();
        for (ApplicationIdentityAttributeEntity attribute : attributes) {
            LOG.debug("attribute: " + attribute);
        }
        return attributes;
    }
}
