package net.link.safeonline.model;

import java.util.List;
import java.util.Set;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;


@Local
public interface Applications extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/ApplicationsBean/local";


    public ApplicationEntity getApplication(String applicationName)
            throws ApplicationNotFoundException;

    public List<ApplicationEntity> listApplications();

    public List<ApplicationEntity> listUserApplications();

    public Set<ApplicationIdentityAttributeEntity> getCurrentApplicationIdentity(ApplicationEntity application)
            throws ApplicationIdentityNotFoundException;

}
