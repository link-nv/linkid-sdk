package net.link.safeonline.attribute.provider.profile.entity;

import java.util.Date;
import javax.persistence.*;


/**
 * Created by IntelliJ IDEA.
 * User: sgdesmet
 * Date: 20/10/11
 * Time: 12:14
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "confirmationId", "email" })  })
@NamedQueries({
        @NamedQuery(name = EmailConfirmationEntity.deleteExpiredAtDate, query = "DELETE FROM EmailConfirmationEntity e WHERE e.expirationDate < :date"),
        @NamedQuery(name = EmailConfirmationEntity.findWithUserId,
                query = "SELECT e FROM EmailConfirmationEntity e WHERE e.userId = :userId"),
        @NamedQuery(name = EmailConfirmationEntity.findWithConfirmationId,
                query = "SELECT e FROM EmailConfirmationEntity s WHERE e.confirmationId = :confirmationId"),
        @NamedQuery(name = EmailConfirmationEntity.findWithEmail,
                query = "SELECT e FROM EmailConfirmationEntity s WHERE e.email = :email")
        })
public class EmailConfirmationEntity {

    public static final String deleteExpiredAtDate = "EmailConfirmationEntity.deleteExpiredAtDate";
    public static final String findWithUserId = "EmailConfirmationEntity.findWithUserId";
    public static final String findWithConfirmationId = "EmailConfirmationEntity.findWithConfirmationId";
    public static final String findWithEmail = "EmailConfirmationEntity.findWithEmail";


    @Id
    String confirmationId;

    @Column(nullable = false)
    String userId;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(nullable = true)
    Date expirationDate;

    String email;

    public EmailConfirmationEntity(final String confirmationId, final String userId, final String email, final Date expirationDate) {

        this.confirmationId = confirmationId;
        this.userId = userId;
        this.expirationDate = expirationDate;
        this.email = email;
    }

    public EmailConfirmationEntity(final String confirmationId, final String userId, final String email) {

        this(confirmationId, userId, email, null);
    }

    public String getConfirmationId() {

        return confirmationId;
    }

    public void setConfirmationId(final String confirmationId) {

        this.confirmationId = confirmationId;
    }

    public String getUserId() {

        return userId;
    }

    public void setUserId(final String userId) {

        this.userId = userId;
    }

    public Date getExpirationDate() {

        return expirationDate;
    }

    public void setExpirationDate(final Date expirationDate) {

        this.expirationDate = expirationDate;
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(final String email) {

        this.email = email;
    }
}
