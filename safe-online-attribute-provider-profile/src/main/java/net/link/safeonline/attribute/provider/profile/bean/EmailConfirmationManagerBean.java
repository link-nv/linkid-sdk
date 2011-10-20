package net.link.safeonline.attribute.provider.profile.bean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.interceptor.Interceptor;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import net.link.safeonline.attribute.provider.profile.attributes.ProfileAttributeConstants;
import net.link.safeonline.attribute.provider.profile.entity.EmailConfirmationEntity;
import net.link.util.j2ee.Configurable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.ejb3.annotation.LocalBinding;


/**
 * Created by IntelliJ IDEA.
 * User: sgdesmet
 * Date: 20/10/11
 * Time: 13:55
 * To change this template use File | Settings | File Templates.
 */
@Stateless
@Configurable
@LocalBinding(jndiBinding = EmailConfirmationManager.JNDI_BINDING)
public class EmailConfirmationManagerBean implements EmailConfirmationManager{

    @Configurable(name = ProfileAttributeConstants.EMAIL_CONFIRMATION_TIMEOUT_CONFIG, group = ProfileAttributeConstants.EMAIL_CONFIRMATION_TIMEOUT_CONFIG_GROUP)
    private Integer emailTimeout = 60;

    static final Log LOG = LogFactory.getLog( EmailConfirmationManagerBean.class );

    @PersistenceContext(unitName = ProfileAttributeConstants.ENTITY_MANAGER)
    private EntityManager em;

    private Date getEmailConfirmationTimeout(Date startDate) {
        Date date =  new Date( startDate.getTime() + emailTimeout * 1000 * 60);
        return date;
    }

    @Override
    public EmailConfirmationEntity createNewEmailConfirmation(final String userId, String email)
            throws ConfirmationInProgressException {
        LOG.debug( "Creating new email confirmation session" );

        String confirmationId = UUID.randomUUID().toString().replace( "-", "" );
        //check for collisions, generate new id if collision
        boolean collision = true;
        while (collision){
            collision = em.find(EmailConfirmationEntity.class, confirmationId  ) != null;
            if (collision){
                confirmationId = UUID.randomUUID().toString().replace( "-", "" );;
            }
        }

        EmailConfirmationEntity entity = (EmailConfirmationEntity)em.createNamedQuery( EmailConfirmationEntity.findWithEmail ).setParameter( "email", email ).getSingleResult();
        if (entity != null){
            if( entity.getExpirationDate().after( new Date(  ) )){
                //confirmation in progress for different user, throw error
                throw new ConfirmationInProgressException("Email confirmation already in progress for user: " + entity.getUserId());
            } else {
                em.remove( entity );
            }
        }

        EmailConfirmationEntity sessionEntity = new EmailConfirmationEntity( confirmationId, userId, email );
        sessionEntity.setExpirationDate( getEmailConfirmationTimeout( new Date(  ) ) );

        em.persist( sessionEntity );

        return sessionEntity;
    }

    @Override
    public String getUserId(final String confirmationId) {
        EmailConfirmationEntity entity = em.find(EmailConfirmationEntity.class, confirmationId  );
        if (entity != null){
            return entity.getUserId();
        } else {
            return null;
        }
    }

    @Override
    public String getConfirmationIdForUser(final String userId, boolean filterExpired) {
        EmailConfirmationEntity entity = (EmailConfirmationEntity)em.createNamedQuery( EmailConfirmationEntity.findWithUserId ).setParameter( "userid", userId ).getSingleResult();
        if (entity != null && (!filterExpired || entity.getExpirationDate().after( new Date(  ) ))){
            return entity.getConfirmationId();
        }
        return null;
    }

    @Override
    public String getConfirmationIdForEmail(final String email, final boolean filterExpired) {
        EmailConfirmationEntity entity = (EmailConfirmationEntity)em.createNamedQuery( EmailConfirmationEntity.findWithEmail ).setParameter( "email", email ).getSingleResult();
        if (entity != null && (!filterExpired || entity.getExpirationDate().after( new Date(  ) ))){
            return entity.getConfirmationId();
        }
        return null;
    }

    @Override
    public void removeAllExpired() {
        em.createNamedQuery( EmailConfirmationEntity.deleteExpiredAtDate).setParameter("date", new Date()).executeUpdate();
    }

    @Override
    public void remove(final String confirmationId) {
        EmailConfirmationEntity entity = em.find(EmailConfirmationEntity.class, confirmationId  );
        if (entity != null){
            em.remove( entity);
        }
    }
}
